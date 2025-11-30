package com.example.trial.data.repository

import com.example.trial.data.local.dao.CategoriaDao
import com.example.trial.data.local.entities.CategoriaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoriaRepository @Inject constructor(
    private val categoriaDao: CategoriaDao
) {

    fun getAllCategorias(): Flow<List<CategoriaEntity>> =
        categoriaDao.getAllCategorias()

    fun getNombresCategorias(): Flow<List<CategoriaEntity>> =
        categoriaDao.getNombresCategorias()


    suspend fun addCategoria(categoria: CategoriaEntity) {
        categoriaDao.insertCategoria(categoria)
    }
}
