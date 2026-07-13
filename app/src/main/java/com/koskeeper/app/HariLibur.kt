package com.koskeeper.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hari_libur")
data class HariLibur(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val hargaPerMalam: Double
)
