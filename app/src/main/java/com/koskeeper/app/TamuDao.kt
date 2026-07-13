package com.koskeeper.app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TamuDao {
    @Insert
    suspend fun insert(tamu: Tamu): Long

    @Update
    suspend fun update(tamu: Tamu)

    @Delete
    suspend fun delete(tamu: Tamu)

    @Query("SELECT * FROM tamu ORDER BY namaLengkap")
    fun getAll(): Flow<List<Tamu>>

    @Query("SELECT * FROM tamu WHERE id = :id")
    suspend fun getById(id: Long): Tamu?

    @Query("SELECT * FROM tamu WHERE namaLengkap LIKE '%' || :kw || '%' OR nomorKontak LIKE '%' || :kw || '%'")
    fun search(kw: String): Flow<List<Tamu>>

    @Query("SELECT COUNT(*) FROM tamu")
    fun count(): Flow<Int>
}
