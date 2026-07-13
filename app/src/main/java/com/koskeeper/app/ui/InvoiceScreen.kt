package com.koskeeper.app.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koskeeper.app.BookingLengkap
import com.koskeeper.app.PondokViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: PondokViewModel,
    bookingId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var booking by remember { mutableStateOf<BookingLengkap?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        booking = viewModel.getBookingById(bookingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            booking?.let { b ->
                // Invoice Preview
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "KOSKEEPER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "INVOICE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        InvoiceRow("No. Invoice", "#${b.id}")
                        InvoiceRow("Tanggal", SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()))
                        Spacer(modifier = Modifier.height(8.dp))

                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        InvoiceRow("Tamu", b.namaLengkap)
                        InvoiceRow("Kamar", b.nomorKamar)
                        InvoiceRow("Check-in", "${b.tanggalCheckin} ${b.jamCheckin}")
                        InvoiceRow("Check-out", "${b.tanggalCheckout} ${b.jamCheckout}")
                        InvoiceRow("Malam", "${hitungMalam(b.tanggalCheckin, b.tanggalCheckout)}")

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        InvoiceRow("Total Bayar", "Rp ${String.format("%,.0f", b.totalBayar)}")
                        InvoiceRow("Status", b.status.uppercase())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        generatePdf(context, b)
                        showSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PDF")
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Berhasil") },
            text = { Text("Invoice PDF berhasil disimpan di Download/KosKeeper/") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun hitungMalam(checkin: String, checkout: String): Int {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val d1 = fmt.parse(checkin) ?: return 1
    val d2 = fmt.parse(checkout) ?: return 1
    return maxOf(((d2.time - d1.time) / 86400000).toInt(), 1)
}

private fun generatePdf(context: Context, booking: BookingLengkap) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { textSize = 12f }

    var y = 50f

    // Header
    val titlePaint = Paint().apply { textSize = 24f; isFakeBoldText = true; color = android.graphics.Color.rgb(5, 150, 105) }
    canvas.drawText("KOSKEEPER", 50f, y, titlePaint)
    y += 30f
    val subtitlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
    canvas.drawText("INVOICE", 50f, y, subtitlePaint)
    y += 40f

    // Divider
    canvas.drawLine(50f, y, 545f, y, Paint().apply { strokeWidth = 2f; color = android.graphics.Color.GRAY })
    y += 30f

    // Details
    canvas.drawText("No. Invoice: #${booking.id}", 50f, y, paint)
    y += 20f
    canvas.drawText("Tanggal: ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())}", 50f, y, paint)
    y += 40f

    canvas.drawText("Tamu: ${booking.namaLengkap}", 50f, y, paint)
    y += 20f
    canvas.drawText("Kamar: ${booking.nomorKamar}", 50f, y, paint)
    y += 20f
    canvas.drawText("Check-in: ${booking.tanggalCheckin} ${booking.jamCheckin}", 50f, y, paint)
    y += 20f
    canvas.drawText("Check-out: ${booking.tanggalCheckout} ${booking.jamCheckout}", 50f, y, paint)
    y += 20f
    canvas.drawText("Malam: ${hitungMalam(booking.tanggalCheckin, booking.tanggalCheckout)}", 50f, y, paint)
    y += 40f

    // Divider
    canvas.drawLine(50f, y, 545f, y, Paint().apply { strokeWidth = 2f; color = android.graphics.Color.GRAY })
    y += 30f

    // Total
    val totalPaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
    canvas.drawText("Total Bayar: Rp ${String.format("%,.0f", booking.totalBayar)}", 50f, y, totalPaint)
    y += 25f
    canvas.drawText("Status: ${booking.status.uppercase()}", 50f, y, paint)
    y += 60f

    // Footer
    val footerPaint = Paint().apply { textSize = 10f; color = android.graphics.Color.GRAY; textAlign = Paint.Align.CENTER }
    canvas.drawText("Terima kasih atas kunjungan Anda!", 297f, y, footerPaint)
    y += 15f
    canvas.drawText("KosKeeper - Sistem Manajemen Kos", 297f, y, footerPaint)

    document.finishPage(page)

    // Save to Downloads
    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "KosKeeper")
    dir.mkdirs()
    val file = File(dir, "Invoice_${booking.id}.pdf")
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()
}
