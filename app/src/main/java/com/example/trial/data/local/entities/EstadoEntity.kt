package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estados")
data class EstadoEntity(
    @PrimaryKey(autoGenerate = true)
    val idEstado: Int = 0,
    val nombre: String,
    val descripcion: String
)