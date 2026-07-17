package com.koskeeper.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.koskeeper.app.BookingDetailLaporan
import com.koskeeper.app.LaporanBulanan
import com.koskeeper.app.PondokViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val laporan by viewModel.laporanBulanan.collectAsState()
    val laporanDetail by viewModel.laporanDetail.collectAsState()
    val dariTanggal by viewModel.dariTanggal.collectAsState()
    val sampaiTanggal by viewModel.sampaiTanggal.collectAsState()
    val context = LocalContext.current
    var dialogMsg by remember { mutableStateOf<String?>(null) }
    var showDariPicker by remember { mutableStateOf(false) }
    var showSampaiPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan") },
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
            // Filter Tanggal
            item {
                Text("Filter Tanggal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dariTanggal,
                        onValueChange = {},
                        label = { Text("Dari Tanggal") },
                        modifier = Modifier.weight(1f).clickable { showDariPicker = true },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                    )
                    OutlinedTextField(
                        value = sampaiTanggal,
                        onValueChange = {},
                        label = { Text("Sampai Tanggal") },
                        modifier = Modifier.weight(1f).clickable { showSampaiPicker = true },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Tabel Detail Laporan
            item {
                Text("Detail Transaksi", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Menampilkan ${laporanDetail.size} transaksi dari $dariTanggal sampai $sampaiTanggal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            if (laporanDetail.isEmpty()) {
                item {
                    Text("Tidak ada data transaksi pada rentang tanggal ini", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Table Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Tanggal", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Tamu", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Homestay", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text("Status", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Table Rows
                itemsIndexed(laporanDetail) { index, row ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall)
                            Text("${row.tanggalCheckin}\n${row.jamCheckin}", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall)
                            Text(row.namaLengkap, modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall)
                            Text("${row.nomorKamar}\n${row.tipeKamar}", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rp\n${String.format("%,.0f", row.totalBayar)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                if (row.hargaStandar != row.totalBayar) {
                                    Text("(Std: ${String.format("%,.0f", row.hargaStandar)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(row.status.uppercase(), modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = if (row.status == "aktif") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Total Summary Row
                item {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${laporanDetail.size} transaksi", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(0.8f))
                            Text("Rp ${String.format("%,.0f", laporanDetail.sumOf { it.totalBayar })}", modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Rangkuman Bulanan
            item {
                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("Rangkuman per Bulan", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if (laporan.isEmpty()) {
                item { Text("Belum ada data laporan", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(laporan) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Bulan: ${row.bulan}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Transaksi: ${row.totalTransaksi}", style = MaterialTheme.typography.bodyLarge)
                        Text("Pendapatan: Rp ${String.format("%,.0f", row.totalPendapatan)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (laporan.isEmpty() && laporanDetail.isEmpty()) {
                            dialogMsg = "Tidak ada data untuk diekspor!"
                            return@Button
                        }
                        exportCsv(context, laporan, laporanDetail, dariTanggal, sampaiTanggal) { msg -> dialogMsg = msg }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ekspor Laporan CSV")
                }
            }
        }
    }

    if (showDariPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDariPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setFilterTanggal(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), sampaiTanggal)
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
                        viewModel.setFilterTanggal(dariTanggal, localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                    showSampaiPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showSampaiPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    dialogMsg?.let {
        AlertDialog(onDismissRequest = { dialogMsg = null }, title = { Text("Info") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } })
    }
}

private fun exportCsv(context: Context, data: List<LaporanBulanan>, detail: List<BookingDetailLaporan>, dariTanggal: String, sampaiTanggal: String, onResult: (String) -> Unit) {
    try {
        val file = File(context.cacheDir, "laporan_bulanan.csv")
        file.bufferedWriter().use { writer ->
            writer.WriteLine("Laporan KosKeeper ($dariTanggal - $sampaiTanggal)")
            writer.WriteLine("")
            writer.WriteLine("Detail Transaksi:")
            writer.WriteLine("No,Tanggal Check-in,Check-out,Tamu,Homestay,Total,Status")
            detail.forEachIndexed { index, row ->
                writer.WriteLine("${index + 1},${row.tanggalCheckin} ${row.jamCheckin},${row.tanggalCheckout} ${row.jamCheckout},${row.namaLengkap},${row.nomorKamar} (${row.tipeKamar}),Rp ${String.format("%,.0f", row.totalBayar)},${row.status}")
            }
            writer.WriteLine("")
            writer.WriteLine("Rangkuman Bulanan:")
            writer.WriteLine("Bulan,Total Transaksi,Total Pendapatan")
            data.forEach { row ->
                writer.WriteLine("${row.bulan},${row.totalTransaksi},Rp ${String.format("%,.0f", row.totalPendapatan)}")
            }
            writer.WriteLine("")
            writer.WriteLine("Total: ${detail.size} transaksi, Rp ${String.format("%,.0f", detail.sumOf { it.totalBayar })}")
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Laporan"))
        onResult("Laporan berhasil diekspor!")
    } catch (e: Exception) {
        onResult("Gagal ekspor: ${e.message}")
    }
}

private fun java.io.Writer.WriteLine(text: String) {
    write(text)
    write("\n")
}
