<div align="center">

# KosKeeper

**Sistem manajemen kos/homestay berbasis Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4)](https://developer.android.com/jetpack/compose)
[![Room DB](https://img.shields.io/badge/Room-2.6.1-3DDC84)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://developer.android.com/about/versions/oreo)

</div>

---

## Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| **Manajemen Kamar** | Kelola data kamar/homestay lengkap dengan status |
| **Sistem Booking** | Buat, edit, dan lacak status booking tamu |
| **Data Tamu** | Database tamu dengan riwayat kunjungan |
| **Kalender** | Visualisasi booking dalam tampilan kalender |
| **Laporan** | Laporan bulanan dengan export ke CSV |
| **Hari Libur** | Atur hari libur untuk perhitungan tarif |
| **Backup & Restore** | Backup dan pulihkan database kapan saja |
| **Dark Mode** | Tema gelap untuk kenyamanan mata |
| **Notifikasi** | Pengingat checkout otomatis |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Database:** Room (SQLite)
- **Architecture:** MVVM
- **Async:** Kotlin Coroutines + Flow
- **Background:** WorkManager

## Screenshots

<div align="center">

> Screenshots akan segera ditambahkan

</div>

## Instalasi

### Prerequisites

- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 17
- Android SDK 34

### Build & Run

```bash
# Clone repository
git clone https://github.com/Chukie99/KosKeeper.git

# Buka project di Android Studio
# Tunggu sync gradle selesai
# Run pada device/emulator
```

Atau build via command line:

```bash
./gradlew assembleDebug
```

APK debug akan tersedia di `app/build/outputs/apk/debug/`

## Struktur Project

```
app/src/main/java/com/koskeeper/app/
├── MainActivity.kt          # Entry point
├── PondokViewModel.kt       # ViewModel utama
├── AppDatabase.kt           # Room database
├── Kamar.kt                 # Entity kamar
├── Booking.kt               # Entity booking
├── Tamu.kt                  # Entity tamu
├── HariLibur.kt             # Entity hari libur
├── *Dao.kt                  # Data Access Objects
├── NotificationHelper.kt    # Notifikasi
├── CheckoutReminderWorker.kt # Background reminder
└── ui/
    ├── HomeScreen.kt        # Dashboard
    ├── KamarScreen.kt       # Manajemen kamar
    ├── BookingScreen.kt     # Form booking
    ├── DaftarBookingScreen.kt # Daftar booking
    ├── BookingDetailScreen.kt # Detail booking
    ├── TamuScreen.kt        # Manajemen tamu
    ├── KalenderScreen.kt    # Tampilan kalender
    ├── LaporanScreen.kt     # Laporan & statistik
    ├── HariLiburScreen.kt   # Pengaturan hari libur
    ├── BackupScreen.kt      # Backup & restore
    └── Theme.kt             # Tema aplikasi
```

## Kontribusi

Kontribusi sangat diterima! Silakan buka issue atau submit pull request.

1. Fork repository
2. Buat branch baru (`git checkout -b feature/fitur-baru`)
3. Commit perubahan (`git commit -m 'Tambah fitur baru'`)
4. Push ke branch (`git push origin feature/fitur-baru`)
5. Buka Pull Request

## License

Project ini dilisensikan di bawah [MIT License](LICENSE).

## Kontak

**Chukie99** - [@Chukie99](https://github.com/Chukie99)

<div align="center">

Dibuat dengan ❤️ untuk kemudahan pengelolaan kos/homestay

</div>
