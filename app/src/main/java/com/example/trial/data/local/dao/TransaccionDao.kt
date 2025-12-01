package com.example.trial.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransaccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaccion(transaccion: TransaccionEntity)

    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun getAllTransacciones(): Flow<List<TransaccionEntity>>


    @Query("SELECT SUM(monto) FROM transacciones")
    fun totalSpending(): Flow<Double?>

    @Query("SELECT SUM(monto) FROM transacciones WHERE borradoLogico = 0")
    fun totalSpendingActivas(): Flow<Double?>


    @Query("SELECT SUM(monto) FROM transacciones WHERE idCuenta = :idCuenta")
    fun getTotalPorCuenta(idCuenta: Int): Flow<Double?>

    @Query("SELECT SUM(monto) FROM transacciones WHERE idCategoria = :idCategoria AND fecha BETWEEN :start AND :end")
    suspend fun getTotalPorCategoriaYPeriodo(idCategoria: Int, start: Long, end: Long): Double?

    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :start AND :end ORDER BY fecha DESC")
    fun getTransfersBetween(start: Long, end: Long): Flow<List<TransaccionEntity>>

    @Query("SELECT SUM(monto) FROM transacciones WHERE borradoLogico = 0 AND fecha BETWEEN :start AND :end")
    suspend fun getTotalBetween(start: Long, end: Long): Double?


    @Query("SELECT c.idCategoria, c.nombre AS category, SUM(t.monto) as total FROM transacciones AS t " +
            "INNER JOIN categorias AS c ON t.idCategoria = c.idCategoria " +
            "WHERE fecha BETWEEN :start AND :end GROUP BY c.nombre")
    fun getSumByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    //
    @Query("UPDATE transacciones SET borradoLogico = 1, fechaBorrado = :fechaActual WHERE idTransaccion = :id")
    suspend fun softDelete(id: Int, fechaActual: Long): Int

    @Query("SELECT * FROM transacciones WHERE borradoLogico = 0 ORDER BY fecha DESC")
    fun getAllTransaccionesActivas(): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE borradoLogico = 0 AND fecha BETWEEN :start AND :end ORDER BY fecha DESC")
    fun getTransfersBetweenActivas(start: Long, end: Long): Flow<List<TransaccionEntity>>

    @Query("""
    SELECT c.idCategoria, c.nombre AS category, SUM(t.monto) as total 
    FROM transacciones AS t 
    INNER JOIN categorias AS c ON t.idCategoria = c.idCategoria 
    WHERE t.borradoLogico = 0 AND t.fecha BETWEEN :start AND :end 
    GROUP BY c.nombre
""")
    fun getSumByCategoryActivas(start: Long, end: Long): Flow<List<CategorySum>>

}