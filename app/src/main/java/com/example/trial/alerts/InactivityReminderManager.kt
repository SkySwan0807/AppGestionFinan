package com.example.trial.alerts

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.trial.ExpenseApp
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InactivityReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TransaccionRepository,
    private val preferences: AlertPreferences
) {

    suspend fun checkInactivity() {
        val lastTransactionTime = repository.getLastTransactionDate() ?: return
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastTransactionTime
        
        val hoursSinceLast = diff / (1000 * 60 * 60)

        if (hoursSinceLast >= 48) {
            if (!preferences.isNotificationSent("inactivity_48h_${lastTransactionTime}")) {
                sendNotification(
                    "👻 ¡Te extrañamos!",
                    "Han pasado 48 horas sin registros. ¿Has tenido gastos recientes?"
                )
                preferences.setNotificationSent("inactivity_48h_${lastTransactionTime}", true)
            }
        } else if (hoursSinceLast >= 24) {
            if (!preferences.isNotificationSent("inactivity_24h_${lastTransactionTime}")) {
                sendNotification(
                    "📝 Recordatorio Diario",
                    "No has registrado gastos en las últimas 24 horas. ¡Mantén tus finanzas al día!"
                )
                preferences.setNotificationSent("inactivity_24h_${lastTransactionTime}", true)
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(9999, notification)
    }
}
