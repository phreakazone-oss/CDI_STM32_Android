# NS200 CDI R7.2 — Modern Android Tuning & Telemetry Suite

Aplikasi Android kendali terpadu untuk unit pengapian **CDI Programmable NS200-CDI-R7.2** (Bajaj Pulsar 200 DTS-i & Modifikasi Dual/Triple Spark). Menggabungkan kokpit telemetri balap gaya MoTeC, pemetaan kurva pengapian 4-slot dinamis, kalibrasi strobo pulser TDC, bengkel panduan kabel interaktif, diagnostik paket data biner BLE, serta simulator akustik mesin knalpot multi-silinder (*Live Audio Engine Test Bench*).

---

## 📋 Daftar Isi
1. [Fitur Utama](#-fitur-utama)
2. [Arsitektur & Tumpukan Teknologi](#-arsitektur--tumpukan-teknologi)
3. [Detail Modul & Layar](#-detail-modul--layar)
   - [1. Dashboard MoTeC & Tacho Slider](#1-dashboard-motec--tacho-slider)
   - [2. Ignition Maps (4-Slot Timing)](#2-ignition-maps-4-slot-timing)
   - [3. Strobe & Pulser Calibration](#3-strobe--pulser-calibration)
   - [4. Live Audio Engine Test Bench & MOGE Super Bass](#4-live-audio-engine-test-bench--moge-super-bass)
   - [5. Quick Setup & Wiring Workshop](#5-quick-setup--wiring-workshop)
   - [6. BLE Terminal & Hex Diagnostics](#6-ble-terminal--hex-diagnostics)
4. [Protokol Komunikasi BLE](#-protokol-komunikasi-ble)
5. [Skema Wiring Pinout CDI R7.2](#-skema-wiring-pinout-cdi-r72)
6. [Instalasi & Kompilasi](#-instalasi--kompilasi)
7. [Catatan Rilis](#-catatan-rilis)

---

## 🚀 Fitur Utama

- **Koneksi Nirkabel BLE Ultra-Stabil**: Scanning otomatis, auto-reconnect, pengiriman perintah berbasis antrean (*queue-based write*), dan perlindungan transisi mode bebas *ghost telemetry*.
- **Telemetri Balap Real-Time (20 Hz)**: Memantau RPM hingga 13.000, bukaan gas (TPS 0–100%), derajat *ignition advance* (° BTDC), tegangan pengisian kapasitor HV Center & Side (240V–280V), voltase aki (Cv), suhu mesin (°C), dan indikator *rev limiter* (Soft/Hard Cut).
- **Pemetaan Kurva Pengapian 4 Slot**: Pilihan instan Slot 0 (Eco/Harian), Slot 1 (Touring/Street), Slot 2 (Wet/Rain), dan Slot 3 (Pro/Race). Dilengkapi *safety ceiling* 36.0° BTDC dan penyimpanan memori non-volatile EEPROM/Flash.
- **Slider RPM Interaktif & Simulasi Putar Tuas Gas (BLIP)**:
  - Pada **Mode Demo**: Slider dapat digeser dan menahan putaran mesin (RPM) secara stabil untuk pengujian audio dan instrumen.
  - Pada **Koneksi BLE Nyata**: Slider secara otomatis mengikuti putaran mesin motor asli secara *real-time* (indikator visual responsif) tanpa mengganggu pembacaan sensor.
  - **Tombol BLIP Gas**: Mensimulasikan putaran tuas gas spontan (*quick throttle twist*) dengan lonjakan TPS kilat, akselerasi RPM kuadratik, suara raungan gas, dan deselerasi kembali ke idle.
- **Simulator Suara Mesin Knalpot (Live Audio Engine Test Bench)**:
  - 12 preset suara knalpot multi-silinder berbasis sintesis kompresi akustik loopable PCM 22.050 Hz 16-bit.
  - Preset baru: **Moge 1800cc Super Bass (Gahar Empuk)** dengan karakter stasioner/idle 850 RPM lambat berbobot, resonansi sub-woofer 38–75 Hz empuk, saturasi analog tube, dan raungan megaphone menggelegar.
  - *Mute Zero-Leakage Architecture*: Mencegah suara berdengung atau menggantung saat volume nol, audio switch dimatikan, atau saat mesin motor mati (< 100 RPM).
  - Dukungan impor file kustom (MP3/WAV/OGG) dengan interpolasi pitch dinamis mengikuti RPM.
- **Bengkel Panduan Wiring & Solder**: Visualisasi interaktif soket 12-pin CDI, kode warna kabel asli NS200, jalur koil sekunder, dan panduan langkah demi langkah.

---

## 🛠 Arsitektur & Tumpukan Teknologi

- **Bahasa**: Kotlin (100% Coroutines & StateFlow)
- **UI Framework**: Jetpack Compose dengan Material Design 3 (M3) Dark Carbon & Racing Theme
- **State Management**: MVVM Architecture via `CdiViewModel` terpusat
- **Bluetooth Stack**: Android BLE API (`BluetoothGatt`, `BluetoothGattCallback`, MTU 64, Auto-Retry)
- **Audio Synthesis Engine**: Android `SoundPool` (multi-stream crossfade) & `MediaPlayer` (custom tracks)
- **Data Serialization**: `kotlinx.serialization` untuk protokol paket biner CDI

---

## 📱 Detail Modul & Layar

### 1. Dashboard MoTeC & Tacho Slider
Menampilkan instrumen balap presisi tinggi:
- **Tachometer Radial & Linear**: Visualisasi jarum analog digital hingga 13.000 RPM dengan indikator redline dinamis.
- **Panel Status Dual Spark**: Indikator busi utama (Center Plug) dan busi sekunder (Side Plugs) aktif berkedip sesuai waktu pembakaran.
- **Telemetry Readout Matrix**:
  - `ADVANCE`: Derajat pengapian (° BTDC)
  - `TPS`: Persentase bukaan gas (0–100%)
  - `BATTERY`: Tegangan aki motor (contoh: 14.1V)
  - `HV CAP`: Tegangan kapasitor discharge CDI (248V Center / 252V Side)
  - `TEMP`: Estimasi suhu mesin
- **Interactive Tacho Slider**: Pengontrol putaran mesin simulasi di mode demo, dan indikator pengikut RPM motor di mode BLE.
- **Tombol Aksi Cepat**: Preset instan `IDLE (1.4K)`, `5K`, `8K`, `LIMITER`, serta tombol interaktif `BLIP GAS`.

### 2. Ignition Maps (4-Slot Timing)
Antarmuka tuning pengapian:
- **Grafik Kurva 2D**: Visualisasi perbandingan kurva derajat pengapian terhadap RPM.
- **Editing Breakpoint**: Pengubahan nilai sudut pengapian per 1.000 RPM dengan batas aman (*hard ceiling*) 36.0° BTDC untuk mencegah detonasi/knocking.
- **Slot Selector**: Berganti cepat antara Slot 0 (Eco), Slot 1 (Street), Slot 2 (Rain), dan Slot 3 (Race).
- **Flash / EEPROM Write**: Pengiriman perintah biner ber-checksum untuk menyimpan kurva permanen ke mikrokontroler CDI.

### 3. Strobe & Pulser Calibration
Kalibrasi mekanikal sensor pick-up:
- **Sinkronisasi Lampu Strobo**: Menyalakan pulsa strobo tetap pada sudut pengapian statis untuk pembacaan timing light pada tanda magnet kruk as.
- **Offset Pulser Derajat**: Menyetel offset pergeseran magnet pick-up coil (-15.0° hingga +15.0°).
- **Candidate Trigger Preview**: Menghitung sudut trigger aktual sebelum diterapkan permanen.

### 4. Live Audio Engine Test Bench & MOGE Super Bass
Simulator suara akustik knalpot motor berdaya tinggi:
- **Kategori Preset**:
  - `KAWASAKI`: Ninja 250 FI (Twin 180°), Ninja ZX-25R (Inline-4 Screamer).
  - `MOGE CC BESAR`: Moge 1800cc Super Bass (Gahar Empuk), Superbike 1000cc, Crossplane 1000cc (CP4), Ducati 1200cc (L-Twin Desmo), Cruiser 1800cc (V-Twin 45°).
  - `SUPERSPORT & BALAP`: Inline-4 600cc, Inline-3 800cc Triple, Twin 270° Cross-Twin, V4 MotoGP Prototype.
  - `STANDAR & KUSTOM`: Single 200 DTS-i asli, Custom Audio File (MP3/WAV).
- **Karakter Preset `Moge 1800cc Super Bass`**:
  - Idle lambat 850 RPM bertenaga (*heavy subwoofer thumps*).
  - Frekuensi sub-bass murni 38–75 Hz berpadu resonansi rongga *Helmholtz*.
  - Saturasi analog tube untuk hasil bass empuk (*non-harsh*) dan volume menggelegar.
- **Master Audio Switch**: Fitur proteksi anti-dengung instan (*zero hanging drone*).

### 5. Quick Setup & Wiring Workshop
Panduan perkabelan CDI R7.2:
- Soket CDI 12-pin lengkap dengan warna kabel motor Bajaj Pulsar 200 NS.
- Penjelasan fungsi jalur: Ground, 12V Switched, Pick-up Coil (+/-), TPS 5V/Signal, Koil Pengapian Utama, dan Koil Pengapian Samping.

### 6. BLE Terminal & Hex Diagnostics
Diagnostik teknis tingkat lanjut:
- Monitor paket heksadesimal mentah (Raw HEX Packet) 20 Hz.
- Log komunikasi RX/TX dengan verifikasi checksum.
- Konsol manual perintah CDI (GET STATUS, GET META, FLASH WRITE, dll.).

---

## 📡 Protokol Komunikasi BLE

Aplikasi berkomunikasi melalui BLE UART Custom Service:

- **Service UUID**: `0000FFE0-0000-1000-8000-00805F9B34FB`
- **Characteristic RX/TX UUID**: `0000FFE1-0000-1000-8000-00805F9B34FB`
- **Format Telemetri Paket Biner (Core 16-byte)**:
  `[0xAA] [0x55] [SEQ_H] [SEQ_L] [RPM_H] [RPM_L] [TPS_H] [TPS_L] [ADV_H] [ADV_L] [HV_C] [HV_S] [BAT_H] [BAT_L] [FLAGS] [CRC8]`
- **Perintah Teks**:
  - `GET,STATUS` : Meminta status telemetri lengkap.
  - `SET,SLOT,<0-3>` : Mengubah slot kurva aktif.
  - `SET,STROBE,<0|1>` : Mengaktifkan mode kalibrasi strobo.
  - `SET,LIMITER,<rpm>` : Mengatur batas putaran mesin (*rev limiter*).
  - `WRITE,FLASH` : Menyimpan parameter aktif ke memori non-volatile.

---

## 🔌 Skema Wiring Pinout CDI R7.2

| Pin | Kode Warna (NS200) | Deskripsi Jalur | Spesifikasi |
|:---:|:-------------------|:----------------|:------------|
| 1   | Hitam / Kuning     | Power Ground    | Rangka / Negatif Aki |
| 2   | Cokelat            | +12V DC Kontak  | Kunci Kontak (Switched) |
| 3   | Putih / Merah      | Pulser Pick-up (+) | Sinyal Sensor Kruk As |
| 4   | Hitam              | Pulser Pick-up (-) | Ground Sensor Pick-up |
| 5   | Biru / Putih       | TPS Signal In   | Sinyal Sensor Gas (0.5V - 4.5V) |
| 6   | Merah / Putih      | TPS Reference   | +5V DC Output Sensor |
| 7   | Oranye             | Koil Busi Tengah| CDI Discharge (~250V Pulse) |
| 8   | Oranye / Hitam     | Koil Busi Sisi  | Dual Spark Secondary Discharge |
| 9   | Biru Muda          | Indikator Tachometer | Sinyal Pulsa RPM Spidometer |
| 10  | Hijau Tua          | Kill Switch In  | Sakelar Engine Cut-off |

---

## 💻 Instalasi & Kompilasi

### Prasyarat Lingkungan
- Android Studio Ladybug / Koala atau lingkungan build berbasis Gradle.
- Android SDK API Level minimum: **26** (Android 8.0 Oreo).
- Android SDK Target: **API 35** (Android 15).
- Versi JDK: **OpenJDK 17 / 21**.

### Langkah Kompilasi
1. Clone repositori ke komputer lokal:
   ```bash
   git clone <repo-url>
   cd ns200-cdi-r7
   ```
2. Kompilasi APK debug:
   ```bash
   ./gradlew assembleDebug
   ```
3. Pasang APK ke perangkat Android fisik dengan dukungan Bluetooth Low Energy:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📝 Catatan Rilis (Changelog)

### Versi 7.2.2 (Pembaruan Terkini)
- **Karakter Baru Moge 1800cc Super Bass**: Sintesis audio diperbarui ke irama 850 RPM stasioner slow-chug, subwoofer bass booster 38–75 Hz, kompresi empuk analog, dan saturasi bebas distorsi tajam.
- **Simulasi Putar Tuas Gas (BLIP)**: Menghadirkan fungsi `triggerThrottleBlip` responsif yang mensimulasikan puntiran gas sekejap (TPS 85%, lonjakan RPM kuadratik, raungan gas, dan inersia kembali ke idle).
- **Perbaikan Audio Leakage / Hanging Sound**: Menghilangkan persistensi dengung saat volume 0% atau saat beralih dari mode demo ke BLE nyata.
- **Tacho Slider Cerdas**: Menahan RPM di mode demo dan mengikuti RPM motor di mode BLE nyata.
- **Penyelesaian Filter Kategori Presets**: Kategori MOGE CC BESAR kini mencakup 5 pilihan lengkap tanpa area kosong.
