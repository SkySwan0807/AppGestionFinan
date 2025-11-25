package com.example.trial

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.trial.data.local.db.AppDatabase
import com.example.trial.data.local.dao.ExpenseDao
import com.example.trial.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ExpenseDatabaseTest {
    private lateinit var expenseDao: ExpenseDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Usamos inMemoryDatabaseBuilder porque no queremos guardar datos reales en el disco, solo probar lógica.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Permitir queries en el hilo principal solo para TEST
            .build()
        expenseDao = db.expenseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val expense = ExpenseEntity(
            title = "Coca Cola",
            amount = 12.50,
            category = "Bebidas",
            date = System.currentTimeMillis()
        )

        expenseDao.insertExpense(expense)

        // Leemos el primer valor que emite el Flow
        val allExpenses = expenseDao.getAllExpenses().first()

        assertEquals(allExpenses[0].title, "Coca Cola")
        assertEquals(allExpenses[0].amount, 12.50, 0.0)
    }
}
