package com.example.trial.data.repository

import com.example.trial.data.local.dao.CategorySum
import com.example.trial.data.local.dao.TransaccionDao
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransaccionRepository @Inject constructor(
    private val transaccionDao: TransaccionDao
) {

    // Lectura
    fun getAllTransacciones(): Flow<List<TransaccionEntity>> =
        transaccionDao.getAllTransacciones()

    fun getTotalPorCuenta(idCuenta: Int): Flow<Double?> =
        transaccionDao.getTotalPorCuenta(idCuenta)

    suspend fun getTotalByCategoriaAndPeriod(idCategoria: Int, start: Long, end: Long): Double {
        return transaccionDao.getTotalPorCategoriaYPeriodo(idCategoria, start, end) ?: 0.0
    }

    fun getSumByCategory(startTs: Long, endTs: Long): Flow<List<CategorySum>> =
        transaccionDao.getSumByCategory(startTs, endTs)

    fun getTransfersBetween(startTs: Long, endTs: Long): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransfersBetween(startTs, endTs)

    suspend fun getTotalBetween(startTs: Long, endTs: Long): Double {
        return transaccionDao.getTotalBetween(startTs, endTs) ?: 0.0
    }

    fun totalSpending(): Flow<Double?> =
        transaccionDao.totalSpending()


    // Escritura
    suspend fun addTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertTransaccion(transaccion)
    }

    suspend fun updateTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.updateTransaccion(transaccion)
    }

    suspend fun deleteTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.deleteTransaccion(transaccion)
    }
}
