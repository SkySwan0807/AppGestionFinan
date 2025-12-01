package com.example.trial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.ui.viewmodels.HistorialViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val historial = viewModel.historial.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Historial de Transacciones",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(historial.value) { transaccion ->
                HistorialItem(transaccion)
            }
        }
    }
}

@Composable
fun HistorialItem(transaccion: TransaccionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {

            Text(transaccion.descripcion ?: "Sin descripción")

            Text(
                text = "Monto: ${transaccion.monto}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Fecha: ${formatDate(transaccion.fecha)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
