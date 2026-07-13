package com.koskeeper.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kamar")
data class Kamar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomorKamar: String,
    val tipeKamar: String,
    val hargaPerMalam: Double,
    @ColumnInfo(defaultValue = "0.0") val hargaWeekend: Double = hargaPerMalam
)
