package id.ns200.cdir7

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.media.SoundPool
import com.example.R
import kotlin.math.max

class EngineSound(private val context: Context) {
    enum class Preset(
        val label: String,
        val anchors: IntArray,
        val category: String,
        val description: String,
        val cylinderCount: String,
        val maxRpmDisplay: String
    ) {
        SINGLE(
            "Single 200 DTS-i",
            intArrayOf(R.raw.single_1200, R.raw.single_4000, R.raw.single_8000),
            "Pulsar 200",
            "1 Silinder 199.5cc DTS-i 4-Valve. Dentuman kompresi silinder tunggal asli.",
            "1 Silinder",
            "11.500 RPM"
        ),
        MOGE_SUPERBASS(
            "Moge 1800cc Super Bass (Gahar Empuk)",
            intArrayOf(R.raw.moge_bass_1200, R.raw.moge_bass_4000, R.raw.moge_bass_8000),
            "Moge CC Besar",
            "2 Silinder 1800cc V-Twin Big-Bore. Subwoofer bass booster empuk menggelegar, idle 850 RPM santai berbobot dan raungan knalpot super gahar.",
            "V-Twin 1800cc",
            "5.500 RPM"
        ),
        NINJA250(
            "Ninja 250 FI (Twin 180°)",
            intArrayOf(R.raw.ninja250_1200, R.raw.ninja250_4000, R.raw.ninja250_8000),
            "Kawasaki 250",
            "2 Silinder 249cc parallel-twin 180°. Irama dengkuran rapat putaran bawah dan raungan padat knalpot racing.",
            "2 Silinder",
            "13.500 RPM"
        ),
        ZX25R(
            "Ninja ZX-25R (Inline-4)",
            intArrayOf(R.raw.zx25r_1200, R.raw.zx25r_4000, R.raw.zx25r_8000),
            "Kawasaki 250",
            "4 Silinder 250cc Screamer. Melengking razor-sharp ultra-high revving ala mobil balap F1.",
            "4 Silinder",
            "17.000 RPM"
        ),
        SUPERBIKE1000(
            "Superbike 1000cc (Inline-4)",
            intArrayOf(R.raw.liter1000_1200, R.raw.liter1000_4000, R.raw.liter1000_8000),
            "Moge Liter",
            "4 Silinder 1000cc Liter-bike (ZX-10R / S1000RR). Bass menggelegar titanium exhaust dan raungan buas.",
            "4 Silinder",
            "14.500 RPM"
        ),
        CROSS4(
            "Crossplane 1000cc (Yamaha CP4)",
            intArrayOf(R.raw.cross4_cp_1200, R.raw.cross4_cp_4000, R.raw.cross4_cp_8000),
            "Moge Liter",
            "4 Silinder 998cc Crossplane CP4 (YZF-R1 / MT-10). Firing 270°-180°-90°-180° geraman serak berwibawa.",
            "4 Silinder CP",
            "14.000 RPM"
        ),
        DUCATI1200(
            "Ducati 1200cc (L-Twin Desmo)",
            intArrayOf(R.raw.ducati1200_1200, R.raw.ducati1200_4000, R.raw.ducati1200_8000),
            "Moge V-Twin",
            "2 Silinder 1198cc L-Twin 90° (Panigale / Monster 1200). Dentuman bass kompresi raksasa bergetar dalam dada.",
            "2 Silinder L-Twin",
            "11.000 RPM"
        ),
        CRUISER_VTWIN(
            "Cruiser 1800cc (V-Twin 45°)",
            intArrayOf(R.raw.cruiser_1200, R.raw.cruiser_4000, R.raw.cruiser_8000),
            "Moge Cruiser",
            "2 Silinder 1800cc V-Twin 45° langkah panjang. Irama klasik dentuman megaphone bass empuk berwibawa.",
            "2 Silinder V-Twin",
            "7.000 RPM"
        ),
        TWIN270(
            "Twin 270° Cross-Twin",
            intArrayOf(R.raw.twin270_1200, R.raw.twin270_4000, R.raw.twin270_8000),
            "Sport Twin",
            "2 Silinder 689cc Cross-Twin 270° (MT-07 / R7). Torsi padat dan dengkur berirama dinamis.",
            "2 Silinder",
            "11.000 RPM"
        ),
        INLINE3(
            "Inline-3 Triple 800cc",
            intArrayOf(R.raw.inline3_1200, R.raw.inline3_4000, R.raw.inline3_8000),
            "Triple",
            "3 Silinder 765/847cc (Triumph / MT-09). Raungan melolong khas siulan 3 silinder yang responsif.",
            "3 Silinder",
            "12.500 RPM"
        ),
        INLINE4(
            "Inline-4 600cc Supersport",
            intArrayOf(R.raw.inline4_1200, R.raw.inline4_4000, R.raw.inline4_8000),
            "Supersport",
            "4 Silinder 600cc (ZX-6R / CBR600RR). Screamer putaran menengah-tinggi khas supersport 600.",
            "4 Silinder",
            "15.500 RPM"
        ),
        V4(
            "V4 MotoGP Prototipe",
            intArrayOf(R.raw.v4_1200, R.raw.v4_4000, R.raw.v4_8000),
            "Balap Prototipe",
            "4 Silinder V4 MotoGP Desmosedici / RC213V. Raungan beringas prototipe balap kelas premier.",
            "4 Silinder V4",
            "18.000 RPM"
        ),
        CUSTOM(
            "Manual Custom Audio (MP3/WAV/OGG)",
            intArrayOf(),
            "Kustom",
            "Impor rekaman audio manual 5/6+ silinder dari memori HP dengan interpolasi pitch sesuai RPM.",
            "Bebas",
            "Sesuai File"
        )
    }

