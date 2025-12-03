package com.example.trial.ui.screens

import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val historial by viewModel.historialFiltrado.collectAsState()

    val categories = listOf(
        0 to "Todas",
        1 to "Comida",
        2 to "Transporte",
        3 to "Servicios",
        4 to "Ocio",
        5 to "Otros",
        6 to "Ingresos"
    )

    // 🔥 Toda la pantalla scrolleable
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Historial de Transacciones",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        item {
            var selectedCategory by remember { mutableStateOf(0) }

            FiltrosDialog(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { id ->
                    selectedCategory = id
                    viewModel.setFiltroCategoria(id)
                },
                onMontoMin = { viewModel.setMontoMin(it) },
                onMontoMax = { viewModel.setMontoMax(it) },
                onFechaMin = { viewModel.setFechaMin(it) },
                onFechaMax = { viewModel.setFechaMax(it) }
            )
        }


        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                if (historial.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay transacciones",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historial) { transaccion ->
                            HistorialItem(transaccion, viewModel)
                        }
                    }
                }
            }
        }

        item {
            Divider()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosDialog(
    categories: List<Pair<Int, String>>,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    onMontoMin: (String) -> Unit,
    onMontoMax: (String) -> Unit,
    onFechaMin: (Long?) -> Unit,
    onFechaMax: (Long?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    var montoMin by remember { mutableStateOf("") }
    var montoMax by remember { mutableStateOf("") }

    var fechaMin by remember { mutableStateOf<Long?>(null) }
    var fechaMax by remember { mutableStateOf<Long?>(null) }


    // Control de DatePicker
    var showDatePickerMin by remember { mutableStateOf(false) }
    var showDatePickerMax by remember { mutableStateOf(false) }

    // Botón para abrir filtros
    TextButton(onClick = { showDialog = true }) {
        Text("Filtro")
    }

    // ----------------------------
    // DATE PICKER FECHA INICIO
    // ----------------------------
    if (showDatePickerMin) {
        DatePickerDialogCustom(
            title = "Selecciona fecha de inicio",
            onConfirm = { millis ->
                fechaMin = millis
                onFechaMin(millis)
                showDatePickerMin = false
            },
            onDismiss = { showDatePickerMin = false }
        )
    }

    // ----------------------------
    // DATE PICKER FECHA FIN
    // ----------------------------
    if (showDatePickerMax) {
        DatePickerDialogCustom(
            title = "Selecciona fecha final",
            onConfirm = { millis ->
                fechaMax = millis
                onFechaMax(millis)
                showDatePickerMax = false
            },
            onDismiss = { showDatePickerMax = false }
        )
    }

    // ----------------------------
    // ALERTDIALOG PRINCIPAL
    // ----------------------------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ajusta los filtros") },
            text = {
                Column {

                    // CATEGORÍAS
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categorías", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))

                        Box(Modifier.weight(1f)) {
                            TextButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(categories.firstOrNull { it.first == selectedCategory }?.second ?: "Todas")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
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

                    Spacer(Modifier.height(12.dp))

                    // MONTO MÍNIMO
                    NumericTextBox(
                        value = montoMin,
                        onValueChange = {
                            montoMin = it
                            onMontoMin(it)
                        },
                        label = "Monto mínimo"
                    )

                    Spacer(Modifier.height(12.dp))

                    // MONTO MÁXIMO
                    NumericTextBox(
                        value = montoMax,
                        onValueChange = {
                            montoMax = it
                            onMontoMax(it)
                        },
                        label = "Monto máximo"
                    )

                    Spacer(Modifier.height(12.dp))

                    // FECHA MÍNIMA
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Fecha inicio", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))

                        TextButton(onClick = { showDatePickerMin = true }) {
                            Text(fechaMin?.let { formatoFecha(it) } ?: "Seleccionar")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // FECHA MÁXIMA
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Fecha final", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))

                        TextButton(onClick = { showDatePickerMax = true }) {
                            Text(fechaMax?.let { formatoFecha(it) } ?: "Seleccionar")
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
                TextButton(onClick = {
                    montoMin = ""
                    montoMax = ""
                    fechaMin = null
                    fechaMax = null

                    onMontoMin("")
                    onMontoMax("")
                    onFechaMin(null)
                    onFechaMax(null)

                    onCategorySelected(0)

                    showDialog = false
                }) {
                    Text("Eliminar Filtros")
                }
            }
        )
    }
}

@Composable
fun NumericTextBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogCustom(
    title: String,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title) },
        text = {
            DatePicker(state = state)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onConfirm(it) }
                }
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

fun formatoFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es", "BO"))
    return sdf.format(Date(timestamp))
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
