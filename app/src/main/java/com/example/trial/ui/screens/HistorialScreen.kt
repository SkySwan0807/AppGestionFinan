package com.example.trial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.ui.viewmodels.HistorialViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val historial by viewModel.historialFiltrado.collectAsState()
    var selectedCategory by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf(
        0 to "Todas",
        1 to "Comida",
        2 to "Transporte",
        3 to "Servicios",
        4 to "Ocio",
        5 to "Otros",
        6 to "Ingresos"
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Historial de Transacciones",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        var selectedCategory by remember { mutableStateOf(0) }

        FiltrosDialog(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { id ->
                selectedCategory = id
                viewModel.setFiltroCategoria(id)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // LazyColumn ocupando solo la mitad de la pantalla
        LazyColumn(
            //modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(historial) { transaccion ->
                HistorialItem(transaccion, viewModel)
            }
        }
    }
}

@Composable
fun HistorialItem(
    transaccion: TransaccionEntity,
    viewModel: HistorialViewModel = hiltViewModel()
) {
    //var filtros by remember { mutableStateOf(FiltrosUI()) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // Estado para el diálogo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(getCategoryColor(transaccion.idCategoria))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de categoría
            Icon(
                imageVector = getCategoryIconById(transaccion.idCategoria),
                contentDescription = "Icono categoría",
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaccion.descripcion ?: "Sin descripción",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Monto: ${transaccion.monto}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Fecha: ${formatDate(transaccion.fecha)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de opciones
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modificar") },
                        onClick = {
                            expanded = false
                            viewModel.onEdit(transaccion)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expanded = false
                            showDeleteDialog = true // Muestra el diálogo
                        }
                    )
                }
            }
        }
    }

    // --------------------
    // Diálogo de confirmación
    // --------------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar transacción") },
            text = { Text("¿Estás seguro de que deseas eliminar esta transacción?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDelete(transaccion)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error // rojo
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant // gris
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )

    }
}

@Composable
fun FiltrosDialog(
    categories: List<Pair<Int, String>>,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Botón que abre el AlertDialog
    TextButton(onClick = { showDialog = true }) {
        Text("Filtro")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ajusta los filtros") },
            text = {
                Column {

                    Row (
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Categorías",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                        )
                        // Dropdown dentro del AlertDialog
                        Box {
                            TextButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = categories.firstOrNull { it.first == selectedCategory }?.second
                                        ?: "Todas"
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            onCategorySelected(id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}



fun getCategoryIconById(idCategoria: Int): ImageVector {
    return when (idCategoria) {
        1 -> Icons.Default.Restaurant
        2 -> Icons.Default.DirectionsBus
        3 -> Icons.Default.FlashOn
        4 -> Icons.Default.Theaters
        5 -> Icons.Default.Category
        6 -> Icons.Default.AttachMoney
        else -> Icons.Default.Help
    }
}
