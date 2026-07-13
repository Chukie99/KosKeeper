package com.koskeeper.app

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pembayaran",
    foreignKeys = [
        ForeignKey(
            entity = Booking::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pembayaran(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val jumlah: Double,
    val tanggal: String,
    val metode: String,
    val catatan: String = "",
    val status: String = "lunas"
)
