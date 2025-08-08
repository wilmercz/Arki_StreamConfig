// Archivo: DatePickerUtils.kt
package com.wilsoft.arki_streamconfig

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun DatePicker(initialDate: Calendar, onDateSelected: (Calendar) -> Unit) {
    val context = LocalContext.current
    val year = initialDate.get(Calendar.YEAR)
    val month = initialDate.get(Calendar.MONTH)
    val day = initialDate.get(Calendar.DAY_OF_MONTH)

    // Estado para controlar el diálogo
    var showDialog by remember { mutableStateOf(false) }

    // Abrir DatePickerDialog
    if (showDialog) {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(selectedDate)
                showDialog = false
            },
            year, month, day
        ).show()
    }

    // Botón para abrir el selector de fecha
    Button(onClick = { showDialog = true }) {
        Text(text = "Seleccionar Fecha")
    }
}