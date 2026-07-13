package com.koskeeper.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.koskeeper.app.PondokViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalenderScreen(
    viewModel: PondokViewModel,
    onBack: () -> Unit
) {
    val semuaKamar by viewModel.semuaKamar.collectAsState()
    val bookingList by viewModel.semuaBooking.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedKamarId by remember { mutableStateOf<Long?>(null) }
    val today = remember { LocalDate.now() }
    var yearMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val availabilityMap = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(selectedKamarId, yearMonth, bookingList.size) {
        if (selectedKamarId != null) {
            val startDay = 1
            val endDay = yearMonth.lengthOfMonth()
            for (day in startDay..endDay) {
                val tanggal = String.format("%04d-%02d-%02d", yearMonth.year, yearMonth.monthValue, day)
                availabilityMap[tanggal] = viewModel.cekKamarTersedia(selectedKamarId!!, tanggal)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalender Ketersediaan") },
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Filter Homestay", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                semuaKamar.forEach { kamar ->
                    FilterChip(
                        selected = selectedKamarId == kamar.id,
                        onClick = { selectedKamarId = kamar.id; availabilityMap.clear() },
                        label = { Text(kamar.nomorKamar) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { yearMonth = yearMonth.minusMonths(1); availabilityMap.clear() }) {
                    Icon(Icons.Default.ChevronLeft, "Prev")
                }
                Text("Kalender Bulan ${yearMonth.month} ${yearMonth.year}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = {
                        availabilityMap.clear()
                        if (selectedKamarId != null) {
                            val startDay = 1
                            val endDay = yearMonth.lengthOfMonth()
                            for (day in startDay..endDay) {
                                val tanggal = String.format("%04d-%02d-%02d", yearMonth.year, yearMonth.monthValue, day)
                                availabilityMap[tanggal] = kotlinx.coroutines.runBlocking { viewModel.cekKamarTersedia(selectedKamarId!!, tanggal) }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { yearMonth = yearMonth.plusMonths(1); availabilityMap.clear() }) {
                        Icon(Icons.Default.ChevronRight, "Next")
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(16.dp).background(Color(0xFFC8E6C9)))
                    Spacer(Modifier.width(4.dp))
                    Text("Tersedia")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(16.dp).background(Color(0xFFFFCDD2)))
                    Spacer(Modifier.width(4.dp))
                    Text("Terisi")
                }
            }

            if (selectedKamarId == null) {
                Text("Pilih homestay terlebih dahulu", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                val dayNames = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEach { d ->
                        Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                val firstDay = yearMonth.atDay(1)
                val startDayOfWeek = firstDay.dayOfWeek.value % 7
                val totalDays = yearMonth.lengthOfMonth()
                val totalCells = startDayOfWeek + totalDays
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - startDayOfWeek + 1
                            if (dayNum in 1..totalDays) {
                                val tanggal = String.format("%04d-%02d-%02d", yearMonth.year, yearMonth.monthValue, dayNum)
                                val isAvailable = availabilityMap[tanggal] ?: true
                                val bgColor = if (isAvailable) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                                val textColor = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)

                                Card(
                                    modifier = Modifier.weight(1f).padding(2.dp).aspectRatio(1f),
                                    colors = CardDefaults.cardColors(containerColor = bgColor)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("$dayNum", color = textColor, style = MaterialTheme.typography.bodyMedium)
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
}
