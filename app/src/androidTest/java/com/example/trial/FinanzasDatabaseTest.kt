package com.example.trial

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.trial.data.local.AppDatabase
import com.example.trial.data.local.dao.CategoriaDao
import com.example.trial.data.local.dao.CuentaDao
import com.example.trial.data.local.dao.TransaccionDao
import com.example.trial.data.local.entities.CategoriaEntity
import com.example.trial.data.local.entities.CuentaEntity
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FinanzasDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var categoriaDao: CategoriaDao
    private lateinit var cuentaDao: CuentaDao
    private lateinit var transaccionDao: TransaccionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // solo para testing
            .build()

        categoriaDao = db.categoriaDao()
        cuentaDao = db.cuentaDao()
        transaccionDao = db.transaccionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadData() = runBlocking {
        // 1. Insertar categoría
        val categoria = CategoriaEntity(
            nombre = "Comida",
            descripcion = "Gastos de alimentación"
        )
        categoriaDao.insertCategoria(categoria)

        val categorias = categoriaDao.getAllCategorias().first()
        assertEquals(1, categorias.size)
        val categoriaInsertada = categorias[0]

        // 2. Insertar cuenta
        val cuenta = CuentaEntity(
            idTipo = 1, // de momento un tipo ficticio
            nombre = "Efectivo",
            descripcion = "Cartera",
            balance = 0.0
        )
        cuentaDao.insertCuenta(cuenta)
        val cuentas = cuentaDao.getAllCuentas().first()
        assertEquals(1, cuentas.size)
        val cuentaInsertada = cuentas[0]

        // 3. Insertar transacción
        val transaccion = TransaccionEntity(
            idCategoria = categoriaInsertada.idCategoria,
            idCuenta = cuentaInsertada.idCuenta,
            monto = 30.0,
            fecha = System.currentTimeMillis(),
            descripcion = "Almuerzo"
        )
        transaccionDao.insertTransaccion(transaccion)

        val transacciones = transaccionDao.getAllTransacciones().first()
        assertEquals(1, transacciones.size)
        assertEquals(30.0, transacciones[0].monto, 0.0)
    }
}
