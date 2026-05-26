package com.example.skinsmart.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ShelfProduct::class, LocalUser::class], version = 3, exportSchema = false)
abstract class SkinSmartDatabase : RoomDatabase() {

    abstract fun skinSmartDao(): SkinSmartDao

    companion object {
        @Volatile
        private var INSTANCE: SkinSmartDatabase? = null

        fun getDatabase(context: Context): SkinSmartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkinSmartDatabase::class.java,
                    "skinsmart_local_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
