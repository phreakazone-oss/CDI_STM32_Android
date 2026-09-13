# NS200 CDI Android R8.2

Aplikasi Android untuk commissioning, telemetry, tuning, OEM Learn, setup manual, audio virtual, diagnosis BLE, dan OTA firmware CDI programmable Pulsar NS200 berbasis WeAct STM32WB55CGU6.

Repositori firmware acuan: [Firmware_CDI_NS200](https://github.com/phreakazone/Firmware_CDI_NS200). Port ESP32 harus mengikuti kontrak R8 di dokumen ini; STM32 tetap menjadi sumber kebenaran protokol.

> Status “production ready” di repositori berarti source dapat dibangun, alur aplikasi konsisten dengan firmware R8, dan pemeriksaan otomatis tersedia. Rangkaian pengapian 345 V tetap memerlukan validasi bench/HIL, isolasi, thermal test, EMC, dan uji kendaraan bertahap sebelum dianggap layak jalan.

## Yang berubah di R8.2

- Layout Compose yang stabil dipertahankan; perubahan difokuskan pada state, teks, wiring, dan sinkronisasi firmware.
- MODE, OEM LEARN, capability, map, telemetry, dan OTA dibaca kembali dari STM32; aplikasi tidak menganggap tombol lokal sebagai bukti keberhasilan.
- OTA memakai frame R8 sebenarnya: offset LE32, panjang, payload, CRC16, serta status biner 16 byte.
- Map UI di-resample deterministik menjadi 8x4 untuk NORMAL atau 16x8 untuk PRO sebelum dikirim.
- J1.8 dan J1.9 dikembalikan sebagai NC. Probe OEM memakai konektor terpisah `J_OEM_TAP`.
- Tidak ada `JP_HV`, `SW_ARM`, atau `JP_PRO` pada flow operasi. Mode PRO memakai `FEATURE,PRO,ON/OFF`.
- Ditambahkan `J_SERVICE_5V` diode-OR agar STM32/BLE tetap hidup ketika mesin dihentikan untuk menyimpan OEM Learn.

## Menu aplikasi

| Menu | Fungsi nyata |
|---|---|
| Tacho | RPM, TPS, advance, aki, HV CENTER/SIDE, suhu, fault, packet rate, dan CRC telemetry |
| Maps | Empat slot, limiter SOFT/HARD, kurva, load/save/readback; grid disesuaikan ke firmware |
| Wiring | Pin harness, pin WeAct, BOM, pinout komponen, dan tutorial tahap per tahap |
| Setup | Alur utama OEM Learn, fallback MANUAL/strobo, TPS, FIRST START, READY, NORMAL/PRO |
| Suara | Virtual engine sound dari preset atau file audio pengguna; tidak mengubah pengapian |
| BLE | Scan/koneksi, terminal frame, capability, hex telemetry, dan OTA `APP.bin` |

## Build Android

Persyaratan:

- Android Studio/Gradle dengan JDK 17.
- Android SDK compile/target 36; minimum Android 7.0/API 24.
- Tidak memerlukan Firebase, Google Services, secret API, Room, Retrofit, atau server eksternal.

Build debug:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

APK berada di `app/build/outputs/apk/debug/`. CI menjalankan perintah yang sama pada push dan pull request.

Release signing hanya aktif bila keempat environment variable tersedia:

```text
KEYSTORE_PATH=/path/keystore.jks
STORE_PASSWORD=...
KEY_ALIAS=...
KEY_PASSWORD=...
```

Tanpa keempat nilai tersebut, build debug tetap normal dan release tidak diam-diam memakai keystore lokal.

## Flash pertama STM32WB55

Gunakan artefak dari repositori firmware:

- `NS200_CDI_R8_FACTORY.hex`: alamat sudah tertanam; pilihan paling aman.
- `NS200_CDI_R8_FACTORY.bin`: tulis mulai `0x08000000`.
- `NS200_CDI_R8_APP.bin`: hanya untuk OTA dari aplikasi, bukan flash factory.

USB DFU WeAct:

1. Lepaskan harness kendaraan dan semua catu selain USB.
2. Tahan `BOOT0`, tekan-lepas `NRST`, lalu lepaskan `BOOT0`.
3. Hubungkan kabel USB data.
4. STM32CubeProgrammer → USB → refresh → USB1 → Connect.
5. Pilih FACTORY HEX, Download, dan Verify.
6. Putuskan USB, pastikan BOOT0 LOW, lalu tekan-lepas NRST.

ST-Link/SWD hanya fallback/debug. USBasp HW-437 adalah programmer AVR dan tidak kompatibel dengan SWD STM32WB55.

## Pin harness CDI J1

Orientasi selalu mengikuti tampilan konektor pada menu Wiring; jangan menebak dari warna saja.

| J1 | Warna/fungsi | Jalur proyek R8 |
|---:|---|---|
| 1 | NC | Kosong dan diisolasi |
| 2 | Hijau-putih, TPS_A | Ke selektor `J_TPS`; salah satu TPS_REF/TPS_SIG |
| 3 | Hitam-putih, TEMP | Jaringan NTC/filter/clamp → PA4 |
| 4 | Abu-abu, TPS_B | Ke selektor `J_TPS`; pasangan J1.2 |
| 5 | +12 V kontak | FMAIN 5A → SB560 → TVS → choke → VIN_FILT |
| 6 | Hitam-merah, COIL_SIDE | Terminal B C_SIDE; output SIDE hanya setelah offset valid |
| 7 | Biru-kuning, FAN_RELAY | Driver low-side/dry-contact terverifikasi dari PB5 |
| 8 | NC | Tetap kosong; bukan input OEM SIDE |
| 9 | NC | Tetap kosong; bukan input OEM CENTER |
| 10 | Putih-merah, PULSER | Conditioner LM339 → PA0 TIM2_CH1 |
| 11 | Hitam-kuning, GND | Titik `GND_STAR` |
| 12 | COIL_CENTER | Terminal B C_CENTER; output utama PA1/SCR1 |

### Probe OEM Learn terpisah

`J_OEM_TAP` adalah JST-XH 3-pin pada adaptor Y non-destruktif:

| Pin | Asal | Tujuan |
|---:|---|---|
| 1 CENTER | Cabang dari kabel J1.12 saat CDI OEM terpasang | 3x22k 1W seri → U_OEM1 pin 1 |
| 2 SIDE | Cabang dari kabel J1.6 saat CDI OEM terpasang | 3x22k 1W seri → U_OEM2 pin 1 |
| 3 OEM_GND | Cabang J1.11 | U_OEM1/U_OEM2 pin 2 |

Untuk tiap PC817C/EL817C DIP-4:

- Pin 1 anode LED; dari tiga resistor 22k 1W seri.
- Pin 2 cathode LED; ke `J_OEM_TAP.3`.
- 1N4148 antiparalel tepat di pin 1-2: katoda ke pin 1, anoda ke pin 2.
- Pin 3 emitter; ke GND_LOGIC.
- Pin 4 collector; U_OEM1 ke PB3, U_OEM2 ke PB4; masing-masing pull-up 4.7k ke 3V3.

Modul optocoupler generik 5/12/24 V tidak otomatis kompatibel. Gunakan hanya jika skematik, resistor input, polaritas, CTR, dan output pull-up benar-benar sesuai.

## Pin WeAct STM32WB55CGU6 yang dipakai

| Header | Pin MCU | Fungsi R8 | Jalur |
|---|---|---|---|
| H_BOTTOM.2 | 5V | Catu logic | `+5V_LOGIC` setelah diode-OR |
| H_TOP.1/H_BOTTOM.1/H_BOTTOM.20 | GND | Ground logic | `GND_STAR` |
| H_BOTTOM.9 | PA0 | Pickup capture | LM339 OUT1 melalui 1k/pull-up 3V3 |
| H_BOTTOM.10 | PA1 | Gate CENTER | Driver QNC/QPC → 330R → gate SCR1 |
| H_BOTTOM.11 | PA2 | Gate SIDE | Driver QNS/QPS → 330R → gate SCR2 |
| H_TOP.9 | PB3 | OEM CENTER | U_OEM1 pin 4 + pull-up 4.7k |
| H_TOP.8 | PB4 | OEM SIDE | U_OEM2 pin 4 + pull-up 4.7k |
| H_BOTTOM.6 | PB9 | Strobo manual | 100R → gate MOSFET LED strobo |
| H_BOTTOM.12 | PA3 | TPS_SIG ADC | Divider/filter/clamp TPS |
| H_BOTTOM.13 | PA4 | TEMP ADC | Jaringan NTC/filter/clamp |
| H_BOTTOM.14 | PA5 | TPS_REF monitor | Divider/filter/clamp |
| H_BOTTOM.15 | PA6 | HV CENTER ADC | 4x270k seri → 8.2k/filter/clamp |
| H_BOTTOM.16 | PA7 | HV SIDE ADC | 4x270k seri → 8.2k/filter/clamp |
| H_TOP.14 | PB0 | Aki ADC | 100k/22k/filter/clamp |
| H_BOTTOM.18 | PA9 | PWM charger A | TC4427 pin 2; OUTA pin 7 → QHV1 |
| H_BOTTOM.7 | PB8 | PWM charger B | TC4427 pin 4; OUTB pin 5 → QHV2 |
| H_TOP.11 | PA10 | Hardware fault LOW | LM339 open-collector clamp |
| H_TOP.7 | PB5 | Kipas | Driver/relay yang sudah diverifikasi |
| H_BOTTOM.19 | PB2 | VIN_HV presence | 100k/27k/filter/clamp |

Pin VBAT WeAct bukan input aki 12 V dan harus dibiarkan kosong.

## Catu RUN dan SERVICE

```text
RUN: J1.5 → proteksi/FLOGIC → LM2596 5.15V → anoda SS34 RUN
SERVICE: JST pin 1, +5V regulated → anoda SS34 SERVICE
katoda kedua SS34 → +5V_LOGIC → WeAct H_BOTTOM.2
semua ground → GND_STAR; JST SERVICE pin 2 → GND_STAR
```

`J_SERVICE_5V` hanya menerima 5 V regulated dari USB charger/power-bank. Adaptor laptop 19 V dan aki 12 V dilarang. Service supply dibutuhkan agar BLE tetap hidup saat kill switch menghentikan mesin sebelum `LEARN,STOP`.

## Flow setup utama: salin CDI OEM

1. Flash firmware R8 factory sekali.
2. Rakit dan periksa Wiring. Selama belajar, CDI OEM adalah satu-satunya pengendali koil; output DIY belum tersambung.
3. Aktifkan `J_SERVICE_5V`, hubungkan BLE, lalu tekan Setup → PERIKSA & LANJUT.
4. Dengan RPM 0 dan HV <30 V, pilih OEM LEARN dan tekan MULAI LEARN. Aplikasi mengirim `MODE,OEM_LEARN`, `LEARN,START`, kemudian membaca `GET,MODE/LEARN`.
5. Setelah ACK, hidupkan mesin memakai CDI OEM. Variasikan RPM/TPS bertahap. Pantau coverage, accepted, rejected, dan SIDE samples.
6. Setelah accepted minimal 20, hentikan mesin/kill switch tetapi jangan matikan SERVICE 5V.
7. Tunggu RPM 0 dan kedua HV <30 V. Tekan SIMPAN & STOP. Tunggu `LEARN_SAVED_CENTER` atau `LEARN_SAVED_CENTER_SIDE`.
8. Matikan semua daya, tunggu HV <30 V, cabut CDI OEM, dan pindahkan pigtail ke CDI DIY.
9. Nyalakan kembali, pilih DIY, lalu konfirmasi `OEM_UNPLUGGED`. Tidak ada takeover otomatis.
10. FIRST START otomatis membatasi 220 V, CENTER saja, advance maksimum 10°, dan limiter 3000 RPM. Idle stabil 500-3200 RPM selama minimal 3 detik membuat proof.
11. Saat RPM kembali 0 dan HV <30 V, firmware menyimpan READY. Bila daya terputus setelah proof, boot berikutnya mempromosikan READY.
12. Kalibrasi TPS, pilih map, dan lakukan tuning hanya saat RPM 0/HV <30 V.

SIDE/tiga busi hanya tersedia bila OEM Learn mengumpulkan minimal 10 sampel SIDE dan offset tersimpan. Jangan mengisi offset SIDE dengan tebakan.

## Flow darurat tanpa CDI OEM

Menu Wiring → Komisi CDI dan Setup → MANUAL tetap tersedia:

1. Verifikasi pickup PA0, edge, PPR, dan quality.
2. Gunakan strobo PB9 pada lubang timing untuk menyimpan TDC; strobo adalah alat melihat tanda mekanis, bukan sensor TDC.
3. Kalibrasi TPS CLOSED dan OPEN.
4. Aktifkan FIRST START konservatif.
5. Setelah proof/READY, pindah ke map NORMAL. SIDE tetap nonaktif sampai offsetnya diukur valid.

## Sinkronisasi firmware R8

Framing command/response:

```text
@sequence,BODY*CRC16_CCITT\n
```

Capability wajib dibaca, tidak diasumsikan:

```text
CAPS,R8.0,PROTO4,OEM_LEARN,MANUAL,OTA_STAGE,AUTO_FIRST_START,NO_JUMPERS
```

Perintah/status utama:

| Fungsi | TX | RX sumber kebenaran |
|---|---|---|
| Status | `GET,STATUS` | `STATUS,rpm,tps,hvC,hvS,slot,mapMode,outputPermission,pro` |
| Metadata map | `GET,META` | nama, mode, limiter, HV target, generation, grid |
| Setup | `GET,SETUP` | stage, edge, trigger, SIDE offset, PPR, gate, TPS, FIRST START, output, fan, quality |
| Mode | `GET,MODE` | `MODE,mode,diyUnplugged,proEnabled,firstStartProven` |
| Learn | `GET,LEARN` | state, coverage, accepted, rejected, SIDE samples/offset |
| PRO | `FEATURE,PRO,ON/OFF` | `ACK,PRO_ON/PRO_OFF`, lalu `GET,MODE/META/STATUS` |
| Map | `LOAD`, `LIVE`, `LIMIT`, `SAVE` | ACK tiap sel lalu `GET,META/CELL` readback |

Kode mode: MANUAL=0, OEM_LEARN=1, DIY=2. Kode LEARN: IDLE=0, ACTIVE=1, COMPLETE=2, ERROR=3.

Telemetry memakai characteristic `...1001`, paket 20 byte v3/v4, CORE dan DIAGNOSTIC bergantian pada 20 Hz total. `PACKET RATE` berasal dari jarak waktu antar-notification; `CRC VALID` berasal dari CRC16 frame telemetry. Keduanya tetap 0/N/A bila CCCD telemetry belum aktif walaupun RESPONSE `...1003` masih bekerja.

UUID:

| UUID akhir | Fungsi |
|---|---|
| 1000 | Service |
| 1001 | Telemetry notify |
| 1002 | Command write |
| 1003 | Response notify |
| 1004 | OTA data write without response |
| 1005 | OTA status notify |

Nama advertising tetap `NS200-CDI-R7` dan PING tetap `PONG_R7_2` untuk kompatibilitas. Identitas R8 dibaca dari CAPS.

## OTA dari aplikasi

Syarat: STM32 melaporkan `OTA_STAGE`, RPM 0, output OFF, charger/HV mati, dan HV CENTER/SIDE <30 V.

1. Pilih hanya `NS200_CDI_R8_APP.bin` dengan ukuran 256 sampai `0x30000` byte.
2. Aplikasi mengirim `OTA,BEGIN,80200,length,crc32` dalam angka desimal.
3. Tiap packet 1004 berisi offset LE32 + length U8 + payload maksimum 208 byte + CRC16 LE.
4. Aplikasi menunggu status 1005 sebelum chunk berikutnya; offset harus berurutan dan kelipatan 8 kecuali panjang payload terakhir.
5. Setelah seluruh byte diterima, aplikasi mengirim `OTA,COMMIT` dan menunggu READY/reboot.

Jangan unggah FACTORY.bin melalui OTA. OTA R8 memverifikasi CRC32, tetapi belum merupakan secure update dengan tanda tangan kriptografis.

## NORMAL dan PRO

- Slot 0-2: NORMAL, grid 8x4, target 285 V.
- Slot 3: PRO, grid 16x8, target 345 V.
- Semua advance dibatasi 0-36° BTDC.
- Limit firmware tetap berlaku meskipun UI mengirim nilai di luar rentang.
- Warning aplikasi dimulai pada HV >=360 V; hardware OVP referensi disetel sekitar 379 V agar target PRO 345 V tidak dianggap fault normal.

## Checklist sebelum uji kendaraan

- FMAIN/FLOGIC/FHV benar dan tidak dibypass.
- +5V_LOGIC 4.75-5.05 V dari RUN maupun SERVICE, tanpa backfeed.
- Semua input MCU memiliki conditioner/clamp yang benar; J1.6/J1.12 tidak pernah langsung ke GPIO.
- Tab BT151 bertegangan tinggi dan terisolasi dari heatsink/chassis.
- Clearance area HV minimal 6 mm dan jauh dari antena/pulser.
- Bleeder terpasang; nilai HV benar-benar turun <30 V sebelum disentuh.
- Output OEM dan DIY tidak pernah aktif paralel pada koil yang sama.
- Uji awal dilakukan bertahap, motor netral, dengan cara mematikan daya yang dapat dijangkau.

## Port ESP32 mendatang

Implementasi ESP32 boleh berbeda pada HAL/BLE stack, tetapi wajib mempertahankan UUID, framing CRC16, field order, state/ACK, map 8x4/16x8, aturan OEM Learn, FIRST START, safety interlock, dan OTA semantics. Aplikasi tidak akan menambahkan cabang UI khusus untuk mengompensasi protokol ESP32 yang berbeda.
