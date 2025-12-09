package com.example.trial.notification_testing

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Detector de secuencia de navegación para activar el modo debug.
 * Secuencia: Metas → Historial → Transacción → Inicio → Metas
 */
class NavigationSequenceDetector(private val context: Context) {
    
    private val sequence = mutableListOf<String>()
    private val targetSequence = listOf("goal", "historial", "expense", "home", "goal")
    
    companion object {
        private var instance: NavigationSequenceDetector? = null
        
        fun getInstance(context: Context): NavigationSequenceDetector {
            if (instance == null) {
                instance = NavigationSequenceDetector(context.applicationContext)
            }
            return instance!!
        }
    }
    
    /**
     * Registra una navegación a una pantalla
     */
    fun onNavigate(route: String) {
        sequence.add(route)
        
        // Mantener solo los últimos 5 elementos
        if (sequence.size > 5) {
            sequence.removeAt(0)
        }
        
        // Verificar si la secuencia coincide
        if (checkSequence()) {
            onSequenceDetected()
        }
    }
    
    /**
     * Verifica si la secuencia actual coincide con la secuencia objetivo
     */
    private fun checkSequence(): Boolean {
        if (sequence.size < targetSequence.size) return false
        
        val lastFive = sequence.takeLast(targetSequence.size)
        return lastFive == targetSequence
    }
    
    /**
     * Se ejecuta cuando se detecta la secuencia
     */
    private fun onSequenceDetected() {
        println("🎯 SECUENCIA DETECTADA: ${sequence.joinToString(" → ")}")
        
        // Toggle del modo debug
        val newState = DebugConfig.toggleDebugMode(context)
        
        // Mostrar Toast al usuario
        val message = if (newState) {
            "🐛 MODO DEBUG ACTIVADO\nVerificación cada 30 segundos"
        } else {
            "🛑 MODO DEBUG DESACTIVADO"
        }
        
        android.widget.Toast.makeText(
            context,
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
        
        if (newState) {
            // Activar notificaciones periódicas
            startPeriodicNotifications()
        } else {
            // Cancelar notificaciones periódicas
            stopPeriodicNotifications()
        }
        
        // Limpiar la secuencia
        sequence.clear()
    }
    
    /**
     * Inicia las notificaciones periódicas cada 30 segundos
     */
    private fun startPeriodicNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()
        
        // NOTE: WorkManager tiene un mínimo de 15 minutos para PeriodicWorkRequest
        // Para testing más frecuente, usaremos un enfoque diferente
        // Aquí configuramos el trabajo periódico con el mínimo permitido
        val notificationWork = PeriodicWorkRequestBuilder<NotificationTestWorker>(
            15, TimeUnit.MINUTES  // Mínimo permitido por Android
        )
            .setConstraints(constraints)
            .addTag("notification_test")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "notification_debug_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            notificationWork
        )
        
        // Para testing más frecuente (cada 30 segundos), iniciar también un servicio
        NotificationTestService.start(context)
        
        println("✅ Notificaciones de prueba programadas")
    }
    
    /**
     * Detiene las notificaciones periódicas
     */
    private fun stopPeriodicNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork("notification_debug_work")
        NotificationTestService.stop(context)
        println("🛑 Notificaciones de prueba canceladas")
    }
    
    /**
     * Reinicia el detector
     */
    fun reset() {
        sequence.clear()
    }
}
