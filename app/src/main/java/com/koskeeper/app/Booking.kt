package com.koskeeper.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "booking",
    foreignKeys = [
        ForeignKey(entity = Kamar::class, parentColumns = ["id"], childColumns = ["idKamar"]),
        ForeignKey(entity = Tamu::class, parentColumns = ["id"], childColumns = ["idTamu"])
    ]
)
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idKamar: Long,
    val idTamu: Long,
    val tanggalCheckin: String,
    val jamCheckin: String,
    val tanggalCheckout: String,
    val jamCheckout: String,
    val totalBayar: Double,
    val status: String = "aktif"
)
