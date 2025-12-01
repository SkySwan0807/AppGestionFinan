package com.example.trial.alerts

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_DAILY_LIMIT = "daily_limit"
        private const val KEY_WEEKLY_LIMIT = "weekly_limit"
        private const val KEY_MONTHLY_LIMIT = "monthly_limit"
        
        private const val DEFAULT_DAILY_LIMIT = 50.0
        private const val DEFAULT_WEEKLY_LIMIT = 300.0
        private const val DEFAULT_MONTHLY_LIMIT = 1000.0
    }

    fun getDailyLimit(): Double = sharedPreferences.getFloat(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT.toFloat()).toDouble()
    fun getWeeklyLimit(): Double = sharedPreferences.getFloat(KEY_WEEKLY_LIMIT, DEFAULT_WEEKLY_LIMIT.toFloat()).toDouble()
    fun getMonthlyLimit(): Double = sharedPreferences.getFloat(KEY_MONTHLY_LIMIT, DEFAULT_MONTHLY_LIMIT.toFloat()).toDouble()

    fun setDailyLimit(limit: Double) = sharedPreferences.edit().putFloat(KEY_DAILY_LIMIT, limit.toFloat()).apply()
    fun setWeeklyLimit(limit: Double) = sharedPreferences.edit().putFloat(KEY_WEEKLY_LIMIT, limit.toFloat()).apply()
    fun setMonthlyLimit(limit: Double) = sharedPreferences.edit().putFloat(KEY_MONTHLY_LIMIT, limit.toFloat()).apply()

    // Control de notificaciones enviadas para evitar spam
    fun isNotificationSent(key: String): Boolean = sharedPreferences.getBoolean("sent_$key", false)
    fun setNotificationSent(key: String, sent: Boolean) = sharedPreferences.edit().putBoolean("sent_$key", sent).apply()
    
    // Limpiar historial de notificaciones (útil al cambiar de día/mes)
    fun clearNotificationHistory(prefix: String) {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys.filter { it.startsWith("sent_$prefix") }.forEach {
            editor.remove(it)
        }
        editor.apply()
    }
}
