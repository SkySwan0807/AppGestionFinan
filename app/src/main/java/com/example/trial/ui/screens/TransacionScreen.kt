package com.example.trial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    var expanded by remember { mutableStateOf(false) }
    var isIngreso by remember { mutableStateOf(false) }

    // Mostrar Snackbar de éxito
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
        // Título
        Text(
            text = "Nuevo Registro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // QuickExpenseButtons → siempre visibles, solo para gastos
        QuickExpenseButtons(viewModel = viewModel)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Registro manual
        Text(
            text = "Registro Manual",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // Switch ingreso/gasto
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isIngreso) "Ingreso " else "Gasto ",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIngreso) GreenSuccess else RedWarning
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

        // Campo de monto
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Monto (BoB)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null && uiState.amount.toDoubleOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

        )

        // Selector de categoría (solo si es gasto manual)
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                                viewModel.onCategoryChange(categoria.idCategoria, categoria.nombre)
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            // Mostrar categoría fija para ingresos
            OutlinedTextField(
                value = "Ingreso",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }

        // Campo de nota
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.onNoteChange(it) },
            label = { Text("Nota (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Botón de añadir registro manual
        Button(
            onClick = {
                viewModel.addTransaccion()
            },
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

        // Mensaje de error
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedWarning.copy(alpha = 0.1f)),
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

        // Mensaje de éxito
        if (uiState.showSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.1f)),
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
    }
}

@Composable
fun QuickExpenseButtons(viewModel: TransaccionViewModel) {
    val categories = listOf(
        1 to listOf(10.0, 20.0, 50.0),  // Alimentación
        2 to listOf(1.0, 2.0, 2.5),     // Transporte
        3 to listOf(50.0, 100.0, 200.0), // Servicios
        4 to listOf(30.0, 50.0, 100.0),  // Ocio
        5 to listOf(10.0, 25.0, 50.0)    // Otros
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Encabezado con color y nombre de categoría
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

        // Botones de montos
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

// Función de extensión para formatear decimales
private fun Double.formatDecimal(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        "%.1f".format(this)
    }
}
