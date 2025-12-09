package com.example.trial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.ui.theme.*
import com.example.trial.ui.viewmodels.MetaAhorroViewModel
import com.example.trial.ui.viewmodels.MetaAhorroWithProgress
import com.example.trial.ui.viewmodels.MetaFilter
import com.example.trial.ui.viewmodels.MetaSortField

@Composable
fun GoalScreen(
    viewModel: MetaAhorroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorias by viewModel.categorias.collectAsState(emptyList())
    var showCreateForm by remember { mutableStateOf(false) }
    
    // Mostrar formulario si estamos en modo edición o si el usuario lo activó
    val displayForm = uiState.isEditMode || showCreateForm

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
                    onClick = { 
                        if (uiState.isEditMode) {
                            viewModel.cancelEditing()
                        } else {
                            showCreateForm = !showCreateForm
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (uiState.isEditMode) "Cancelar Edición" 
                        else if (displayForm) "Cancelar" 
                        else "+ Nueva Meta"
                    )
                }
            }
        }

        // FILTROS
        item {
            FilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterChange = { viewModel.setFilter(it) }
            )
        }

        // ORDENAMIENTO
        item {
            SortSelector(
                selectedSort = uiState.sortBy,
                onSortChange = { viewModel.setSortField(it) }
            )
        }

        // FORMULARIO
        if (displayForm) {
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
                    onDelete = { viewModel.showDeleteConfirmation(metaProgress.meta) },
                    onEdit = { viewModel.startEditing(metaProgress.meta) },
                    onMarkCompleted = { viewModel.markAsCompleted(metaProgress.meta) },
                    onMarkCancelled = { viewModel.markAsCancelled(metaProgress.meta) }
                )
            }
        }
    }

    // DIALOG DE CONFIRMACIÓN DE ELIMINACIÓN
    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            metaNombre = uiState.metaToDelete?.nombre ?: "esta meta",
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // FECHA LÍMITE (DatePicker)
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.endDateMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val now = System.currentTimeMillis()
                        val maxDate = now + (100L * 365 * 24 * 60 * 60 * 1000) // 100 años
                        return utcTimeMillis >= now && utcTimeMillis <= maxDate
                    }
                }
            )
            
            var showDatePicker by remember { mutableStateOf(false) }
            val dateFormatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
            
            OutlinedTextField(
                value = if (uiState.endDateMillis != null) {
                    dateFormatter.format(java.util.Date(uiState.endDateMillis!!))
                } else {
                    ""
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha límite") },
                placeholder = { Text("Selecciona una fecha") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    viewModel.onEndDateChange(it)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // DESCRIPCIÓN
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // BOTÓN GUARDAR
            Button(
                onClick = { viewModel.saveMeta() },
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
                    Text(
                        if (uiState.isEditMode) "Guardar Cambios" else "Crear Meta",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ERROR
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = RedWarning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = RedWarning,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar error",
                                tint = RedWarning
                            )
                        }
                    }
                }
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
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkCancelled: () -> Unit
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

                Row {
                    // Botón Editar
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Menú de opciones
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Marcar como completada") },
                            onClick = {
                                onMarkCompleted()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancelar meta") },
                            onClick = {
                                onMarkCancelled()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Cancel, null, tint = OrangeMedium)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                onDelete()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = RedWarning)
                            }
                        )
                    }
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

// ---------------------------------------------------------
// CHIPS DE FILTROS
// ---------------------------------------------------------

@Composable
fun FilterChips(
    selectedFilter: MetaFilter,
    onFilterChange: (MetaFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetaFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { 
                    Text(
                        text = when (filter) {
                            MetaFilter.TODAS -> "Todas"
                            MetaFilter.ACTIVAS -> "Activas"
                            MetaFilter.COMPLETADAS -> "Completadas"
                            MetaFilter.CANCELADAS -> "Canceladas"
                            MetaFilter.NO_CUMPLIDAS -> "No cumplidas"
                            MetaFilter.VIGENTES -> "Vigentes"
                        }
                    ) 
                },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

// ---------------------------------------------------------
// DIALOG DE CONFIRMACIÓN DE ELIMINACIÓN
// ---------------------------------------------------------

@Composable
fun DeleteConfirmationDialog(
    metaNombre: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = RedWarning,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar meta?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar la meta \"$metaNombre\"? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedWarning
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ---------------------------------------------------------
// SELECTOR DE ORDENAMIENTO
// ---------------------------------------------------------

@Composable
fun SortSelector(
    selectedSort: MetaSortField,
    onSortChange: (MetaSortField) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ordenar por:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (selectedSort) {
                        MetaSortField.FECHA_OBJETIVO -> "Fecha"
                        MetaSortField.PROGRESO -> "Progreso"
                        MetaSortField.MONTO -> "Monto"
                    }
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Ordenar"
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Por Fecha objetivo") },
                    onClick = {
                        onSortChange(MetaSortField.FECHA_OBJETIVO)
                        expanded = false
                    },
                    leadingIcon = {
                        if (selectedSort == MetaSortField.FECHA_OBJETIVO) {
                            Icon(Icons.Default.Check, null)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Por Progreso") },
                    onClick = {
                        onSortChange(MetaSortField.PROGRESO)
                        expanded = false
                    },
                    leadingIcon = {
                        if (selectedSort == MetaSortField.PROGRESO) {
                            Icon(Icons.Default.Check, null)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Por Monto") },
                    onClick = {
                        onSortChange(MetaSortField.MONTO)
                        expanded = false
                    },
                    leadingIcon = {
                        if (selectedSort == MetaSortField.MONTO) {
                            Icon(Icons.Default.Check, null)
                        }
                    }
                )
            }
        }
    }
}

