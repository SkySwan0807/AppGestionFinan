package com.example.trial.data.repository

import com.example.trial.data.local.dao.MetaAhorroDao
import com.example.trial.data.local.entities.MetaAhorroEntity
import kotlinx.coroutines.flow.Flow
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

    suspend fun actualizarMonto(idMeta: Int, nuevoMonto: Double) {
        dao.actualizarMonto(idMeta, nuevoMonto)
    }

    fun getMetasVigentes(fechaActual: Long): Flow<List<MetaAhorroEntity>> {
        return dao.getMetasVigentes(fechaActual)
    }

    fun getActiveMetas(): Flow<List<MetaAhorroEntity>>
    {
        return dao.getActiveMetas()
    }
}
