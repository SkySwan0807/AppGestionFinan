package com.example.trial.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat

object AlertScheduler {

    private const val ALARM_REQUEST_CODE = 1001

    fun scheduleAlerts(context: Context) {
        // 1. Programar la alarma periódica
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java) // Usar clase explícita para Manifest Receiver
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intervalo de 6 horas
        val intervalMillis = AlarmManager.INTERVAL_HOUR * 6
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            intervalMillis,
            pendingIntent
        )
    }
}
