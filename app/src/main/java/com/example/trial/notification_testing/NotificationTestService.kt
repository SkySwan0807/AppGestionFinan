package com.example.trial.notification_testing

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import com.example.trial.notifications.GoalNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Servicio en segundo plano que verifica las metas cada 30 segundos
 * y envía notificaciones REALES del sistema de metas.
 * 
 * Este servicio acelera las verificaciones que normalmente ocurrirían
 * solo cuando cambian los datos, permitiendo probar el sistema de notificaciones
 * sin esperar 24-48 horas.
 */
@AndroidEntryPoint
class NotificationTestService : Service() {
    
    @Inject
    lateinit var metaRepository: MetaAhorroRepository
    
    @Inject
    lateinit var transaccionRepository: TransaccionRepository
    
    @Inject
    lateinit var notificationManager: GoalNotificationManager
    
    @Inject
    lateinit var inactivityManager: InactivityNotificationManager
    
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var checkCount = 0
    
    companion object {
        private const val NOTIFICATION_INTERVAL = 30_000L  // 30 segundos
        private const val ACTION_START = "ACTION_START_NOTIFICATION_TEST"
        private const val ACTION_STOP = "ACTION_STOP_NOTIFICATION_TEST"
        
        /**
         * Inicia el servicio de verificación de notificaciones
         */
        fun start(context: Context) {
            val intent = Intent(context, NotificationTestService::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }
        
        /**
         * Detiene el servicio de verificación de notificaciones
         */
        fun stop(context: Context) {
            val intent = Intent(context, NotificationTestService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        println("🔧 NotificationTestService creado (verificación de metas reales)")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startNotificationLoop()
            ACTION_STOP -> stopSelf()
        }
        
        return START_STICKY
    }
    
    /**
     * Inicia el loop de verificación cada 30 segundos
     */
    private fun startNotificationLoop() {
        if (handler != null) {
            println("⚠️ El servicio ya está ejecutándose")
            return
        }
        
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                // Verificar si el modo debug sigue activo
                if (!DebugConfig.isDebugModeEnabled(applicationContext)) {
                    println("🛑 Modo debug desactivado, deteniendo servicio")
                    stopSelf()
                    return
                }
                
                // Verificar metas en coroutine
                serviceScope.launch {
                    checkGoalNotifications()
                }
                
                // Programar la siguiente ejecución
                handler?.postDelayed(this, NOTIFICATION_INTERVAL)
            }
        }
        
        // Iniciar el loop
        handler?.post(runnable!!)
        println("▶️ Loop de verificación iniciado (cada 30 segundos)")
        println("   ✓ Verificando metas reales (progreso, fecha límite)")
        println("   ✓ Verificando inactividad (24h/48h en modo acelerado)")
    }
    
    /**
     * Verifica todas las metas activas Y la inactividad del usuario
     */
    private suspend fun checkGoalNotifications() {
        checkCount++
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🔍 [$timestamp] Verificación #$checkCount")
        
        try {
            // ========== VERIFICAR INACTIVIDAD ==========
            inactivityManager.checkInactivity()
            val inactivityStatus = inactivityManager.getInactivityStatus()
            println("   $inactivityStatus")
            
            // ========== VERIFICAR METAS ==========
            // Obtener metas activas
            val metas = metaRepository.getActiveMetas().firstOrNull() ?: emptyList()
            
            if (metas.isEmpty()) {
                println("ℹ️  [$timestamp] #$checkCount: No hay metas activas para verificar")
                return
            }
            
            println("🔍 [$timestamp] #$checkCount: Verificando ${metas.size} metas...")
            
            var notificationsSent = 0
            
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
                
                val progressPercent = (progress * 100).toInt()
                val emoji = when {
                    progress >= 1.0f -> "🎉"
                    progress >= 0.9f -> "⚠️"
                    progress >= 0.75f -> "📊"
                    progress >= 0.5f -> "📈"
                    else -> "💡"
                }
                
                println("   $emoji '${meta.nombre}': $progressPercent% | $remainingDays días | Bs.${currentSpent}/${meta.monto}")
            }
            
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("✅ [$timestamp] Verificación completada\n")
            
        } catch (e: Exception) {
            println("❌ Error al verificar metas: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Calcula los días restantes hasta la fecha objetivo
     */
    private fun calculateRemainingDays(endDate: Long): Int {
        val now = System.currentTimeMillis()
        val diff = endDate - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Detener el handler
        runnable?.let { handler?.removeCallbacks(it) }
        handler = null
        runnable = null
        
        // Cancelar coroutines
        serviceScope.cancel()
        
        println("🛑 NotificationTestService destruido (${checkCount} verificaciones realizadas)")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
