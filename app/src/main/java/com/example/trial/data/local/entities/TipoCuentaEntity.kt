package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipo_cuentas")
data class TipoCuentaEntity(
    @PrimaryKey(autoGenerate = true)
    val idTipo: Int = 0,
    val nombre: String,
    val descripcion: String
)