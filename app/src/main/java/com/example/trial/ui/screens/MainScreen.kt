package com.example.trial.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trial.notification_testing.NavigationSequenceDetector
import com.example.trial.notification_testing.DebugConfig

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    object Home : Screen("home", "Inicio", Icons.Default.Home)

    object Expense : Screen("expense", "Transación", Icons.Default.Add)

    object Goal : Screen("goal", "Metas", Icons.Default.Star)

    object Historial : Screen("historial", "Historial", Icons.Default.History)

    object Graficos : Screen("graficos", "Graficos", Icons.Default.BarChart)
    
    object Debug : Screen("debug", "Debug", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sequenceDetector = remember { NavigationSequenceDetector.getInstance(context) }
    
    // Observar estado del modo debug
    var isDebugMode by remember { mutableStateOf(DebugConfig.isDebugModeEnabled(context)) }
    
    // Items de navegación (Debug solo cuando está activado)
    val baseItems = listOf(Screen.Home, Screen.Expense, Screen.Goal, Screen.Historial, Screen.Graficos)
    val items = if (isDebugMode) {
        baseItems + Screen.Debug
    } else {
        baseItems
    }
    
    // Observar cambios en la navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Registrar navegación cuando cambia la ruta
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            sequenceDetector.onNavigate(route)
            // Actualizar estado del modo debug después de la navegación
            isDebugMode = DebugConfig.isDebugModeEnabled(context)
            
            // Si el debug se desactivó y estamos en la pantalla debug, volver a home
            if (!isDebugMode && route == Screen.Debug.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Expense.route) { ExpenseScreen() }
            composable(Screen.Goal.route) { GoalScreen() }
            composable(Screen.Historial.route) { HistorialScreen() }
            composable(Screen.Graficos.route) { GraficosScreen() }
            composable(Screen.Debug.route) { com.example.trial.notification_testing.DebugScreen() }
        }
    }
}
