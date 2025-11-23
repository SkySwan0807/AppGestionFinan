package com.example.trial.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.trial.data.local.entity.ExpenseEntity

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT category as category, SUM(amount) as total FROM expenses WHERE date BETWEEN :start AND :end GROUP BY category")
    fun getSumByCategory(start: Long, end: Long): Flow<List<com.example.trial.data.repository.CategorySum>>
}