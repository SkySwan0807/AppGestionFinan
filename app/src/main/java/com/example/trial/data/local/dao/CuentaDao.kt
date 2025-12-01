package com.example.trial.data.local.dao

import androidx.room.*
import com.example.trial.data.local.entities.CuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuenta(cuenta: CuentaEntity)

    @Query("SELECT COUNT(*) FROM cuentas")
    suspend fun getCount(): Int

    @Update
    suspend fun updateCuenta(cuenta: CuentaEntity)

    @Query("SELECT * FROM cuentas ORDER BY nombre ASC")
    fun getAllCuentas(): Flow<List<CuentaEntity>>

    @Query("UPDATE cuentas SET balance = :nuevoBalance WHERE idCuenta = :idCuenta")
    suspend fun actualizarBalance(idCuenta: Int, nuevoBalance: Double)
}