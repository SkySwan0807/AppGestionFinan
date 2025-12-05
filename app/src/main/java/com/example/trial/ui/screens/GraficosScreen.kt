package com.example.trial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trial.data.local.dao.CategorySum
import com.example.trial.ui.viewmodels.CategorySumFecha
import com.example.trial.ui.viewmodels.GraficosViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import com.github.mikephil.charting.data.Entry


@Composable
fun GraficosScreen(
    viewModel: GraficosViewModel = hiltViewModel()
) {
    val gastoPorCategoria by viewModel.gastoDeCategorias.collectAsState()
    val gastoPorCategoriaPorDia by viewModel.ingresosTotales.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Analisis de Datos",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            item {
                Text(
                    text = "Gasto por Categoría",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (gastoPorCategoria.isEmpty()) {
                    Text("No hay datos para mostrar")
                } else {
                    GastoPorCategoriaChart(gastoPorCategoria)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Variacion de Ingresos Por fecha",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (gastoPorCategoria.isEmpty()) {
                    Text("No hay datos para mostrar")
                } else {
                    EvolucionSaldoChart(gastoPorCategoriaPorDia ?: emptyList())

                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }
    }
}

@Composable
fun GastoPorCategoriaChart(
    gastoPorCategoria: List<CategorySum>
) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setFitBars(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val entries = gastoPorCategoria.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.total.toFloat())
            }

            val dataSet = BarDataSet(entries, "Gastos")

            // Asignar un color por barra
            val colors = gastoPorCategoria.map { item ->
                getCategoryColor(item.idCategoria).toArgb()
            }
            dataSet.colors = colors

            val barData = BarData(dataSet)
            barData.barWidth = 0.6f

            chart.data = barData
            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(gastoPorCategoria.map { it.category })
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@Composable
fun EvolucionSaldoChart(
    ingresosTotales: List<CategorySumFecha>
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                legend.isEnabled = false
            }
        },
        update = { chart ->

            // Ordenar por fecha, asegurándonos que fecha no sea null
            val sortedList = ingresosTotales.filter { it.fecha != null }.sortedBy { it.fecha!! }

            // Acumular saldo
            var acumulado = 0.0
            val entries = sortedList.mapIndexed { index, item ->
                acumulado += item.total
                Entry(index.toFloat(), acumulado.toFloat())
            }

            val dataSet = LineDataSet(entries, "Saldo")
            dataSet.setDrawValues(false)
            dataSet.lineWidth = 2f
            dataSet.setDrawCircles(true)
            dataSet.circleRadius = 4f
            dataSet.setCircleColors(sortedList.map { getCategoryColor(it.idCategoria).toArgb() })
            dataSet.color = getCategoryColor(sortedList.first().idCategoria).toArgb()

            val lineData = LineData(dataSet)
            chart.data = lineData

            // Etiquetas eje X como fecha
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                sortedList.map { item ->
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(item.fecha!!))
                }
            )

            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}



