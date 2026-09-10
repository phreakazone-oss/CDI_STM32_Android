# NS200 CDI R8 — Modern Android Tuning, Telemetry & OTA Suite

Aplikasi Android kendali terpadu untuk unit pengapian **CDI Programmable NS200-CDI-R8** (Bajaj Pulsar 200 DTS-i & Modifikasi Dual/Triple Spark). Menggabungkan kokpit telemetri balap gaya MoTeC, pemetaan kurva pengapian 4-slot dinamis, kalibrasi strobo pulser TDC, mode pembelajaran kurva asli (**OEM Learn Mode**), sistem pengunggah firmware nirkabel (**BLE OTA Firmware Uploader**), alur aktivasi mandiri aman (**DIY Safety Mode**), bengkel panduan kabel interaktif, diagnostik paket data biner BLE, serta simulator akustik mesin knalpot multi-silinder (*Live Audio Engine Test Bench*).

---

## 📋 Daftar Isi
1. [Fitur Utama](#-fitur-utama)
2. [Pembaruan Besar Firmware R8](#-pembaruan-besar-firmware-r8)
3. [Arsitektur & Tumpukan Teknologi](#-arsitektur--tumpukan-teknologi)
4. [Detail Modul & Layar](#-detail-modul--layar)
   - [1. Dashboard MoTeC & Tacho Slider](#1-dashboard-motec--tacho-slider)
   - [2. Ignition Maps (4-Slot Timing)](#2-ignition-maps-4-slot-timing)
   - [3. Setup CDI, OEM Learn & Kalibrasi TDC](#3-setup-cdi-oem-learn--kalibrasi-tdc)
   - [4. Pengunggah Firmware BLE OTA (APP.bin)](#4-pengunggah-firmware-ble-ota-appbin)
   - [5. Live Audio Engine Test Bench & MOGE Super Bass](#5-live-audio-engine-test-bench--moge-super-bass)
   - [6. Quick Setup & Wiring Workshop](#6-quick-setup--wiring-workshop)
   - [7. BLE Terminal & Hex Diagnostics](#7-ble-terminal--hex-diagnostics)
5. [Protokol Komunikasi BLE Firmware R8](#-protokol-komunikasi-ble-firmware-r8)
6. [Skema Wiring Pinout CDI R8](#-skema-wiring-pinout-cdi-r8)
7. [Instalasi & Kompilasi](#-instalasi--kompilasi)
8. [Catatan Rilis (Changelog)](#-catatan-rilis-changelog)

---

## 🚀 Fitur Utama

- **Koneksi Nirkabel BLE Ultra-Stabil**: Scanning otomatis, auto-reconnect, pengiriman perintah berbasis antrean (*queue-based write*), dan proteksi transisi mode bebas *ghost telemetry*.
- **Telemetri Balap Real-Time (20 Hz)**: Memantau RPM hingga 13.000, bukaan gas (TPS 0–100%), derajat *ignition advance* (° BTDC), tegangan pengisian kapasitor HV Center & Side (hingga 345V PRO), voltase aki (Cv), suhu mesin (°C), dan status *rev limiter*.
- **Mode Pembelajaran Mandiri (OEM Learn)**: Membaca pulsa pengapian CDI bawaan pabrik secara pasif melalui PB3 (Center) & PB4 (Side) saat mesin hidup, merekam kurva pengapian asli motor secara otomatis.
- **Pengunggah Firmware BLE OTA**: Memperbarui binary mikrokontroler STM32WB55 (`APP.bin`) langsung lewat Bluetooth LE tanpa kabel ST-Link/SWD, dilengkapi verifikasi CRC32 dan interlock keselamatan preflight.
- **Transisi Otomatis FIRST START ke READY**: Otomatis menyimpan kalibrasi setelah idle stabil 3 detik pada mode aman (220V, center saja, advance ≤10°, limiter 3.000 RPM) dan otomatis berstatus READY saat mesin mati atau boot berikutnya.
- **Seleksi Tegangan HV R8 (285 V & 345 V)**: Pilihan level tegangan kapasitor Normal 285 V (efisiensi harian) atau Pro 345 V (kompetisi percikan api tinggi) melalui konfigurasi software.
- **Pemetaan Kurva Pengapian 4 Slot**: Pilihan instan Slot 0 (Eco/Harian), Slot 1 (Touring/Street), Slot 2 (Wet/Rain), dan Slot 3 (Pro/Race 16x8 matrix). Dilengkapi *safety ceiling* 36.0° BTDC.
- **Slider RPM Interaktif & BLIP Gas**:
  - Pada **Mode Demo**: Slider menahan RPM simulasi secara stabil untuk pengujian audio dan visual.
  - Pada **Koneksi BLE Nyata**: Slider mengikuti putaran mesin motor asli secara *real-time*.
  - **Tombol BLIP Gas**: Simulasi puntiran gas kilat dengan lonjakan TPS spontan, akselerasi kuadratik, dan deselerasi inersia.
- **Simulator Suara Mesin Knalpot (Live Audio Test Bench)**: 12 preset suara knalpot multi-silinder berbasis sintesis kompresi akustik PCM 22.050 Hz 16-bit, termasuk preset **Moge 1800cc Super Bass (Gahar Empuk)**.
- **Bengkel Panduan Wiring & Solder**: Visualisasi interaktif soket 12-pin CDI, kode warna harness asli NS200, jalur koil sekunder, dan panduan langkah demi langkah.

---

## ⚡ Pembaruan Besar Firmware R8

| Fitur | Firmware R7.2 Lama | Firmware R8 Baru |
|---|---|---|
| **Alur Akuisisi Timing** | Wajib strobo manual / timing light tanda T | **OEM Learn Pasif**: Rekam kurva CDI OEM langsung via PB3/PB4 (Strobo tetap tersedia di mode MANUAL) |
| **Interlock Fisik** | Mengharuskan jumper `JP_HV`, `SW_ARM`, `JP_PRO` | **Software Interlock & Safety Confirmation**: Menghilangkan batasan saklar fisik; kontrol mode terpadu di aplikasi |
| **Aktivasi Mode DIY** | Manual via jumper dan langkah rumit | **Safe DIY Mode**: Wajib konfirmasi `OEM_UNPLUGGED` (tidak ada pengambilalihan otomatis berbahaya) |
| **First Start & Ready** | Mengharuskan pencabutan jumper berulang kali | **Otomatis 3 Detik**: Deteksi idle stabil ≥3s langsung mengunci status dan otomatis READY saat mesin mati |
| **Level Tegangan HV** | Terkunci pada 240V–280V | **Dual Target R8**: NORMAL (285 V) dan PRO (345 V) via perintah software |
| **Update Firmware MCU** | Wajib buka bodi & ST-Link V2 / DFU USB | **BLE OTA Flashing**: Unggah `APP.bin` langsung lewat aplikasi Android dengan proteksi CRC32 |

https://github.com/phreakazone/Firmware_CDI_NS200
---

## 🛠 Arsitektur & Tumpukan Teknologi

- **Bahasa**: Kotlin (100% Coroutines & StateFlow)
- **UI Framework**: Jetpack Compose dengan Material Design 3 (M3) Dark Carbon & MoTeC Racing Theme
- **State Management**: MVVM Architecture via `CdiViewModel` terpusat
- **Bluetooth Stack**: Android BLE API (`BluetoothGatt`, MTU 64/247, Queue-based Writer, BLE OTA Chunk Streaming)
- **Audio Synthesis Engine**: Android `SoundPool` (multi-stream crossfade) & `MediaPlayer` (custom tracks)
- **Data Serialization**: `kotlinx.serialization` untuk protokol paket biner CDI
- **Hardware Target**: Mikrokontroler Dual-Core ARM Cortex-M4/M0+ **WeAct STM32WB55CGU6**

---

## 📱 Detail Modul & Layar

### 1. Dashboard MoTeC & Tacho Slider
Menampilkan instrumen balap presisi tinggi:
- **Tachometer Radial & Linear**: Visualisasi jarum analog digital hingga 13.000 RPM dengan indikator redline dinamis.
- **Panel Status Dual/Triple Spark**: Indikator busi utama (Center Plug) dan busi sekunder (Side Plugs) aktif berkedip sesuai sinyal pemantik.
- **Telemetry Readout Matrix**:
  - `ADVANCE`: Derajat pengapian (° BTDC)
  - `TPS`: Persentase bukaan gas (0–100%)
  - `BATTERY`: Tegangan aki motor (contoh: 14.1V)
  - `HV CAP`: Tegangan kapasitor discharge CDI (hingga 345V pada mode PRO)
  - `TEMP`: Estimasi suhu mesin
  - `MODE FIRMWARE`: Indikator mode aktif (OEM_LEARN / MANUAL / DIY)
- **Interactive Tacho Slider**: Pengontrol putaran mesin simulasi di mode demo, dan pengikut RPM motor di mode BLE.
- **Tombol Aksi Cepat**: Preset instan `IDLE (1.4K)`, `5K`, `8K`, `LIMITER`, serta tombol interaktif `BLIP GAS`.

### 2. Ignition Maps (4-Slot Timing)
Antarmuka tuning pengapian komprehensif:
- **Grafik Kurva 2D**: Visualisasi perbandingan kurva derajat pengapian terhadap RPM.
- **Editing Breakpoint**: Pengubahan nilai sudut pengapian per 1.000 RPM dengan batas aman (*hard ceiling*) 36.0° BTDC untuk mencegah detonasi/knocking.
- **Slot Selector**: Berganti cepat antara Slot 0 (Eco), Slot 1 (Street), Slot 2 (Rain), dan Slot 3 (Pro 16x8 matrix).
- **EEPROM / Flash Write**: Pengiriman perintah biner ber-checksum untuk menyimpan kurva permanen ke mikrokontroler CDI.

### 3. Setup CDI, OEM Learn & Kalibrasi TDC
Wizard komisi terpadu satu tahap per layar: BARU → PULSER → TDC → TPS → FIRST START → READY.
- **Panel Kontrol Mode Firmware R8**:
  - **Mode `OEM_LEARN`**: Merekam timing asli dari CDI bawaan pabrik via PB3 (Center) dan PB4 (Side). Menampilkan counter jumlah pulsa terekam secara live. Tombol *Mulai Learn* dan *Simpan & Stop*.
  - **Mode `DIY`**: Mengaktifkan pengapian mandiri oleh CDI STM32 setelah soket CDI OEM dicabut. Dilengkapi pengaman konfirmasi `OEM_UNPLUGGED` untuk mencegah tabrakan driver pengapian ganda.
  - **Mode `MANUAL`**: Mempertahankan kalibrasi strobo PB9 dan offset TDC untuk kondisi darurat ketika CDI OEM rusak/mati.
- **Target Tegangan HV R8**: Pilihan instan antara **NORMAL (285 V)** untuk pemakaian harian dan **PRO (345 V)** untuk daya percikan maksimal di lintasan balap.
- **FIRST START Otomatis**:
  - Mode pengamanan: 220V, CENTER saja, advance ≤10°, limiter 3.000 RPM.
  - Begitu mesin terdeteksi stabil hidup selama ≥3 detik, status langsung terkunci dan sistem otomatis beralih ke status **READY** saat mesin dimatikan atau saat kontak dihidupkan ulang.

### 4. Pengunggah Firmware BLE OTA (APP.bin)
Pembaruan firmware nirkabel terintegrasi di tab BLE Terminal:
- **Interlock Keselamatan Preflight**: Tombol upload terkunci otomatis kecuali syarat aman terpenuhi:
  - `RPM == 0` (mesin wajib mati).
  - `Output Coils == OFF` (koil pemantik tidak aktif).
  - `HV Center & Side < 30V` (kapasitor daya tinggi sudah terkuras aman).
- **Transmisi Chunk Cerdas**: Mengirim binary `APP.bin` per-blok melalui BLE GATT write without response, dilengkapi progress bar persentase, total byte terkirim, dan tombol pembatalan seketika.
- **Integritas Flash**: Verifikasi checksum CRC32 otomatis di sisi mikrokontroler sebelum menjalankan reboot aplikasi baru.

### 5. Live Audio Engine Test Bench & MOGE Super Bass
Simulator suara akustik knalpot motor:
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

### 6. Quick Setup & Wiring Workshop
Panduan perkabelan dan alur inisialisasi tahap demi tahap:
- **Preflight tahap BARU**: Memeriksa `PING`, `GET,STATUS`, dan `GET,SETUP` (wajib RPM 0 dan HV < 30V).
- **Konfigurasi Pulser Lanjutan (`PulserAdvancedSettings`)**:
  - Pilihan Trigger Edge: `FALLING` (standar NS200) atau `RISING`.
  - Pilihan Rasio Pulsa: 1, 2, 3, atau 4 PPR (Pulse Per Revolution).
  - Durasi Gate SCR: 60 µs, 80 µs (standar NS200), 100 µs, atau 120 µs.
- **Kontrol Mode Kipas Radiator (`FanModeSettings`)**:
  - Pilihan mode: `OFF`, `ON`, dan `AUTO` (pin J1.7 / PB5 WeAct) dengan safety interlock.
- **Soket CDI 12-pin**: Kode warna kabel asli NS200, jalur koil sekunder, sensor TPS, dan pinout WeAct STM32WB55CGU6.

### 7. BLE Terminal & Hex Diagnostics
Diagnostik teknis tingkat lanjut:
- **Statistik Paket Real-Time (Sliding Window 5 Detik)**: `packetRateHz` (18–22 Hz) dan integritas `crcValidPercent`.
- **Indikator Kualitas Sambungan BLE (`LinkQuality`)**: STABIL (Hijau), CUKUP (Kuning), BURUK (Merah), TERPUTUS (Abu-abu).
- **Monitor Paket Heksadesimal Mentah**: Menampilkan 20 bytes data v3 lengkap dengan penyorotan warna per field.
- **Konsol Manual Perintah CDI**: Terminal input untuk eksekusi perintah teks ASCII MCU dan log respons.

---

## 📡 Protokol Komunikasi BLE Firmware R8

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

### Perintah Teks Firmware R8
- **Mode & Pembelajaran**:
  - `MODE,OEM_LEARN` : Mengaktifkan mode belajar timing pasif dari CDI OEM.
  - `MODE,MANUAL` : Mengaktifkan mode manual/strobo darurat.
  - `MODE,DIY,OEM_UNPLUGGED` : Mengaktifkan operasi CDI mandiri setelah soket OEM dicabut.
  - `LEARN,START` : Memulai perekaman pulsa pengapian OEM via PB3 & PB4.
  - `LEARN,STOP` : Menghentikan perekaman dan menyimpan timing ke flash.
  - Respons pulsa: `@<seq>,LEARN,PULSES,<center_count>,<side_samples>`
- **Pengaturan Tegangan R8**:
  - `SETUP,VOLTAGE,NORMAL` : Mengatur target HV ke 285 V (penggunaan standar/harian).
  - `SETUP,VOLTAGE,PRO` : Mengatur target HV ke 345 V (kompetisi balap).
- **Protokol OTA Firmware**:
  - `OTA,START,<file_size>,<crc32_hex>` : Inisialisasi proses update firmware.
  - Pengiriman paket data biner 208-byte via kanal BLE.
  - `OTA,ABORT` : Pembatalan darurat upload firmware.
- **Perintah Setup & Status Umum**:
  - `GET,STATUS` : Meminta status telemetri lengkap.
  - `GET,SETUP` : Sinkronisasi penuh parameter setup workflow.
  - `GET,META` : Meminta metadata firmware dan build string.
  - `SETUP,EDGE,<FALLING|RISING>` : Mengatur trigger edge pulser.
  - `SETUP,PPR,<1-4>` : Mengatur rasio pulsa per putaran.
  - `SETUP,GATE_US,<60|80|100|120>` : Mengatur durasi pulsa SCR gate.
  - `SETUP,FAN,<OFF|ON|AUTO>` : Mengatur mode kontrol relai kipas radiator.
  - `SETUP,FIRST_START` : Memulai mode first start aman (220V, auto-lock 3s).
  - `SETUP,READY,CENTER` / `SETUP,READY,THREE,<offset>` : Tahap pengaktifan output penuh.
  - `SETUP,RESET,CONFIRM` : Reset alur setup ke tahap awal.

---

## 🔌 Skema Wiring Pinout CDI R8 (Konektor 12-Pin J1)

Sesuai rancangan CDI R8 dan modul `WiringDataProvider.kt`:

| J1 | Fungsi | Kode Warna (Harness NS200) | Deskripsi Jalur & Destinasi Board (WeAct STM32WB55CGU6) |
|:--:|:-------|:---------------------------|:--------------------------------------------------------|
| 1  | NC | Kosong / NC | Tidak terhubung (Not Connected). Isolasi rapi dengan heat-shrink. |
| 2  | TPS_A | Hijau-Putih | Input sensor bukaan gas pasangan A. Terhubung ke J_TPS (PA3 / PA5 ADC). |
| 3  | TEMP | Hitam-Putih | Input sensor suhu mesin NTC (Pull-up 4.7k ke 5V, divider clamp BAT54S ke PA4 ADC). |
| 4  | TPS_B | Abu-Abu | Input sensor bukaan gas pasangan B. Terhubung ke J_TPS (PA3 / PA5 ADC). |
| 5  | +12 V kontak | Cokelat (+12V) | Input daya utama kunci kontak ON. Melewati sekring FMAIN 5A, DREV SB560, dan choke L47uH. |
| 6  | COIL_SIDE | Hitam-Merah | Output pulsa HV koil busi samping (Side Plugs). SCR2 BT151 via driver PA2. |
| 7  | FAN_RELAY | Biru-Kuning | Output kendali relay kipas radiator (Kolektor transistor BC547 sink via PB5). |
| 8  | NC / OEM_SIDE | Kosong / NC | Jalur cadangan / probe monitor pasif pulsa koil samping OEM (H_TOP.8 / PB4). |
| 9  | NC / OEM_CTR | Kosong / NC | Jalur cadangan / probe monitor pasif pulsa koil utama OEM (H_TOP.9 / PB3). |
| 10 | PULSER | Putih-Merah | Sinyal input pick-up coil magnet spul (Komparator LM339 precision conditioning ke PA0 TIM2_CH1). |
| 11 | GND | Hitam-Kuning | Ground utama massa motor (Pusat titik temu ground bintang GND_STAR). |
| 12 | COIL_CENTER | Oranye | Output pulsa HV koil busi utama tengah (Center Plug). SCR1 BT151 via driver PA1. |

> **Catatan Alokasi Pin WeAct STM32WB55 pada Firmware R8**:
> - **PB3 (H_TOP.9)**: Monitor input pasif pulsa koil CENTER OEM saat mode `OEM_LEARN`.
> - **PB4 (H_TOP.8)**: Monitor input pasif pulsa koil SIDE OEM saat mode `OEM_LEARN`.
> - **PB2 (H_BOTTOM.19)**: Pemantau tegangan jalur daya HV (VIN_HV Presence Sense).
> - **PA1 & PA2**: Driver SCR koil pengapian CENTER dan SIDE (aktif pada mode DIY).
> - **PB9**: Driver output lampu strobo kalibrasi timing magnet kruk as (mode MANUAL).

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
   cd ns200-cdi-r8
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

### Versi 8.0.0 (Rilis Utama Firmware R8)
- **Dukungan Penuh Firmware R8**: Integrasi menyeluruh dengan arsitektur firmware terbaru STM32WB55 Dual-Core.
- **Pengunggah Firmware BLE OTA (`APP.bin`)**:
  - Menu pengunggah binary firmware langsung via BLE GATT di tab Terminal / Hex.
  - Pemeriksaan keselamatan preflight ketat: hanya dapat dimulai saat RPM = 0, koil OFF, dan HV < 30V.
  - Streaming chunk 208-byte dengan verifikasi checksum CRC32 dan tombol pembatalan.
- **Alur OEM Learn Pasif**:
  - Pembacaan sinyal timing asli dari CDI bawaan motor via pin PB3 (Center) & PB4 (Side).
  - Indikator counter pulsa Center dan sampel Side secara langsung (@seq,LEARN,PULSES,...).
  - Tombol kontrol *Mulai Learn* dan *Simpan & Stop*.
- **Mode Kontrol Firmware Fleksibel**:
  - Pilihan mode `OEM_LEARN`, `MANUAL` (strobo/TDC darurat), dan `DIY`.
  - Mode DIY dilindungi pengaman konfirmasi wajib `OEM_UNPLUGGED` untuk mencegah aktivasi simultan dengan CDI bawaan.
- **Otomatisasi FIRST START & Status READY**:
  - Deteksi idle stabil ≥3 detik pada mode aman (220V, center saja, limiter 3.000 RPM) otomatis mengunci kalibrasi.
  - Transisi status otomatis ke READY begitu mesin dimatikan atau pada proses booting berikutnya.
- **Dual Target Tegangan Tinggi (HV)**:
  - Dukungan seleksi tegangan HV **NORMAL (285 V)** dan **PRO (345 V)** via perintah BLE `SETUP,VOLTAGE`.
  - Ambang batas proteksi tegangan berlebih disesuaikan ke 360 V untuk mendukung mode PRO 345 V.
- **Pembersihan Logika Interlock Hardware Lama**: Menghilangkan dependensi interlock fisik `JP_HV`, `SW_ARM`, dan `JP_PRO` dari firmware R8, digantikan dengan software safety checks dan monitoring pasif.

### Versi 7.2.3
- **Preservasi Status Quick Setup**: Mencegah resetting status workflow setup stage saat menerima frame telemetri v3 dari mikrokontroler.
- **Konfigurasi Pulser Lanjutan**: Pilihan Trigger Edge (`FALLING`/`RISING`), rasio pulsa 1–4 PPR, dan durasi trigger gate SCR (60–120 µs).
- **Kontrol Kipas Radiator Terintegrasi**: Mode kipas (`OFF`/`ON`/`AUTO`) untuk relai radiator J1.7 (PB5) dilengkapi safety interlock.
- **Statistik Paket & Integritas Real-Time**: Sliding window 5 detik untuk frekuensi paket aktual (`Hz`) dan rasio validitas CRC16 (`%`).
- **Indikator Kualitas Link BLE Dinamis**: Klasifikasi status kestabilan koneksi (`STABIL`, `CUKUP`, `BURUK`, `TERPUTUS`).
- **Unit Test Komprehensif**: Pengujian unit otomatis untuk algoritma CRC16-CCITT dan parser telemetri.

### Versi 7.2.2
- **Karakter Baru Moge 1800cc Super Bass**: Sintesis audio diperbarui ke irama 850 RPM stasioner slow-chug, subwoofer bass booster 38–75 Hz, dan saturasi empuk analog.
- **Simulasi Putar Tuas Gas (BLIP)**: Menghadirkan fungsi `triggerThrottleBlip` responsif (TPS 85%, lonjakan RPM kuadratik, raungan gas, dan inersia kembali ke idle).
- **Tacho Slider Cerdas**: Menahan RPM di mode demo dan mengikuti RPM motor di mode BLE nyata.
