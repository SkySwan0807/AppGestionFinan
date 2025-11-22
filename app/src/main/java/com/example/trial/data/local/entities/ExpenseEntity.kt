package com.example.trial.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses") // Nombre de la tabla en SQL
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // El ID se genera solo, por eso el default es 0

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String, // Ej: "Comida", "Transporte"

    @ColumnInfo(name = "date_timestamp")
    val date: Long, // Guardamos la fecha como número (Long)

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false // Preparado para el futuro (Fase 2)
)
