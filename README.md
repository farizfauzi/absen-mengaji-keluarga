# Absen Mengaji Keluarga - with Photo & Admin

Saya sudah menambahkan fitur yang diminta: foto bukti, tombol Admin (PIN default 1234), dan riwayat anggota.

Catatan penting untuk testing di Android 11+:
- Saat menekan "Foto & Hadir" app mencoba mem-launch camera via Intent dan menyimpan foto ke internal files dir.
- Jika camera intent tidak menulis file (beberapa emulator/implementasi), Anda mungkin perlu menyesuaikan menggunakan MediaStore APIs.

Build APK
- Setelah push, buka Actions -> Build APK or run `./gradlew assembleDebug` locally.

Jika ada masalah permission atau foto tidak muncul di emulator, coba tes di perangkat fisik.
