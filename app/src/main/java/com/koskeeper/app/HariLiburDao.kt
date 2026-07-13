package com.koskeeper.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HariLiburDao {
    @Insert
    suspend fun insert(hariLibur: HariLibur): Long

    @Update
    suspend fun update(hariLibur: HariLibur)

    @Delete
    suspend fun delete(hariLibur: HariLibur)

    @Query("SELECT * FROM hari_libur ORDER BY tanggalMulai")
    fun getAll(): Flow<List<HariLibur>>

    @Query("SELECT * FROM hari_libur")
    suspend fun getAllSync(): List<HariLibur>
}
