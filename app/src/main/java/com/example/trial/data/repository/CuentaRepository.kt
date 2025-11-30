package com.example.trial.data.repository

import com.example.trial.data.local.dao.CuentaDao
import com.example.trial.data.local.entities.CuentaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CuentaRepository @Inject constructor(
    private val cuentaDao: CuentaDao
) {

    fun getAllCuentas(): Flow<List<CuentaEntity>> =
        cuentaDao.getAllCuentas()

    suspend fun addCuenta(cuenta: CuentaEntity) {
        cuentaDao.insertCuenta(cuenta)
    }

    suspend fun actualizarBalance(idCuenta: Int, nuevoBalance: Double) {
        cuentaDao.actualizarBalance(idCuenta, nuevoBalance)
    }
}
