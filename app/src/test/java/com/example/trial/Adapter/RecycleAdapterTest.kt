package com.example.trial.adapter

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import com.example.trial.data.local.entities.ExpenseEntity
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class RecycleAdapterTest {

    @Test
    fun `adapter should return correct item count`() {
        // Given
        val expenses = listOf(
            ExpenseEntity(title = "Almuerzo", amount = 50.0, category = "Comida", date = System.currentTimeMillis()),
            ExpenseEntity(title = "Transporte", amount = 10.0, category = "Movilidad", date = System.currentTimeMillis())
        )
        val adapter = RecycleAdapter(expenses)

        // When
        val itemCount = adapter.itemCount

        // Then
        assertEquals(2, itemCount)
    }

    @Test
    fun `adapter should return correct item at position`() {
        // Given
        val expectedExpense = ExpenseEntity(title = "Test", amount = 100.0, category = "Otros", date = System.currentTimeMillis())
        val expenses = listOf(expectedExpense)
        val adapter = RecycleAdapter(expenses)

        // When
        val actualExpense = adapter.getItem(0)

        // Then
        assertEquals(expectedExpense.title, actualExpense.title)
        assertEquals(expectedExpense.amount, actualExpense.amount, 0.01)
    }

    @Test
    fun `updateData should refresh adapter items`() {
        // Given
        val initialExpenses = listOf(ExpenseEntity(title = "Old", amount = 10.0, category = "Test", date = System.currentTimeMillis()))
        val adapter = RecycleAdapter(initialExpenses)

        val newExpenses = listOf(
            ExpenseEntity(title = "New1", amount = 20.0, category = "Test", date = System.currentTimeMillis()),
            ExpenseEntity(title = "New2", amount = 30.0, category = "Test", date = System.currentTimeMillis())
        )

        // When
        adapter.updateData(newExpenses)

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals("New1", adapter.getItem(0).title)
    }
}