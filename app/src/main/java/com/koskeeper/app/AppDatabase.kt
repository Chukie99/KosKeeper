package com.koskeeper.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Kamar::class, Tamu::class, Booking::class, HariLibur::class, Pembayaran::class],
    version = 5,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE booking ADD COLUMN hargaStandar REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE booking ADD COLUMN catatanHarga TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pembayaran ADD COLUMN tipeBayar TEXT NOT NULL DEFAULT 'pelunasan'")
                db.execSQL("ALTER TABLE pembayaran ADD COLUMN kodeQris TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "koskeeper.db"
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
