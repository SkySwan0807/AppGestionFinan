package com.example.trial.data.local.dao

import androidx.room.*
import com.example.trial.data.local.entities.TipoCuentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoCuentaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: TipoCuentaEntity): Long

    @Update
    suspend fun update(tipo: TipoCuentaEntity)

    @Delete
    suspend fun delete(tipo: TipoCuentaEntity)

    @Query("SELECT * FROM tipo_cuentas ORDER BY nombre ASC")
    fun getAll(): Flow<List<TipoCuentaEntity>>

    @Query("SELECT * FROM tipo_cuentas WHERE idTipo = :id")
    suspend fun getById(id: Int): TipoCuentaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tipos: List<TipoCuentaEntity>)
}
