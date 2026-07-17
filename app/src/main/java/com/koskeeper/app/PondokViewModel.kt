package com.koskeeper.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class PondokViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val kamarDao = db.kamarDao()
    private val tamuDao = db.tamuDao()
    private val bookingDao = db.bookingDao()
    private val hariLiburDao = db.hariLiburDao()
    private val pembayaranDao = db.pembayaranDao()

    val semuaKamar: StateFlow<List<Kamar>> = kamarDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semuaTamu: StateFlow<List<Tamu>> = tamuDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookingAktif: StateFlow<List<BookingLengkap>> = bookingDao.getActiveLengkap()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semuaBooking: StateFlow<List<BookingLengkap>> = bookingDao.getAllLengkap()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val laporanBulanan: StateFlow<List<LaporanBulanan>> = bookingDao.getLaporanBulanan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dariTanggal = MutableStateFlow("2020-01-01")
    val dariTanggal: StateFlow<String> = _dariTanggal.asStateFlow()

    private val _sampaiTanggal = MutableStateFlow("2099-12-31")
    val sampaiTanggal: StateFlow<String> = _sampaiTanggal.asStateFlow()

    val laporanDetail: StateFlow<List<BookingDetailLaporan>> = combine(_dariTanggal, _sampaiTanggal) { dari, sampai ->
        Pair(dari, sampai)
    }.flatMapLatest { (dari, sampai) -> bookingDao.getLaporanDetail(dari, sampai) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterTanggal(dari: String, sampai: String) {
        _dariTanggal.value = dari
        _sampaiTanggal.value = sampai
    }

    val semuaHariLibur: StateFlow<List<HariLibur>> = hariLiburDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tambahHariLibur(nama: String, dari: String, sampai: String, harga: Double, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                hariLiburDao.insert(HariLibur(nama = nama, tanggalMulai = dari, tanggalSelesai = sampai, hargaPerMalam = harga))
                onResult(true, "Hari libur berhasil ditambahkan")
            } catch (e: Exception) {
                onResult(false, "Gagal menambah hari libur")
            }
        }
    }

    fun updateHariLibur(hariLibur: HariLibur, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                hariLiburDao.update(hariLibur)
                onResult(true, "Hari libur berhasil diupdate")
            } catch (e: Exception) {
                onResult(false, "Gagal update hari libur")
            }
        }
    }

    fun hapusHariLibur(hariLibur: HariLibur) {
        viewModelScope.launch { hariLiburDao.delete(hariLibur) }
    }

    private suspend fun hitungTotalBayar(kamar: Kamar, checkin: String, checkout: String): Double {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d1 = fmt.parse(checkin) ?: return kamar.hargaPerMalam
        val d2 = fmt.parse(checkout) ?: return kamar.hargaPerMalam
        val malam = maxOf(((d2.time - d1.time) / 86400000).toInt(), 1)

        var total = 0.0
        val start = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val liburList = hariLiburDao.getAllSync()

        for (i in 0 until malam) {
            val tanggal = start.plusDays(i.toLong())
            val tanggalStr = tanggal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val libur = liburList.find { l ->
                tanggalStr >= l.tanggalMulai && tanggalStr <= l.tanggalSelesai
            }

            total += if (libur != null) {
                libur.hargaPerMalam
            } else {
                val isWeekend = tanggal.dayOfWeek == DayOfWeek.SATURDAY || tanggal.dayOfWeek == DayOfWeek.SUNDAY
                if (isWeekend) kamar.hargaWeekend else kamar.hargaPerMalam
            }
        }
        return total
    }

    val totalKamar: StateFlow<Int> = kamarDao.count()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTamu: StateFlow<Int> = tamuDao.count()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bookingAktifCount: StateFlow<Int> = bookingDao.countBookingAktif()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendapatanAktif: StateFlow<Double> = bookingDao.totalPendapatanAktif()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _selectedKamarId = MutableStateFlow<Long?>(null)
    val selectedKamarId: StateFlow<Long?> = _selectedKamarId.asStateFlow()

    fun selectKamar(id: Long) { _selectedKamarId.value = id }

    // Kamar
    fun tambahKamar(nomor: String, tipe: String, harga: Double, hargaWeekend: Double = harga, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                kamarDao.insert(Kamar(nomorKamar = nomor, tipeKamar = tipe, hargaPerMalam = harga, hargaWeekend = hargaWeekend))
                onResult(true, "Kamar berhasil ditambahkan")
            } catch (e: Exception) {
                onResult(false, "Nomor kamar sudah ada")
            }
        }
    }

    fun updateKamar(kamar: Kamar, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                kamarDao.update(kamar)
                onResult(true, "Kamar berhasil diupdate")
            } catch (e: Exception) {
                onResult(false, "Gagal update kamar")
            }
        }
    }

    fun hapusKamar(kamar: Kamar) {
        viewModelScope.launch { kamarDao.delete(kamar) }
    }

    // Tamu
    fun tambahTamu(nama: String, kontak: String, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = tamuDao.insert(Tamu(namaLengkap = nama, nomorKontak = kontak))
            onResult(id)
        }
    }

    fun updateTamu(tamu: Tamu, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                tamuDao.update(tamu)
                onResult(true, "Tamu berhasil diupdate")
            } catch (e: Exception) {
                onResult(false, "Gagal update tamu")
            }
        }
    }

    fun hapusTamu(tamu: Tamu) {
        viewModelScope.launch { tamuDao.delete(tamu) }
    }

    // Booking
    fun tambahBooking(
        idKamar: Long, idTamu: Long,
        checkin: String, jamIn: String,
        checkout: String, jamOut: String,
        hargaKhusus: Double? = null,
        catatanHarga: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (bookingDao.hasConflict(idKamar, checkin, jamIn, checkout, jamOut)) {
                val details = bookingDao.getConflictDetails(idKamar, checkin, jamIn, checkout, jamOut)
                var msg = "KAMAR SUDAH TERISI!\n\nBentrok dengan:\n"
                for (d in details) {
                    msg += "  #${d.id} | ${d.tanggalCheckin} ${d.jamCheckin} s/d ${d.tanggalCheckout} ${d.jamCheckout}\n  Tamu: ${d.namaLengkap}\n"
                }
                onResult(false, msg)
                return@launch
            }

            val kamar = kamarDao.getById(idKamar)
            if (kamar == null) {
                onResult(false, "Kamar tidak ditemukan")
                return@launch
            }

            val hargaStandar = hitungTotalBayar(kamar, checkin, checkout)
            val totalBayar = hargaKhusus ?: hargaStandar

            bookingDao.insert(
                Booking(
                    idKamar = idKamar, idTamu = idTamu,
                    tanggalCheckin = checkin, jamCheckin = jamIn,
                    tanggalCheckout = checkout, jamCheckout = jamOut,
                    totalBayar = totalBayar,
                    hargaStandar = hargaStandar,
                    catatanHarga = catatanHarga
                )
            )

            val msg = if (hargaKhusus != null) {
                "Booking berhasil! Harga Khusus: Rp ${String.format("%,.0f", hargaKhusus)} (Standar: Rp ${String.format("%,.0f", hargaStandar)})"
            } else {
                "Booking berhasil! Total: Rp ${String.format("%,.0f", totalBayar)}"
            }
            onResult(true, msg)
        }
    }

    fun selesaikanBooking(id: Long) {
        viewModelScope.launch { bookingDao.updateStatus(id, "selesai") }
    }

    fun batalBooking(id: Long) {
        viewModelScope.launch { bookingDao.updateStatus(id, "dibatalkan") }
    }

    fun editBooking(
        bookingId: Long,
        idKamar: Long, idTamu: Long,
        checkin: String, jamIn: String,
        checkout: String, jamOut: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (bookingDao.hasConflictExclude(idKamar, checkin, jamIn, checkout, jamOut, bookingId)) {
                val details = bookingDao.getConflictDetails(idKamar, checkin, jamIn, checkout, jamOut)
                var msg = "KAMAR SUDAH TERISI!\n\nBentrok dengan:\n"
                for (d in details) {
                    msg += "  #${d.id} | ${d.tanggalCheckin} ${d.jamCheckin} s/d ${d.tanggalCheckout} ${d.jamCheckout}\n  Tamu: ${d.namaLengkap}\n"
                }
                onResult(false, msg)
                return@launch
            }

            val kamar = kamarDao.getById(idKamar)
            if (kamar == null) {
                onResult(false, "Kamar tidak ditemukan")
                return@launch
            }

            val existingBooking = bookingDao.getByIdLengkap(bookingId)
            val total = hitungTotalBayar(kamar, checkin, checkout)

            bookingDao.update(
                Booking(
                    id = bookingId,
                    idKamar = idKamar, idTamu = idTamu,
                    tanggalCheckin = checkin, jamCheckin = jamIn,
                    tanggalCheckout = checkout, jamCheckout = jamOut,
                    totalBayar = existingBooking?.let { if (it.hargaStandar != it.totalBayar) it.totalBayar else total } ?: total,
                    hargaStandar = total,
                    catatanHarga = existingBooking?.catatanHarga ?: "",
                    status = "aktif"
                )
            )
            onResult(true, "Booking berhasil diupdate! Total: Rp ${String.format("%,.0f", total)}")
        }
    }

    fun hapusBooking(id: Long) {
        viewModelScope.launch { bookingDao.deleteById(id) }
    }

    suspend fun getBookingById(id: Long): BookingLengkap? {
        return bookingDao.getByIdLengkap(id)
    }

    suspend fun cekKamarTersedia(idKamar: Long, tanggal: String): Boolean {
        return bookingDao.isKamarAvailable(idKamar, tanggal)
    }

    // Pembayaran
    val semuaPembayaran: StateFlow<List<PembayaranLengkap>> = pembayaranDao.getAllLengkap()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLunas: StateFlow<Double> = pembayaranDao.getTotalLunas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPending: StateFlow<Double> = pembayaranDao.getTotalPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    suspend fun hitungSisaBayar(bookingId: Long): Double {
        val booking = bookingDao.getByIdLengkap(bookingId) ?: return 0.0
        val sudahDibayar = pembayaranDao.getTotalDibayarByBooking(bookingId)
        return booking.totalBayar - sudahDibayar
    }

    fun tambahPembayaran(
        bookingId: Long, jumlah: Double, tanggal: String,
        metode: String, catatan: String,
        tipeBayar: String = "pelunasan", kodeQris: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                pembayaranDao.insert(
                    Pembayaran(
                        bookingId = bookingId, jumlah = jumlah,
                        tanggal = tanggal, metode = metode, catatan = catatan,
                        tipeBayar = tipeBayar, kodeQris = kodeQris
                    )
                )
                onResult(true, "Pembayaran berhasil dicatat")
            } catch (e: Exception) {
                onResult(false, "Gagal mencatat pembayaran")
            }
        }
    }

    fun updateStatusPembayaran(pembayaran: Pembayaran, status: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                pembayaranDao.update(pembayaran.copy(status = status))
                onResult(true, "Status pembayaran diupdate")
            } catch (e: Exception) {
                onResult(false, "Gagal update status")
            }
        }
    }

    fun hapusPembayaran(pembayaran: Pembayaran) {
        viewModelScope.launch { pembayaranDao.delete(pembayaran) }
    }

    fun getPembayaranByBooking(bookingId: Long): Flow<List<PembayaranLengkap>> {
        return pembayaranDao.getByBookingId(bookingId)
    }
}
