package com.example.trial.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trial.data.local.dao.CategoriaDao
import com.example.trial.data.local.dao.CuentaDao
import com.example.trial.data.local.dao.TransaccionDao
import com.example.trial.data.local.entities.*

@Database(
    entities = [
        CategoriaEntity::class,
        TipoCuentaEntity::class,
        CuentaEntity::class,
        EstadoEntity::class,
        MetaAhorroEntity::class,
        TransaccionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun transaccionDao(): TransaccionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finanzas_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
