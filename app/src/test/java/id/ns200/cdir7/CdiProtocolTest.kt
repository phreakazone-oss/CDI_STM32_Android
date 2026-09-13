package id.ns200.cdir7

import id.ns200.cdir7.ui.screens.LinkQuality
import id.ns200.cdir7.ui.screens.evaluateLinkQuality
import org.junit.Assert.*
import org.junit.Test

class CdiProtocolTest {

    private fun createValidCorePacket(
        sequence: Int = 101,
        rpm: Int = 3000,
        tps: Int = 200,
        advanceCdeg: Int = 1500,
        batteryCv: Int = 1260,
        hvCenter: Int = 225,
        hvSide: Int = 215
    ): ByteArray {
        val packet = ByteArray(20)
        packet[0] = 0x15.toByte()
        packet[1] = 0xcd.toByte()
        packet[2] = 3.toByte() // VERSION
        packet[3] = 0.toByte() // KIND_CORE

        packet[4] = (sequence and 0xff).toByte()
        packet[5] = ((sequence ushr 8) and 0xff).toByte()

        packet[6] = (rpm and 0xff).toByte()
        packet[7] = ((rpm ushr 8) and 0xff).toByte()

        packet[8] = (tps and 0xff).toByte()
        packet[9] = ((tps ushr 8) and 0xff).toByte()

        packet[10] = (advanceCdeg and 0xff).toByte()
        packet[11] = ((advanceCdeg ushr 8) and 0xff).toByte()

        packet[12] = (batteryCv and 0xff).toByte()
        packet[13] = ((batteryCv ushr 8) and 0xff).toByte()

        packet[14] = (hvCenter and 0xff).toByte()
        packet[15] = ((hvCenter ushr 8) and 0xff).toByte()

        packet[16] = (hvSide and 0xff).toByte()
        packet[17] = ((hvSide ushr 8) and 0xff).toByte()

        val crc = CdiProtocol.crc16(packet, 18)
        packet[18] = (crc and 0xff).toByte()
        packet[19] = ((crc ushr 8) and 0xff).toByte()

        return packet
    }

    @Test
    fun testCrc16Validation() {
        val validPacket = createValidCorePacket()
        val calculatedCrc = CdiProtocol.crc16(validPacket, 18)
        val packetCrc = (validPacket[18].toInt() and 0xff) or ((validPacket[19].toInt() and 0xff) shl 8)
        assertEquals("Calculated CRC should match packet CRC bytes", packetCrc, calculatedCrc)

        // Corrupt a byte
        val corruptedPacket = validPacket.clone()
        corruptedPacket[6] = (corruptedPacket[6] + 1).toByte()
        val corruptedCrc = CdiProtocol.crc16(corruptedPacket, 18)
        assertNotEquals("Corrupted packet CRC should not match original CRC", packetCrc, corruptedCrc)
    }

    @Test
    fun testTelemetryParsingNormal() {
        val packet = createValidCorePacket(
            sequence = 42,
            rpm = 4500,
            tps = 350,
            advanceCdeg = 2400,
            batteryCv = 1380,
            hvCenter = 230,
            hvSide = 220
        )

        val telemetry = CdiProtocol.telemetry(packet)
        assertNotNull("Valid packet must parse successfully", telemetry)
        telemetry!!
        assertEquals(42, telemetry.sequence)
        assertEquals(4500, telemetry.rpm)
        assertEquals(350, telemetry.tps)
        assertEquals(2400, telemetry.advanceCdeg)
        assertEquals(1380, telemetry.batteryCv)
        assertEquals(230, telemetry.hvCenter)
        assertEquals(220, telemetry.hvSide)
    }

    @Test
    fun testCorruptedFrameReturnsNullAndPreservesPrevious() {
        val validPacket = createValidCorePacket(rpm = 5000)
        val initialTelemetry = CdiProtocol.telemetry(validPacket)
        assertNotNull(initialTelemetry)

        // Corrupt packet by zeroing CRC bytes
        val corruptPacket = validPacket.clone()
        corruptPacket[18] = 0
        corruptPacket[19] = 0

        val result = CdiProtocol.telemetry(corruptPacket, initialTelemetry!!)
        assertNull("Corrupt packet should fail parsing and return null", result)
    }

