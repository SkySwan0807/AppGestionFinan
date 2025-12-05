package com.example.trial.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.trial.ExpenseApp
import com.example.trial.MainActivity
import com.example.trial.R
import com.example.trial.builder.AppNotificationBuilder
import com.example.trial.data.local.entities.MetaAhorroEntity
import com.example.trial.ui.viewmodels.MetaAhorroWithProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val sentNotifications = mutableSetOf<String>() // Para evitar duplicados

    fun checkAndSendNotifications(metaWithProgress: MetaAhorroWithProgress) {
        val meta = metaWithProgress.meta
        val progress = metaWithProgress.progress
        val remainingDays = metaWithProgress.remainingDays

        // Notificación de meta completada (100%)
        if (progress >= 1.0f && !wasNotificationSent(meta.idMeta, "completed")) {
            sendGoalCompletedNotification(meta)
            markNotificationAsSent(meta.idMeta, "completed")
        }
        // Notificación de 90% alcanzado
        else if (progress >= 0.9f && progress < 1.0f && !wasNotificationSent(meta.idMeta, "90percent")) {
            sendProgressNotification(meta, 90)
            markNotificationAsSent(meta.idMeta, "90percent")
        }
        // Notificación de 75% alcanzado
        else if (progress >= 0.75f && progress < 0.9f && !wasNotificationSent(meta.idMeta, "75percent")) {
            sendProgressNotification(meta, 75)
            markNotificationAsSent(meta.idMeta, "75percent")
        }
        // Notificación de 50% alcanzado
        else if (progress >= 0.5f && progress < 0.75f && !wasNotificationSent(meta.idMeta, "50percent")) {
            sendProgressNotification(meta, 50)
            markNotificationAsSent(meta.idMeta, "50percent")
        }

        // Notificación de días restantes (3 días o menos)
        if (remainingDays in 1..3 && !wasNotificationSent(meta.idMeta, "deadline_$remainingDays")) {
            sendDeadlineNotification(meta, remainingDays)
            markNotificationAsSent(meta.idMeta, "deadline_$remainingDays")
        }
    }

    private fun sendGoalCompletedNotification(meta: MetaAhorroEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            meta.idMeta,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 ¡Meta Completada!")
            .setContentText("Has completado tu meta: ${meta.nombre}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(meta.idMeta + 1000, notification)
    }

    private fun sendProgressNotification(meta: MetaAhorroEntity, percentage: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            meta.idMeta + percentage,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (percentage) {
            90 -> "⚠️"
            75 -> "📊"
            50 -> "📈"
            else -> "ℹ️"
        }

        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$emoji Progreso de Meta")
            .setContentText("Has usado el $percentage% de tu presupuesto en ${meta.nombre}")
            .setPriority(
                if (percentage >= 90) NotificationCompat.PRIORITY_HIGH 
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(meta.idMeta + 2000 + percentage, notification)
    }

    private fun sendDeadlineNotification(meta: MetaAhorroEntity, daysRemaining: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            meta.idMeta + daysRemaining,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Meta por vencer")
            .setContentText("Quedan $daysRemaining día(s) para tu meta: ${meta.nombre}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(meta.idMeta + 3000 + daysRemaining, notification)
    }

    private fun wasNotificationSent(metaId: Int, type: String): Boolean {
        return sentNotifications.contains("$metaId-$type")
    }

    private fun markNotificationAsSent(metaId: Int, type: String) {
        sentNotifications.add("$metaId-$type")
    }

    fun clearNotificationHistory(metaId: Int) {
        sentNotifications.removeAll { it.startsWith("$metaId-") }
    }

    fun clearAllNotificationHistory() {
        sentNotifications.clear()
        notificationManager.cancelAll()
    }
}
