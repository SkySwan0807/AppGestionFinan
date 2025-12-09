package com.example.trial

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.trial.notification_testing.GoalExpirationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class ExpenseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleGoalExpirationCheck()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Expense Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de gastos, metas y logros"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Programa la verificación diaria de metas expiradas
     */
    private fun scheduleGoalExpirationCheck() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        // Ejecutar diariamente a las 20:00 (8 PM)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        val now = Calendar.getInstance()
        val initialDelay = if (calendar.before(now)) {
            // Si ya pasaron las 8 PM hoy, programar para mañana
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.timeInMillis - now.timeInMillis
        } else {
            // Programar para hoy a las 8 PM
            calendar.timeInMillis - now.timeInMillis
        }
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<GoalExpirationWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("goal_expiration_check")
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_goal_expiration_check",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
        
        println("✅ Verificación diaria de metas programada para las 20:00")
    }

    companion object {
        const val CHANNEL_ID = "expense_channel"
    }
}
