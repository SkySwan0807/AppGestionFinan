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
import com.example.trial.ui.theme.*
import com.example.trial.ui.viewmodels.GoalViewModel
import com.example.trial.ui.viewmodels.GoalWithProgress

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Metas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showCreateForm = !showCreateForm },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (showCreateForm) "Cancelar" else "+ Nueva Meta")
                }
            }
        }

        // Formulario de crear meta
        if (showCreateForm) {
            item {
                CreateGoalForm(
                    viewModel = viewModel,
                    uiState = uiState,
                    onSuccess = { showCreateForm = false }
                )
            }
        }

        // Lista de metas activas
        item {
            Text(
                text = "Metas Activas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (uiState.goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes metas activas\n¡Crea una nueva!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(uiState.goals) { goalWithProgress ->
                GoalCard(
                    goalWithProgress = goalWithProgress,
                    onDelete = { viewModel.deleteGoal(goalWithProgress.goal) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalForm(
    viewModel: GoalViewModel,
    uiState: com.example.trial.ui.viewmodels.GoalUiState,
    onSuccess: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            onSuccess()
            viewModel.clearSuccess()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nueva Meta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
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
                    viewModel.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.onCategoryChange(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Monto objetivo
            OutlinedTextField(
                value = uiState.targetAmount,
                onValueChange = { viewModel.onTargetAmountChange(it) },
                label = { Text("Monto límite (BoB)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Plazo
            OutlinedTextField(
                value = uiState.months,
                onValueChange = { viewModel.onMonthsChange(it) },
                label = { Text("Plazo (meses)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            // Botón crear
            Button(
                onClick = { viewModel.createGoal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Crear Meta", fontWeight = FontWeight.Bold)
                }
            }

            // Error
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = RedWarning,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goalWithProgress: GoalWithProgress,
    onDelete: () -> Unit
) {
    val goal = goalWithProgress.goal
    val progress = goalWithProgress.progress
    val color = getCategoryColor(goal.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.category,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RedWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            if (!goal.description.isNullOrBlank()) {
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatCurrency(goal.currentAmount)} BoB",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "de ${formatCurrency(goal.targetAmount)} BoB",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    progress >= 1.0f -> RedWarning
                    progress >= 0.9f -> OrangeMedium
                    progress >= 0.7f -> Color(0xFFFFEB3B)
                    else -> GreenSuccess
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Info adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% usado",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        progress >= 0.9f -> RedWarning
                        progress >= 0.7f -> OrangeMedium
                        else -> GreenSuccess
                    }
                )
                Text(
                    text = "${goalWithProgress.remainingDays} días restantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Mensaje de alerta
            if (progress >= 0.9f) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (progress >= 1.0f) RedWarning.copy(alpha = 0.1f) 
                                       else OrangeMedium.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (progress >= 1.0f) 
                            "⚠️ ¡Superaste tu meta!" 
                        else 
                            "⚠️ Cerca del límite: ${formatCurrency(goalWithProgress.remainingAmount)} BoB restantes",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (progress >= 1.0f) RedWarning else OrangeMedium
                    )
                }
            }
        }
    }
}
