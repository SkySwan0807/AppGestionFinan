package com.example.trial.data.repository

import kotlinx.coroutines.flow.Flow
import com.example.trial.data.local.entity.ExpenseEntity

interface IExpenseRepository {
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(id: Long)
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getSumByCategory(startTs: Long, endTs: Long): Flow<List<CategorySum>>
    suspend fun getTotalBetween(startTs: Long, endTs: Long): Double
}

data class CategorySum(val category: String, val total: Double)