package com.jurysim.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CaseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JurySimDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao

    companion object {
        @Volatile
        private var INSTANCE: JurySimDatabase? = null

        fun getDatabase(context: Context): JurySimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JurySimDatabase::class.java,
                    "jury_sim_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
