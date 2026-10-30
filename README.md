# Sukabumi Launcher V3 — Mekkah Edition

Project Android native sederhana untuk Android TV 11, dibuat agar dapat dibangun di GitHub Actions dengan JDK 17 dan Android SDK cloud.

## Fitur pada starter ini
- Tampilan home landscape dengan jam/tanggal WIB.
- Jadwal sholat Sukabumi dari AlAdhan (metode API `method=20`, hasil perlu diverifikasi dengan jadwal resmi Kemenag setempat).
- Cuaca Sukabumi dari Open-Meteo.
- Live score dari ESPN endpoint `soccer/idn.1` bila kompetisi tersedia di feed tersebut.
- Shortcut YouTube, File Manager, Pengaturan. Tombol Live TV dan HDMI saat ini membuka Pengaturan Android karena intent input TV berbeda antar produsen.
- Navigasi tombol remote melalui fokus tombol Android.

## Build APK di GitHub (tanpa Android Studio)
1. Buat repository GitHub baru, misalnya `sukabumi-launcher`.
2. Extract ZIP ini di perangkat Anda.
3. Upload **isi folder project** (bukan folder ZIP-nya) ke root repository. Pastikan `.github/workflows/build.yml` ikut ter-upload.
4. Buka tab **Actions**, pilih workflow **Build Sukabumi Launcher APK**, lalu **Run workflow**. Push ke branch main/master juga otomatis memicu build.
5. Tunggu workflow selesai. Buka run terbaru → bagian **Artifacts** → unduh `SukabumiLauncher-v3-debug`.
6. Extract artifact dan install `app-debug.apk` ke TV melalui USB atau ADB.

## Adzan MP3
File audio tidak disertakan. Letakkan file audio yang Anda punya hak untuk menggunakannya di:
`app/src/main/res/raw/adzan_mekkah.mp3`
Folder `raw` dapat dibuat jika belum ada. Versi starter ini belum mengaktifkan pemutaran/adzan otomatis; integrasi alarm, kalkulasi waktu lokal, izin notifikasi, dan pengujian restart TV perlu dikerjakan sebagai tahap berikutnya. Jangan mengunduh audio dari sumber yang tidak memberi izin penggunaan.

## Catatan penting / batasan
- Ini adalah source project dan workflow build, bukan APK yang sudah dibangun atau diuji di perangkat AQUA.
- Jadwal sholat memakai respons API pihak ketiga; cocokkan dengan jadwal resmi Kemenag/masjid setempat sebelum dijadikan pengingat utama.
- ESPN feed bisa berubah atau tidak menyediakan seluruh pertandingan Liga 1; UI menampilkan fallback jika feed gagal.
- Tombol Live TV/HDMI belum mengendalikan tuner/input AQUA secara langsung. Perlu identifikasi intent/TV Input API khusus firmware AQUA.
- Jangan disable launcher bawaan AQUA sampai launcher baru diuji. Tetapkan default HOME hanya jika TV menawarkan pemilih launcher dan uji cara kembali ke launcher stok.
- Target SDK 32 dipilih untuk kompatibilitas Android TV 11; perilaku izin notifikasi berbeda antar firmware.
- Debug APK tidak ditandatangani untuk distribusi publik. Untuk pemakaian pribadi sideload, debug build biasanya cukup.
