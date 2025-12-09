package com.example.trial.notification_testing

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuración para el modo debug de notificaciones.
 * Se activa mediante una secuencia específica de navegación.
 */
object DebugConfig {
    private const val PREFS_NAME = "notification_debug_prefs"
    private const val KEY_DEBUG_MODE = "debug_mode_enabled"
    private const val KEY_ACTIVATION_COUNT = "activation_count"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Verifica si el modo debug está activado
     */
    fun isDebugModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DEBUG_MODE, false)
    }
    
    /**
     * Activa el modo debug
     */
    fun enableDebugMode(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_DEBUG_MODE, true)
            val count = getPrefs(context).getInt(KEY_ACTIVATION_COUNT, 0)
            putInt(KEY_ACTIVATION_COUNT, count + 1)
            apply()
        }
        println("🐛 DEBUG MODE ACTIVADO - Las notificaciones de prueba se ejecutarán cada 30 segundos")
    }
    
    /**
     * Desactiva el modo debug
     */
    fun disableDebugMode(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_DEBUG_MODE, false)
            apply()
        }
        println("🐛 DEBUG MODE DESACTIVADO")
    }
    
    /**
     * Obtiene el número de veces que se ha activado el modo debug
     */
    fun getActivationCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_ACTIVATION_COUNT, 0)
    }
    
    /**
     * Toggle del modo debug
     */
    fun toggleDebugMode(context: Context): Boolean {
        val isEnabled = isDebugModeEnabled(context)
        if (isEnabled) {
            disableDebugMode(context)
        } else {
            enableDebugMode(context)
        }
        return !isEnabled
    }
}
