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
Panduan perkabelan dan alur inisialisasi tahap demi tahap (Workflow BARU -> PULSER -> TDC -> TPS -> FIRST START -> READY):
- **Preservasi Status Workflow**: Nilai tahap setup (`setupStage`) dipreservasi dari respons `GET,SETUP` atau penyimpanan lokal agar tidak ter-reset saat menerima frame telemetri berkala.
- **Konfigurasi Pulser Lanjutan (`PulserAdvancedSettings`)**:
  - Pilihan Trigger Edge: `FALLING` (standar NS200) atau `RISING`.
  - Pilihan Rasio Pulsa: 1, 2, 3, atau 4 PPR (Pulse Per Revolution).
  - Durasi Gate SCR: 60 µs, 80 µs (standar NS200), 100 µs, atau 120 µs.
- **Kontrol Mode Kipas Radiator (`FanModeSettings`)**:
  - Pilihan mode: `OFF`, `ON`, dan `AUTO` (pin J1.7 / PB5 WeAct).
  - Dilengkapi *Safety Interlock*: tombol hanya dapat ditekan saat mesin mati (`RPM == 0`) dan tegangan kapasitor aman (`HV < 30V`).
- **Proteksi Transaksi Ganda**: Seluruh tombol aksi alur setup dilengkapi state pending (`setupCommandPending`) dengan timeout proteksi 5 detik.
- **Soket CDI 12-pin**: Kode warna kabel asli NS200, jalur koil sekunder, sensor TPS, dan koneksi pinout WeAct STM32WB55CGU6.

### 6. BLE Terminal & Hex Diagnostics
Diagnostik teknis tingkat lanjut:
- **Statistik Paket Real-Time (Sliding Window 5 Detik)**:
  - Frekuensi aktual transmisi paket (`packetRateHz`) dihitung secara dinamis dari stempel waktu frame masuk.
  - Persentase integritas paket valid (`crcValidPercent`) dihitung berbasis validasi CRC16 terhadap total paket dalam jendela 5 detik.
- **Indikator Kualitas Sambungan BLE (`LinkQuality`)**:
  - **STABIL** (Hijau): Laju 18–22 Hz dan integritas CRC ≥ 99%.
  - **CUKUP** (Kuning/Oranye): Laju 12–17 Hz, laju > 22 Hz, atau integritas CRC 95–98.9%.
  - **BURUK** (Merah): Laju < 12 Hz atau integritas CRC < 95%.
  - **TERPUTUS** (Abu-abu): Status offline atau 0 Hz.
- **GATT Specification Overview**: Menampilkan UUID Service, Telemetry Notify Char, Command Write Char, dan Response Notify Char.
- **Monitor Paket Heksadesimal Mentah (Raw HEX Packet)**: Menampilkan 20 bytes data v3 lengkap dengan byte highlight per field.
- **Konsol Manual Perintah CDI**: Terminal input untuk eksekusi perintah teks MCU dan log respons.

---

## 📡 Protokol Komunikasi BLE

Aplikasi berkomunikasi melalui BLE GATT Custom Service:

- **Service UUID**: `7a8f1000-6c9d-4e40-a45f-0b4b4e533230`
- **Telemetry Characteristic UUID (Notify 20 Hz)**: `7a8f1001-6c9d-4e40-a45f-0b4b4e533230`
- **Command Characteristic UUID (Write)**: `7a8f1002-6c9d-4e40-a45f-0b4b4e533230`
- **Response Characteristic UUID (Notify ASCII Stream)**: `7a8f1003-6c9d-4e40-a45f-0b4b4e533230`
- **Format Telemetri Paket Biner v3 (20 Bytes)**:
  - `[0..1]` : Magic Header `0x15, 0xCD` (NS200 CDI Identifier)
  - `[2]` : Protocol Version (`0x03`)
  - `[3]` : Frame Kind (`0` = CORE, `1` = EXTRA)
  - `[4..5]` : Sequence Counter (16-bit LE)
  - `[6..7]` : Engine Speed RPM (16-bit LE)
  - `[8..9]` : Throttle Position Sensor ADC/Raw (16-bit LE)
  - `[10..11]` : Spark Advance (centi-degree BTDC, 16-bit LE)
  - `[12..13]` : Battery Voltage (centi-volt, 16-bit LE)
  - `[14..15]` : HV Center Capacitor Voltage (16-bit LE)
  - `[16..17]` : HV Side Capacitor Voltage (16-bit LE)
  - `[18..19]` : CRC16-CCITT Checksum (Polinomial `0x1021`, Initial `0xFFFF`, Final XOR `0x0000`)
- **Protokol Respons ASCII (`GET,SETUP`)**:
  Format: `SETUP,<stage>,<edge>,<triggerCdeg>,<sideOffsetCdeg>,<ppr>,<gateUs>,<tpsClosed>,<tpsOpen>,<firstStartHv>,<center>,<side>,<fanMode>,<quality>`
  - Parsed penuh ke dalam `StateFlow` dan disinkronkan otomatis pasca-ACK (`refreshSetupAfterAck`).
