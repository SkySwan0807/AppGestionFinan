package com.example.trial.data.repository

import com.example.trial.data.local.dao.CategorySum
import com.example.trial.data.local.dao.TransaccionDao
import com.example.trial.data.local.entities.TransaccionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
/*
class TransaccionRepository @Inject constructor(
    private val transaccionDao: TransaccionDao
) {

    // Lectura (actualizadas para usar solo transacciones activas)
    fun getAllTransaccionesActivas(): Flow<List<TransaccionEntity>> =
        transaccionDao.getAllTransaccionesActivas()

    fun getTotalPorCuenta(idCuenta: Int): Flow<Double?> =
        transaccionDao.getTotalPorCuenta(idCuenta)

    suspend fun getTotalByCategoriaAndPeriod(idCategoria: Int, start: Long, end: Long): Double {
        return transaccionDao.getTotalPorCategoriaYPeriodo(idCategoria, start, end) ?: 0.0
    }

    fun getSumByCategoryActivas(startTs: Long, endTs: Long): Flow<List<CategorySum>> =
        transaccionDao.getSumByCategoryActivas(startTs, endTs)

    fun getTransfersBetweenActivas(startTs: Long, endTs: Long): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransfersBetweenActivas(startTs, endTs)

    suspend fun getTotalBetween(startTs: Long, endTs: Long): Double {
        return transaccionDao.getTotalBetween(startTs, endTs) ?: 0.0
    }

    fun totalSpending(): Flow<Double?> =
        transaccionDao.totalSpending()

    // Escritura
    suspend fun addTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertTransaccion(transaccion)
    }

    // 👇 NUEVO - Borrado lógico
    suspend fun softDeleteTransaccion(id: Int): Boolean {
        val filas = transaccionDao.softDelete(id, System.currentTimeMillis())
        return filas > 0
    }
}
*/
class TransaccionRepository @Inject constructor(
    private val transaccionDao: TransaccionDao
) {
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

    suspend fun addTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertTransaccion(transaccion)
    }
    // Borrado lógico
    suspend fun softDeleteTransaccion(id: Int): Boolean {
        val filas = transaccionDao.softDelete(id, System.currentTimeMillis())
        return filas > 0
    }

    fun totalSpendingActivas(): Flow<Double?> =
        transaccionDao.totalSpendingActivas()


    // Solo transacciones no borradas
    fun getAllTransaccionesActivas(): Flow<List<TransaccionEntity>> =
        transaccionDao.getAllTransaccionesActivas()


    fun getTransfersBetweenActivas(startTs: Long, endTs: Long): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransfersBetweenActivas(startTs, endTs)

    fun getSumByCategoryActivas(startTs: Long, endTs: Long): Flow<List<CategorySum>> =
        transaccionDao.getSumByCategoryActivas(startTs, endTs)


}

