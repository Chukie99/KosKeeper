package com.koskeeper.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koskeeper.app.BookingLengkap
import com.koskeeper.app.PondokViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    viewModel: PondokViewModel,
    bookingId: Long,
    onBack: () -> Unit,
    onRiwayatTamu: (String) -> Unit
) {
    var booking by remember { mutableStateOf<BookingLengkap?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bookingId) {
        booking = viewModel.getBookingById(bookingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Booking #${bookingId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        booking?.let { b ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status
                item {
                    val statusColor = if (b.status == "aktif") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (b.status == "aktif") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                null,
                                tint = statusColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Status: ${b.status.uppercase()}", style = MaterialTheme.typography.titleMedium, color = statusColor)
                                Text("Booking #${b.id}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Info Tamu
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Data Tamu", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(b.namaLengkap, style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(b.nomorKontak, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onRiwayatTamu(b.namaLengkap) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.History, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Lihat Riwayat Tamu Ini")
                            }
                        }
                    }
                }

                // Info Homestay
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Homestay", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.KingBed, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("${b.nomorKamar} - ${b.tipeKamar}", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // Check-in
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Check-in", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Login, null, tint = Color(0xFF2E7D32))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(b.tanggalCheckin, style = MaterialTheme.typography.bodyLarge)
                                    Text("Jam ${b.jamCheckin}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Check-out
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Check-out", style = MaterialTheme.typography.titleMedium, color = Color(0xFFC62828))
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, null, tint = Color(0xFFC62828))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(b.tanggalCheckout, style = MaterialTheme.typography.bodyLarge)
                                    Text("Jam ${b.jamCheckout}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Total
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Bayar", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Rp ${String.format("%,.0f", b.totalBayar)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatTamuScreen(
    viewModel: PondokViewModel,
    namaTamu: String,
    onBack: () -> Unit
) {
    val semuaBooking by viewModel.semuaBooking.collectAsState()
    val riwayat = semuaBooking.filter { it.namaLengkap == namaTamu }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat: $namaTamu") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (riwayat.isEmpty()) {
            Text(
                "Belum ada riwayat booking",
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Total ${riwayat.size} booking, Rp ${String.format("%,.0f", riwayat.sumOf { it.totalBayar })}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(riwayat) { b ->
                    val statusColor = when (b.status) {
                        "aktif" -> MaterialTheme.colorScheme.primary
                        "selesai" -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.error
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Booking #${b.id}", style = MaterialTheme.typography.titleMedium)
                                Text(b.status.uppercase(), style = MaterialTheme.typography.labelMedium, color = statusColor)
                            }
                            Text("${b.nomorKamar} (${b.tipeKamar})", style = MaterialTheme.typography.bodyMedium)
                            Text("In: ${b.tanggalCheckin} | Out: ${b.tanggalCheckout}", style = MaterialTheme.typography.bodySmall)
                            Text("Total: Rp ${String.format("%,.0f", b.totalBayar)}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