- **Perintah Teks Terjadwal**:
  - `GET,STATUS` : Meminta status telemetri lengkap.
  - `GET,SETUP` : Sinkronisasi penuh parameter setup workflow.
  - `GET,META` : Meminta metadata firmware dan ID hardware.
  - `SETUP,EDGE,<FALLING|RISING>` : Mengatur trigger edge pulser.
  - `SETUP,PPR,<1-4>` : Mengatur rasio pulsa per putaran.
  - `SETUP,GATE_US,<60|80|100|120>` : Mengatur durasi pulsa SCR gate.
  - `SETUP,FAN,<OFF|ON|AUTO>` : Mengatur mode kontrol relai kipas radiator.
  - `SETUP,PICKUP,CONFIRM` / `SETUP,SAVE_TDC` / `SETUP,MANUAL_TDC,<cdeg>,CONFIRM` : Kalibrasi titik pengapian.
  - `SETUP,TPS,<CLOSED|OPEN>` : Kalibrasi rentang bukaan gas.
  - `SETUP,FIRST_START` / `SETUP,READY,CENTER` / `SETUP,READY,THREE,<offset>` : Tahap pengaktifan output koil.
  - `SETUP,RESET,CONFIRM` : Reset alur setup ke tahap awal.

---

## 🔌 Skema Wiring Pinout CDI R7.2 (Konektor 12-Pin J1)

Sesuai rancangan CDI R7.2 dan modul `WiringDataProvider.kt`:

| J1 | Fungsi | Kode Warna (Harness NS200) | Deskripsi Jalur & Destinasi Board (WeAct STM32WB55CGU6) |
|:--:|:-------|:---------------------------|:--------------------------------------------------------|
| 1  | NC | Kosong / NC | Tidak terhubung (Not Connected). Isolasi rapi dengan heat-shrink. |
| 2  | TPS_A | Hijau-Putih | Input sensor bukaan gas pasangan A. Terhubung ke J_TPS (PA3 / PA5 ADC). |
| 3  | TEMP | Hitam-Putih | Input sensor suhu mesin NTC (Pull-up 4.7k ke 5V, divider clamp BAT54S ke PA4 ADC). |
| 4  | TPS_B | Abu-Abu | Input sensor bukaan gas pasangan B. Terhubung ke J_TPS (PA3 / PA5 ADC). |
| 5  | +12 V kontak | Cokelat (+12V) | Input daya utama kunci kontak ON. Melewati sekring FMAIN 5A, DREV SB560, dan choke L47uH. |
| 6  | COIL_SIDE | Hitam-Merah | Output pulsa HV koil busi samping (Side Plugs). SCR2 BT151 via driver PA2. |
| 7  | FAN_RELAY | Biru-Kuning | Output kendali relay kipas radiator (Kolektor transistor BC547 sink via PB5). |
| 8  | NC | Kosong / NC | Tidak terhubung (Not Connected). Cadangan harness. |
| 9  | NC | Kosong / NC | Tidak terhubung (Not Connected). Cadangan harness. |
| 10 | PULSER | Putih-Merah | Sinyal input pick-up coil magnet spul (Komparator LM339 precision conditioning ke PA0 TIM2_CH1). |
| 11 | GND | Hitam-Kuning | Ground utama massa motor (Pusat titik temu ground bintang GND_STAR). |
| 12 | COIL_CENTER | Oranye | Output pulsa HV koil busi utama tengah (Center Plug). SCR1 BT151 via driver PA1. |

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

### Versi 7.2.3 (Pembaruan Terkini)
- **Preservasi Status Quick Setup**: Mencegah resetting status workflow setup stage saat menerima frame telemetri v3 dari mikrokontroler.
- **Konfigurasi Pulser Lanjutan**: Dukungan pemilihan Trigger Edge (`FALLING`/`RISING`), rasio pulsa 1–4 PPR, dan durasi trigger gate SCR (60–120 µs) langsung dari antarmuka Quick Setup.
- **Kontrol Kipas Radiator Terintegrasi**: Pengaturan mode kipas (`OFF`/`ON`/`AUTO`) untuk relai radiator J1.7 (PB5) dilengkapi *Safety Interlock* (wajib RPM 0 dan HV < 30V).
- **Statistik Paket & Integritas Real-Time**: Implementasi sliding window 5 detik untuk frekuensi paket aktual (`Hz`) dan rasio validitas CRC16 (`%`).
- **Indikator Kualitas Link BLE Dinamis**: Klasifikasi status kestabilan koneksi (`STABIL`, `CUKUP`, `BURUK`, `TERPUTUS`) dengan kode warna visual.
- **Sinkronisasi Otomatis Pasca-ACK**: MCU state otomatis di-refresh melalui `GET,SETUP` setelah setiap eksekusi perintah setup berhasil.
- **Unit Test Komprehensif**: Pengujian unit otomatis untuk algoritma CRC16-CCITT, parsing paket telemetri normal/korup, serta evaluator kualitas sambungan BLE (`CdiProtocolTest.kt`).

### Versi 7.2.2
- **Karakter Baru Moge 1800cc Super Bass**: Sintesis audio diperbarui ke irama 850 RPM stasioner slow-chug, subwoofer bass booster 38–75 Hz, kompresi empuk analog, dan saturasi bebas distorsi tajam.
- **Simulasi Putar Tuas Gas (BLIP)**: Menghadirkan fungsi `triggerThrottleBlip` responsif yang mensimulasikan puntiran gas sekejap (TPS 85%, lonjakan RPM kuadratik, raungan gas, dan inersia kembali ke idle).
- **Perbaikan Audio Leakage / Hanging Sound**: Menghilangkan persistensi dengung saat volume 0% atau saat beralih dari mode demo ke BLE nyata.
- **Tacho Slider Cerdas**: Menahan RPM di mode demo dan mengikuti RPM motor di mode BLE nyata.
- **Penyelesaian Filter Kategori Presets**: Kategori MOGE CC BESAR kini mencakup 5 pilihan lengkap tanpa area kosong.
