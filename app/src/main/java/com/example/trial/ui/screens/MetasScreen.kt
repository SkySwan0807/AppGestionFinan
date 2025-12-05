package com.example.trial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.trial.ui.viewmodels.MetaAhorroViewModel
import com.example.trial.ui.viewmodels.MetaAhorroWithProgress

@Composable
fun GoalScreen(
    viewModel: MetaAhorroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categorias.collectAsState(emptyList())
    var showCreateForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TÍTULO
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

        // FORMULARIO
        if (showCreateForm) {
            item {
                CreateGoalForm(
                    viewModel = viewModel,
                    uiState = uiState,
                    categorias = categorias,
                    onSuccess = { showCreateForm = false }
                )
            }
        }

        // LISTA DE METAS
        item {
            Text(
                text = "Metas Activas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (uiState.metas.isEmpty()) {
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
            items(uiState.metas) { metaProgress ->
                GoalCard(
                    goalWithProgress = metaProgress,
                    categoriaNombre = categorias.firstOrNull { it.idCategoria == metaProgress.meta.idCategoria }?.nombre
                        ?: "Categoría",
                    onDelete = { viewModel.deleteMeta(metaProgress.meta) }
                )
            }
        }
    }
}

// ---------------------------------------------------------
// FORMULARIO
// ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalForm(
    viewModel: MetaAhorroViewModel,
    uiState: com.example.trial.ui.viewmodels.MetaAhorroUiState,
    categorias: List<com.example.trial.data.local.entities.CategoriaEntity>,
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Nueva Meta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // -------------------------------
            // SELECTOR DE CATEGORÍA
            // -------------------------------
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = categorias.firstOrNull { it.idCategoria == uiState.categoryId }?.nombre
                        ?: "Seleccionar...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.onCategoryChange(categoria.idCategoria)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // MONTO
            OutlinedTextField(
                value = uiState.targetAmount,
                onValueChange = { viewModel.onTargetAmountChange(it) },
                label = { Text("Monto límite (BOB)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // MESES
            OutlinedTextField(
                value = uiState.months,
                onValueChange = { viewModel.onMonthsChange(it) },
                label = { Text("Plazo (meses)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // DESCRIPCIÓN
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // BOTÓN CREAR
            Button(
                onClick = { viewModel.createMeta() },
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

            // ERROR
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

// ---------------------------------------------------------
// CARD DE METAS
// ---------------------------------------------------------

@Composable
fun GoalCard(
    goalWithProgress: MetaAhorroWithProgress,
    categoriaNombre: String,
    onDelete: () -> Unit
) {
    val goal = goalWithProgress.meta
    val progress = goalWithProgress.progress
    val color = getCategoryColor(goal.idCategoria)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoriaNombre,
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

            // Descripción
            if (!goal.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = goal.descripcion ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${goal.montoActual} BOB",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "de ${goal.monto} BOB",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = GreenSuccess,
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
