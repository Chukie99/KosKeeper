package com.koskeeper.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class PembayaranLengkap(
    val id: Long,
    val bookingId: Long,
    val jumlah: Double,
    val tanggal: String,
    val metode: String,
    val catatan: String,
    val status: String,
    val namaTamu: String,
    val nomorKamar: String
)

@Dao
interface PembayaranDao {
    @Query("""
        SELECT p.id, p.bookingId, p.jumlah, p.tanggal, p.metode, p.catatan, p.status,
               t.namaLengkap as namaTamu, k.nomorKamar as nomorKamar
        FROM pembayaran p
        INNER JOIN booking b ON p.bookingId = b.id
        INNER JOIN tamu t ON b.idTamu = t.id
        INNER JOIN kamar k ON b.idKamar = k.id
        ORDER BY p.tanggal DESC
    """)
    fun getAllLengkap(): Flow<List<PembayaranLengkap>>

    @Query("""
        SELECT p.id, p.bookingId, p.jumlah, p.tanggal, p.metode, p.catatan, p.status,
               t.namaLengkap as namaTamu, k.nomorKamar as nomorKamar
        FROM pembayaran p
        INNER JOIN booking b ON p.bookingId = b.id
        INNER JOIN tamu t ON b.idTamu = t.id
        INNER JOIN kamar k ON b.idKamar = k.id
        WHERE p.bookingId = :bookingId
        ORDER BY p.tanggal DESC
    """)
    fun getByBookingId(bookingId: Long): Flow<List<PembayaranLengkap>>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM pembayaran WHERE status = 'lunas'")
    fun getTotalLunas(): Flow<Double>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM pembayaran WHERE status = 'pending'")
    fun getTotalPending(): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(jumlah), 0) FROM pembayaran
        WHERE status = 'lunas' AND tanggal BETWEEN :dari AND :sampai
    """)
    fun getTotalLunasByTanggal(dari: String, sampai: String): Flow<Double>

    @Insert
    suspend fun insert(pembayaran: Pembayaran): Long

    @Update
    suspend fun update(pembayaran: Pembayaran)

    @Delete
    suspend fun delete(pembayaran: Pembayaran)
}
