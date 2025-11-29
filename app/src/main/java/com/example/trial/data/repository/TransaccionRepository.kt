package com.example.trial.data.repository

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

    // Escritura
    suspend fun addTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertTransaccion(transaccion)
    }
}
