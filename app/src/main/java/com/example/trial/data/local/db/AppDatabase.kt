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
    version = 6, // Incrementa la versión para forzar recreación
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun cuentaDao(): CuentaDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun tipoCuentaDao(): TipoCuentaDao
    abstract fun estadoDao(): EstadoDao
    abstract fun metaAhorroDao(): MetaAhorroDao

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
                    .fallbackToDestructiveMigration() // Esto borra y recrea la BD
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Se ejecuta cuando la BD se crea por primera vez
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prepopulate(database)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Verificar y poblar datos si es necesario al abrir la BD
                            CoroutineScope(Dispatchers.IO).launch {
                                checkAndPopulateData(context)
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulate(db: AppDatabase) {
            println("📦 PREPOBLANDO BASE DE DATOS...")
            insertEssentialData(db)
        }

        private suspend fun checkAndPopulateData(context: Context) {
            val db = getDatabase(context)
            val categoriasCount = db.categoriaDao().getCount()
            println("📊 Categorías en BD: $categoriasCount")

            if (categoriasCount == 0) {
                println("🔄 No hay categorías, insertando datos esenciales...")
                insertEssentialData(db)
            }
        }

        private suspend fun insertEssentialData(db: AppDatabase) {
            try {
                println("🎯 INSERTANDO DATOS ESENCIALES...")

                // --- Categorías ---
                val categorias = listOf(
                    CategoriaEntity(nombre = "Alimentación", descripcion = "Gastos de comida"),
                    CategoriaEntity(nombre = "Transporte", descripcion = "Movilidad y transporte"),
                    CategoriaEntity(nombre = "Servicios", descripcion = "Pagos de servicios básicos"),
                    CategoriaEntity(nombre = "Ocio", descripcion = "Entretenimiento y recreación"),
                    CategoriaEntity(nombre = "Otros", descripcion = "Gastos varios"),
                    CategoriaEntity(nombre = "Ingreso", descripcion = "Ingresos varios")
                )

                db.categoriaDao().insertAll(categorias)
                println("✅ Categorías insertadas: ${categorias.size}")

                // --- Estados ---
                val estados = listOf(
                    EstadoEntity(nombre = "Activo", descripcion = "Meta en progreso"),
                    EstadoEntity(nombre = "Completado", descripcion = "Meta finalizada"),
                    EstadoEntity(nombre = "Cancelado", descripcion = "Meta cancelada"),
                    EstadoEntity(
                        nombre = "No cumplido",
                        descripcion = "Meta finalizada sin alcanzar el objetivo dentro del tiempo establecido"
                    )
                )
                db.estadoDao().insertAll(estados)
                println("✅ Estados insertados: ${estados.size}")

                // --- Tipos de Cuenta ---
                val tiposCuenta = listOf(
                    TipoCuentaEntity(nombre = "Ahorro", descripcion = "Cuenta de ahorros"),
                    TipoCuentaEntity(nombre = "Corriente", descripcion = "Cuenta corriente"),
                    TipoCuentaEntity(nombre = "Efectivo", descripcion = "Dinero en efectivo"),
                    TipoCuentaEntity(nombre = "Tarjeta de crédito", descripcion = "Crédito bancario")
                )
                db.tipoCuentaDao().insertAll(tiposCuenta)
                println("✅ Tipos de cuenta insertados: ${tiposCuenta.size}")

                // --- Cuenta inicial ---
                val cuentasCount = db.cuentaDao().getCount()
                if (cuentasCount == 0) {
                    db.cuentaDao().insertCuenta(
                        CuentaEntity(idTipo = 3, nombre = "Dinero en Efectivo", descripcion = null, balance = 0.0)
                    )
                    println("✅ Cuenta inicial insertada")
                }

                println("🎉 PREPOBLACIÓN COMPLETADA EXITOSAMENTE")

            } catch (e: Exception) {
                println("❌ ERROR en prepoblación: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}