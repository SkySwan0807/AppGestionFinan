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

    @Query("SELECT SUM(monto) FROM transacciones WHERE idCuenta = :idCuenta")
    fun getTotalPorCuenta(idCuenta: Int): Flow<Double?>
}