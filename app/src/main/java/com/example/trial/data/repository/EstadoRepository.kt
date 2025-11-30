package com.example.trial.data.repository

import com.example.trial.data.local.dao.EstadoDao
import com.example.trial.data.local.entities.EstadoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EstadoRepository @Inject constructor(
    private val dao: EstadoDao
) {

    suspend fun insert(estado: EstadoEntity): Long {
        return dao.insert(estado)
    }

    suspend fun update(estado: EstadoEntity) {
        dao.update(estado)
    }

    suspend fun delete(estado: EstadoEntity) {
        dao.delete(estado)
    }

    fun getAll(): Flow<List<EstadoEntity>> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): EstadoEntity? {
        return dao.getById(id)
    }
}
