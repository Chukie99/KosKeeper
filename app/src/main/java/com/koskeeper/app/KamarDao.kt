package com.koskeeper.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KamarDao {
    @Insert
    suspend fun insert(kamar: Kamar): Long

    @Update
    suspend fun update(kamar: Kamar)

    @Delete
    suspend fun delete(kamar: Kamar)

    @Query("SELECT * FROM kamar ORDER BY nomorKamar")
    fun getAll(): Flow<List<Kamar>>

    @Query("SELECT * FROM kamar WHERE id = :id")
    suspend fun getById(id: Long): Kamar?

    @Query("SELECT COUNT(*) FROM kamar")
    fun count(): Flow<Int>
}
