package com.koskeeper.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koskeeper.app.PondokViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaKamar by viewModel.semuaKamar.collectAsState()
    val semuaTamu by viewModel.semuaTamu.collectAsState()

    var selectedKamarId by remember { mutableStateOf<Long?>(null) }
    var selectedTamuId by remember { mutableStateOf<Long?>(null) }
    var nama by remember { mutableStateOf("") }
    var kontak by remember { mutableStateOf("") }
    var checkin by remember { mutableStateOf("") }
    var checkout by remember { mutableStateOf("") }
    var jamIn by remember { mutableStateOf("14:00") }
    var jamOut by remember { mutableStateOf("12:00") }
    var dialogMsg by remember { mutableStateOf<String?>(null) }
    var showCheckinPicker by remember { mutableStateOf(false) }
    var showCheckoutPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Pemesanan") },
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
                Text("Data Tamu", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = kontak, onValueChange = { kontak = it }, label = { Text("Nomor Kontak (HP/WA)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Phone, null) })
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("Pilih Homestay", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            items(semuaKamar) { kamar ->
                FilterChip(
                    selected = selectedKamarId == kamar.id,
                    onClick = { selectedKamarId = kamar.id },
                    label = { Text("Homestay ${kamar.nomorKamar} - ${kamar.tipeKamar} - Rp ${String.format("%,.0f", kamar.hargaPerMalam)}/mlm") },
                    leadingIcon = if (selectedKamarId == kamar.id) {{ Icon(Icons.Default.Check, null) }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (semuaTamu.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Pilih Tamu (atau isi Nama di atas)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                items(semuaTamu) { tamu ->
                    FilterChip(
                        selected = selectedTamuId == tamu.id,
                        onClick = {
                            selectedTamuId = tamu.id
                            nama = tamu.namaLengkap
                            kontak = tamu.nomorKontak
                        },
                        label = { Text("${tamu.namaLengkap} - ${tamu.nomorKontak}") },
                        leadingIcon = if (selectedTamuId == tamu.id) {{ Icon(Icons.Default.Check, null) }} else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("Tanggal & Jam Check-in", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (checkin.isNotEmpty()) checkin else "",
                    onValueChange = {},
                    label = { Text("Tanggal Check-in") },
                    modifier = Modifier.fillMaxWidth().clickable { showCheckinPicker = true },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    enabled = false
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = jamIn, onValueChange = { jamIn = it }, label = { Text("Jam Check-in (HH:MM)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Schedule, null) })
                Spacer(Modifier.height(8.dp))
                Text("Tanggal & Jam Check-out", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (checkout.isNotEmpty()) checkout else "",
                    onValueChange = {},
                    label = { Text("Tanggal Check-out") },
                    modifier = Modifier.fillMaxWidth().clickable { showCheckoutPicker = true },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    enabled = false
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = jamOut, onValueChange = { jamOut = it }, label = { Text("Jam Check-out (HH:MM)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Schedule, null) })

                // Price Preview
                val selectedKamar = semuaKamar.find { it.id == selectedKamarId }
                val semuaLibur by viewModel.semuaHariLibur.collectAsState()
                if (selectedKamar != null && checkin.isNotBlank() && checkout.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    val priceInfo = try {
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d1 = fmt.parse(checkin)
                        val d2 = fmt.parse(checkout)
                        if (d1 != null && d2 != null && d2.after(d1)) {
                            val malam = maxOf(((d2.time - d1.time) / 86400000).toInt(), 1)
                            val start = d1.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            var weekdayCount = 0
                            var weekendCount = 0
                            var liburCount = 0
                            var liburTotal = 0.0
                            var total = 0.0
                            for (i in 0 until malam) {
                                val tgl = start.plusDays(i.toLong())
                                val tglStr = tgl.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                val libur = semuaLibur.find { tglStr >= it.tanggalMulai && tglStr <= it.tanggalSelesai }
                                if (libur != null) {
                                    liburCount++
                                    liburTotal += libur.hargaPerMalam
                                    total += libur.hargaPerMalam
                                } else if (tgl.dayOfWeek == java.time.DayOfWeek.SATURDAY || tgl.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                                    weekendCount++
                                    total += selectedKamar.hargaWeekend
                                } else {
                                    weekdayCount++
                                    total += selectedKamar.hargaPerMalam
                                }
                            }
                            listOf(weekdayCount.toDouble(), weekendCount.toDouble(), liburCount.toDouble(), total)
                        } else null
                    } catch (_: Exception) { null }

                    priceInfo?.let { info ->
                        val weekday = info[0].toInt()
                        val weekend = info[1].toInt()
                        val libur = info[2].toInt()
                        val total = info[3]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Rincian Harga", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                if (weekday > 0) Text("Weekday: $weekday malam x Rp ${String.format("%,.0f", selectedKamar.hargaPerMalam)}", style = MaterialTheme.typography.bodySmall)
                                if (weekend > 0) Text("Weekend: $weekend malam x Rp ${String.format("%,.0f", selectedKamar.hargaWeekend)}", style = MaterialTheme.typography.bodySmall)
                                if (libur > 0) Text("Libur: $libur malam (harga khusus)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(4.dp))
                                Text("Total: Rp ${String.format("%,.0f", total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedKamarId == null) { dialogMsg = "Pilih homestay terlebih dahulu!"; return@Button }
                        if (nama.isBlank()) { dialogMsg = "Nama tamu harus diisi!"; return@Button }
                        if (checkin.isBlank() || checkout.isBlank()) { dialogMsg = "Tanggal check-in dan check-out harus diisi!"; return@Button }
                        try {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val d1 = fmt.parse(checkin)
                            val d2 = fmt.parse(checkout)
                            if (d2 == null || d1 == null || !d2.after(d1)) { dialogMsg = "Check-out harus setelah check-in!"; return@Button }
                        } catch (e: Exception) { dialogMsg = "Format tanggal salah! Gunakan YYYY-MM-DD"; return@Button }

                        val doBooking = { tamuId: Long ->
                            viewModel.tambahBooking(selectedKamarId!!, tamuId, checkin, jamIn, checkout, jamOut) { ok, msg ->
                                dialogMsg = msg
                                if (ok) { selectedKamarId = null; selectedTamuId = null; nama = ""; kontak = ""; checkin = ""; checkout = ""; jamIn = "14:00"; jamOut = "12:00" }
                            }
                        }

                        if (selectedTamuId != null) {
                            doBooking(selectedTamuId!!)
                        } else {
                            if (kontak.isBlank()) { dialogMsg = "Nomor kontak harus diisi!"; return@Button }
                            viewModel.tambahTamu(nama, kontak) { tamuId -> doBooking(tamuId) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan Booking")
                }
            }
        }
    }

    dialogMsg?.let {
        AlertDialog(onDismissRequest = { dialogMsg = null }, title = { Text("Info") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } })
    }

    if (showCheckinPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCheckinPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.ofEpochMilli(millis)
                        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        checkin = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showCheckinPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckinPicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCheckoutPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCheckoutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.ofEpochMilli(millis)
                        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        checkout = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showCheckoutPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutPicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
