// Archivo: PantallaPublicidadExistente.kt
package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ================================
// MODELOS DE DATOS
// ================================

// Modelo para Spot simplificado para la selección
data class SpotSeleccion(
    val auid: String,
    val titulo: String,
    val estado: String,
    val inicio: String,
    val fin: String,
    val urlImagenLowerthirds: String,
    val asesor: String,
    val nombreArchivo: String
)

// Callback para cuando se selecciona un spot
data class SpotSeleccionado(
    val titulo: String,
    val fechaInicio: String,
    val fechaFinal: String,
    val urlImagen: String
)

// ================================
// COMPONENTE PRINCIPAL
// ================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPublicidadExistente(
    onSpotSeleccionado: (SpotSeleccionado) -> Unit,
    onDismiss: () -> Unit
) {
    var spots by remember { mutableStateOf<List<SpotSeleccion>>(emptyList()) }
    var spotsOriginal by remember { mutableStateOf<List<SpotSeleccion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var filtroActivo by remember { mutableStateOf("TODOS") }
    var busqueda by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cargar spots al iniciar
    LaunchedEffect(Unit) {
        cargarSpotsConImagenes(
            onSuccess = { listaSpots ->
                spotsOriginal = listaSpots
                spots = listaSpots
                isLoading = false
            },
            onError = { errorMsg ->
                error = errorMsg
                isLoading = false
            }
        )
    }

    // Aplicar filtros cuando cambie el filtro o la búsqueda
    LaunchedEffect(filtroActivo, busqueda) {
        spots = aplicarFiltros(spotsOriginal, filtroActivo, busqueda)
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seleccionar Spot Existente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Spots con imágenes válidas: ${spots.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de búsqueda
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar spot...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtros = listOf("TODOS", "ACTIVOS", "PROXIMOS", "INACTIVOS")
                    filtros.forEach { filtro ->
                        FilterChip(
                            onClick = { filtroActivo = filtro },
                            label = { Text(filtro) },
                            selected = filtroActivo == filtro,
                            leadingIcon = {
                                when (filtro) {
                                    "ACTIVOS" -> Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "PROXIMOS" -> Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "INACTIVOS" -> Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    else -> Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido principal
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Cargando spots...")
                            }
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error cargando spots",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = error!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    spots.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = "Sin resultados",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No se encontraron spots",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (busqueda.isNotEmpty()) "Intenta con otros términos de búsqueda"
                                    else "No hay spots con imágenes válidas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        // Lista de spots
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(spots) { spot ->
                                SpotCard(
                                    spot = spot,
                                    onSeleccionar = {
                                        val spotSeleccionado = SpotSeleccionado(
                                            titulo = spot.titulo,
                                            fechaInicio = spot.inicio,
                                            fechaFinal = spot.fin,
                                            urlImagen = spot.urlImagenLowerthirds
                                        )
                                        onSpotSeleccionado(spotSeleccionado)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Snackbar para mensajes
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
}

// ================================
// COMPONENTE SPOT CARD
// ================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotCard(
    spot: SpotSeleccion,
    onSeleccionar: () -> Unit
) {
    Card(
        onClick = onSeleccionar,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del spot
            AsyncImage(
                model = spot.urlImagenLowerthirds,
                contentDescription = "Imagen del spot",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                // Nota: Debes tener estos recursos en tu proyecto o usar composables personalizados
                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                error = painterResource(android.R.drawable.ic_menu_report_image)
            )

            // Información del spot
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título
                Text(
                    text = spot.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Información adicional
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Fechas",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${spot.inicio} - ${spot.fin}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (spot.asesor.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Asesor",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = spot.asesor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Estado del spot
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (obtenerEstadoSpot(spot)) {
                            "ACTIVO" -> MaterialTheme.colorScheme.primaryContainer
                            "PRÓXIMO" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Text(
                            text = obtenerEstadoSpot(spot),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (obtenerEstadoSpot(spot)) {
                                "ACTIVO" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "PRÓXIMO" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
            }

            // Icono de selección
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ================================
// FUNCIONES DE UTILIDAD
// ================================

private fun cargarSpotsConImagenes(
    onSuccess: (List<SpotSeleccion>) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance().reference
    val spotsRef = database.child("CONTROLFM").child("CUÑAS_LINKS")

    spotsRef.get()
        .addOnSuccessListener { snapshot ->
            try {
                val spotsValidos = mutableListOf<SpotSeleccion>()

                for (auidSnapshot in snapshot.children) {
                    val auid = auidSnapshot.key ?: continue

                    // Extraer datos del spot
                    val titulo = auidSnapshot.child("TITULO").getValue(String::class.java)?.trim('"') ?: ""
                    val estado = auidSnapshot.child("ESTADO").getValue(String::class.java) ?: "0"
                    val inicio = auidSnapshot.child("INICIO").getValue(String::class.java) ?: ""
                    val fin = auidSnapshot.child("FIN").getValue(String::class.java) ?: ""
                    val urlImagenLowerthirds = auidSnapshot.child("urlImagenLowerthirds").getValue(String::class.java) ?: ""
                    val asesor = auidSnapshot.child("ASESOR").getValue(String::class.java) ?: ""
                    val nombreArchivo = auidSnapshot.child("NOMBRE_ARCHIVO").getValue(String::class.java) ?: ""

                    // Filtrar solo spots con imagen válida
                    if (urlImagenLowerthirds.isNotEmpty() && esUrlValida(urlImagenLowerthirds)) {
                        spotsValidos.add(
                            SpotSeleccion(
                                auid = auid,
                                titulo = titulo,
                                estado = estado,
                                inicio = inicio,
                                fin = fin,
                                urlImagenLowerthirds = urlImagenLowerthirds,
                                asesor = asesor,
                                nombreArchivo = nombreArchivo
                            )
                        )
                    }
                }

                // Ordenar por título
                val spotsOrdenados = spotsValidos.sortedBy { it.titulo }
                onSuccess(spotsOrdenados)

            } catch (e: Exception) {
                onError("Error procesando datos: ${e.message}")
            }
        }
        .addOnFailureListener { exception ->
            onError("Error cargando spots: ${exception.message}")
        }
}

private fun aplicarFiltros(
    spotsOriginal: List<SpotSeleccion>,
    filtro: String,
    busqueda: String
): List<SpotSeleccion> {
    var spotsFiltered = when (filtro) {
        "ACTIVOS" -> spotsOriginal.filter {
            it.estado == "1" && haSpotIniciado(it.inicio) && !haSpotCaducado(it.fin)
        }
        "PROXIMOS" -> spotsOriginal.filter {
            it.estado == "1" && !haSpotIniciado(it.inicio) && !haSpotCaducado(it.fin)
        }
        "INACTIVOS" -> spotsOriginal.filter {
            it.estado == "0" || haSpotCaducado(it.fin)
        }
        else -> spotsOriginal
    }

    // Aplicar búsqueda
    if (busqueda.isNotEmpty()) {
        spotsFiltered = spotsFiltered.filter {
            it.titulo.contains(busqueda, ignoreCase = true) ||
                    it.asesor.contains(busqueda, ignoreCase = true) ||
                    it.auid.contains(busqueda, ignoreCase = true)
        }
    }

    return spotsFiltered
}

private fun haSpotIniciado(fechaInicio: String): Boolean {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicioDate = formatter.parse(fechaInicio)
        val fechaActual = Date()
        fechaInicioDate?.before(fechaActual) ?: false
    } catch (e: Exception) {
        false
    }
}

private fun haSpotCaducado(fechaFin: String): Boolean {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFinDate = formatter.parse(fechaFin)
        val fechaActual = Date()
        fechaFinDate?.before(fechaActual) ?: false
    } catch (e: Exception) {
        false
    }
}

private fun obtenerEstadoSpot(spot: SpotSeleccion): String {
    return when {
        spot.estado == "1" && haSpotIniciado(spot.inicio) && !haSpotCaducado(spot.fin) -> "ACTIVO"
        spot.estado == "1" && !haSpotIniciado(spot.inicio) && !haSpotCaducado(spot.fin) -> "PRÓXIMO"
        else -> "INACTIVO"
    }
}

private fun esUrlValida(url: String): Boolean {
    return url.trim().let { cleanUrl ->
        cleanUrl.isNotEmpty() &&
                (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) &&
                (cleanUrl.contains(".jpg", ignoreCase = true) ||
                        cleanUrl.contains(".png", ignoreCase = true) ||
                        cleanUrl.contains(".jpeg", ignoreCase = true) ||
                        cleanUrl.contains(".webp", ignoreCase = true))
    }
}