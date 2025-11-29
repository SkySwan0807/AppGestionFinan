package com.example.trial.data.local.dao

import androidx.room.*
import com.example.trial.data.local.entities.MetaAhorroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaAhorroDao {

    // Insertar una meta (devuelve el id recién generado)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: MetaAhorroEntity): Long

    // Actualizar una meta
    @Update
    suspend fun update(meta: MetaAhorroEntity)

    // Eliminar una meta
    @Delete
    suspend fun delete(meta: MetaAhorroEntity)

    // Obtener una meta por ID
    @Query("SELECT * FROM metas_ahorro WHERE idMeta = :id")
    suspend fun getById(id: Int): MetaAhorroEntity?

    // Obtener todas las metas
    @Query("SELECT * FROM metas_ahorro ORDER BY fechaObjetivo ASC")
    fun getAll(): Flow<List<MetaAhorroEntity>>

    // Obtener metas activas según idEstado
    @Query("SELECT * FROM metas_ahorro WHERE idEstado = :estado ORDER BY fechaObjetivo ASC")
    fun getByEstado(estado: Int): Flow<List<MetaAhorroEntity>>

    @Query("SELECT * FROM metas_ahorro WHERE idCategoria = :categoryId AND idEstado = 0")
    suspend fun getActiveMetaByCategory(categoryId: Int): MetaAhorroEntity?

    @Query("SELECT * FROM metas_ahorro WHERE idEstado = 1 ORDER BY fechaObjetivo ASC")
    fun getActiveMetas(): Flow<List<MetaAhorroEntity>>

    // Actualizar el montoActual de una meta
    @Query("UPDATE metas_ahorro SET montoActual = :nuevoMonto WHERE idMeta = :idMeta")
    suspend fun actualizarMonto(idMeta: Int, nuevoMonto: Double)

    @Query("UPDATE metas_ahorro SET idEstado = 1 WHERE idMeta = :idMeta")
    suspend fun marcarComoCompletada(idMeta: Int)

    // Obtener metas cuya fecha objetivo no pasó
    @Query("SELECT * FROM metas_ahorro WHERE fechaObjetivo >= :fechaActual ORDER BY fechaObjetivo ASC")
    fun getMetasVigentes(fechaActual: Long): Flow<List<MetaAhorroEntity>>
}
