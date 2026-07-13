package com.koskeeper.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.koskeeper.app.BookingLengkap
import com.koskeeper.app.PondokViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit,
    onBookingClick: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("booking") }

    val semuaBooking by viewModel.semuaBooking.collectAsState()
    val semuaTamu by viewModel.semuaTamu.collectAsState()
    val semuaKamar by viewModel.semuaKamar.collectAsState()

    val filteredBookings = remember(query, semuaBooking) {
        if (query.isEmpty()) semuaBooking
        else semuaBooking.filter {
            it.namaLengkap.contains(query, ignoreCase = true) ||
            it.nomorKamar.contains(query, ignoreCase = true) ||
            it.id.toString().contains(query)
        }
    }

    val filteredTamu = remember(query, semuaTamu) {
        if (query.isEmpty()) semuaTamu
        else semuaTamu.filter {
            it.namaLengkap.contains(query, ignoreCase = true) ||
            it.nomorKontak.contains(query, ignoreCase = true)
        }
    }

    val filteredKamar = remember(query, semuaKamar) {
        if (query.isEmpty()) semuaKamar
        else semuaKamar.filter {
            it.nomorKamar.contains(query, ignoreCase = true) ||
            it.tipeKamar.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari") },
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
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari booking, tamu, atau kamar...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, "Hapus")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = searchType == "booking",
                    onClick = { searchType = "booking" },
                    label = { Text("Booking") },
                    leadingIcon = if (searchType == "booking") {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = searchType == "tamu",
                    onClick = { searchType = "tamu" },
                    label = { Text("Tamu") },
                    leadingIcon = if (searchType == "tamu") {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = searchType == "kamar",
                    onClick = { searchType = "kamar" },
                    label = { Text("Kamar") },
                    leadingIcon = if (searchType == "kamar") {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results
            when (searchType) {
                "booking" -> {
                    Text(
                        "Hasil: ${filteredBookings.size} booking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredBookings) { booking ->
                            SearchResultCard(
                                icon = Icons.Default.CalendarMonth,
                                title = "#${booking.id} - ${booking.namaLengkap}",
                                subtitle = "Kamar ${booking.nomorKamar} | ${booking.tanggalCheckin} - ${booking.tanggalCheckout}",
                                onClick = { onBookingClick(booking.id) }
                            )
                        }
                    }
                }
                "tamu" -> {
                    Text(
                        "Hasil: ${filteredTamu.size} tamu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredTamu) { tamu ->
                            SearchResultCard(
                                icon = Icons.Default.Person,
                                title = tamu.namaLengkap,
                                subtitle = tamu.nomorKontak,
                                onClick = {}
                            )
                        }
                    }
                }
                "kamar" -> {
                    Text(
                        "Hasil: ${filteredKamar.size} kamar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredKamar) { kamar ->
                            SearchResultCard(
                                icon = Icons.Default.KingBed,
                                title = "Kamar ${kamar.nomorKamar}",
                                subtitle = "${kamar.tipeKamar} | Rp ${String.format("%,.0f", kamar.hargaPerMalam)}/malam",
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
