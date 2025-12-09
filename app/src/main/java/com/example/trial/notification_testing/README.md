# 🧪 Sistema de Prueba de Notificaciones - Modo Debug

## 📋 Descripción

Este módulo implementa un sistema de **prueba de notificaciones** que se activa mediante una **secuencia secreta de navegación**. Permite verificar que las notificaciones funcionen correctamente en diferentes estados de la aplicación.

---

## 🔐 Activación del Modo Debug

### Secuencia de Navegación

Para activar/desactivar el modo debug, navega por las pantallas en este orden exacto:

```
Metas → Historial → Transacción → Inicio → Metas
```

**Pasos detallados:**
1. Toca el ícono **⭐ Metas** en la barra inferior
2. Toca el ícono **🕒 Historial**
3. Toca el ícono **➕ Transacción**
4. Toca el ícono **🏠 Inicio**
5. Toca nuevamente el ícono **⭐ Metas**

### Indicadores de Activación

Cuando el modo debug se activa:
- ✅ Verás un mensaje en el Logcat: `🎯 SECUENCIA DETECTADA`
- ✅ Verás: `🐛 DEBUG MODE ACTIVADO`
- ✅ Las notificaciones comenzarán a enviarse cada **30 segundos**

Para desactivar, repite la misma secuencia.

---

## 🔔 Notificaciones de Prueba

### Frecuencia
- **Cada 30 segundos** mientras el modo debug esté activo

### Información Incluida

Cada notificación muestra:
- ⏰ **Hora exacta** del envío (HH:mm:ss)
- 📱 **Estado de la app**:
  - `Primer plano 🟢` - App abierta y visible
  - `Visible 🟡` - App visible pero no en foco
  - `Segundo plano 🟠` - App en segundo plano
  - `En caché ⚪` - App en memoria pero inactiva
  - `Servicio 🔵` - Solo servicio ejecutándose
- 🔢 **Contador** de notificaciones en esta sesión
- 🔄 **Número de sesiones** (veces que se ha activado)

### Ejemplo de Notificación

```
🧪 Prueba #5 (Sesión #2)
17:23:45 - App: Primer plano 🟢

⏰ Hora: 17:23:45
📱 App: Primer plano 🟢
🔢 Notificaciones: 5
🔄 Sesiones: 2
✅ Servicio: Activo cada 30s
```

---

## 🧩 Componentes del Sistema

### 1. **DebugConfig.kt**
- Gestiona el estado del modo debug
- Usa `SharedPreferences` para persistencia
- Lleva cuenta de activaciones

**Métodos principales:**
```kotlin
DebugConfig.isDebugModeEnabled(context)  // Verifica estado
DebugConfig.enableDebugMode(context)     // Activa modo
DebugConfig.disableDebugMode(context)    // Desactiva modo
DebugConfig.toggleDebugMode(context)     // Toggle
```

### 2. **NavigationSequenceDetector.kt**
- Detecta la secuencia de navegación
- Singleton que persiste durante la app
- Activa/desactiva automáticamente el modo debug

**Secuencia monitoreada:**
```kotlin
listOf("goal", "historial", "expense", "home", "goal")
```

### 3. **NotificationTestWorker.kt**
- WorkManager worker para notificaciones periódicas
- Ejecuta cada 15 minutos (mínimo de Android)
- Verifica estado de la app antes de enviar

### 4. **NotificationTestService.kt**
- Servicio que envía notificaciones cada 30 segundos
- Se ejecuta en segundo plano
- Proporciona información detallada del estado

---

## 🎯 Casos de Uso y Pruebas

### Prueba 1: App en Primer Plano
1. Activa el modo debug
2. Mantén la app abierta
3. **Resultado esperado**: Notificaciones cada 30s indicando "Primer plano 🟢"

### Prueba 2: App en Segundo Plano
1. Activa el modo debug
2. Presiona el botón Home
3. **Resultado esperado**: Notificaciones cada 30s indicando estado de segundo plano

### Prueba 3: App Cerrada
1. Activa el modo debug
2. Cierra la app completamente (desliza desde recientes)
3. **Resultado esperado**: El servicio se detiene (las notificaciones cesan)

### Prueba 4: Reinicio de App
1. Activa el modo debug
2. Cierra completamente la app
3. Vuelve a abrir la app
4. **Resultado esperado**: El modo debug permanece activo (persiste en SharedPreferences)

