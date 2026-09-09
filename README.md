# NS200 CDI R7.2 — Modern Android Tuning & Telemetry Suite

Aplikasi Android kendali terpadu untuk unit pengapian **CDI Programmable NS200-CDI-R7.2** (Bajaj Pulsar 200 DTS-i & Modifikasi Dual/Triple Spark). Menggabungkan kokpit telemetri balap gaya MoTeC, pemetaan kurva pengapian 4-slot dinamis, kalibrasi strobo pulser TDC, bengkel panduan kabel interaktif, diagnostik paket data biner BLE, serta simulator akustik mesin knalpot multi-silinder (*Live Audio Engine Test Bench*).

---

## 📋 Daftar Isi
1. [Fitur Utama](#-fitur-utama)
2. [Arsitektur & Tumpukan Teknologi](#-arsitektur--tumpukan-teknologi)
3. [Detail Modul & Layar](#-detail-modul--layar)
   - [1. Dashboard MoTeC & Tacho Slider](#1-dashboard-motec--tacho-slider)
   - [2. Ignition Maps (4-Slot Timing)](#2-ignition-maps-4-slot-timing)
   - [3. Setup CDI & Kalibrasi TDC](#3-setup-cdi--kalibrasi-tdc)
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

### 3. Setup CDI & Kalibrasi TDC
Tab utama **Setup** membuka alur komisi CDI lengkap. Halaman yang sama juga dapat dibuka melalui **Wiring → Komisi CDI**, sehingga tidak ada lagi dua konfigurasi strobo yang berbeda.

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
- **Preflight tahap BARU**: Tombol `PERIKSA & LANJUT` mengantrekan `PING`, `GET,STATUS`, dan `GET,SETUP`. Tahap 2 hanya dibuka setelah ketiga respons diterima serta RPM = 0 dan kedua HV < 30 V. Pesan layar menyebutkan respons yang hilang jika timeout.
- **Status MCU dan halaman wizard dipisahkan**: Firmware menyimpan lima status (`0..4`), sedangkan aplikasi memiliki enam halaman (`0..5`) karena TPS adalah halaman panduan tambahan. `wizardStageFromFirmware()` memetakan keduanya agar FIRST START dan READY tidak bergeser satu tahap.
- **Navigasi aman**: Kepala kartu tahap dapat diketuk untuk kembali ke tahap yang sudah terbuka. Membuka tahap yang belum selesai tidak mengubah flash MCU dan akan ditolak dengan alasan yang terlihat.
- **Konfigurasi Pulser Lanjutan (`PulserAdvancedSettings`)**:
  - Pilihan Trigger Edge: `FALLING` (standar NS200) atau `RISING`.
  - Pilihan Rasio Pulsa: 1, 2, 3, atau 4 PPR (Pulse Per Revolution).
  - Durasi Gate SCR: 60 µs, 80 µs (standar NS200), 100 µs, atau 120 µs.
- **Kontrol Mode Kipas Radiator (`FanModeSettings`)**:
  - Pilihan mode: `OFF`, `ON`, dan `AUTO` (pin J1.7 / PB5 WeAct).
  - Dilengkapi *Safety Interlock*: tombol hanya dapat ditekan saat mesin mati (`RPM == 0`) dan tegangan kapasitor aman (`HV < 30V`).
- **Proteksi Transaksi Ganda**: Seluruh tombol aksi alur setup dilengkapi state pending (`setupCommandPending`) dengan timeout proteksi 5 detik.
- **Soket CDI 12-pin**: Kode warna kabel asli NS200, jalur koil sekunder, sensor TPS, dan koneksi pinout WeAct STM32WB55CGU6.

#### Cara konfigurasi awal melalui menu Setup

Alur ini dapat dibuka dari dua tempat: tab utama **Setup**, atau **Wiring → Komisi CDI**. Keduanya menggunakan `CdiViewModel` dan status MCU yang sama; perubahan pada salah satu halaman langsung terlihat pada halaman lainnya.

> **Bahaya tegangan tinggi:** kapasitor CDI dapat menyimpan ratusan volt. Pasang atau lepas `JP_HV` hanya saat kontak/kill switch OFF dan pembacaan HV CENTER serta SIDE sudah di bawah 30 V.

1. **Persiapan sebelum menghubungkan BLE**
   - Pastikan jalur PCB dan soket J1 sudah diverifikasi sesuai menu Wiring.
   - Lepas `JP_HV`; keluaran charger HV dan koil harus belum aktif.
   - Nyalakan kontak agar STM32 mendapat daya, tetapi jangan hidupkan mesin.
   - Buka menu BLE dan hubungkan `NS200-CDI-R7` tanpa melakukan pairing/PIN Android. Tunggu status `GATT READY`.

2. **Tahap 1 — BARU / pemeriksaan komunikasi**
   - Buka menu **Setup** dan tekan `PERIKSA & LANJUT KE TAHAP 2`.
   - Aplikasi memeriksa tiga respons secara berurutan: `PING`, `GET,STATUS`, dan `GET,SETUP`.
   - Hasil lulus membutuhkan ketiga respons diterima, RPM = 0, HV CENTER <30 V, dan HV SIDE <30 V.
   - Jika `PACKET RATE` masih 0 Hz tetapi tiga respons lulus, kanal kontrol 1003 bekerja namun notifikasi Telemetry 1001 belum masuk. Tahap PULSER dapat dibuka, tetapi jangan mengonfirmasi pulser sebelum nilai RPM/quality sudah terbaca.

3. **Tahap 2 — PULSER**
   - Konfigurasi awal NS200: edge `FALLING`, `PPR = 1`, dan gate `80 µs`.
   - Dengan `JP_HV` tetap dilepas, tekan starter selama 2–3 detik.
   - Pastikan nilai `PULSER QUALITY` mencapai sedikitnya 10 dan RPM berubah dari nol saat starter berputar.
   - Setelah sinyal stabil, lepaskan starter dan tekan `KONFIRMASI PULSER OK & LANJUT TDC`. Tunggu ACK MCU.

4. **Tahap 3 — TDC / STROBO**
   - Kontrol strobo sekarang berada di tahap TDC pada menu Setup; tidak ada menu strobo kedua.
   - Pilih `DENGAN STROBO LED` untuk pengukuran yang dianjurkan. Hubungkan driver lampu timing ke PB9 sesuai Wiring, aktifkan strobo, lalu arahkan lampu ke jendela timing.
   - Putar starter dan geser offset sedikit demi sedikit sampai tanda `T` tampak diam serta tepat sejajar dengan garis crankcase.
   - Lepaskan starter. Setelah RPM kembali 0 dan HV tetap <30 V, tekan `SIMPAN TDC STROBO` dan tunggu ACK `TDC_SAVED`.
   - Pilihan `TANPA STROBO (MANUAL)` hanya menyimpan nilai provisional. Jangan gunakan beban atau RPM tinggi sebelum timing nyata diverifikasi.

5. **Tahap 4 — TPS**
   - Mesin harus mati dan kontak tetap ON.
   - Biarkan grip gas tertutup penuh, lalu tekan `GAS TUTUP (0%)` dan tunggu ACK.
   - Buka gas penuh, lalu tekan `GAS PENUH (100%)`. Firmware hanya menerima nilai OPEN bila lebih besar sedikitnya 50 hitungan ADC dari CLOSED.
   - Setelah kedua nilai tersimpan, halaman FIRST START akan terbuka.

6. **Tahap 5 — FIRST START**
   - Dengan `JP_HV` masih dilepas, tekan `AKTIFKAN FIRST START SAFETY MODE` dan tunggu ACK `FIRST_START`.
   - Mode ini membatasi sistem pada 220 V, koil CENTER saja, advance maksimum 10°, dan limiter 3.000 RPM.
   - Matikan kontak/kill switch, pastikan kedua HV <30 V, lalu pasang `JP_HV`.
   - Nyalakan kembali dan starter motor. Biarkan hidup stabil sedikitnya 3 detik tanpa melewati 3.000 RPM.
   - Matikan melalui kill switch, tunggu RPM 0 dan kedua HV kembali <30 V.

7. **Tahap 6 — READY**
   - Tekan `READY - CENTER SAJA` untuk konfigurasi awal yang tidak menebak offset busi samping.
   - `READY - 3 BUSI` hanya boleh dipilih setelah offset SIDE benar-benar diukur; jangan mengisi angka perkiraan.
   - Tunggu ACK `READY_CENTER` atau `READY_THREE`. Status READY disimpan dalam flash MCU dan tidak memerlukan firmware lain.

8. **Pemakaian setelah READY**
   - Dengan kontak OFF dan HV <30 V, pastikan `JP_HV` terpasang untuk operasi normal.
   - Nyalakan kontak, hubungkan BLE, lalu pastikan Dashboard menunjukkan Telemetry `ACTIVE`, sekitar 18–22 Hz, dan CRC mendekati 100%.
   - Jika perlu mengulang kalibrasi, buka kembali menu Setup. Mengetuk kepala kartu hanya memindahkan halaman; flash MCU berubah hanya setelah perintah mendapat ACK.

### 6. BLE Terminal & Hex Diagnostics
Diagnostik teknis tingkat lanjut:
- **Statistik Paket Real-Time (Sliding Window 5 Detik)**:
  - `packetRateHz` berasal hanya dari notifikasi biner karakteristik **Telemetry 1001**, bukan dari respons perintah 1003. Frekuensi dihitung dari selisih stempel waktu semua frame yang masuk pada jendela 5 detik.
  - `crcValidPercent` membandingkan CRC16 hasil hitung byte `[0..17]` dengan CRC yang dikirim pada byte `[18..19]`, lalu membagi jumlah frame valid dengan seluruh frame pada jendela 5 detik.
  - `0 Hz / BELUM ADA FRAME` berarti aplikasi belum menerima notifikasi Telemetry 1001. Ini berbeda dari CRC gagal; koneksi GATT dan respons `PING` pada karakteristik 1003 masih mungkin berfungsi.
  - Watchdog mengubah laju menjadi 0 Hz bila tidak ada frame baru selama 1,5 detik dan menampilkan apakah telemetry belum pernah masuk atau berhenti setelah sempat aktif.
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
