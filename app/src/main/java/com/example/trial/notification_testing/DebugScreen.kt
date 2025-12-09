package com.example.trial.notification_testing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Pantalla de debug para verificar el estado del sistema de notificaciones
 */
@Composable
fun DebugScreen() {
    val context = LocalContext.current
    var debugInfo by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Auto-refresh cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            refreshTrigger++
        }
    }
    
    // Actualizar info
    LaunchedEffect(refreshTrigger) {
        val isDebugMode = DebugConfig.isDebugModeEnabled(context)
        val activationCount = DebugConfig.getActivationCount(context)
        
        debugInfo = buildString {
            appendLine("═══════════════════════════════")
            appendLine("🐛 ESTADO DEL MODO DEBUG")
            appendLine("═══════════════════════════════")
            appendLine()
            appendLine("Modo Debug: ${if (isDebugMode) "✅ ACTIVADO" else "❌ DESACTIVADO"}")
            appendLine("Activaciones totales: $activationCount")
            appendLine()
            appendLine("───────────────────────────────")
            appendLine("📋 INSTRUCCIONES")
            appendLine("───────────────────────────────")
            appendLine()
            appendLine("Secuencia para activar:")
            appendLine("⭐ Metas → 🕒 Historial →")
            appendLine("➕ Transacción → 🏠 Inicio → ⭐ Metas")
            appendLine()
            if (isDebugMode) {
                appendLine("───────────────────────────────")
                appendLine("✅ MODO DEBUG ACTIVO")
                appendLine("───────────────────────────────")
                appendLine()
                appendLine("• Verificaciones cada 30 segundos")
                appendLine("• Notificaciones de metas")
                appendLine("• Notificaciones de inactividad:")
                appendLine("  - 24h = 30 segundos")
                appendLine("  - 48h = 60 segundos")
                appendLine()
                appendLine("💡 TIP: Revisa Logcat para ver logs")
                appendLine("Filtro: System.out | 🔍 | 📬")
            } else {
                appendLine("───────────────────────────────")
                appendLine("ℹ️  MODO DEBUG INACTIVO")
                appendLine("───────────────────────────────")
                appendLine()
                appendLine("Sigue la secuencia para activar")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🔧 DEBUG PANEL",
            fontSize = 24.sp,
            color = Color.Green,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = debugInfo,
            fontSize = 12.sp,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newState = DebugConfig.toggleDebugMode(context)
                    if (newState) {
                        NotificationTestService.start(context)
                    } else {
                        NotificationTestService.stop(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (DebugConfig.isDebugModeEnabled(context)) 
                        Color.Red else Color.Green
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (DebugConfig.isDebugModeEnabled(context))
                        "🛑 DESACTIVAR"
                    else
                        "▶️ ACTIVAR"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                // Enviar notificación de prueba inmediata
                val notificationManager = context.getSystemService(
                    android.content.Context.NOTIFICATION_SERVICE
                ) as android.app.NotificationManager
                
                val notification = androidx.core.app.NotificationCompat.Builder(
                    context,
                    com.example.trial.ExpenseApp.CHANNEL_ID
                )
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("🧪 Test Manual")
                    .setContentText("Si ves esto, las notificaciones funcionan ✅")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .build()
                
                notificationManager.notify(99999, notification)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📬 ENVIAR NOTIFICACIÓN DE PRUEBA")
        }
    }
}
