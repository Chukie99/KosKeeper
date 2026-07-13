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
import com.koskeeper.app.PondokViewModel
import com.koskeeper.app.Tamu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TamuScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaTamu by viewModel.semuaTamu.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingTamu by remember { mutableStateOf<Tamu?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Tamu?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogMsg by remember { mutableStateOf<String?>(null) }

    val filteredTamu = if (searchQuery.isBlank()) semuaTamu
    else semuaTamu.filter {
        it.namaLengkap.contains(searchQuery, ignoreCase = true) ||
        it.nomorKontak.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Tamu") },
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
                Icon(Icons.Default.Add, "Tambah Tamu")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari nama atau nomor kontak...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Hapus")
                        }
                    }
                },
                singleLine = true
            )

            Text(
                "Total: ${filteredTamu.size} tamu",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (filteredTamu.isEmpty()) {
                Text(
                    if (searchQuery.isBlank()) "Belum ada data tamu" else "Tidak ditemukan",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTamu) { tamu ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tamu.namaLengkap, style = MaterialTheme.typography.titleMedium)
                                    Text(tamu.nomorKontak, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    IconButton(onClick = { editingTamu = tamu }) {
                                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showDeleteConfirm = tamu }) {
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
        TamuDialog(
            title = "Tambah Tamu",
            initialNama = "",
            initialKontak = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { nama, kontak ->
                viewModel.tambahTamu(nama, kontak) { showAddDialog = false }
            }
        )
    }

    editingTamu?.let { tamu ->
        TamuDialog(
            title = "Edit Tamu",
            initialNama = tamu.namaLengkap,
            initialKontak = tamu.nomorKontak,
            onDismiss = { editingTamu = null },
            onConfirm = { nama, kontak ->
                viewModel.updateTamu(tamu.copy(namaLengkap = nama, nomorKontak = kontak)) { ok, msg ->
                    if (!ok) dialogMsg = msg
                    editingTamu = null
                }
            }
        )
    }

    showDeleteConfirm?.let { tamu ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus Tamu?") },
            text = { Text("Hapus tamu '${tamu.namaLengkap}'? Data ini akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hapusTamu(tamu)
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

@Composable
fun TamuDialog(
    title: String,
    initialNama: String,
    initialKontak: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nama by remember { mutableStateOf(initialNama) }
    var kontak by remember { mutableStateOf(initialKontak) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                OutlinedTextField(
                    value = kontak,
                    onValueChange = { kontak = it },
                    label = { Text("Nomor Kontak") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nama, kontak) },
                enabled = nama.isNotBlank() && kontak.isNotBlank()
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
