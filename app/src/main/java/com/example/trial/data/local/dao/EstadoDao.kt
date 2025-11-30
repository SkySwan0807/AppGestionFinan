package com.example.trial.data.local.dao

import androidx.room.*
import com.example.trial.data.local.entities.EstadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(estado: EstadoEntity): Long

    @Update
    suspend fun update(estado: EstadoEntity)

    @Delete
    suspend fun delete(estado: EstadoEntity)

    @Query("SELECT COUNT(*) FROM estados")
    suspend fun getCount(): Int

    @Query("SELECT * FROM estados ORDER BY nombre ASC")
    fun getAll(): Flow<List<EstadoEntity>>

    @Query("SELECT * FROM estados WHERE idEstado = :id")
    suspend fun getById(id: Int): EstadoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(estados: List<EstadoEntity>)
}
