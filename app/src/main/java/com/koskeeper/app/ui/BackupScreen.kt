package com.koskeeper.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.koskeeper.app.PondokViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var dialogMsg by remember { mutableStateOf<String?>(null) }
    var backupInProgress by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            backupInProgress = true
            val success = backupDatabase(context, it)
            backupInProgress = false
            dialogMsg = if (success) "Backup berhasil!" else "Gagal backup database"
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val success = restoreDatabase(context, it)
            dialogMsg = if (success) "Restore berhasil! Silakan restart aplikasi." else "Gagal restore database"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Backup Database", style = MaterialTheme.typography.titleMedium)
                            Text("Simpan semua data ke file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { createBackupLauncher.launch("koskeeper_backup.db") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !backupInProgress
                    ) {
                        if (backupInProgress) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Memproses...")
                        } else {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Backup Sekarang")
                        }
                    }
                }
            }

            // Restore Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Restore Database", style = MaterialTheme.typography.titleMedium)
                            Text("Muat data dari file backup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("application/octet-stream")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih File Backup")
                    }
                }
            }

            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Catatan:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text("• Backup akan menyimpan semua data homestay, tamu, dan booking", style = MaterialTheme.typography.bodySmall)
                    Text("• Restore akan mengganti seluruh data yang ada", style = MaterialTheme.typography.bodySmall)
                    Text("• Setelah restore, restart aplikasi agar perubahan berlaku", style = MaterialTheme.typography.bodySmall)
                    Text("• Simpan file backup di tempat yang aman", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    dialogMsg?.let {
        AlertDialog(onDismissRequest = { dialogMsg = null }, title = { Text("Info") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } })
    }
}

private fun backupDatabase(context: Context, uri: Uri): Boolean {
    return try {
        val dbFile = context.getDatabasePath("koskeeper.db")
        if (!dbFile.exists()) return false

        // Copy WAL and SHM files if they exist
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

private fun restoreDatabase(context: Context, uri: Uri): Boolean {
    return try {
        val dbFile = context.getDatabasePath("koskeeper.db")

        // Close existing database connections
        val db = com.koskeeper.app.AppDatabase.getDatabase(context)
        db.close()

        context.contentResolver.openInputStream(uri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Delete WAL and SHM files
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()

        true
    } catch (e: Exception) {
        false
    }
}
