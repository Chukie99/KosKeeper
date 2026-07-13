package com.koskeeper.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.koskeeper.app.PondokViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val totalKamar by viewModel.totalKamar.collectAsState()
    val totalTamu by viewModel.totalTamu.collectAsState()
    val bookingAktif by viewModel.bookingAktifCount.collectAsState()
    val pendapatan by viewModel.pendapatanAktif.collectAsState()
    val totalLunas by viewModel.totalLunas.collectAsState()
    val totalPending by viewModel.totalPending.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Section
            Text(
                "Ringkasan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsCard(
                    title = "Total Kamar",
                    value = "$totalKamar",
                    icon = Icons.Default.KingBed,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "Total Tamu",
                    value = "$totalTamu",
                    icon = Icons.Default.People,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsCard(
                    title = "Booking Aktif",
                    value = "$bookingAktif",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "Pendapatan",
                    value = "Rp ${String.format("%,.0f", pendapatan)}",
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // Payment Summary
            Text(
                "Ringkasan Pembayaran",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentSummaryCard(
                    title = "Total Lunas",
                    amount = totalLunas,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                PaymentSummaryCard(
                    title = "Total Pending",
                    amount = totalPending,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // Occupancy Chart
            Text(
                "Tingkat Hunian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OccupancyChart(
                totalRooms = totalKamar,
                activeBookings = bookingAktif
            )

            Divider()

            // Revenue Bar Chart
            Text(
                "Pendapatan per Kamar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            RevenueBarChart(
                totalRooms = totalKamar,
                activeBookings = bookingAktif,
                totalRevenue = pendapatan
            )
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PaymentSummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Text(
                "Rp ${String.format("%,.0f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun OccupancyChart(
    totalRooms: Int,
    activeBookings: Int
) {
    val occupancyRate = if (totalRooms > 0) {
        (activeBookings.toFloat() / totalRooms * 100).toInt()
    } else 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$occupancyRate%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    occupancyRate >= 80 -> Color(0xFF4CAF50)
                    occupancyRate >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            Text(
                "Tingkat Hunian",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = occupancyRate / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    occupancyRate >= 80 -> Color(0xFF4CAF50)
                    occupancyRate >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$activeBookings kamar terisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$totalRooms kamar total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RevenueBarChart(
    totalRooms: Int,
    activeBookings: Int,
    totalRevenue: Double
) {
    val avgRevenuePerRoom = if (activeBookings > 0) {
        totalRevenue / activeBookings
    } else 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simple bar visualization
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Occupied rooms bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "$activeBookings",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height((activeBookings * 80f / maxOf(totalRooms, 1)).dp.coerceAtLeast(8.dp))
                            .background(Color(0xFF4CAF50), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Text(
                        "Terisi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Available rooms bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val available = totalRooms - activeBookings
                    Text(
                        "$available",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height((available * 80f / maxOf(totalRooms, 1)).dp.coerceAtLeast(8.dp))
                            .background(Color(0xFF2196F3), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Text(
                        "Kosong",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Average revenue
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Rata-rata per Kamar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Rp ${String.format("%,.0f", avgRevenuePerRoom)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
