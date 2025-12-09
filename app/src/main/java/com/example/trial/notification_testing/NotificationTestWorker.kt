package com.example.trial.notification_testing

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import com.example.trial.notifications.GoalNotificationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker que verifica el progreso de las metas y envía notificaciones REALES
 * usando el GoalNotificationManager existente.
 * 
 * En modo debug, se ejecuta cada 30 segundos para acelerar las verificaciones
 * que normalmente ocurrirían solo cuando cambian los datos.
 */
class NotificationTestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationTestWorkerEntryPoint {
        fun metaRepository(): MetaAhorroRepository
        fun transaccionRepository(): TransaccionRepository
        fun notificationManager(): GoalNotificationManager
    }
    
    override suspend fun doWork(): Result {
        // Verificar si el modo debug está activo
        if (!DebugConfig.isDebugModeEnabled(applicationContext)) {
            return Result.success()
        }
        
        try {
            checkGoalNotifications()
        } catch (e: Exception) {
            println("❌ Error al verificar notificaciones: ${e.message}")
            e.printStackTrace()
        }
        
        return Result.success()
    }
    
    /**
     * Verifica todas las metas activas y envía notificaciones según el progreso
     */
    private suspend fun checkGoalNotifications() {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Obtener acceso a las dependencias mediante Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationTestWorkerEntryPoint::class.java
        )
        
        val metaRepository = entryPoint.metaRepository()
        val transaccionRepository = entryPoint.transaccionRepository()
        val notificationManager = entryPoint.notificationManager()
        
        // Obtener metas activas
        val metas = metaRepository.getActiveMetas().firstOrNull() ?: emptyList()
        
        println("🔍 [$timestamp] Verificando ${metas.size} metas activas...")
        
        // Para cada meta, calcular progreso y verificar notificaciones
        metas.forEach { meta ->
            // CAMBIO: Obtener solo GASTOS de la categoría
            val totalGastado = transaccionRepository.getGastosPorCategoria(meta.idCategoria)
                .firstOrNull() ?: 0.0
            
            val currentSpent = totalGastado
            // NO limitar a 100% - permitir valores mayores cuando te excedes
            val progress = if (meta.monto > 0) {
                (currentSpent / meta.monto).toFloat()
            } else 0f
            
            val remainingDays = calculateRemainingDays(meta.fechaObjetivo)
            
            // Crear objeto con progreso
            val metaWithProgress = com.example.trial.ui.viewmodels.MetaAhorroWithProgress(
                meta = meta.copy(montoActual = currentSpent),
                progress = progress,
                remainingAmount = (meta.monto - currentSpent).coerceAtLeast(0.0),
                remainingDays = remainingDays
            )
            
            // Verificar y enviar notificaciones usando el sistema real
            notificationManager.checkAndSendNotifications(metaWithProgress)
            
            println("   ✓ Meta '${meta.nombre}': ${(progress * 100).toInt()}% | ${remainingDays}d")
        }
        
        println("✅ [$timestamp] Verificación completada")
    }
    
    /**
     * Calcula los días restantes hasta la fecha objetivo
     */
    private fun calculateRemainingDays(endDate: Long): Int {
        val now = System.currentTimeMillis()
        val diff = endDate - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
}
