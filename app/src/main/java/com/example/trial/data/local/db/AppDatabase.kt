package com.example.trial.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trial.data.local.dao.*
import com.example.trial.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    abstract fun tipoCuentaDao(): TipoCuentaDao
    abstract fun estadoDao(): EstadoDao

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
                    .addCallback(prepopulateCallback())   // 👈 IMPORTANTE
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // --------------------------------------------------------
        //      CALLBACK QUE INSERTA DATOS AL CREAR LA BD
        // --------------------------------------------------------
        private fun prepopulateCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    INSTANCE?.let { database ->
                        CoroutineScope(Dispatchers.IO).launch {
                            prepopulate(database)
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------
        //              DATOS INICIALES DE CADA TABLA
        // --------------------------------------------------------
        private suspend fun prepopulate(db: AppDatabase) {

            // --- Categorías ---
            db.categoriaDao().insertAll(
                listOf(
                    CategoriaEntity(nombre = "Alimentación", descripcion = "Gastos de comida"),
                    CategoriaEntity(nombre = "Transporte", descripcion = "Movilidad y transporte"),
                    CategoriaEntity(nombre = "Servicios", descripcion = "Pagos de servicios básicos"),
                    CategoriaEntity(nombre = "Ocio", descripcion = "Entretenimiento y recreación"),
                    CategoriaEntity(nombre = "Otros", descripcion = "Gastos varios")
                )
            )

            // --- Estados ---
            db.estadoDao().insertAll(
                listOf(
                    EstadoEntity(nombre = "Activo", descripcion = "Meta en progreso"),
                    EstadoEntity(nombre = "Completado", descripcion = "Meta finalizada"),
                    EstadoEntity(nombre = "Cancelado", descripcion = "Meta cancelada"),
                    EstadoEntity(
                        nombre = "No cumplido",
                        descripcion = "Meta finalizada sin alcanzar el objetivo dentro del tiempo establecido"
                    )
                )
            )

            // --- Tipos de Cuenta ---
            db.tipoCuentaDao().insertAll(
                listOf(
                    TipoCuentaEntity(nombre = "Ahorro", descripcion = "Cuenta de ahorros"),
                    TipoCuentaEntity(nombre = "Corriente", descripcion = "Cuenta corriente"),
                    TipoCuentaEntity(nombre = "Efectivo", descripcion = "Dinero en efectivo"),
                    TipoCuentaEntity(nombre = "Tarjeta de crédito", descripcion = "Crédito bancario")
                )
            )
        }
    }
}
