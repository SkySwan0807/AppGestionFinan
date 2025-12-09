package com.example.trial.data.repository

import com.example.trial.data.local.dao.MetaAhorroDao
import com.example.trial.data.local.entities.MetaAhorroEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetaAhorroRepository @Inject constructor(
    private val dao: MetaAhorroDao
) {

    suspend fun insert(meta: MetaAhorroEntity): Long {
        return dao.insert(meta)
    }

    suspend fun update(meta: MetaAhorroEntity) {
        dao.update(meta)
    }

    suspend fun delete(meta: MetaAhorroEntity) {
        dao.delete(meta)
    }

    suspend fun getById(id: Int): MetaAhorroEntity? {
        return dao.getById(id)
    }

    fun getAll(): Flow<List<MetaAhorroEntity>> {
        return dao.getAll()
    }

    fun getByEstado(estado: Int): Flow<List<MetaAhorroEntity>> {
        return dao.getByEstado(estado)
    }

    suspend fun getActiveMetaByCategory(categoryId: Int): MetaAhorroEntity? {
        return dao.getActiveMetaByCategory(categoryId)
    }

    suspend fun actualizarMonto(idMeta: Int, nuevoMonto: Double) {
        dao.actualizarMonto(idMeta, nuevoMonto)
    }

    fun getMetasVigentes(fechaActual: Long): Flow<List<MetaAhorroEntity>> {
        return dao.getMetasVigentes(fechaActual).flowOn(Dispatchers.IO)
    }

    fun getActiveMetas(): Flow<List<MetaAhorroEntity>> {
        return dao.getActiveMetas().flowOn(Dispatchers.IO)
    }

    // Marcar meta como completada
    suspend fun marcarComoCompletada(idMeta: Int) {
        dao.marcarComoCompletada(idMeta)
    }
    
    // Marcar meta como fallida
    suspend fun marcarComoFallida(idMeta: Int) {
        dao.marcarComoFallida(idMeta)
    }
    
    // Obtener metas que expiraron hoy
    suspend fun getMetasExpiradasHoy(startOfDay: Long, endOfDay: Long): List<MetaAhorroEntity> {
        return dao.getMetasExpiradasHoy(startOfDay, endOfDay)
    }
}
