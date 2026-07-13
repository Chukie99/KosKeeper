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
import androidx.compose.ui.unit.dp
import com.koskeeper.app.Kamar
import com.koskeeper.app.PondokViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KamarScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaKamar by viewModel.semuaKamar.collectAsState()
    var nomor by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var hargaWeekend by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var dialogMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Homestay") },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Tambah / Edit Homestay", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nomor, onValueChange = { nomor = it }, label = { Text("Nomor Kamar (contoh: A1)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.KingBed, null) })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tipe, onValueChange = { tipe = it }, label = { Text("Tipe (Standar/VIP/President)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Bed, null) })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = harga, onValueChange = { harga = it }, label = { Text("Harga Weekday (Rp/malam)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.AttachMoney, null) })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = hargaWeekend, onValueChange = { hargaWeekend = it }, label = { Text("Harga Weekend (Sabtu/Minggu, Rp/malam)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.AttachMoney, null) })
                Spacer(Modifier.height(4.dp))
                Text("* Kosongkan jika harga weekend sama dengan weekday", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (nomor.isBlank() || tipe.isBlank() || harga.isBlank()) {
                            dialogMsg = "Nomor, tipe, dan harga harus diisi!"
                            return@Button
                        }
                        val hargaVal = harga.toDoubleOrNull()
                        if (hargaVal == null) {
                            dialogMsg = "Harga harus angka!"
                            return@Button
                        }
                        val hargaWeekendVal = hargaWeekend.toDoubleOrNull() ?: hargaVal
                        if (editingId != null) {
                            viewModel.updateKamar(Kamar(editingId!!, nomor, tipe, hargaVal, hargaWeekendVal)) { ok, msg ->
                                dialogMsg = msg
                                if (ok) { nomor = ""; tipe = ""; harga = ""; hargaWeekend = ""; editingId = null }
                            }
                        } else {
                            viewModel.tambahKamar(nomor, tipe, hargaVal, hargaWeekendVal) { ok, msg ->
                                dialogMsg = msg
                                if (ok) { nomor = ""; tipe = ""; harga = ""; hargaWeekend = "" }
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Simpan")
                    }
                    OutlinedButton(onClick = { nomor = ""; tipe = ""; harga = ""; hargaWeekend = ""; editingId = null }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("Daftar Homestay", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            if (semuaKamar.isEmpty()) {
                item { Text("Belum ada homestay", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(semuaKamar) { kamar ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Homestay ${kamar.nomorKamar}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Tipe: ${kamar.tipeKamar}")
                        Text("Weekday: Rp ${String.format("%,.0f", kamar.hargaPerMalam)}/malam", color = MaterialTheme.colorScheme.primary)
                        if (kamar.hargaWeekend != kamar.hargaPerMalam) {
                            Text("Weekend: Rp ${String.format("%,.0f", kamar.hargaWeekend)}/malam", color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                nomor = kamar.nomorKamar; tipe = kamar.tipeKamar; harga = kamar.hargaPerMalam.toString(); hargaWeekend = kamar.hargaWeekend.toString(); editingId = kamar.id
                            }) { Text("Edit") }
                            OutlinedButton(onClick = { viewModel.hapusKamar(kamar) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Hapus") }
                        }
                    }
                }
            }
        }
    }

    dialogMsg?.let {
        AlertDialog(onDismissRequest = { dialogMsg = null }, title = { Text("Info") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } })
    }
}
