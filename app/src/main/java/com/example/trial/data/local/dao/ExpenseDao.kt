package com.example.trial.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trial.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Operaciones de Escritura (llevan 'suspend' para no congelar la pantalla)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Operaciones de Lectura (retornan Flow para actualizaciones en tiempo real)
    @Query("SELECT * FROM expenses ORDER BY date_timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    // Requerimiento específico: "Sumar gastos"
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpending(): Flow<Double?>
    // Devuelve Double? (nullable) porque si la tabla está vacía, la suma es null
}