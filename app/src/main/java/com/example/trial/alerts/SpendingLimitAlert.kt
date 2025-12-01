package com.example.trial.alerts

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.trial.ExpenseApp
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendingLimitAlert @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TransaccionRepository,
    private val preferences: AlertPreferences
) {

    suspend fun checkLimits() {
        checkDailyLimit()
        checkWeeklyLimit()
        checkMonthlyLimit()
    }

    private suspend fun checkDailyLimit() {
        val limit = preferences.getDailyLimit()
        val (start, end) = getDayRange()
        val totalSpent = repository.getTotalBetween(start, end)
        
        checkAndNotify(totalSpent, limit, "Diario", "daily_${getDayKey()}")
    }

    private suspend fun checkWeeklyLimit() {
        val limit = preferences.getWeeklyLimit()
        val (start, end) = getWeekRange()
        val totalSpent = repository.getTotalBetween(start, end)

        checkAndNotify(totalSpent, limit, "Semanal", "weekly_${getWeekKey()}")
    }

    private suspend fun checkMonthlyLimit() {
        val limit = preferences.getMonthlyLimit()
        val (start, end) = getMonthRange()
        val totalSpent = repository.getTotalBetween(start, end)

        checkAndNotify(totalSpent, limit, "Mensual", "monthly_${getMonthKey()}")
    }

    private fun checkAndNotify(spent: Double, limit: Double, type: String, keyPrefix: String) {
        val percentage = (spent / limit) * 100

        when {
            percentage >= 100 -> {
                if (!preferences.isNotificationSent("${keyPrefix}_100")) {
                    sendNotification(
                        "🚨 Límite $type Excedido",
                        "Has superado tu límite $type de $$limit. Gasto actual: $$spent"
                    )
                    preferences.setNotificationSent("${keyPrefix}_100", true)
                }
            }
            percentage >= 90 -> {
                if (!preferences.isNotificationSent("${keyPrefix}_90")) {
                    sendNotification(
                        "⚠️ 90% del Límite $type",
                        "Estás cerca de tu límite $type. Has gastado $$spent de $$limit"
                    )
                    preferences.setNotificationSent("${keyPrefix}_90", true)
                }
            }
            percentage >= 80 -> {
                if (!preferences.isNotificationSent("${keyPrefix}_80")) {
                    sendNotification(
                        "📊 80% del Límite $type",
                        "Has alcanzado el 80% de tu límite $type."
                    )
                    preferences.setNotificationSent("${keyPrefix}_80", true)
                }
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, ExpenseApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getDayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }

    private fun getDayKey(): String = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()
    private fun getWeekKey(): String = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR).toString()
    private fun getMonthKey(): String = Calendar.getInstance().get(Calendar.MONTH).toString()
}
