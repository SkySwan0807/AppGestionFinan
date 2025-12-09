package com.example.trial.notification_testing

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.trial.ExpenseApp
import com.example.trial.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de notificaciones de inactividad.
 * Envía recordatorios cuando el usuario no ha usado la app en 24 o 48 horas.
 * 
 * En modo debug, los tiempos se aceleran:
 * - 24 horas = 30 segundos
 * - 48 horas = 60 segundos
 */
@Singleton
class InactivityNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val prefs = context.getSharedPreferences("inactivity_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LAST_ACTIVITY = "last_activity_timestamp"
        private const val KEY_24H_NOTIF_SENT = "notification_24h_sent"
        private const val KEY_48H_NOTIF_SENT = "notification_48h_sent"
        
        // Tiempos reales
        private const val HOURS_24 = 24 * 60 * 60 * 1000L  // 24 horas en milisegundos
        private const val HOURS_48 = 48 * 60 * 60 * 1000L  // 48 horas en milisegundos
        
        // Tiempos de debug (acelerados)
        private const val DEBUG_24H = 30 * 1000L  // 30 segundos
        private const val DEBUG_48H = 60 * 1000L  // 60 segundos
    }
    
    /**
     * Registra que el usuario está usando la app ahora
     */
    fun recordActivity() {
        prefs.edit().apply {
            putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
            putBoolean(KEY_24H_NOTIF_SENT, false)
            putBoolean(KEY_48H_NOTIF_SENT, false)
            apply()
        }
    }
    
    /**
     * Verifica si debe enviar notificaciones de inactividad
     */
    fun checkInactivity() {
        val lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val timeSinceLastActivity = now - lastActivity
        
        val isDebugMode = DebugConfig.isDebugModeEnabled(context)
        
        // Usar tiempos de debug o normales según el modo
        val threshold24h = if (isDebugMode) DEBUG_24H else HOURS_24
        val threshold48h = if (isDebugMode) DEBUG_48H else HOURS_48
        
        val notif24hSent = prefs.getBoolean(KEY_24H_NOTIF_SENT, false)
        val notif48hSent = prefs.getBoolean(KEY_48H_NOTIF_SENT, false)
        
        // Verificar 48 horas primero
        if (timeSinceLastActivity >= threshold48h && !notif48hSent) {
            sendInactivityNotification(48)
            prefs.edit().putBoolean(KEY_48H_NOTIF_SENT, true).apply()
            
            if (isDebugMode) {
                println("📬 Notificación de inactividad 48h enviada (DEBUG: ${timeSinceLastActivity}ms)")
            }
        }
        // Luego verificar 24 horas
        else if (timeSinceLastActivity >= threshold24h && !notif24hSent) {
            sendInactivityNotification(24)
            prefs.edit().putBoolean(KEY_24H_NOTIF_SENT, true).apply()
            
            if (isDebugMode) {
                println("📬 Notificación de inactividad 24h enviada (DEBUG: ${timeSinceLastActivity}ms)")
            }
        }
    }
    
    /**
     * Envía una notificación de recordatorio por inactividad
     */
    private fun sendInactivityNotification(hours: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            hours,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val (title, message) = when (hours) {
            24 -> Pair(
                "⏰ ¡Te extrañamos!",
                "Hace 24 horas que no registras tus gastos. ¿Qué tal si actualizas tu presupuesto?"
            )
            48 -> Pair(
                "💸 ¿Todo bien?",
                "Han pasado 48 horas sin actividad. Mantén tu presupuesto al día para un mejor control financiero."
            )
            else -> Pair(
                "📱 Recordatorio",
                "No olvides actualizar tus gastos en FinanSmart"
            )
        }
        
        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()
        
        notificationManager.notify(5000 + hours, notification)
    }
    
    /**
     * Obtiene información de estado para debugging
     */
    fun getInactivityStatus(): String {
        val lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val timeSinceLastActivity = now - lastActivity
        
        val isDebugMode = DebugConfig.isDebugModeEnabled(context)
        val threshold24h = if (isDebugMode) DEBUG_24H else HOURS_24
        val threshold48h = if (isDebugMode) DEBUG_48H else HOURS_48
        
        val notif24hSent = prefs.getBoolean(KEY_24H_NOTIF_SENT, false)
        val notif48hSent = prefs.getBoolean(KEY_48H_NOTIF_SENT, false)
        
        val seconds = (timeSinceLastActivity / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return buildString {
            if (isDebugMode) {
                append("⏱️  Inactividad: ${seconds}s (24h=${threshold24h/1000}s, 48h=${threshold48h/1000}s)")
            } else {
                append("⏱️  Inactividad: ${hours}h ${minutes % 60}m")
            }
            append(" | 24h: ${if(notif24hSent) "✅" else "❌"}")
            append(" | 48h: ${if(notif48hSent) "✅" else "❌"}")
        }
    }
    
    /**
     * Resetea las notificaciones enviadas (útil para testing)
     */
    fun resetNotifications() {
        prefs.edit().apply {
            putBoolean(KEY_24H_NOTIF_SENT, false)
            putBoolean(KEY_48H_NOTIF_SENT, false)
            apply()
        }
        println("🔄 Notificaciones de inactividad reseteadas")
    }
}
