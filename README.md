# NS200 CDI R8 (v8.1.0) — Modern Android Tuning, Telemetry, Modular Hardware & OTA Suite

Aplikasi Android kendali terpadu untuk unit pengapian **CDI Programmable NS200-CDI-R8** (Bajaj Pulsar 200 DTS-i & Modifikasi Dual/Triple Spark). Menggabungkan kokpit telemetri balap gaya MoTeC, pemetaan kurva pengapian 4-slot dinamis, kalibrasi strobo pulser TDC, mode pembelajaran kurva asli (**OEM Learn Mode**), sistem pengunggah firmware nirkabel (**BLE OTA Firmware Uploader**), alur aktivasi mandiri aman (**Safe DIY Mode**), katalog modul jadi pasaran (*Commercial Off-the-Shelf Drop-in Modules*), bengkel panduan kabel interaktif, diagnostik paket data biner BLE, serta simulator akustik mesin knalpot multi-silinder (*Live Audio Engine Test Bench*).

Mendukung Arsitektur Lintas Platform (*Dual-Platform*): [**WeAct STM32WB55**](https://github.com/phreakazone/Firmware_CDI_NS200) dan [**ESP32 WROOM**](https://github.com/phreakazone/Firmware_CDI_NS200_ESP32).

---

## 📋 Daftar Isi
1. [Fitur Utama](#-fitur-utama)
2. [Pembaruan Besar Firmware R8 & Aplikasi v8.1](#-pembaruan-besar-firmware-r8--aplikasi-v81)
3. [Katalog & Panduan Modul Siap Pakai di Pasaran (Drop-In Modular Upgrade)](#-katalog--panduan-modul-siap-pakai-di-pasaran-drop-in-modular-upgrade)
4. [Arsitektur & Tumpukan Teknologi](#-arsitektur--tumpukan-teknologi)
5. [Detail Modul & Layar](#-detail-modul--layar)
   - [1. Dashboard MoTeC & Tacho Slider](#1-dashboard-motec--tacho-slider)
   - [2. Ignition Maps (4-Slot Timing)](#2-ignition-maps-4-slot-timing)
   - [3. Setup CDI, OEM Learn & Kalibrasi TDC](#3-setup-cdi-oem-learn--kalibrasi-tdc)
   - [4. Pengunggah Firmware BLE OTA (APP.bin)](#4-pengunggah-firmware-ble-ota-appbin)
   - [5. Live Audio Engine Test Bench & MOGE Super Bass](#5-live-audio-engine-test-bench--moge-super-bass)
   - [6. Quick Setup & Wiring Workshop](#6-quick-setup--wiring-workshop)
   - [7. BLE Terminal & Hex Diagnostics](#7-ble-terminal--hex-diagnostics)
6. [Protokol Komunikasi BLE Firmware R8 & Kontrak Android](#-protokol-komunikasi-ble-firmware-r8--kontrak-android)
7. [Skema Wiring Pinout & Transisi Fase (Konektor 12-Pin J1)](#-skema-wiring-pinout--transisi-fase-konektor-12-pin-j1)
8. [Instalasi & Kompilasi](#-instalasi--kompilasi)
9. [Catatan Rilis (Changelog)](#-catatan-rilis-changelog)

---

## 🚀 Fitur Utama

- **Koneksi Nirkabel BLE Ultra-Stabil**: Scanning otomatis, auto-reconnect, pengiriman perintah berbasis antrean (*queue-based write*), handshaking kapabilitas `GET,CAPS`, dan proteksi transisi mode bebas *ghost telemetry*.
- **Telemetri Balap Real-Time (20 Hz)**: Memantau RPM hingga 13.000, bukaan gas (TPS 0–100%), derajat *ignition advance* (° BTDC), tegangan pengisian kapasitor HV Center & Side (hingga 345V PRO), voltase aki (Cv), suhu mesin (°C), dan status *rev limiter*.
- **Mode Pembelajaran Mandiri (OEM Learn Pasif)**: Membaca pulsa pengapian CDI bawaan pabrik secara pasif melalui input mikrokontroler saat mesin hidup, merekam kurva pengapian asli motor secara otomatis.
- **Rangkaian Pengaman & Opsi Modul Pasaran**: Panduan visual interaktif rangkaian isolasi optik 4-channel PC817, modul buck DC-DC MP1584, modul boost HV ZVS 45–390V, serta modul komparator pulser LM393 siap pakai di pasaran untuk memangkas kerumitan perakitan solderan hingga 85%.
- **Pengunggah Firmware BLE OTA**: Memperbarui binary mikrokontroler (`APP.bin`) langsung lewat Bluetooth LE tanpa kabel, dilengkapi verifikasi CRC32 dan interlock keselamatan preflight.
- **Alur Setup Checkpoint & Verifikasi Flash Nyata**: 
  - Alur OEM: Rekam timing pasif ➔ Konfirmasi cabut output koil OEM (`OEM_UNPLUGGED`) ➔ FIRST START aman (220V, center saja, advance ≤10°, limiter 3.000 RPM).
  - Menunggu bukti status `READY` nyata yang tersimpan di flash MCU (bukan hanya berhenti pada timer lokal).
  - Layout Strobo manual lama diisolasi eksklusif hanya untuk jalur `MANUAL` (darurat saat CDI OEM mati).
- **Seleksi Tegangan HV R8 Aktif (285 V & 345 V)**: Sakelar PRO memuat profil R8 yang tepat (`LOAD,<slot>`), sehingga target tegangan aktual berpindah nyata 285 V ↔ 345 V di MCU.
- **Pemetaan Kurva Pengapian 4 Slot**: Pilihan instan Slot 0 (Eco/Harian), Slot 1 (Touring/Street), Slot 2 (Wet/Rain), dan Slot 3 (Pro/Race 16x8 matrix). Dilengkapi *safety ceiling* 36.0° BTDC.
- **Slider RPM Interaktif & BLIP Gas**:
  - Pada **Mode Demo**: Slider menahan RPM simulasi secara stabil untuk pengujian audio dan visual.
  - Pada **Koneksi BLE Nyata**: Slider mengikuti putaran mesin motor asli secara *real-time*.
  - **Tombol BLIP Gas**: Simulasi puntiran gas kilat dengan lonjakan TPS spontan, akselerasi kuadratik, dan deselerasi inersia.
- **Simulator Suara Mesin Knalpot (Live Audio Test Bench)**: 12 preset suara knalpot multi-silinder berbasis sintesis kompresi akustik PCM 22.050 Hz 16-bit, termasuk preset **Moge 1800cc Super Bass (Gahar Empuk)**.
- **Bengkel Panduan Wiring & Solder**: Visualisasi interaktif soket 12-pin CDI, kode warna harness asli NS200, jalur koil sekunder, dan panduan langkah demi langkah.

---

## ⚡ Pembaruan Besar Firmware R8 & Aplikasi v8.1

| Fitur | Firmware R7 Lama | Firmware R8 / Aplikasi v8.1 Baru |
|---|---|---|
| **Alur Akuisisi Timing** | Wajib strobo manual / timing light tanda T | **OEM Learn Pasif**: Rekam kurva CDI OEM langsung via Input MCU. |
| **Pilihan Perakitan Hardware** | Wajib solder puluhan komponen diskrit di perfboard | **Drop-in Modular Ready**: Mendukung modul jadi pasaran (Modul PC817 4-ch, Modul Buck MP1584EN, Modul Boost ZVS HV 45-390V). |
| **Interlock Fisik** | Mengharuskan jumper `JP_HV`, `SW_ARM`, `JP_PRO` | **Software Interlock & Safety Confirmation**: Menghilangkan batasan saklar fisik; kontrol mode terpadu di aplikasi. |
| **Aktivasi Mode DIY** | Manual via jumper dan langkah rumit | **Safe DIY Mode**: Wajib konfirmasi `OEM_UNPLUGGED` (tidak ada pengambilalihan otomatis berbahaya). |
| **First Start & Ready** | Mengharuskan pencabutan jumper berulang kali | **Verifikasi Flash Nyata**: Deteksi idle stabil ≥3s mengunci kalibrasi, lalu aplikasi menunggu status `READY` nyata dari flash MCU. |
| **Level Tegangan HV** | Terkunci pada 240V–280V | **Dual Target R8 Aktif**: NORMAL (285 V) dan PRO (345 V) via pemuatan profil firmware R8 sesungguhnya. |
| **Handshake Protokol BLE** | Terbatas pada GET,STATUS | **Kontrak Lengkap**: Mengenali `GET,CAPS`, `MODE`, `LEARN`, `OTA`, sambil menjaga kompatibilitas UUID dan paket telemetri biner v3. |
| **Update Firmware MCU** | Wajib buka bodi & ST-Link V2 / DFU USB | **BLE OTA Flashing**: Unggah `APP.bin` langsung lewat aplikasi Android dengan proteksi CRC32. |

---

## 🛒 Katalog & Panduan Modul Siap Pakai di Pasaran (Drop-In Modular Upgrade)

Untuk mengurangi kerumitan wiring kabel dan solder-menyolder komponen diskrit, sistem **NS200 CDI R8** mendukung perakitan berbasis **Modul Jadi Siap Pakai di Pasaran** (*Commercial Off-The-Shelf Modules*).

> **PENTING**: Seluruh wiring kabel harness bawaan motor NS200 (konektor 12-pin J1) **tetap dipertahankan 100%**. Penggunaan modul bersifat opsional untuk menggantikan masing-masing blok fungsi internal di dalam boks CDI.

### Ringkasan Blok Fungsi & Modul Pengganti

> **FILOSOFI MODULAR v8.1**: Rekomendasi utama berfokus pada **2 MODUL JADI PASARAN (NOL PCB CUSTOM)** yang memangkas perakitan paling rumit, sementara blok yang kritis tetap mempertahankan performa komponen teruji.

| Blok Fungsi CDI | Status Rekomendasi | Modul Pasaran Siap Pakai | Estimasi Harga | Alasan Teknis & Keuntungan Utama |
|---|---|---|---|---|
| **1. OEM Learn Signal Isolator** | ⭐ **SANGAT DIREKOMENDASIKAN #1** | **Modul Optocoupler PC817 4-Channel Isolation Board** | Rp 12.000 – Rp 18.000 | Terminal sekrup (baut obeng), 4x LED indikator kedip pulsa, jumper pull-up onboard, isolasi optik 5000V. Cukup 1 modul untuk dua kanal sekaligus: sadapan Center (J1.12) dan Side (J1.6). |
| **2. Driver Relay Kipas** (J1.7 / Radiator Fan) | ⭐ **SANGAT DIREKOMENDASIKAN #2** | **Modul Relay 1-Channel 5V dengan Optocoupler** | Rp 8.000 – Rp 14.000 | Menggantikan transistor BC547 diskrit. Pin MCU langsung masuk ke pin `IN` modul. Sudah ada optoisolator, dioda flyback proteksi lonjakan motor kipas, dan terminal sekrup. |
| **3. Catu Daya Logic 5V** (+12V Kontak ke +5V MCU) | Alternatif Opsional | **Modul Mini DC-DC Buck MP1584EN / LM2596** | Rp 8.000 – Rp 15.000 | Menggantikan regulator linear panas. Menghasilkan 5.0V DC dingin & stabil untuk MCU. |

---

### ⚠️ Evaluasi Modul Pasaran yang DITOLAK (JANGAN DIGUNAKAN)

Setelah pengujian teknis mendalam terhadap karakteristik CDI kapasitif DTS-i, modul-modul berikut **TIDAK DIREKOMENDASIKAN**:

1. ❌ **Modul Boost Converter 12V → 300–1200V untuk Charge Pump HV**:
   - **Penyebab**: Kemampuan arus keluaran modul pasaran ini rata-rata hanya **2 – 20 mA**. Untuk sistem pengapian 3 busi (Triple Spark) pada putaran tinggi (10.000 RPM), sistem membutuhkan arus pengisian kapasitor minimal **80 – 120 mA**. Arus 2–20 mA tidak akan mampu mengisi kapasitor tepat waktu sehingga pengapian akan drop / misfire parah di putaran menengah ke atas. Selain itu, modul boost generik tidak memiliki pin kontrol PWM dari firmware untuk switching dinamis level tegangan 285V (Normal) dan 345V (Mode PRO).
2. ❌ **Modul Bridge Rectifier Generik**:
   - **Penyebab**: Modul penyearah jembatan generik di pasaran dirancang untuk frekuensi jala-jala listrik PLN (50/60 Hz), bukan frekuensi switching tinggi trafo frekuensi tinggi ATX/flyback (~100 kHz). Jika dipaksakan, dioda akan mengalami panas ekstrem (*thermal breakdown*) dan *forward voltage drop* yang tinggi. Gunakan dioda ultrafast diskrit seperti **UF4007** (trr < 75ns).
3. ❌ **Modul SCR / Dimmer AC**:
   - **Penyebab**: Rangkaian gerbang pemicu pada modul dimmer AC dirancang untuk arus bolak-balik AC 220V frekuensi rendah. Tidak responsif untuk pulsa trigger mikrodetik (60–100 µs) discharge kapasitor CDI DC. Tetap gunakan thyristor **BT151-800R** atau **TYN612**.
4. ❌ **Modul Sensor Tegangan Generik (Voltage Divider Module)**:
   - **Penyebab**: Modul sensor tegangan pasaran umumnya menggunakan rasio pembagi tetap (misal 5:1 untuk Arduino 5V 0–25V). Rasio ini tidak cocok dan tidak presisi untuk kalibrasi ADC 3.3V firmware pada pembacaan feedback tegangan HV (300V+), TPS, maupun sensor suhu NTC. Pembagi resistif terkalibrasi diskrit dengan dioda clamp pengaman **BAT54S** jauh lebih presisi dan aman.

---

### Detail Pemasangan Modul Optocoupler PC817 4-Channel (OEM Training / Learn)
Modul ini digunakan HANYA pada Fase 1 (OEM_LEARN) untuk membaca sinyal timing koil pengapian CDI OEM secara pasif dan aman tanpa risiko merusak mikrokontroler.

> **Catatan Sadapan Kabel OEM Learn**:
> Sinyal asli dari koil memiliki tegangan ratusan volt yang akan merusak MCU jika tidak diisolasi. Buat 2 kabel cabang/paralel (*pigtail probe*) dari soket motor:
> - **Kabel Sadap Utama** ➔ Diambil dari sambungan paralel **J1.12** (Koil Center OEM). Masuk ke IN1+ modul PC817 via R 47kΩ 2W.
> - **Kabel Sadap Samping** ➔ Diambil dari sambungan paralel **J1.6** (Koil Side OEM). Masuk ke IN2+ modul PC817 via R 47kΩ 2W.

```text
┌───────────────────────────────────────────────────────────────┐
│       MODUL OPTOCOUPLER PC817 4-CHANNEL ISOLATION BOARD       │
├───────────────────────────────┬───────────────────────────────┤
│   [TERMINAL INPUT KOIL OEM]   │     [TERMINAL OUTPUT MCU]     │
│                               │                               │
│ IN1+ ──[ R 47kΩ 2W ]── J1.12  │ OUT1 ──────> PIN INPUT CTR    │
│      (Kabel Sadapan J1.12)    │      (STM32 PB3 / ESP GPIO16) │
│ IN1- ───────────────── J1.11  │ OUT2 ──────> PIN INPUT SIDE   │
│      (GND Motor Massa)        │      (STM32 PB4 / ESP GPIO17) │
│                               │ OUT3 ──────  (Cadangan)       │
│ IN2+ ──[ R 47kΩ 2W ]── J1.6   │ OUT4 ──────  (Cadangan)       │
│      (Kabel Sadapan J1.6)     │                               │
│ IN2- ───────────────── J1.11  │ VCC  ──────> 3V3 (MCU)        │
│      (GND Motor Massa)        │ GND  ──────> GND (MCU)        │
├───────────────────────────────┴───────────────────────────────┤
│ [LED1] [LED2] [LED3] [LED4]  • Indikator Kedip Pulsa          │
│ [JP1]  [JP2]  [JP3]  [JP4]   • Jumper Output Level (Set VCC)  │
└───────────────────────────────────────────────────────────────┘
```

**Langkah & Tutorial Singkat**:
1. Siapkan 2 buah Resistor **47 kΩ 2 Watt** (wajib daya besar 2 Watt). Pasang secara seri pada kabel sebelum masuk ke terminal `IN1+` dan `IN2+` untuk menahan spike tegangan dari pulsa koil pengapian.
2. Sambungkan terminal `IN1-` dan `IN2-` ke Ground massa motor (`J1.11 GND`).
3. Beri daya modul sisi output dengan menyambungkan `VCC` ke Pin **3V3** MCU dan `GND` ke Pin **GND** MCU.
4. Pasang jumper JP1 & JP2 modul pada posisi **VCC** (pull-up internal aktif ke 3.3V).
5. Sambungkan terminal `OUT1` dan `OUT2` ke pin Input MCU yang sesuai (Lihat Bab Skema Wiring Pinout).
6. Saat mesin dihidupkan dengan CDI OEM, LED1 dan LED2 pada modul akan berkedip mengikuti percikan busi, dan counter pulsa di aplikasi Android akan bergerak naik!

---

## 🛠 Arsitektur & Tumpukan Teknologi

- **Bahasa**: Kotlin (100% Coroutines & StateFlow)
- **UI Framework**: Jetpack Compose dengan Material Design 3 (M3) Dark Carbon & MoTeC Racing Theme
- **State Management**: MVVM Architecture via `CdiViewModel` terpusat
- **Bluetooth Stack**: Android BLE API (`BluetoothGatt`, MTU 64/247, Queue-based Writer, BLE OTA Chunk Streaming)
- **Audio Synthesis Engine**: Android `SoundPool` (multi-stream crossfade) & `MediaPlayer` (custom tracks)
- **Data Serialization**: `kotlinx.serialization` untuk protokol paket biner CDI
- **Hardware Target (Dual-Platform)**:
  - [**WeAct STM32WB55**](https://github.com/phreakazone/Firmware_CDI_NS200) (Platform Orisinal).
  - [**ESP32 WROOM**](https://github.com/phreakazone/Firmware_CDI_NS200_ESP32) (Platform Porting).

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
Wizard komisi terpadu satu tahap per layar: BARU ➔ PULSER ➔ TDC ➔ TPS ➔ FIRST START ➔ READY.
- **Alur OEM Learn Pasif**:
  1. **Rekam Timing OEM**: Merekam timing asli dari CDI bawaan motor. Counter pulsa Center dan sampel Side terbaca secara real-time.
  2. **Konfirmasi Cabut Output OEM (`OEM_UNPLUGGED`)**: Tombol pengaman wajib untuk memastikan soket koil CDI OEM telah dilepas sebelum mengalihkan pengapian ke modul MCU, mencegah benturan driver.
  3. **FIRST START Aman**: Sistem mengunci mode aman (220V, center saja, advance ≤10°, limiter 3.000 RPM). Begitu idle stabil ≥3 detik, status tersimpan di flash MCU.
  4. **Verifikasi Flash READY Nyata**: Aplikasi menunggu status `READY` nyata dari mikrokontroler (tidak berhenti hanya karena timer lokal).
- **Layout Strobo Khusus Manual**: Offset TDC hanya dimunculkan pada mode `MANUAL` (jalur darurat jika CDI OEM rusak).
- **Target Tegangan HV R8 Aktif**:
  - Tombol seleksi tegangan HV **NORMAL (285 V)** dan **PRO (345 V)**.
  - Memuat profil R8 yang tepat (`LOAD,<slot>`) sehingga target aktual pada hardware MCU benar-benar berpindah 285 V ↔ 345 V.

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
- **Katalog Drop-In Modul Pasaran**: Panduan modul siap pakai pengganti blok diskrit.
- **Preflight tahap BARU**: Memeriksa `PING`, `GET,STATUS`, dan `GET,SETUP` (wajib RPM 0 dan HV < 30V).
- **Konfigurasi Pulser Lanjutan (`PulserAdvancedSettings`)**:
  - Pilihan Trigger Edge: `FALLING` (standar NS200) atau `RISING`.
  - Pilihan Rasio Pulsa: 1, 2, 3, atau 4 PPR (Pulse Per Revolution).
  - Durasi Gate SCR: 60 µs, 80 µs (standar NS200), 100 µs, atau 120 µs.
- **Kontrol Mode Kipas Radiator (`FanModeSettings`)**:
  - Pilihan mode: `OFF`, `ON`, dan `AUTO` dengan safety interlock.
- **Soket CDI 12-pin**: Kode warna kabel asli NS200, jalur koil sekunder, dan sensor TPS.

### 7. BLE Terminal & Hex Diagnostics
Diagnostik teknis tingkat lanjut:
- **Statistik Paket Real-Time (Sliding Window 5 Detik)**: `packetRateHz` (18–22 Hz) dan integritas `crcValidPercent`.
- **Indikator Kualitas Sambungan BLE (`LinkQuality`)**: STABIL (Hijau), CUKUP (Kuning), BURUK (Merah), TERPUTUS (Abu-abu).
- **Monitor Paket Heksadesimal Mentah**: Menampilkan 20 bytes data v3 lengkap dengan penyorotan warna per field.
- **Konsol Manual Perintah CDI**: Terminal input untuk eksekusi perintah teks ASCII MCU dan log respons.

---

## 📡 Protokol Komunikasi BLE Firmware R8 & Kontrak Android

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
- **Handshake Kapabilitas**:
  - `GET,CAPS` : Menanyakan kapabilitas firmware R8 (`CAPS,OK,OEM_LEARN,PRO_HV,OTA`).
- **Mode & Pembelajaran**:
  - `MODE,OEM_LEARN` : Mengaktifkan mode belajar timing pasif dari CDI OEM.
  - `MODE,MANUAL` : Mengaktifkan mode manual/strobo darurat.
  - `MODE,DIY,OEM_UNPLUGGED` : Mengaktifkan operasi CDI mandiri setelah soket OEM dicabut.
  - `LEARN,START` : Memulai perekaman pulsa pengapian OEM.
  - `LEARN,STOP` : Menghentikan perekaman dan menyimpan timing ke flash.
  - Respons pulsa: `@<seq>,LEARN,PULSES,<center_count>,<side_samples>`
- **Pengaturan Tegangan R8**:
  - `SETUP,VOLTAGE,NORMAL` : Mengatur target HV ke 285 V (penggunaan standar/harian).
  - `SETUP,VOLTAGE,PRO` : Mengatur target HV ke 345 V (kompetisi balap).
  - `LOAD,<slot>` : Memuat profil slot pengapian sesuai target voltase aktif.
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

## 🔌 Skema Wiring Pinout & Transisi Fase (Konektor 12-Pin J1)

Tabel ini memetakan fungsi kabel harness bawaan motor NS200 ke pin yang tepat untuk platform STM32 maupun ESP32, guna menghilangkan segala bentuk ambiguitas operasional.

| J1 | Fungsi | Warna Kabel | Pin STM32 | Pin ESP32 | Deskripsi Kelistrikan & Routing |
|:--:|:-------|:------------|:----------|:----------|:--------------------------------|
| 1  | NC | Kosong / NC | - | - | Tidak terhubung. Isolasi rapi. |
| 2  | TPS_A | Hijau-Putih | PA3 / PA5 | **GPIO36** | Input sensor bukaan gas pasangan A. |
| 3  | TEMP | Hitam-Putih | PA4 | **GPIO39** | Input sensor suhu mesin NTC (Pull-up 4.7k ke 5V). |
| 4  | TPS_B | Abu-Abu | PA3 / PA5 | **GPIO34** | Input sensor bukaan gas pasangan B / Referensi. |
| 5  | +12 V kontak | Cokelat | - | - | Input daya utama kunci kontak ON. Melewati penurun tegangan ke 5V. |
| 6  | COIL_SIDE | Hitam-Merah | **PA2** | **GPIO26** | **OUTPUT (DIY):** Menembak koil samping via SCR driver menuju J1.6. |
| 7  | FAN_RELAY | Biru-Kuning | PB5 | **GPIO13** | Output kendali relay kipas radiator otomatis. |
| 8  | OEM_SIDE | Kosong / NC | **PB4** | **GPIO17** | **INPUT (Learn):** Menyadap pulsa koil samping pabrik via Optocoupler dari kabel **J1.6**. |
| 9  | OEM_CTR | Kosong / NC | **PB3** | **GPIO16** | **INPUT (Learn):** Menyadap pulsa koil utama pabrik via Optocoupler dari kabel **J1.12**. |
| 10 | PULSER | Putih-Merah | **PA0** | **GPIO4** | Input sensor magnet. Tersambung permanen ke MCU via modul komparator LM393. |
| 11 | GND | Hitam-Kuning | GND | GND | Ground utama massa motor. |
| 12 | COIL_CENTER | Oranye | **PA1** | **GPIO25** | **OUTPUT (DIY):** Menembak koil tengah via SCR driver menuju J1.12. |

### ⚠️ PERHATIAN: Transisi Hardware (Fase LEARN ➔ Fase DIY)
Untuk menghindari benturan arus driver koil dan memastikan keselamatan mikrokontroler, fungsionalitas pin J1.12 dan J1.6 diperlakukan berbeda secara fisik sesuai fasenya.

**FASE 1: Penyadapan Pasif (Mode OEM_LEARN)**
Pada fase ini, **CDI bawaan pabrik (OEM) WAJIB tetap menancap di soket motor** dan mengendalikan mesin. Mikrokontroler bertindak murni sebagai PENDENGAR (Input).
1. **Jalur Input (Wajib Pasang):** Kabel Pulser (J1.10) terhubung permanen ke pin pembaca pulser (PA0 / GPIO4).
   - Kabel Sadap koil tengah diambil dengan cara menyambung paralel kabel dari **J1.12** ➔ Optocoupler PC817 ➔ Pin Pembaca (PB3 / GPIO16).
   - Kabel Sadap koil samping diambil dengan menyambung paralel dari **J1.6** ➔ Optocoupler PC817 ➔ Pin Pembaca (PB4 / GPIO17).
2. **Jalur Output (Wajib Terputus):** Pin penembak koil MCU (PA1/PA2 atau GPIO25/GPIO26) **TIDAK BOLEH** tersambung ke koil. Pin ini dibiarkan menggantung bebas.

**FASE 2: Pengambilalihan Penuh (Mode DIY / FIRST_START)**
Pada fase ini, **CDI bawaan pabrik (OEM) WAJIB dicabut secara fisik dari soket motor**. Mikrokontroler kini bertindak sebagai PENEMBAK (Output) yang mengontrol pengapian secara penuh.
1. **Konfirmasi Cabut CDI Pabrik:** Buka aplikasi Android, ubah mode ke DIY, dan centang konfirmasi bahwa soket OEM telah dilepas (`OEM_UNPLUGGED`).
2. **Jalur Input (Wajib Lepas):** Pin pembaca koil penyadap (PB3/PB4 atau GPIO16/GPIO17) dilepas/diabaikan dari rangkaian karena CDI OEM sudah dicabut.
3. **Jalur Output (Wajib Pasang):** Pin penembak koil MCU (PA1/PA2 atau GPIO25/GPIO26) dihubungkan permanen ke sirkuit SCR menuju soket jalur **J1.12** dan **J1.6** untuk memicu busi secara mandiri.

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

### Versi 8.1.0 (Rilis Arsitektur Modular & Pengaman OEM Learn R8)
- **Katalog & Panduan Modul Siap Pakai di Pasaran**:
  - Menyediakan panduan lengkap penggantian blok diskrit dengan modul siap pakai di pasaran (*drop-in modules*) untuk memangkas kerumitan perakitan solderan hingga 85%.
  - Integrasi visual dan tutorial **Modul Optocoupler PC817 4-Channel** dengan terminal sekrup baut, LED indikator pulsa, dan jumper pull-up onboard untuk alur OEM Learn.
  - Panduan modul pelengkap: **Modul Buck MP1584EN** (+12V ke +5V), **Modul Boost ZVS HV 45-390V** (Pengecas Kapasitor 285V/345V), **Modul Komparator LM393 Speed Sensor** (Pulser Pick-up J1.10), dan **Modul Relay Opto 1-Channel** (Kipas Radiator J1.7).
  - Mempertahankan 100% kompatibilitas wiring soket harness bawaan NS200 12-pin (J1).
- **Kontrak Firmware R8 & Handshake Kapabilitas**:
  - Menambahkan handshake `GET,CAPS` saat koneksi BLE terhubung untuk mendeteksi kapabilitas firmware R8 (`OEM_LEARN`, `PRO_HV`, `OTA`).
  - Menjaga keutuhan UUID BLE GATT dan struktur telemetri biner v3 (20-byte).
  - Menghapus asumsi pin lawas sebagai jumper fisik lama; pin sadap kini murni diakui sebagai probe pasif OEM Center & Side.
- **Penyempurnaan Alur Setup Checkpoint**:
  - Alur OEM Learn: Tahap Rekam Timing ➔ Konfirmasi Cabut Output OEM (`OEM_UNPLUGGED`) ➔ Tahap First Start Aman.
  - Layout Strobo lama diisolasi eksklusif hanya pada jalur `MANUAL` (darurat).
  - Logika verifikasi status: Aplikasi menunggu status `READY` nyata dari flash MCU (`t.ready == true` atau stage 4+), bukan berhenti hanya pada hitungan detik lokal.
- **Sakelar Tegangan PRO Aktif**:
  - Sakelar PRO kini memuat profil firmware R8 yang sesuai (`LOAD,<slot>`) dan memverifikasi sinkronisasi status ke MCU, memastikan tegangan pengisian kapasitor aktual berpindah antara 285 V dan 345 V.

### Versi 8.0.0 (Rilis Utama Firmware R8)
- **Dukungan Penuh Firmware R8**: Integrasi menyeluruh dengan arsitektur firmware terbaru Dual-Core MCU.
- **Pengunggah Firmware BLE OTA (`APP.bin`)**:
  - Menu pengunggah binary firmware langsung via BLE GATT di tab Terminal / Hex.
  - Pemeriksaan keselamatan preflight ketat: hanya dapat dimulai saat RPM = 0, koil OFF, dan HV < 30V.
  - Streaming chunk 208-byte dengan verifikasi checksum CRC32 dan tombol pembatalan.
- **Alur OEM Learn Pasif**:
  - Pembacaan sinyal timing asli dari CDI bawaan motor.
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
- **Pembersihan Logika Interlock Hardware Lama**: Menghilangkan dependensi interlock fisik jumper dari firmware R8, digantikan dengan software safety checks dan monitoring pasif.

### Versi 7.2.3
- **Preservasi Status Quick Setup**: Mencegah resetting status workflow setup stage saat menerima frame telemetri v3 dari mikrokontroler.
- **Konfigurasi Pulser Lanjutan**: Pilihan Trigger Edge (`FALLING`/`RISING`), rasio pulsa 1–4 PPR, dan durasi trigger gate SCR (60–120 µs).
- **Kontrol Kipas Radiator Terintegrasi**: Mode kipas (`OFF`/`ON`/`AUTO`) untuk relai radiator dilengkapi safety interlock.
- **Statistik Paket & Integritas Real-Time**: Sliding window 5 detik untuk frekuensi paket aktual (`Hz`) dan rasio validitas CRC16 (`%`).
- **Indikator Kualitas Link BLE Dinamis**: Klasifikasi status kestabilan koneksi (`STABIL`, `CUKUP`, `BURUK`, `TERPUTUS`).
- **Unit Test Komprehensif**: Pengujian unit otomatis untuk algoritma CRC16-CCITT dan parser telemetri.

### Versi 7.2.2
- **Karakter Baru Moge 1800cc Super Bass**: Sintesis audio diperbarui ke irama 850 RPM stasioner slow-chug, subwoofer bass booster 38–75 Hz, dan saturasi empuk analog.
- **Simulasi Putar Tuas Gas (BLIP)**: Menghadirkan fungsi `triggerThrottleBlip` responsif (TPS 85%, lonjakan RPM kuadratik, raungan gas, dan inersia kembali ke idle).
- **Tacho Slider Cerdas**: Menahan RPM di mode demo dan mengikuti RPM motor di mode BLE nyata.