### Prueba 5: Toggle del Modo
1. Activa el modo (haz la secuencia)
2. Haz la secuencia nuevamente
3. **Resultado esperado**: El modo se desactiva y las notificaciones cesan

---

## 🔧 Configuración Técnica

### WorkManager
```kotlin
// Notificaciones cada 15 minutos (mínimo de Android)
val notificationWork = PeriodicWorkRequestBuilder<NotificationTestWorker>(
    15, TimeUnit.MINUTES
).build()
```

### Servicio de Prueba
```kotlin
// Notificaciones cada 30 segundos
private const val NOTIFICATION_INTERVAL = 30_000L  // 30 segundos
```

### Persistencia
```kotlin
// SharedPreferences
private const val PREFS_NAME = "notification_debug_prefs"
private const val KEY_DEBUG_MODE = "debug_mode_enabled"
```

---

## 📊 Logs para Debugging

Busca estos mensajes en Logcat:

```
🎯 SECUENCIA DETECTADA: goal → historial → expense → home → goal
🐛 DEBUG MODE ACTIVADO
▶️ Loop de notificaciones iniciado (cada 30 segundos)
📬 Notificación #1 enviada: 17:23:45 | Estado: Primer plano 🟢
🛑 Modo debug desactivado, deteniendo servicio
```

**Filtro sugerido en Logcat:**
```
🎯|🐛|▶️|📬|🛑
```

---

## ⚙️ Integración con el Proyecto

### MainScreen.kt
```kotlin
val sequenceDetector = remember { 
    NavigationSequenceDetector.getInstance(context) 
}

LaunchedEffect(currentRoute) {
    currentRoute?.let { route ->
        sequenceDetector.onNavigate(route)
    }
}
```

### AndroidManifest.xml
```xml
<service
    android:name=".notification_testing.NotificationTestService"
    android:enabled="true"
    android:exported="false" />
```

### build.gradle.kts
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

---

## 🚨 Consideraciones Importantes

### Limitaciones de Android

1. **WorkManager**: Mínimo 15 minutos entre ejecuciones periódicas
2. **Doze Mode**: En modo ahorro de energía, las notificaciones pueden retrasarse
3. **Optimización de batería**: Algunos fabricantes limitan servicios en segundo plano

### Permisos Necesarios

- ✅ `POST_NOTIFICATIONS` (Android 13+)
- ✅ Ya está configurado en el AndroidManifest

### Consumo de Batería

⚠️ **ADVERTENCIA**: Este modo debug consume batería al ejecutarse constantemente. 
- Úsalo solo para pruebas
- Desactívalo cuando no lo necesites
- No dejar activado en producción

---

## 🐛 Troubleshooting

### Las notificaciones no llegan

1. **Verifica el modo debug**:
   ```kotlin
   println("Debug mode: ${DebugConfig.isDebugModeEnabled(context)}")
   ```

2. **Revisa permisos de notificaciones**:
   - Settings → Apps → FinanSmart → Notifications → Allow

3. **Verifica optimización de batería**:
   - Settings → Battery → App battery usage → FinanSmart → Unrestricted

4. **Revisa el servicio**:
   - Settings → Apps → Running services
   - Busca "NotificationTestService"

### El modo no se activa

1. **Verifica la secuencia**:
   - Asegúrate de seguir el orden exacto
   - No toques otras pantallas entre la secuencia

2. **Revisa el Logcat**:
   - Busca mensajes de "SECUENCIA DETECTADA"

3. **Reinicia la app**:
   - A veces ayuda reiniciar completamente

---

## 📁 Estructura de Archivos

```
app/src/main/java/com/example/trial/notification_testing/
├── DebugConfig.kt                    # Configuración del modo debug
├── NavigationSequenceDetector.kt     # Detector de secuencia
├── NotificationTestWorker.kt         # Worker de notificaciones
├── NotificationTestService.kt        # Servicio de 30 segundos
└── README.md                         # Esta documentación
```

---

## 🎓 Notas para Desarrolladores

Este sistema está diseñado para:
- ✅ Verificar que las notificaciones funcionen en todos los estados
- ✅ Probar persistencia de WorkManager
- ✅ Debugging de problemas de notificaciones
- ✅ Demostración de patrones de diseño (Singleton, Observer)

**NO** debe usarse en producción. Considera eliminarlo o deshabilitarlo en builds de release.

---

## 📝 Versión

- **Versión**: 1.0
- **Fecha**: Diciembre 2024
- **Autor**: Sistema de Debug AppGestionFinan
