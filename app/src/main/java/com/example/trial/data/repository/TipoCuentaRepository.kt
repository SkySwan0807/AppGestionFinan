package com.example.trial.data.repository

import com.example.trial.data.local.dao.TipoCuentaDao
import com.example.trial.data.local.entities.TipoCuentaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipoCuentaRepository @Inject constructor(
    private val dao: TipoCuentaDao
) {

    suspend fun insert(tipo: TipoCuentaEntity): Long {
        return dao.insert(tipo)
    }

    suspend fun update(tipo: TipoCuentaEntity) {
        dao.update(tipo)
    }

    suspend fun delete(tipo: TipoCuentaEntity) {
        dao.delete(tipo)
    }

    fun getAll(): Flow<List<TipoCuentaEntity>> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): TipoCuentaEntity? {
        return dao.getById(id)
    }
}
