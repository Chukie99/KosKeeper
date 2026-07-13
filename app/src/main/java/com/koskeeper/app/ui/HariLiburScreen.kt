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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.koskeeper.app.HariLibur
import com.koskeeper.app.PondokViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HariLiburScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaLibur by viewModel.semuaHariLibur.collectAsState()
    var editingLibur by remember { mutableStateOf<HariLibur?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<HariLibur?>(null) }
    var dialogMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hari Libur & Harga Khusus") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
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
                Icon(Icons.Default.Add, "Tambah Hari Libur")
            }
        }
    ) { padding ->
        if (semuaLibur.isEmpty()) {
            Text(
                "Belum ada hari libur.\nKet + untuk menambah.",
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
                        "Hari libur akan otomatis menggunakan harga khusus saat booking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(semuaLibur) { libur ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(libur.nama, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                    Text("${libur.tanggalMulai} s/d ${libur.tanggalSelesai}", style = MaterialTheme.typography.bodySmall)
                                    Text("Harga: Rp ${String.format("%,.0f", libur.hargaPerMalam)}/malam", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Row {
                                    IconButton(onClick = { editingLibur = libur }) {
                                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showDeleteConfirm = libur }) {
                                        Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        HariLiburDialog(
            title = "Tambah Hari Libur",
            initial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { nama, dari, sampai, harga ->
                viewModel.tambahHariLibur(nama, dari, sampai, harga) { ok, msg ->
                    dialogMsg = msg
                    showAddDialog = false
                }
            }
        )
    }

    editingLibur?.let { libur ->
        HariLiburDialog(
            title = "Edit Hari Libur",
            initial = libur,
            onDismiss = { editingLibur = null },
            onConfirm = { nama, dari, sampai, harga ->
                viewModel.updateHariLibur(libur.copy(nama = nama, tanggalMulai = dari, tanggalSelesai = sampai, hargaPerMalam = harga)) { ok, msg ->
                    dialogMsg = msg
                    editingLibur = null
                }
            }
        )
    }

    showDeleteConfirm?.let { libur ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus Hari Libur?") },
            text = { Text("Hapus '${libur.nama}'? Harga khusus tidak akan berlaku lagi.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hapusHariLibur(libur)
                    showDeleteConfirm = null
                }) { Text("Ya, Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Batal") } }
        )
    }

    dialogMsg?.let {
        AlertDialog(onDismissRequest = { dialogMsg = null }, title = { Text("Info") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HariLiburDialog(
    title: String,
    initial: HariLibur?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double) -> Unit
) {
    var nama by remember { mutableStateOf(initial?.nama ?: "") }
    var harga by remember { mutableStateOf(initial?.hargaPerMalam?.toString() ?: "") }
    var showDariPicker by remember { mutableStateOf(false) }
    var showSampaiPicker by remember { mutableStateOf(false) }
    var dariTanggal by remember { mutableStateOf(initial?.tanggalMulai ?: "") }
    var sampaiTanggal by remember { mutableStateOf(initial?.tanggalSelesai ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama (contoh: Lebaran 2025)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Event, null) }
                )
                OutlinedTextField(
                    value = dariTanggal,
                    onValueChange = {},
                    label = { Text("Dari Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable { showDariPicker = true },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                OutlinedTextField(
                    value = sampaiTanggal,
                    onValueChange = {},
                    label = { Text("Sampai Tanggal") },
                    modifier = Modifier.fillMaxWidth().clickable { showSampaiPicker = true },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it },
                    label = { Text("Harga Per Malam (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hargaVal = harga.toDoubleOrNull() ?: 0.0
                    onConfirm(nama, dariTanggal, sampaiTanggal, hargaVal)
                },
                enabled = nama.isNotBlank() && dariTanggal.isNotBlank() && sampaiTanggal.isNotBlank() && harga.isNotBlank()
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )

    if (showDariPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDariPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        dariTanggal = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showDariPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDariPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showSampaiPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showSampaiPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        sampaiTanggal = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showSampaiPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showSampaiPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }
}
