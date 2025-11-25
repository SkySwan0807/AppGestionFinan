package com.example.trial.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trial.data.local.dao.ExpenseDao
import com.example.trial.data.local.entities.ExpenseEntity

// 1. Defines las entidades y la versión
@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. Expones los DAOs
    abstract fun expenseDao(): ExpenseDao

    // 3. El Singleton
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la retornamos
            return INSTANCE ?: synchronized(this) {
                // Si no existe, la creamos (bloqueando otros hilos para evitar duplicados)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db" // Nombre del archivo físico
                )
                    .fallbackToDestructiveMigration() // ⚠️ Borra datos si cambias la versión (útil en desarrollo)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}