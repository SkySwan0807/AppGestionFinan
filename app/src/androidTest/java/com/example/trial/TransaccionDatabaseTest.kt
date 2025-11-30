package com.example.trial

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.trial.data.local.db.AppDatabase
import com.example.trial.data.local.dao.TransaccionDao
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class TransaccionDatabaseTest {
    private lateinit var transaccionDao: TransaccionDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transaccionDao = db.transaccionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeTransaccionAndRead() = runBlocking {
        val transaccion = TransaccionEntity(
            monto = -12.5,
            idCategoria = 1,
            descripcion = "Coca Cola",
            fecha = System.currentTimeMillis(),
            idCuenta = 0
        )

        transaccionDao.insertTransaccion(transaccion)

        val allTransacciones = transaccionDao.getAllTransacciones().first()

        assertEquals(1, allTransacciones.size)
        assertEquals(-12.5, allTransacciones[0].monto, 0.0)
        assertEquals(1, allTransacciones[0].idCategoria)
        assertEquals("Coca Cola", allTransacciones[0].descripcion)
    }
}
