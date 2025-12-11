package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*



data class FiltrosUI(
    val selectedCategory: Int = 0,
    val selectedTipo: String = "",
    val montoMin: String = "",
    val montoMax: String = "",
    val fechaInicio: Long? = null,
    val fechaFinal: Long? = null
)

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosUI())

    // Flow con TODAS las transacciones
    val _historial: StateFlow<List<TransaccionEntity>> =
        transaccionRepository.getAllTransacciones() // Debes tener este DAO/Repo
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val historialFiltrado: StateFlow<List<TransaccionEntity>> =
        combine(_historial, _filtros) { lista, filtros ->
            val montoMin = filtros.montoMin.toDoubleOrNull()
            val montoMax = filtros.montoMax.toDoubleOrNull()

            lista.filter { trans ->
                val montoAbs = abs(trans.monto)

                val cumpleCategoria = filtros.selectedCategory == 0 || trans.idCategoria == filtros.selectedCategory
                val cumpleMin = montoMin?.let { montoAbs >= it } ?: true
                val cumpleMax = montoMax?.let { montoAbs <= it } ?: true

                val cumpleFechaInicio = filtros.fechaInicio?.let { trans.fecha >= it } ?: true
                val cumpleFechaFinal  = filtros.fechaFinal?.let { trans.fecha <= it } ?: true

                cumpleCategoria && cumpleMin && cumpleMax && cumpleFechaInicio && cumpleFechaFinal
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun setFiltroCategoria(categoriaId: Int) {
        _filtros.value = _filtros.value.copy(selectedCategory = categoriaId)
    }

    fun setMontoMin(monto: String) {
        _filtros.value = _filtros.value.copy(montoMin = monto)
    }

    fun setMontoMax(monto: String) {
        _filtros.value = _filtros.value.copy(montoMax = monto)
    }

    fun setFechaMin(fechaMillis: Long?) {
        _filtros.value = _filtros.value.copy(fechaInicio = fechaMillis)
    }

    fun setFechaMax(fechaMillis: Long?) {
        _filtros.value = _filtros.value.copy(fechaFinal = fechaMillis)
    }


    fun onDelete(transaccion: TransaccionEntity) {
        viewModelScope.launch {
            transaccionRepository.deleteTransaccion(transaccion)
        }
    }

    fun onEdit(transaccion: TransaccionEntity) {
        viewModelScope.launch {
            transaccionRepository.updateTransaccion(transaccion)
        }
    }
    fun exportarCSV(context: Context, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transacciones = historialFiltrado.value
                if (transacciones.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("No hay datos para exportar")
                    }
                    return@launch
                }

                // Crear nombre del archivo con fecha
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "transacciones_${dateFormat.format(Date())}.csv"

                // Usar directorio cache de la app (no requiere permisos)
                val cacheDir = File(context.cacheDir, "exports")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                val file = File(cacheDir, fileName)

                // Escribir datos en CSV
                PrintWriter(file).use { writer ->
                    // Cabeceras
                    writer.println("ID,Descripción,Monto,Fecha,Categoría,Tipo")

                    // Datos
                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    transacciones.forEach { trans ->
                        writer.println(
                            "${trans.idTransaccion}," +
                                    "\"${trans.descripcion ?: ""}\"," +
                                    "${trans.monto}," +
                                    "\"${dateFormatter.format(Date(trans.fecha))}\"," +
                                    "${getCategoriaNombre(trans.idCategoria)}," +
                                    "${if (trans.monto >= 0) "Ingreso" else "Gasto"}"
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    onSuccess(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error al exportar CSV: ${e.message}")
                }
            }
        }
    }

    fun exportarPDF(context: Context, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transacciones = historialFiltrado.value
                if (transacciones.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("No hay datos para exportar")
                    }
                    return@launch
                }

                // Crear documento PDF
                val document = PdfDocument()

                // Crear una página A4 (595 x 842 puntos)
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                var page = document.startPage(pageInfo)
                var canvas = page.canvas

                val paint = android.graphics.Paint()
                paint.textSize = 12f

                var yPos = 50f
                val margin = 40f
                val columnWidth = 100f
                var pageNumber = 1

                // Título
                paint.textSize = 16f
                paint.isFakeBoldText = true
                canvas.drawText("Historial de Transacciones", margin, yPos, paint)
                yPos += 30f

                // Fecha de generación
                paint.textSize = 10f
                paint.isFakeBoldText = false
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                canvas.drawText("Generado: ${dateFormat.format(Date())}", margin, yPos, paint)
                yPos += 20f

                // Cabeceras de tabla
                paint.textSize = 12f
                paint.isFakeBoldText = true
                canvas.drawText("Fecha", margin, yPos, paint)
                canvas.drawText("Descripción", margin + columnWidth, yPos, paint)
                canvas.drawText("Categoría", margin + columnWidth * 2, yPos, paint)
                canvas.drawText("Monto", margin + columnWidth * 3, yPos, paint)
                yPos += 20f

                // Línea separadora
                paint.strokeWidth = 1f
                canvas.drawLine(margin, yPos - 5, margin + columnWidth * 4, yPos - 5, paint)

                // Datos
                paint.textSize = 11f
                paint.isFakeBoldText = false
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "BO"))

                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                transacciones.forEach { trans ->
                    // Nueva página si se necesita
                    if (yPos > 800f) {
                        document.finishPage(page)
                        pageNumber++
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPos = 50f
                        
                        // Recrear cabeceras
                        paint.textSize = 12f
                        paint.isFakeBoldText = true
                        canvas.drawText("Fecha", margin, yPos, paint)
                        canvas.drawText("Descripción", margin + columnWidth, yPos, paint)
                        canvas.drawText("Categoría", margin + columnWidth * 2, yPos, paint)
                        canvas.drawText("Monto", margin + columnWidth * 3, yPos, paint)
                        yPos += 20f
                        paint.textSize = 11f
                        paint.isFakeBoldText = false
                    }
                    
                    paint.color = 0xFF000000.toInt()
                    canvas.drawText(dateFormatter.format(Date(trans.fecha)), margin, yPos, paint)
                    
                    val desc = trans.descripcion ?: ""
                    val descRecortada = if (desc.length > 15) desc.substring(0, 15) + "..." else desc
                    canvas.drawText(descRecortada, margin + columnWidth, yPos, paint)
                    canvas.drawText(getCategoriaNombre(trans.idCategoria), margin + columnWidth * 2, yPos, paint)

                    // Formatear monto con color
                    paint.color = if (trans.monto >= 0) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                    canvas.drawText(numberFormat.format(trans.monto), margin + columnWidth * 3, yPos, paint)

                    // Restaurar color negro
                    paint.color = 0xFF000000.toInt()
                    yPos += 20f
                }

                document.finishPage(page)

                // Crear nombre del archivo
                val fileName = "transacciones_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
                
                // Usar directorio cache de la app (no requiere permisos)
                val cacheDir = File(context.cacheDir, "exports")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                val file = File(cacheDir, fileName)

                // Guardar PDF
                document.writeTo(FileOutputStream(file))
                document.close()

                withContext(Dispatchers.Main) {
                    onSuccess(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error al exportar PDF: ${e.message}")
                }
            }
        }
    }
    private fun getCategoriaNombre(idCategoria: Int): String {
        return when (idCategoria) {
            1 -> "Comida"
            2 -> "Transporte"
            3 -> "Servicios"
            4 -> "Ocio"
            5 -> "Otros"
            6 -> "Ingresos"
            else -> "Desconocido"
        }
    }
}
