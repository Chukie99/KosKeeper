package com.koskeeper.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.koskeeper.app.ui.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: PondokViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[PondokViewModel::class.java]

        NotificationHelper.createChannel(this)
        requestNotificationPermission()
        scheduleCheckoutReminder()

        val prefs = getSharedPreferences("koskeeper", Context.MODE_PRIVATE)
        var isDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            var darkMode by remember { mutableStateOf(isDarkMode) }

            KosKeeperTheme(isDarkTheme = darkMode) {
                var currentScreen by remember { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it },
                        isDarkMode = darkMode,
                        onToggleDarkMode = {
                            darkMode = it
                            prefs.edit().putBoolean("dark_mode", it).apply()
                        }
                    )
                    "kamar" -> KamarScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "tamu" -> TamuScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "booking" -> BookingScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "kalender" -> KalenderScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "daftar_booking" -> DaftarBookingScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" },
                        onDetail = { id -> currentScreen = "booking_detail_$id" }
                    )
                    "laporan" -> LaporanScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "backup" -> BackupScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "hari_libur" -> HariLiburScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    else -> {
                        if (currentScreen.startsWith("booking_detail_")) {
                            val id = currentScreen.removePrefix("booking_detail_").toLongOrNull() ?: 0L
                            BookingDetailScreen(
                                viewModel = viewModel,
                                bookingId = id,
                                onBack = { currentScreen = "daftar_booking" },
                                onRiwayatTamu = { nama -> currentScreen = "riwayat_tamu_$nama" }
                            )
                        } else if (currentScreen.startsWith("riwayat_tamu_")) {
                            val nama = currentScreen.removePrefix("riwayat_tamu_")
                            RiwayatTamuScreen(
                                viewModel = viewModel,
                                namaTamu = nama,
                                onBack = { currentScreen = "home" }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleCheckoutReminder() {
        val workRequest = PeriodicWorkRequestBuilder<CheckoutReminderWorker>(
            5, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "checkout_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
