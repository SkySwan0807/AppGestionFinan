package com.example.trial.data.repository

import com.example.trial.data.local.dao.ExpenseDao
import com.example.trial.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    // Lectura
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalSpending: Flow<Double?> = expenseDao.getTotalSpending()

    // Escritura
    suspend fun addExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}
