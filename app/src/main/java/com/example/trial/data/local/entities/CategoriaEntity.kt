package com.example.trial.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val idCategoria: Int = 1,
    val nombre: String,
    val descripcion: String
)