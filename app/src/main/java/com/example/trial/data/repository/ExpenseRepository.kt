package com.example.trial.data.repository

import com.example.trial.data.local.dao.ExpenseDao
import com.example.trial.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository private constructor(private val dao: ExpenseDao): IExpenseRepository {

    override suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insert(expense)
    }

    override suspend fun deleteExpense(id: Long) {
        dao.deleteById(id)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAll()

    override fun getSumByCategory(startTs: Long, endTs: Long): Flow<List<CategorySum>> = dao.getSumByCategory(startTs, endTs)

    override suspend fun getTotalBetween(startTs: Long, endTs: Long): Double {
        // simple impl: sum the flow once
        val list = dao.getAll() // can't directly collect here; implement in real repo
        return 0.0 // TODO: implement in concrete repository
    }

    companion object {
        @Volatile private var instance: ExpenseRepository? = null

        fun getInstance(dao: ExpenseDao): ExpenseRepository =
            instance ?: synchronized(this) {
                instance ?: ExpenseRepository(dao).also { instance = it }
            }
    }
}