    @Test
    fun testEvaluateLinkQuality() {
        // Disconnected or 0 Hz -> TERPUTUS
        assertEquals(LinkQuality.TERPUTUS, evaluateLinkQuality(connected = false, rateHz = 20, crcPercent = 100f))
        assertEquals(LinkQuality.TERPUTUS, evaluateLinkQuality(connected = true, rateHz = 0, crcPercent = 100f))

        // Stable: 18..22 Hz, CRC >= 99%
        assertEquals(LinkQuality.STABIL, evaluateLinkQuality(connected = true, rateHz = 20, crcPercent = 100f))
        assertEquals(LinkQuality.STABIL, evaluateLinkQuality(connected = true, rateHz = 18, crcPercent = 99.0f))

        // Fair: 12..17 Hz or CRC 95..98.9%
        assertEquals(LinkQuality.CUKUP, evaluateLinkQuality(connected = true, rateHz = 15, crcPercent = 99.0f))
        assertEquals(LinkQuality.CUKUP, evaluateLinkQuality(connected = true, rateHz = 20, crcPercent = 96.5f))
        assertEquals(LinkQuality.CUKUP, evaluateLinkQuality(connected = true, rateHz = 12, crcPercent = 95.0f))

        // Poor: <12 Hz or CRC < 95%
        assertEquals(LinkQuality.BURUK, evaluateLinkQuality(connected = true, rateHz = 10, crcPercent = 99.0f))
        assertEquals(LinkQuality.BURUK, evaluateLinkQuality(connected = true, rateHz = 20, crcPercent = 90.0f))
        assertEquals(LinkQuality.BURUK, evaluateLinkQuality(connected = true, rateHz = 8, crcPercent = 85.0f))
    }

    @Test
    fun excessivePacketRateIsNotStable() {
        assertEquals(
            LinkQuality.CUKUP,
            evaluateLinkQuality(
                connected = true,
                rateHz = 30,
                crcPercent = 100f
            )
        )
    }

    @Test
    fun firmwareSetupStageMapsToSixPageWizard() {
        assertEquals(SetupStage.BARU.code, CdiProtocol.wizardStageFromFirmware(0, 0, 0))
        assertEquals(SetupStage.TDC.code, CdiProtocol.wizardStageFromFirmware(1, 0, 0))
        assertEquals(SetupStage.TPS_CAL.code, CdiProtocol.wizardStageFromFirmware(2, 640, 0))
        assertEquals(SetupStage.FIRST_START.code, CdiProtocol.wizardStageFromFirmware(2, 640, 3200))
        assertEquals(SetupStage.FIRST_START.code, CdiProtocol.wizardStageFromFirmware(3, 640, 3200))
        assertEquals(SetupStage.READY.code, CdiProtocol.wizardStageFromFirmware(4, 640, 3200))
    }

    @Test
    fun parsesR8ModeAndOemLearnStatus() {
        val mode = CdiProtocol.firmwareMode("MODE,2,1,1,0")
        assertEquals(FirmwareRunMode.DIY, mode?.mode)
        assertEquals(true, mode?.diyUnplugged)
        assertEquals(true, mode?.proEnabled)
        assertEquals(false, mode?.firstStartProven)

        val learn = CdiProtocol.oemLearnStatus("LEARN,1,72,144,3,18,-125")
        assertEquals(OemLearnState.ACTIVE, learn?.state)
        assertEquals(72, learn?.coveragePercent)
        assertEquals(144, learn?.acceptedPulses)
        assertEquals(3, learn?.rejectedPulses)
        assertEquals(18, learn?.sideSamples)
        assertEquals(-125, learn?.sideOffsetCdeg)
    }

    @Test
    fun buildsR8OtaPacketWithOffsetLengthAndCrc() {
        val payload = ByteArray(16) { (it + 1).toByte() }
        val packet = CdiProtocol.otaDataPacket(208, payload)
        assertEquals(23, packet.size)
        assertEquals(0xd0, packet[0].toInt() and 0xff)
        assertEquals(16, packet[4].toInt() and 0xff)
        assertArrayEquals(payload, packet.copyOfRange(5, 21))
        val expectedCrc = CdiProtocol.crc16(packet, packet.size - 2)
        val actualCrc = (packet[21].toInt() and 0xff) or ((packet[22].toInt() and 0xff) shl 8)
        assertEquals(expectedCrc, actualCrc)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnalignedR8OtaOffset() {
        CdiProtocol.otaDataPacket(3, byteArrayOf(1))
    }

    @Test
    fun parsesAndRejectsCorruptR8OtaStatus() {
        val packet = ByteArray(CdiProtocol.OTA_STATUS_SIZE)
        packet[0] = 0x18
        packet[1] = 0xcd.toByte()
        packet[2] = 1
        packet[3] = FirmwareOtaState.RECEIVING.code.toByte()
        packet[4] = 0xd0.toByte() // received = 208
        packet[8] = 0x00
        packet[9] = 0x10 // expected = 4096
        val crc = CdiProtocol.crc16(packet, 14)
        packet[14] = crc.toByte()
        packet[15] = (crc ushr 8).toByte()

        val status = CdiProtocol.otaStatus(packet)
        assertEquals(FirmwareOtaState.RECEIVING, status?.state)
        assertEquals(208L, status?.receivedBytes)
        assertEquals(4096L, status?.expectedBytes)

        packet[4] = 0
        assertNull(CdiProtocol.otaStatus(packet))
    }
}
