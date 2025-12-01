package com.example.trial.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlertReceiverEntryPoint {
        fun getSpendingLimitAlert(): SpendingLimitAlert
        fun getInactivityReminderManager(): InactivityReminderManager
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Inyección manual de dependencias usando EntryPoint
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            AlertReceiverEntryPoint::class.java
        )

        val spendingLimitAlert = entryPoint.getSpendingLimitAlert()
        val inactivityReminderManager = entryPoint.getInactivityReminderManager()

        // Ejecutar verificaciones en corrutina (IO)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                spendingLimitAlert.checkLimits()
                inactivityReminderManager.checkInactivity()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
