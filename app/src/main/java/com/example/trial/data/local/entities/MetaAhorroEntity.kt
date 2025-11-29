package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas_ahorro")
data class MetaAhorroEntity(
    @PrimaryKey(autoGenerate = true)
    val idMeta: Int = 0,
    val idCategoria: Int,
    val idEstado: Int,
    val nombre: String,
    val descripcion: String?,
    val monto: Double,
    val montoActual: Double,
    val fechaInicio: Long,
    val fechaObjetivo: Long
)