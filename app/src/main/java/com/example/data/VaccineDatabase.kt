package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChildEntity::class, VaccineRecordEntity::class], version = 1, exportSchema = false)
abstract class VaccineDatabase : RoomDatabase() {
    abstract fun vaccineDao(): VaccineDao

    companion object {
        @Volatile
        private var INSTANCE: VaccineDatabase? = null

        fun getDatabase(context: Context): VaccineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaccineDatabase::class.java,
                    "vaccine_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
