package com.koskeeper.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class BookingLengkap(
    val id: Long,
    val tanggalCheckin: String,
    val jamCheckin: String,
    val tanggalCheckout: String,
    val jamCheckout: String,
    val totalBayar: Double,
    val hargaStandar: Double,
    val catatanHarga: String,
    val status: String,
    val nomorKamar: String,
    val tipeKamar: String,
    val namaLengkap: String,
    val nomorKontak: String
)

data class LaporanBulanan(
    val bulan: String,
    val totalTransaksi: Int,
    val totalPendapatan: Double
)

data class BookingDetailLaporan(
    val id: Long,
    val tanggalCheckin: String,
    val jamCheckin: String,
    val tanggalCheckout: String,
    val jamCheckout: String,
    val totalBayar: Double,
    val hargaStandar: Double,
    val catatanHarga: String,
    val status: String,
    val namaLengkap: String,
    val nomorKamar: String,
    val tipeKamar: String
)

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: Booking): Long

    @Update
    suspend fun update(booking: Booking)

    @Query("UPDATE booking SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, k.nomorKamar, k.tipeKamar, t.namaLengkap, t.nomorKontak
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        ORDER BY b.tanggalCheckin DESC
    """)
    fun getAllLengkap(): Flow<List<BookingLengkap>>

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, k.nomorKamar, k.tipeKamar, t.namaLengkap, t.nomorKontak
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        WHERE b.status = 'aktif'
        ORDER BY b.tanggalCheckin
    """)
    fun getActiveLengkap(): Flow<List<BookingLengkap>>

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, k.nomorKamar, k.tipeKamar, t.namaLengkap, t.nomorKontak
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        WHERE b.status = 'aktif'
        ORDER BY b.tanggalCheckin
    """)
    suspend fun getActiveLengkapSync(): List<BookingLengkap>

    @Query("""
        SELECT COUNT(*) > 0 FROM booking
        WHERE idKamar = :idKamar AND status = 'aktif'
        AND NOT (
            tanggalCheckout < :checkin
            OR (tanggalCheckout = :checkin AND jamCheckout <= :jamCheckin)
            OR tanggalCheckin > :checkout
            OR (tanggalCheckin = :checkout AND jamCheckin >= :jamCheckout)
        )
    """)
    suspend fun hasConflict(idKamar: Long, checkin: String, jamCheckin: String, checkout: String, jamCheckout: String): Boolean

    @Query("""
        SELECT COUNT(*) > 0 FROM booking
        WHERE idKamar = :idKamar AND status = 'aktif' AND id != :excludeId
        AND NOT (
            tanggalCheckout < :checkin
            OR (tanggalCheckout = :checkin AND jamCheckout <= :jamCheckin)
            OR tanggalCheckin > :checkout
            OR (tanggalCheckin = :checkout AND jamCheckin >= :jamCheckout)
        )
    """)
    suspend fun hasConflictExclude(idKamar: Long, checkin: String, jamCheckin: String, checkout: String, jamCheckout: String, excludeId: Long): Boolean

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, k.nomorKamar, k.tipeKamar, t.namaLengkap, t.nomorKontak
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        WHERE b.idKamar = :idKamar AND b.status = 'aktif'
        AND NOT (
            b.tanggalCheckout < :checkin
            OR (b.tanggalCheckout = :checkin AND b.jamCheckout <= :jamCheckin)
            OR b.tanggalCheckin > :checkout
            OR (b.tanggalCheckin = :checkout AND b.jamCheckin >= :jamCheckout)
        )
    """)
    suspend fun getConflictDetails(idKamar: Long, checkin: String, jamCheckin: String, checkout: String, jamCheckout: String): List<BookingLengkap>

    @Query("""
        SELECT COUNT(*) = 0 FROM booking
        WHERE idKamar = :idKamar AND status = 'aktif'
        AND tanggalCheckin <= :tanggal AND tanggalCheckout >= :tanggal
    """)
    suspend fun isKamarAvailable(idKamar: Long, tanggal: String): Boolean

    @Query("""
        SELECT substr(tanggalCheckin, 1, 7) AS bulan,
               COUNT(*) AS totalTransaksi,
               COALESCE(SUM(totalBayar), 0) AS totalPendapatan
        FROM booking
        WHERE status != 'dibatalkan'
        GROUP BY bulan
        ORDER BY bulan DESC
    """)
    fun getLaporanBulanan(): Flow<List<LaporanBulanan>>

    @Query("SELECT COUNT(*) FROM booking WHERE status = 'aktif'")
    fun countBookingAktif(): Flow<Int>

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, k.nomorKamar, k.tipeKamar, t.namaLengkap, t.nomorKontak
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        WHERE b.id = :id
    """)
    suspend fun getByIdLengkap(id: Long): BookingLengkap?

    @Query("DELETE FROM booking WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT b.id, b.tanggalCheckin, b.jamCheckin, b.tanggalCheckout, b.jamCheckout,
               b.totalBayar, b.hargaStandar, b.catatanHarga, b.status, t.namaLengkap, k.nomorKamar, k.tipeKamar
        FROM booking b
        JOIN kamar k ON b.idKamar = k.id
        JOIN tamu t ON b.idTamu = t.id
        WHERE b.status != 'dibatalkan'
        AND b.tanggalCheckin >= :dariTanggal
        AND b.tanggalCheckin <= :sampaiTanggal
        ORDER BY b.tanggalCheckin DESC
    """)
    fun getLaporanDetail(dariTanggal: String, sampaiTanggal: String): Flow<List<BookingDetailLaporan>>

    @Query("SELECT COALESCE(SUM(totalBayar), 0) FROM booking WHERE status = 'aktif'")
    fun totalPendapatanAktif(): Flow<Double>
}
