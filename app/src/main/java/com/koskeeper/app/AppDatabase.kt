package com.koskeeper.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Kamar::class, Tamu::class, Booking::class, HariLibur::class, Pembayaran::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kamarDao(): KamarDao
    abstract fun tamuDao(): TamuDao
    abstract fun bookingDao(): BookingDao
    abstract fun hariLiburDao(): HariLiburDao
    abstract fun pembayaranDao(): PembayaranDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "koskeeper.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
