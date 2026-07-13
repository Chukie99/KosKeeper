package com.koskeeper.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tamu")
data class Tamu(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val namaLengkap: String,
    val nomorKontak: String
)
