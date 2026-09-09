package id.ns200.cdir7

enum class SetupStage(val code: Int, val label: String, val desc: String) {
    BARU(0, "BARU", "Cek catu daya & BLE; starter dengan JP_HV lepas. HV <30V. Charger & koil OFF"),
    PULSER(1, "PULSER", "Uji input pulser J1.10; PPR=1; gate 80µs; quality >=10"),
    TDC(2, "TDC", "Strobo PB9; sejajarkan tanda 'T'; SAVE TDC ke flash"),
    TPS_CAL(3, "TPS", "Simpan gas tertutup (0%) dan terbuka penuh (100%)"),
    FIRST_START(4, "FIRST START", "Mode aman 220V, CENTER saja, advance <=10°, limiter 3.000 RPM"),
    READY(5, "READY", "Hidup stabil >=3 detik, simpan CENTER; boot berikutnya langsung pakai map")
}

data class Telemetry(
    val sequence: Int, val rpm: Int, val tps: Int, val advanceCdeg: Int,
    val batteryCv: Int, val hvCenter: Int, val hvSide: Int, val tempCdeg: Int,
    val slot: Int, val limiter: Int, val flags: Int, val faults: Int,
    val setupStage: Int, val outputFlags: Int, val triggerCdeg: Int,
    val pickupQuality: Int, val firstStartSeconds: Int
) {
    val armed get() = flags and 0x01 != 0
    val proJumper get() = flags and 0x02 != 0
    val hvEnabled get() = flags and 0x04 != 0
    val calibrated get() = flags and 0x08 != 0
    val bleLink get() = flags and 0x10 != 0
    val ready get() = flags and 0x20 != 0
    val firstStart get() = flags and 0x40 != 0
    val centerEnabled get() = outputFlags and 1 != 0
    val sideEnabled get() = outputFlags and 2 != 0
    val strobeEnabled get() = outputFlags and 4 != 0
    val fanEnabled get() = outputFlags and 8 != 0
    val stage get() = SetupStage.entries.find { it.code == setupStage } ?: SetupStage.BARU
    val isHvOver300 get() = hvCenter >= 300 || hvSide >= 300
    val isHvOverLimitWarning get() = hvCenter >= 300 || hvSide >= 300
}

data class ProtocolResponse(val sequence: Int, val body: String)

object CdiProtocol {
    const val SERVICE = "7a8f1000-6c9d-4e40-a45f-0b4b4e533230"
    const val TELEMETRY = "7a8f1001-6c9d-4e40-a45f-0b4b4e533230"
    const val COMMAND = "7a8f1002-6c9d-4e40-a45f-0b4b4e533230"
    const val RESPONSE = "7a8f1003-6c9d-4e40-a45f-0b4b4e533230"
    const val TELEMETRY_SIZE = 20
    const val VERSION = 3
    const val KIND_CORE = 0
    const val KIND_DIAGNOSTIC = 1

    fun emptyTelemetry() = Telemetry(0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, SetupStage.BARU.code, 0, 6000, 0, 0)

    fun crc16(data: ByteArray, length: Int = data.size): Int {
        var crc = 0xffff
        repeat(length) { i ->
            crc = crc xor ((data[i].toInt() and 0xff) shl 8)
            repeat(8) { crc = ((crc shl 1) xor if (crc and 0x8000 != 0) 0x1021 else 0) and 0xffff }
        }
        return crc
    }

    fun command(sequence: Int, body: String): ByteArray {
        val payload = "$sequence,$body"
        return "@$payload*%04X\n".format(crc16(payload.toByteArray(Charsets.US_ASCII)))
            .toByteArray(Charsets.US_ASCII)
    }

    fun response(frame: String): ProtocolResponse? {
        val clean = frame.trim()
        if (!clean.startsWith('@')) return null
        val star = clean.lastIndexOf('*')
        if (star <= 1 || clean.length < star + 5) return null
        val payload = clean.substring(1, star)
        val supplied = clean.substring(star + 1, star + 5).toIntOrNull(16) ?: return null
        if (crc16(payload.toByteArray(Charsets.US_ASCII)) != supplied) return null
        val comma = payload.indexOf(',')
        if (comma < 1) return null
        return ProtocolResponse(payload.substring(0, comma).toIntOrNull() ?: return null,
            payload.substring(comma + 1))
    }

    private fun u16(a: ByteArray, offset: Int) =
        (a[offset].toInt() and 0xff) or ((a[offset + 1].toInt() and 0xff) shl 8)
    private fun s16(a: ByteArray, offset: Int) = u16(a, offset).toShort().toInt()
    private fun put16(a: ByteArray, offset: Int, value: Int) {
        a[offset] = (value and 0xff).toByte(); a[offset + 1] = ((value ushr 8) and 0xff).toByte()
    }

    fun telemetry(packet: ByteArray, previous: Telemetry = emptyTelemetry()): Telemetry? {
        if (packet.size != TELEMETRY_SIZE || u16(packet, 0) != 0xcd15 ||
            (packet[2].toInt() and 0xff) != VERSION ||
            (packet[3].toInt() and 0xff) !in KIND_CORE..KIND_DIAGNOSTIC ||
            crc16(packet, 18) != u16(packet, 18)) return null
        val sequence = u16(packet, 4)
        return if ((packet[3].toInt() and 0xff) == KIND_CORE) previous.copy(
            sequence = sequence, rpm = u16(packet, 6), tps = u16(packet, 8),
            advanceCdeg = s16(packet, 10), batteryCv = u16(packet, 12),
            hvCenter = u16(packet, 14), hvSide = u16(packet, 16)
        ) else previous.copy(
            sequence = sequence, tempCdeg = s16(packet, 6), slot = packet[8].toInt() and 0xff,
            limiter = packet[9].toInt() and 0xff, flags = packet[10].toInt() and 0xff,
            outputFlags = packet[11].toInt() and 0xff, faults = u16(packet, 12),
            triggerCdeg = u16(packet, 14), pickupQuality = packet[16].toInt() and 0xff,
            firstStartSeconds = packet[17].toInt() and 0xff
        )
    }

    fun packetFromTelemetry(t: Telemetry, kind: Int): ByteArray {
        val out = ByteArray(TELEMETRY_SIZE)
        put16(out, 0, 0xcd15); out[2] = VERSION.toByte(); out[3] = kind.toByte()
        put16(out, 4, t.sequence)
        if (kind == KIND_CORE) {
            put16(out, 6, t.rpm); put16(out, 8, t.tps); put16(out, 10, t.advanceCdeg)
            put16(out, 12, t.batteryCv); put16(out, 14, t.hvCenter); put16(out, 16, t.hvSide)
        } else {
            put16(out, 6, t.tempCdeg); out[8] = t.slot.toByte(); out[9] = t.limiter.toByte()
            out[10] = t.flags.toByte(); out[11] = t.outputFlags.toByte(); put16(out, 12, t.faults)
            put16(out, 14, t.triggerCdeg); out[16] = t.pickupQuality.toByte(); out[17] = t.firstStartSeconds.toByte()
        }
        put16(out, 18, crc16(out, 18)); return out
    }

    fun toHexDump(bytes: ByteArray) = bytes.joinToString(" ") { "%02X".format(it) }
}
