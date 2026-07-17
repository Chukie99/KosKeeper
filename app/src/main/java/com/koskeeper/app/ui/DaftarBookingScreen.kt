package com.koskeeper.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.koskeeper.app.BookingLengkap
import com.koskeeper.app.PondokViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarBookingScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit,
    onDetail: (Long) -> Unit = {}
) {
    val bookingAktif by viewModel.bookingAktif.collectAsState()
    val semuaBooking by viewModel.semuaBooking.collectAsState()
    val semuaKamar by viewModel.semuaKamar.collectAsState()
    val semuaTamu by viewModel.semuaTamu.collectAsState()

    var filter by remember { mutableStateOf("aktif") }
    var searchQuery by remember { mutableStateOf("") }
    var filterKamarId by remember { mutableStateOf<Long?>(null) }
    var showConfirm by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }
    var editingBooking by remember { mutableStateOf<BookingLengkap?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var selectedKamarId by remember { mutableStateOf<Long?>(null) }
    val today = remember { LocalDate.now() }
    var yearMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val filteredBookings = when (filter) {
        "aktif" -> bookingAktif
        "selesai" -> semuaBooking.filter { it.status == "selesai" }
        else -> semuaBooking
    }.filter { b ->
        val matchSearch = searchQuery.isBlank() ||
            b.namaLengkap.contains(searchQuery, ignoreCase = true) ||
            b.nomorKamar.contains(searchQuery, ignoreCase = true) ||
            b.nomorKontak.contains(searchQuery) ||
            "${b.id}".contains(searchQuery)
        val matchKamar = filterKamarId == null || semuaKamar.find { it.id == filterKamarId }?.nomorKamar == b.nomorKamar
        matchSearch && matchKamar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Booking") },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("aktif" to "Aktif", "selesai" to "Selesai", "semua" to "Semua").forEach { (key, label) ->
                    FilterChip(
                        selected = filter == key,
                        onClick = { filter = key },
                        label = { Text(label) },
                        leadingIcon = if (filter == key) {{ Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }} else null
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari nama, kamar, kontak...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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

            // Filter Kamar
            if (semuaKamar.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterKamarId == null,
                        onClick = { filterKamarId = null },
                        label = { Text("Semua Kamar") }
                    )
                    semuaKamar.forEach { kamar ->
                        FilterChip(
                            selected = filterKamarId == kamar.id,
                            onClick = { filterKamarId = kamar.id },
                            label = { Text(kamar.nomorKamar) }
                        )
                    }
                }
            }

            Text(
                "Menampilkan ${filteredBookings.size} booking",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Kalender Ketersediaan
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kalender Ketersediaan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { showCalendar = !showCalendar }) {
                            Icon(
                                if (showCalendar) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showCalendar) "Sembunyikan" else "Tampilkan"
                            )
                        }
                    }

                    if (showCalendar) {
                        // Filter kamar
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedKamarId == null,
                                onClick = { selectedKamarId = null },
                                label = { Text("Semua") }
                            )
                            semuaKamar.forEach { kamar ->
                                FilterChip(
                                    selected = selectedKamarId == kamar.id,
                                    onClick = { selectedKamarId = kamar.id },
                                    label = { Text(kamar.nomorKamar) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Navigasi bulan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                                Icon(Icons.Default.ChevronLeft, "Bulan Sebelum")
                            }
                            Text("${yearMonth.month} ${yearMonth.year}", style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, "Bulan Berikutnya")
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Legend
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFC8E6C9)))
                                Spacer(Modifier.width(4.dp))
                                Text("Kosong", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFCDD2)))
                                Spacer(Modifier.width(4.dp))
                                Text("Terisi", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFF9C4)))
                                Spacer(Modifier.width(4.dp))
                                Text("Checkout", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Grid kalender
                        val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            dayNames.forEach { d ->
                                Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        val firstDay = yearMonth.atDay(1)
                        val startDayOfWeek = firstDay.dayOfWeek.value % 7
                        val totalDays = yearMonth.lengthOfMonth()
                        val totalCells = startDayOfWeek + totalDays
                        val rows = (totalCells + 6) / 7

                        // Filter booking berdasarkan filter aktif & kamar
                        val relevantBookings = remember(filteredBookings, selectedKamarId) {
                            if (selectedKamarId != null) {
                                filteredBookings.filter { b ->
                                    val kamar = semuaKamar.find { it.nomorKamar == b.nomorKamar }
                                    kamar?.id == selectedKamarId
                                }
                            } else {
                                filteredBookings
                            }
                        }

                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startDayOfWeek + 1
                                    if (dayNum in 1..totalDays) {
                                        val tanggal = String.format("%04d-%02d-%02d", yearMonth.year, yearMonth.monthValue, dayNum)

                                        // Cek status tanggal
                                        val nowStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        val currentTime = LocalTime.now()

                                        val isCheckoutDay = relevantBookings.any { b ->
                                            b.tanggalCheckout == tanggal
                                        }
                                        val isOccupied = if (isCheckoutDay) false else relevantBookings.any { b ->
                                            b.tanggalCheckin <= tanggal && b.tanggalCheckout > tanggal
                                        }

                                        val bgColor = when {
                                            isCheckoutDay -> Color(0xFFFFF9C4)  // Kuning - checkout
                                            isOccupied -> Color(0xFFFFCDD2)     // Merah - terisi
                                            else -> Color(0xFFC8E6C9)           // Hijau - kosong
                                        }
                                        val textColor = when {
                                            isCheckoutDay -> Color(0xFFF57F17)   // Kuning tua
                                            isOccupied -> Color(0xFFC62828)      // Merah tua
                                            else -> Color(0xFF2E7D32)            // Hijau tua
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f).padding(1.dp).aspectRatio(1f),
                                            colors = CardDefaults.cardColors(containerColor = bgColor)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("$dayNum", color = textColor, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredBookings.isEmpty()) {
                Text("Tidak ada booking", modifier = Modifier.fillMaxWidth().padding(32.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBookings) { b ->
                    val isActive = b.status == "aktif"
                    val statusColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDetail(b.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Booking #${b.id}", style = MaterialTheme.typography.titleMedium, color = statusColor)
                                Text(b.status.uppercase(), style = MaterialTheme.typography.labelMedium, color = statusColor)
                            }
                            Text("Homestay: ${b.nomorKamar} (${b.tipeKamar})", style = MaterialTheme.typography.bodyLarge)
                            Text("Tamu: ${b.namaLengkap} - ${b.nomorKontak}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Login, null, Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Check-in: ${b.tanggalCheckin} Jam ${b.jamCheckin}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Logout, null, Modifier.size(16.dp), tint = Color(0xFFC62828))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Check-out: ${b.tanggalCheckout} Jam ${b.jamCheckout}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Total: Rp ${String.format("%,.0f", b.totalBayar)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        b.status.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (b.hargaStandar != b.totalBayar) {
                                Text("(Standar: Rp ${String.format("%,.0f", b.hargaStandar)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (isActive) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { editingBooking = b }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit")
                                    }
                                    Button(onClick = { showConfirm = Pair("selesai", b.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Selesai")
                                    }
                                    OutlinedButton(onClick = { showConfirm = Pair("batal", b.id) }, modifier = Modifier.weight(1f)) {
                                        Text("Batal")
                                    }
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    OutlinedButton(onClick = { showDeleteConfirm = b.id }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Hapus dari History")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showConfirm?.let { (action, id) ->
        AlertDialog(
            onDismissRequest = { showConfirm = null },
            title = { Text(if (action == "selesai") "Tandai Selesai?" else "Batalkan Booking?") },
            text = { Text(if (action == "selesai") "Tandai booking ini sebagai selesai?" else "Batalkan booking ini?") },
            confirmButton = {
                TextButton(onClick = {
                    if (action == "selesai") viewModel.selesaikanBooking(id)
                    else viewModel.batalBooking(id)
                    showConfirm = null
                }) { Text("Ya") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = null }) { Text("Batal") } }
        )
    }

    showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus dari History?") },
            text = { Text("Booking #$id akan dihapus permanen dari daftar. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hapusBooking(id)
                    showDeleteConfirm = null
                }) { Text("Ya, Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Tidak") } }
        )
    }

    editingBooking?.let { booking ->
        EditBookingDialog(
            booking = booking,
            semuaKamar = semuaKamar,
            viewModel = viewModel,
            onDismiss = { editingBooking = null },
            onResult = { _, _ -> editingBooking = null },
            onCancel = { id ->
                viewModel.batalBooking(id)
                editingBooking = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingDialog(
    booking: BookingLengkap,
    semuaKamar: List<com.koskeeper.app.Kamar>,
    viewModel: PondokViewModel,
    onDismiss: () -> Unit,
    onResult: (Boolean, String) -> Unit,
    onCancel: (Long) -> Unit
) {
    var selectedKamarId by remember { mutableStateOf<Long?>(null) }
    var checkin by remember { mutableStateOf(booking.tanggalCheckin) }
    var checkout by remember { mutableStateOf(booking.tanggalCheckout) }
    var jamIn by remember { mutableStateOf(booking.jamCheckin) }
    var jamOut by remember { mutableStateOf(booking.jamCheckout) }
    var dialogMsg by remember { mutableStateOf<String?>(null) }
    var showCheckinPicker by remember { mutableStateOf(false) }
    var showCheckoutPicker by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(booking) {
        val kamar = semuaKamar.find { it.nomorKamar == booking.nomorKamar }
        selectedKamarId = kamar?.id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booking #${booking.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Info Tamu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Data Tamu", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text("${booking.namaLengkap}", style = MaterialTheme.typography.bodyLarge)
                        Text("${booking.nomorKontak}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Pilih Homestay
                Text("Pindah Homestay", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text("Sekarang: ${booking.nomorKamar} (${booking.tipeKamar})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    semuaKamar.forEach { k ->
                        FilterChip(
                            selected = selectedKamarId == k.id,
                            onClick = { selectedKamarId = k.id },
                            label = { Text(k.nomorKamar) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Check-in
                Text("Tanggal & Jam Check-in", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = checkin,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().clickable { showCheckinPicker = true },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                OutlinedTextField(value = jamIn, onValueChange = { jamIn = it }, label = { Text("Jam Check-in (HH:MM)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Schedule, null) })

                Spacer(Modifier.height(8.dp))

                // Check-out
                Text("Tanggal & Jam Check-out", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = checkout,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().clickable { showCheckoutPicker = true },
                    readOnly = true,
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                OutlinedTextField(value = jamOut, onValueChange = { jamOut = it }, label = { Text("Jam Check-out (HH:MM)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Schedule, null) })

                Spacer(Modifier.height(8.dp))

                // Info Harga
                val kamarBaru = semuaKamar.find { it.id == selectedKamarId }
                val priceInfo = remember(kamarBaru, checkin, checkout) {
                    if (kamarBaru != null && checkin.isNotBlank() && checkout.isNotBlank()) {
                        try {
                            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            val d1 = fmt.parse(checkin)
                            val d2 = fmt.parse(checkout)
                            if (d1 != null && d2 != null && d2.after(d1)) {
                                val malam = maxOf(((d2.time - d1.time) / 86400000).toInt(), 1)
                                val total = kamarBaru.hargaPerMalam * malam
                                Pair(malam, total)
                            } else null
                        } catch (_: Exception) { null }
                    } else null
                }
                priceInfo?.let { (malam, total) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${malam} malam x Rp ${String.format("%,.0f", kamarBaru!!.hargaPerMalam)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Total Baru: Rp ${String.format("%,.0f", total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showCancelConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Cancel Booking")
                }
                TextButton(onClick = onDismiss) { Text("Batal") }
                TextButton(onClick = {
                    if (selectedKamarId == null) { dialogMsg = "Pilih homestay!"; return@TextButton }
                    if (checkin.isBlank() || checkout.isBlank()) { dialogMsg = "Isi tanggal!"; return@TextButton }

                    viewModel.editBooking(
                        booking.id, selectedKamarId!!, 0,
                        checkin, jamIn, checkout, jamOut
                    ) { ok, msg ->
                        if (ok) { onResult(true, msg); onDismiss() }
                        else dialogMsg = msg
                    }
                }) { Text("Simpan") }
            }
        },
        dismissButton = {}
    )

    if (showCheckinPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCheckinPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        checkin = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showCheckinPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCheckinPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showCheckoutPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCheckoutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        checkout = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showCheckoutPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCheckoutPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Batalkan Booking?") },
            text = { Text("Booking #${booking.id} (${booking.namaLengkap}) akan dibatalkan. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    onCancel(booking.id)
                    showCancelConfirm = false
                }) { Text("Ya, Batalkan", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showCancelConfirm = false }) { Text("Tidak") } }
        )
    }

    dialogMsg?.let {
        AlertDialog(
            onDismissRequest = { dialogMsg = null },
            title = { Text("Info") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { dialogMsg = null }) { Text("OK") } }
        )
    }
}


