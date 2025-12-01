package com.example.trial.data.local.dao

import androidx.room.*
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    // ----------------------
    // Insert, Update, Delete
    // ----------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaccion(transaccion: TransaccionEntity)

    @Update
    suspend fun updateTransaccion(transaccion: TransaccionEntity)

    @Delete
    suspend fun deleteTransaccion(transaccion: TransaccionEntity)

    // ----------------------
    // Consultas básicas
    // ----------------------
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun getAllTransacciones(): Flow<List<TransaccionEntity>>

    @Query("SELECT SUM(monto) FROM transacciones")
    fun totalSpending(): Flow<Double?>

    @Query("SELECT SUM(monto) FROM transacciones WHERE idCuenta = :idCuenta")
    fun getTotalPorCuenta(idCuenta: Int): Flow<Double?>

    // ----------------------
    // Consultas por categoría y periodo
    // ----------------------
    @Query("SELECT SUM(monto) FROM transacciones WHERE idCategoria = :idCategoria AND fecha BETWEEN :start AND :end")
    suspend fun getTotalPorCategoriaYPeriodo(idCategoria: Int, start: Long, end: Long): Double?

    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :start AND :end ORDER BY fecha DESC")
    fun getTransfersBetween(start: Long, end: Long): Flow<List<TransaccionEntity>>

    @Query("SELECT SUM(monto) FROM transacciones WHERE fecha BETWEEN :start AND :end")
    suspend fun getTotalBetween(start: Long, end: Long): Double?

    @Query(
        "SELECT c.idCategoria, c.nombre AS category, SUM(t.monto) as total " +
                "FROM transacciones AS t " +
                "INNER JOIN categorias AS c ON t.idCategoria = c.idCategoria " +
                "WHERE fecha BETWEEN :start AND :end " +
                "GROUP BY c.nombre"
    )
    fun getSumByCategory(start: Long, end: Long): Flow<List<CategorySum>>
}
