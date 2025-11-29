package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true)
    val idCuenta: Int = 0,
    val idTipo: Int,
    val nombre: String,
    val descripcion: String?,
    val balance: Double
)