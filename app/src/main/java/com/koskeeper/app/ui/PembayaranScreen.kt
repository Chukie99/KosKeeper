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
import com.koskeeper.app.PembayaranLengkap
import com.koskeeper.app.PondokViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaPembayaran by viewModel.semuaPembayaran.collectAsState()
    val totalLunas by viewModel.totalLunas.collectAsState()
    val totalPending by viewModel.totalPending.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Tambah Pembayaran")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Lunas",
                    amount = totalLunas,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Pending",
                    amount = totalPending,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Riwayat Pembayaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (semuaPembayaran.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada pembayaran", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(semuaPembayaran) { pembayaran ->
                        PembayaranCard(pembayaran = pembayaran)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPembayaranDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = color)
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
fun PembayaranCard(pembayaran: PembayaranLengkap) {
    val statusColor = when (pembayaran.status) {
        "lunas" -> Color(0xFF4CAF50)
        "pending" -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val tipeColor = when (pembayaran.tipeBayar) {
        "dp" -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Payment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pembayaran.namaTamu,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = tipeColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            pembayaran.tipeBayar.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = tipeColor
                        )
                    }
                }
                Text(
                    "Kamar ${pembayaran.nomorKamar} • ${pembayaran.metode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    pembayaran.tanggal,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pembayaran.metode == "QRIS" && pembayaran.kodeQris.isNotBlank()) {
                    Text(
                        "QRIS: ${pembayaran.kodeQris}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Rp ${String.format("%,.0f", pembayaran.jumlah)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        pembayaran.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPembayaranDialog(
    viewModel: PondokViewModel,
    onDismiss: () -> Unit
) {
    val bookingList by viewModel.semuaBooking.collectAsState()
    var selectedBooking by remember { mutableStateOf<Long?>(null) }
    var jumlah by remember { mutableStateOf("") }
    var metode by remember { mutableStateOf("Tunai") }
    var catatan by remember { mutableStateOf("") }
    var tipeBayar by remember { mutableStateOf("pelunasan") }
    var kodeQris by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var sisaBayar by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val selectedBookingData = bookingList.find { it.id == selectedBooking }

    // Hitung sisa bayar saat booking dipilih
    LaunchedEffect(selectedBooking) {
        selectedBooking?.let {
            sisaBayar = viewModel.hitungSisaBayar(it)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pembayaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Booking selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedBookingData?.let {
                            "#${it.id} - ${it.namaLengkap}"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Booking") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        bookingList.filter { it.status == "aktif" }.forEach { booking ->
                            DropdownMenuItem(
                                text = { Text("#${booking.id} - ${booking.namaLengkap}") },
                                onClick = {
                                    selectedBooking = booking.id
                                    jumlah = booking.totalBayar.toString()
                                    tipeBayar = "pelunasan"
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Info sisa bayar
                if (selectedBooking != null && sisaBayar > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Sisa Bayar: Rp ${String.format("%,.0f", sisaBayar)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            selectedBookingData?.let {
                                Text(
                                    "Total Booking: Rp ${String.format("%,.0f", it.totalBayar)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Tipe pembayaran
                Text("Tipe Pembayaran", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipeBayar == "dp",
                        onClick = {
                            tipeBayar = "dp"
                            selectedBookingData?.let {
                                jumlah = (sisaBayar * 0.5).toLong().toString()
                            }
                        },
                        label = { Text("DP (50%)") }
                    )
                    FilterChip(
                        selected = tipeBayar == "dp_custom",
                        onClick = {
                            tipeBayar = "dp_custom"
                            jumlah = ""
                        },
                        label = { Text("DP Custom") }
                    )
                    FilterChip(
                        selected = tipeBayar == "pelunasan",
                        onClick = {
                            tipeBayar = "pelunasan"
                            jumlah = sisaBayar.toLong().toString()
                        },
                        label = { Text("Pelunasan") }
                    )
                }

                // Preset persentase untuk DP Custom
                if (tipeBayar == "dp_custom") {
                    Text("Pilih Persentase:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("25%" to 0.25, "50%" to 0.5, "75%" to 0.75, "100%" to 1.0).forEach { (label, percent) ->
                            OutlinedButton(
                                onClick = {
                                    selectedBookingData?.let {
                                        jumlah = (sisaBayar * percent).toLong().toString()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { jumlah = it },
                    label = { Text("Jumlah (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (selectedBookingData != null) {
                            val jml = jumlah.toDoubleOrNull() ?: 0.0
                            val persen = if (selectedBookingData!!.totalBayar > 0) (jml / selectedBookingData!!.totalBayar * 100) else 0.0
                            Text("${String.format("%.1f", persen)}% dari total Rp ${String.format("%,.0f", selectedBookingData!!.totalBayar)}")
                        }
                    }
                )
                // Metode pembayaran
                Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tunai", "Transfer", "QRIS", "Lainnya").forEach { method ->
                        FilterChip(
                            selected = metode == method,
                            onClick = { metode = method },
                            label = { Text(method) }
                        )
                    }
                }

                // QRIS input
                if (metode == "QRIS") {
                    OutlinedTextField(
                        value = kodeQris,
                        onValueChange = { kodeQris = it },
                        label = { Text("Kode/Link QRIS") },
                        placeholder = { Text("Masukkan kode atau link QRIS") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.QrCode, null) }
                    )
                }

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedBooking?.let { bookingId ->
                        jumlah.toDoubleOrNull()?.let { jml ->
                            viewModel.tambahPembayaran(
                                bookingId = bookingId,
                                jumlah = jml,
                                tanggal = today,
                                metode = metode,
                                catatan = catatan,
                                tipeBayar = if (tipeBayar == "dp_custom") "dp" else tipeBayar,
                                kodeQris = if (metode == "QRIS") kodeQris else ""
                            ) { success, msg ->
                                if (success) onDismiss()
                            }
                        }
                    }
                },
                enabled = selectedBooking != null && jumlah.toDoubleOrNull() != null
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}




