package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    val idTransaccion: Int = 0,
    val idCategoria: Int,
    val idCuenta: Int,
    val monto: Double,
    val fecha: Long,
    val descripcion: String?
)