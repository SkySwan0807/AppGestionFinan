package com.example.trial.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.trial.data.local.entities.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Insert
    suspend fun insertAll(categorias: List<CategoriaEntity>)

    @Insert
    suspend fun insertCategoria(categoria: CategoriaEntity)

    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun getCount(): Int

    @Query("SELECT * FROM categorias")
    fun getAllCategorias(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias")
    suspend fun getAllCategoriasSync(): List<CategoriaEntity>

    @Query("DELETE FROM categorias")
    suspend fun deleteAll()
}

data class CategorySum(
    val idCategoria: Int,
    val category: String,
    val total: Double
)