    private var pool: SoundPool? = null
    private var loadedPreset: Preset? = null
    private var activeSoundIds: IntArray? = null
    private var loadedCount = 0
    var preset = Preset.SINGLE
        private set
    private var streams = intArrayOf(0, 0, 0)
    private var customUri: Uri? = null
    private var customPlayer: MediaPlayer? = null
    private var customBaseRpm = 2000
    var masterVolume = 0.85f
        set(value) {
            val v = value.coerceIn(0f, 1f)
            field = v
            if (v <= 0.001f) {
                stop()
            }
        }
    var enabled = false
        set(value) {
            field = value
            if (!value) {
                stop()
            } else if (masterVolume > 0.001f) {
                ensurePresetLoaded(preset)
            }
        }

    init {
        getOrCreatePool()
        ensurePresetLoaded(preset)
    }

    private fun getOrCreatePool(): SoundPool {
        return pool ?: SoundPool.Builder().setMaxStreams(4).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        ).build().also { p ->
            p.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    val ids = activeSoundIds
                    if (ids != null && ids.contains(sampleId)) {
                        loadedCount++
                        if (enabled && masterVolume > 0.001f && loadedCount == 3) {
                            startStreamsIfReady()
                        }
                    }
                }
            }
            pool = p
        }
    }

    private fun startStreamsIfReady() {
        if (!enabled || masterVolume <= 0.001f) {
            stop()
            return
        }
        val p = pool ?: return
        val ids = activeSoundIds ?: return
        if (loadedCount >= 3 && streams.all { it == 0 }) {
            try {
                // Punchy initial volume directly scaled to master volume, zero when master is zero
                val initVol = (masterVolume * 0.45f).coerceIn(0f, 1f)
                if (initVol > 0.001f) {
                    streams = IntArray(3) { i ->
                        p.play(ids[i], initVol, initVol, 1, -1, 1f)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun ensurePresetLoaded(p: Preset) {
        if (p == Preset.CUSTOM || p.anchors.size != 3) {
            unloadActivePreset()
            return
        }
        if (loadedPreset == p && activeSoundIds != null) return

        unloadActivePreset()
        val pPool = getOrCreatePool()
        loadedCount = 0
        loadedPreset = p
        activeSoundIds = IntArray(3) { i -> pPool.load(context, p.anchors[i], 1) }
    }

    private fun unloadActivePreset() {
        activeSoundIds?.forEach { id ->
            try { pool?.unload(id) } catch (_: Exception) {}
        }
        activeSoundIds = null
        loadedPreset = null
        loadedCount = 0
    }

    fun setCustom(uri: Uri?, baseRpm: Int = customBaseRpm) {
        stop()
        customUri = uri
        customBaseRpm = baseRpm.coerceIn(600, 8000)
        preset = Preset.CUSTOM
        unloadActivePreset()
    }

    fun setCustomBaseRpm(value: Int) { customBaseRpm = value.coerceIn(600, 8000) }

    fun select(value: Preset) {
        if (value == preset) return
        stop()
        preset = value
        ensurePresetLoaded(preset)
        if (enabled && masterVolume > 0.001f) {
            startIfNeeded()
        }
    }

    private fun startIfNeeded() {
        if (!enabled || masterVolume <= 0.001f) {
            stop()
            return
        }
        if (preset == Preset.CUSTOM) {
            if (customPlayer != null) return
            val uri = customUri ?: return
            try {
                val initVol = (masterVolume * 0.40f).coerceIn(0f, 1f)
                customPlayer = MediaPlayer.create(context, uri)?.apply {
                    isLooping = true
                    setVolume(initVol, initVol)
                    start()
                }
            } catch (_: Exception) {}
            return
        }
        ensurePresetLoaded(preset)
        if (streams.any { it != 0 }) return
        if (loadedCount >= 3) {
            startStreamsIfReady()
        }
    }

    fun update(t: Telemetry) {
        // Absolute silence if muted or volume zero
        if (!enabled || masterVolume <= 0.001f) {
            if (streams.any { it != 0 } || customPlayer != null) {
                stop()
            }
            return
        }

        // If motor engine is off (< 100 RPM), cease all playback - no hanging drone
        if (t.rpm < 100) {
            if (streams.any { it != 0 } || customPlayer != null) {
                stop()
            }
            return
        }

        try {
            startIfNeeded()
            val rpm = t.rpm.toFloat().coerceAtLeast(600f)
            // Enhanced volume presence: 0.50f idle presence + 0.50f throttle load for deep, loud rumble
            val load = 0.50f + 0.50f * (t.tps / 1000f).coerceIn(0f, 1f)
            val limiterGain = when (t.limiter) {
                2 -> if (t.sequence and 1 == 0) 0f else .15f
                1 -> if (t.sequence % 4 == 0) .20f else 1f
                else -> 1f
            }
            if (preset == Preset.CUSTOM) {
                customPlayer?.let { player ->
                    val volume = (load * limiterGain * masterVolume).coerceIn(0f, 1f)
                    try {
                        player.setVolume(volume, volume)
                        player.playbackParams = PlaybackParams()
                            .setSpeed((rpm / customBaseRpm).coerceIn(.5f, 2f))
                            .setPitch(1f)
                    } catch (_: Exception) { /* codec/params exception */ }
                }
                return
            }
            val p = pool ?: return
            val isMogeSuperbass = preset == Preset.MOGE_SUPERBASS
            val soundRpm = if (isMogeSuperbass) {
                // Map bike's higher idle (~1400 RPM) to authentic slow 1800cc V-Twin idle (850 RPM)
                850f + ((rpm - 1400f).coerceAtLeast(0f) * 0.46f)
            } else {
                rpm
            }

            val anchors = if (isMogeSuperbass) {
                floatArrayOf(850f, 2800f, 5500f)
            } else {
                floatArrayOf(1200f, 4000f, 8000f)
            }

            val low = if (isMogeSuperbass) {
                (1f - ((soundRpm - 850f) / (2800f - 850f))).coerceIn(0f, 1f)
            } else {
                (1f - ((soundRpm - 1200f) / 2800f)).coerceIn(0f, 1f)
            }
            val high = if (isMogeSuperbass) {
                ((soundRpm - 2800f) / (5500f - 2800f)).coerceIn(0f, 1f)
            } else {
                ((soundRpm - 4000f) / 4000f).coerceIn(0f, 1f)
            }
            val mid = (if (soundRpm < anchors[1]) 1f - low else 1f - high).coerceIn(0f, 1f)
            
            // Equal-power crossfade weights for louder, consistent body across the rev range
            val w0 = kotlin.math.sqrt(low)
            val w1 = kotlin.math.sqrt(mid)
            val w2 = kotlin.math.sqrt(high)
            val sumW = (w0 + w1 + w2).coerceAtLeast(0.001f)
            val weights = floatArrayOf(w0 / sumW, w1 / sumW, w2 / sumW)
            val presetGain = if (isMogeSuperbass) 1.25f else 1.0f

            streams.forEachIndexed { i, stream ->
                if (stream != 0) {
                    try {
                        val volume = (weights[i] * load * limiterGain * masterVolume * presetGain).coerceIn(0f, 1f)
                        p.setVolume(stream, volume, volume)
                        p.setRate(stream, (soundRpm / anchors[i]).coerceIn(.5f, 2f))
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
            // Protect coroutine loop from audio hardware failure
        }
    }

    fun stop() {
        try {
            val p = pool
            if (p != null) {
                p.autoPause()
                streams.forEach { streamId ->
                    if (streamId != 0) {
                        try { p.setVolume(streamId, 0f, 0f) } catch (_: Exception) {}
                        try { p.stop(streamId) } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}
        streams = intArrayOf(0, 0, 0)
        try {
            customPlayer?.setVolume(0f, 0f)
            customPlayer?.pause()
            customPlayer?.release()
        } catch (_: Exception) {}
        customPlayer = null
    }

    fun release() {
        stop()
        unloadActivePreset()
        pool?.release()
        pool = null
    }
}
