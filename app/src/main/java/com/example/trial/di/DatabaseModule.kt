package com.example.trial.di

import android.content.Context
import com.example.trial.data.local.db.AppDatabase
import com.example.trial.data.local.dao.CategoriaDao
import com.example.trial.data.local.dao.EstadoDao
import com.example.trial.data.local.dao.TipoCuentaDao
import com.example.trial.data.local.dao.CuentaDao
import com.example.trial.data.local.dao.TransaccionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideCategoriaDao(database: AppDatabase): CategoriaDao {
        return database.categoriaDao()
    }

    @Provides
    fun provideEstadoDao(database: AppDatabase): EstadoDao {
        return database.estadoDao()
    }

    @Provides
    fun provideTipoCuentaDao(database: AppDatabase): TipoCuentaDao {
        return database.tipoCuentaDao()
    }

    @Provides
    fun provideCuentaDao(database: AppDatabase): CuentaDao {
        return database.cuentaDao()
    }

    @Provides
    fun provideTransaccionDao(database: AppDatabase): TransaccionDao {
        return database.transaccionDao()
    }
}
