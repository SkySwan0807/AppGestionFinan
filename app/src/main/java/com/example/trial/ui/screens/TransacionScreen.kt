package com.example.trial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.ui.theme.*
import com.example.trial.ui.viewmodels.TransaccionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: TransaccionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categoriasFiltradas.collectAsState()
    val eliminarDialog by viewModel.eliminarDialog.collectAsState()
    val transaccionAEliminar by viewModel.transaccionAEliminar.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var isIngreso by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            delay(2000)
            viewModel.clearSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo Registro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        QuickExpenseButtons(viewModel = viewModel)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Registro Manual",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isIngreso) "Ingreso" else "Gasto",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isIngreso,
                onCheckedChange = {
                    isIngreso = it
                    viewModel.onIncomeToggle(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GreenSuccess,
                    uncheckedThumbColor = RedWarning
                )
            )
        }

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Monto (BoB)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null && uiState.amount.toDoubleOrNull() == null
        )

        if (!isIngreso) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = uiState.categoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.onCategoryChange(
                                    categoria.idCategoria,
                                    categoria.nombre
                                )
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = "Ingreso",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.onNoteChange(it) },
            label = { Text("Nota (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Button(
            onClick = { viewModel.addTransaccion() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading && uiState.amount.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = if (isIngreso) "Añadir Ingreso" else "Añadir Gasto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RedWarning.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(12.dp),
                    color = RedWarning,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (uiState.showSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GreenSuccess.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = GreenSuccess
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIngreso) "¡Ingreso registrado!" else "¡Gasto registrado!",
                        color = GreenSuccess,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transacciones recientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.transacciones,
                key = { it.idTransaccion }
            ) { transaccion ->
                TransaccionItem(
                    transaccion = transaccion,
                    onEliminarClick = { viewModel.onEliminarTransaccion(transaccion) }
                )
            }
        }
    }

    if (eliminarDialog && transaccionAEliminar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminar() },
            title = { Text("Eliminar transacción") },
            text = { Text("¿Seguro que quieres eliminar esta transacción?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarEliminar() }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminar() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TransaccionItem(
    transaccion: TransaccionEntity,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = transaccion.descripcion ?: "Sin descripción")
                Text(text = transaccion.monto.toString())
            }
            IconButton(onClick = onEliminarClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun QuickExpenseButtons(viewModel: TransaccionViewModel) {
    val categories = listOf(
        1 to listOf(10.0, 20.0, 50.0),
        2 to listOf(1.0, 2.0, 2.5),
        3 to listOf(50.0, 100.0, 200.0),
        4 to listOf(30.0, 50.0, 100.0),
        5 to listOf(10.0, 25.0, 50.0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gastos Rápidos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        categories.forEach { (categoryId, amounts) ->
            CategoryQuickButtons(
                category = viewModel.getCategoryNameById(categoryId),
                amounts = amounts,
                color = getCategoryColor(categoryId),
                onAmountClick = { amount ->
                    viewModel.addQuickTransaccion(amount, categoryId)
                }
            )
        }
    }
}

@Composable
fun CategoryQuickButtons(
    category: String,
    amounts: List<Double>,
    color: Color,
    onAmountClick: (Double) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            amounts.forEach { amount ->
                OutlinedButton(
                    onClick = { onAmountClick(amount) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = color
                    )
                ) {
                    Text(
                        text = "-${amount.formatDecimal()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun Double.formatDecimal(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        "%.1f".format(this)
    }
}
