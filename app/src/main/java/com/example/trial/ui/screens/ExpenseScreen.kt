package com.example.trial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.ui.theme.*
import com.example.trial.ui.viewmodels.ExpenseViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // Mostrar Snackbar de éxito
    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            kotlinx.coroutines.delay(2000)
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

        // Botones rápidos por categoría
        Text(
            text = "Registro Rápido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        QuickExpenseButtons(viewModel = viewModel)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Formulario manual
        Text(
            text = "Registro Manual",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // Campo de monto
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = { viewModel.onAmountChange(it) },
            label = { Text("Monto (BoB)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null && uiState.amount.toDoubleOrNull() == null
        )

        // Selector de categoría
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.category,
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
                viewModel.getCategories().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            viewModel.onCategoryChange(category)
                            expanded = false
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(getCategoryColor(category))
                            )
                        }
                    )
                }
            }
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

        // Botón de añadir
        Button(
            onClick = { viewModel.addExpense() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Añadir Gasto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Mensaje de error
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RedWarning.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = uiState.errorMessage!!,
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
                        text = "¡Gasto registrado exitosamente!",
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
fun QuickExpenseButtons(viewModel: ExpenseViewModel) {
    val categories = listOf(
        "Alimentación" to listOf(10.0, 20.0, 50.0),
        "Transporte" to listOf(5.0, 10.0, 20.0),
        "Servicios" to listOf(50.0, 100.0, 200.0),
        "Ocio" to listOf(30.0, 50.0, 100.0),
        "Otros" to listOf(10.0, 25.0, 50.0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { (category, amounts) ->
            CategoryQuickButtons(
                category = category,
                amounts = amounts,
                color = getCategoryColor(category),
                onAmountClick = { amount ->
                    viewModel.addQuickExpense(amount, category)
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
                        text = "+${amount.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
