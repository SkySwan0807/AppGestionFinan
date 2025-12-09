package com.example.trial.notification_testing

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trial.ExpenseApp
import com.example.trial.MainActivity
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

/**
 * Worker que verifica metas expiradas y determina si se cumplieron o fallaron.
 * Se ejecuta diariamente para revisar las metas que vencieron.
 */
class GoalExpirationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GoalExpirationWorkerEntryPoint {
        fun metaRepository(): MetaAhorroRepository
        fun transaccionRepository(): TransaccionRepository
    }

    override suspend fun doWork(): Result {
        try {
            checkExpiredGoals()
        } catch (e: Exception) {
            println("❌ Error al verificar metas expiradas: ${e.message}")
            e.printStackTrace()
        }
        return Result.success()
    }

    /**
     * Verifica metas que expiraron hoy y determina si se cumplieron o fallaron
     */
    private suspend fun checkExpiredGoals() {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            GoalExpirationWorkerEntryPoint::class.java
        )

        val metaRepository = entryPoint.metaRepository()
        val transaccionRepository = entryPoint.transaccionRepository()

        // Obtener inicio y fin del día actual
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        // Obtener metas que expiran hoy
        val metasExpiradas = metaRepository.getMetasExpiradasHoy(startOfDay, endOfDay)

        if (metasExpiradas.isEmpty()) {
            println("ℹ️  No hay metas que expiren hoy")
            return
        }

        println("🔍 Verificando ${metasExpiradas.size} metas expiradas...")

        val metasFallidas = mutableListOf<Pair<String, Int>>() // Nombre y porcentaje
        val metasCumplidas =mutableListOf<String>()

        metasExpiradas.forEach { meta ->
            // Obtener gasto total de la categoría
            val totalGastado = transaccionRepository.getGastosPorCategoria(meta.idCategoria)
                .firstOrNull() ?: 0.0

            val progress = if (meta.monto > 0) {
                ((totalGastado / meta.monto) * 100).toInt()
            } else 0

            when {
                progress > 100 -> {
                    // Se excedió del presupuesto - META FALLIDA
                    metaRepository.marcarComoFallida(meta.idMeta)
                    metasFallidas.add(meta.nombre to progress)
                    println("❌ Meta '${meta.nombre}': FALLIDA ($progress%)")
                }
                progress >= 90 -> {
                    // Llegó al límite o se acercó mucho - META CUMPLIDA
                    metaRepository.marcarComoCompletada(meta.idMeta)
                    metasCumplidas.add(meta.nombre)
                    println("✅ Meta '${meta.nombre}': CUMPLIDA ($progress%)")
                }
                else -> {
                    // No llegó al límite - META CUMPLIDA (gastó menos de lo permitido)
                    metaRepository.marcarComoCompletada(meta.idMeta)
                    metasCumplidas.add(meta.nombre)
                    println("✅ Meta '${meta.nombre}': CUMPLIDA ($progress%)")
                }
            }
        }

        // Enviar notificaciones
        if (metasFallidas.isNotEmpty()) {
            sendFailedGoalsNotification(metasFallidas)
        }

        if (metasCumplidas.isNotEmpty()) {
            sendSuccessGoalsNotification(metasCumplidas)
        }
    }

    /**
     * Envía notificación con lista de metas NO cumplidas (excedieron presupuesto)
     */
    private fun sendFailedGoalsNotification(metasFallidas: List<Pair<String, Int>>) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val listaTexto = metasFallidas.joinToString("\n") { (nombre, porcentaje) ->
            "• $nombre: $porcentaje% gastado"
        }

        val notification = NotificationCompat.Builder(applicationContext, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("❌ ${metasFallidas.size} Meta(s) No Cumplida(s)")
            .setContentText("Te excediste del presupuesto en ${metasFallidas.size} meta(s) este mes")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("No se cumplieron estas metas:\n\n$listaTexto\n\n⚠️ Intenta mejorar tu control de gastos para el próximo período")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(3000, notification)

        println("📬 Notificación de metas fallidas enviada")
    }

    /**
     * Envía notificación con lista de metas CUMPLIDAS
     */
    private fun sendSuccessGoalsNotification(metasCumplidas: List<String>) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val listaTexto = metasCumplidas.joinToString("\n") { "✅ $it" }

        val notification = NotificationCompat.Builder(applicationContext, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 ¡${metasCumplidas.size} Meta(s) Cumplida(s)!")
            .setContentText("Te mantuviste dentro del presupuesto en ${metasCumplidas.size} meta(s)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Metas cumplidas este período:\n\n$listaTexto\n\n¡Excelente control financiero!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(3001, notification)

        println("📬 Notificación de metas cumplidas enviada")
    }
}
