package com.wilsoft.arki_streamconfig

// Importaciones principales
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log // Para el manejo de logs
// Importaciones adicionales
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay

val MaterialTheme.warningColor: Color
    @Composable
    get() = Color(0xFFFF9800) // Color naranja para warning

// Data classes optimizadas
data class Entrevistado(
    val id: String = "",
    val nombre: String = "",
    val rol: String = "",
    val tema: String = "",
    val fecha: String = "", // Formato YYYY-MM-DD para facilitar filtros
    val lugar: String = "Estudio Principal", // Campo nuevo
    val timestamp: Long = System.currentTimeMillis(),
    val activo: Boolean = true // Para soft delete
)

// Enum para los estados del botón
enum class AiringButtonState {
    NORMAL,      // Estado normal
    COUNTDOWN,   // Contando hacia atrás antes de enviar
    PROCESSING,  // Enviando a Firebase
    ON_AIR       // Actualmente al aire
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGeneradorCaracteres(
    navController: NavController,
    firebaseRepository: FirebaseRepository
) {
    // ================================
    // DECLARACIÓN DE ESTADOS PRINCIPALES
    // ================================

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var invitadosList by remember { mutableStateOf<List<Invitado>>(emptyList()) }
    var nombrePerfil by remember { mutableStateOf("") }
    var colorFondo1 by remember { mutableStateOf(Color.White) }
    var colorLetra1 by remember { mutableStateOf(Color.Black) }

    // Estados de campos de texto
    var invitadoNombre by remember { mutableStateOf("") }
    var invitadoRol by remember { mutableStateOf("") }
    var tema by remember { mutableStateOf("") }
    var subTema by remember { mutableStateOf("") }
    var rutaLogo by remember { mutableStateOf("") }
    var rutaPublicidad by remember { mutableStateOf("") }

    // Estados para publicidades
    var publicidadesList by remember { mutableStateOf(listOf<String>()) }
    var selectedPublicidad by remember { mutableStateOf("") }
    var expandedPublicidades by remember { mutableStateOf(false) }

    // Estados antiguos de switches (mantener compatibilidad)
    var mostrarInvitado by remember { mutableStateOf(false) }
    var mostrarTema by remember { mutableStateOf(false) }
    var mostrarSubTema by remember { mutableStateOf(false) }
    var mostrarLogo by remember { mutableStateOf(false) }
    var mostrarPublicidad by remember { mutableStateOf(false) }

    // ================================
    // NUEVOS ESTADOS PARA SWITCHES MEJORADOS
    // ================================

    // Estados para switches con control de múltiples clics - LOWER THIRD
    var lowerThirdEnabled by remember { mutableStateOf(true) }
    var lowerThirdChecked by remember { mutableStateOf(false) }
    var lowerThirdProcessing by remember { mutableStateOf(false) }

    // Estados para switches con control de múltiples clics - TEMA
    var temaEnabled by remember { mutableStateOf(true) }
    var temaChecked by remember { mutableStateOf(false) }
    var temaProcessing by remember { mutableStateOf(false) }

    // Estados para switches con control de múltiples clics - LOGO
    var logoEnabled by remember { mutableStateOf(true) }
    var logoChecked by remember { mutableStateOf(false) }
    var logoProcessing by remember { mutableStateOf(false) }

    // Estados para switches con control de múltiples clics - PUBLICIDAD
    var publicidadEnabled by remember { mutableStateOf(true) }
    var publicidadChecked by remember { mutableStateOf(false) }
    var publicidadProcessing by remember { mutableStateOf(false) }

    // Estados para diálogos
    var showNombreDialog by remember { mutableStateOf(false) }
    var showRolDialog by remember { mutableStateOf(false) }
    var showTemaDialog by remember { mutableStateOf(false) }

    // Estados para tabs
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Entrevistas", "Publicidad", "Control")
    val tabIcons = listOf(
        Icons.Default.People,      // 👥 Para entrevistas
        Icons.Default.Campaign,    // 📺 Para publicidad
        Icons.Default.Dashboard    // 🎛️ Para control de switches
    )

    // Estados adicionales para la nueva estructura
    var selectedPlace by remember { mutableStateOf("Estudio Principal") }
    val availablePlaces = listOf("Estudio Principal", "Estudio 2", "Sala de Conferencias", "Exterior")

    // Estados para filtros y expansión
    var filtroSeleccionado by remember { mutableStateOf("Hoy") } // "Hoy", "Mes", "Todos"
    var seccionEditorExpandida by remember { mutableStateOf(true) }

    // Estado para confirmación de poner al aire
    var showConfirmacionAire by remember { mutableStateOf(false) }
    var invitadoParaAire by remember { mutableStateOf<Invitado?>(null) }

    // Lista unificada de invitados (eliminar redundancia)
    var listaUnificadaInvitados by remember { mutableStateOf<List<Invitado>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para distinguir entre agregar/editar
    var modoEdicion by remember { mutableStateOf(false) }
    var invitadoEnEdicion by remember { mutableStateOf<Invitado?>(null) }

    // Estados para confirmación de eliminación
    var showConfirmacionEliminar by remember { mutableStateOf(false) }
    var invitadoParaEliminar by remember { mutableStateOf<Invitado?>(null) }

    // Agregar un estado para mostrar estado de sincronización
    var estadoSincronizacion by remember { mutableStateOf("Conectado") }

    // Estados para el botón "Al Aire" inteligente
    var airingButtonState by remember { mutableStateOf(AiringButtonState.NORMAL) }
    var countdownTime by remember { mutableStateOf(0) }
    var airingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // ================================
    // FUNCIONES DE CONTROL
    // ================================

    // Función para limpiar campos excepto tema y lugar
    fun limpiarCampos() {
        invitadoNombre = ""
        invitadoRol = ""
        // tema y selectedPlace se mantienen
    }

    // Función para actualizar el nombre y rol cuando se selecciona un invitado (simplificada)
    fun onInvitadoSelected(invitado: Invitado) {
        invitadoNombre = invitado.nombre
        invitadoRol = invitado.rol
        tema = invitado.tema
        selectedPlace = invitado.lugar
    }

    // Función auxiliar para procesar snapshot de Firebase
    fun procesarSnapshotInvitados(snapshot: DataSnapshot): List<Invitado> {
        val invitados = mutableListOf<Invitado>()
        snapshot.children.forEach { child ->
            val invitadoData = child.value as? Map<String, Any>
            invitadoData?.let { data ->
                val invitado = Invitado(
                    id = child.key ?: "",
                    nombre = data["nombre"] as? String ?: "",
                    rol = data["rol"] as? String ?: "",
                    tema = data["tema"] as? String ?: "",
                    subTema = data["subTema"] as? String ?: "",
                    fecha = data["fecha"] as? String ?: "",
                    lugar = data["lugar"] as? String ?: "Estudio Principal",
                    timestamp = data["timestamp"] as? Long ?: 0L,
                    graficoInvitado = data["activo"] as? Boolean ?: true
                )
                if (invitado.graficoInvitado) { // Solo mostrar activos
                    invitados.add(invitado)
                }
            }
        }
        return invitados.sortedByDescending { it.timestamp }
    }


    // Función unificada para cargar invitados según filtro
    fun cargarInvitadosSegunFiltro(filtro: String) {
        when (filtro) {
            "Hoy" -> {
                val fechaHoy = Calendar.getInstance()
                val fechaStr = "${fechaHoy.get(Calendar.YEAR)}-${String.format("%02d", fechaHoy.get(Calendar.MONTH) + 1)}-${String.format("%02d", fechaHoy.get(Calendar.DAY_OF_MONTH))}"

                firebaseRepository.db.child("CLAVE_STREAM_FB/INVITADOS")
                    .orderByChild("fecha")
                    .equalTo(fechaStr)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            listaUnificadaInvitados = procesarSnapshotInvitados(snapshot)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            listaUnificadaInvitados = emptyList()
                        }
                    })
            }

            "Mes" -> {
                val currentDate = Calendar.getInstance()
                val year = currentDate.get(Calendar.YEAR)
                val month = String.format("%02d", currentDate.get(Calendar.MONTH) + 1)
                val startDate = "$year-$month-01"
                val endDate = "$year-$month-31"

                firebaseRepository.db.child("CLAVE_STREAM_FB/INVITADOS")
                    .orderByChild("fecha")
                    .startAt(startDate)
                    .endAt(endDate)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            listaUnificadaInvitados = procesarSnapshotInvitados(snapshot)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            listaUnificadaInvitados = emptyList()
                        }
                    })
            }

            "Todos" -> {
                firebaseRepository.db.child("CLAVE_STREAM_FB/INVITADOS")
                    .orderByChild("timestamp")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            listaUnificadaInvitados = procesarSnapshotInvitados(snapshot)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            listaUnificadaInvitados = emptyList()
                        }
                    })
            }
        }
    }


    // Función para poner al aire un invitado
    fun ponerInvitadoAlAire(invitado: Invitado) {
        val updates = mapOf(
            "Invitado" to invitado.nombre,
            "Rol" to invitado.rol,
            "Tema" to invitado.tema
        )

        firebaseRepository.saveData(
            "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
            updates,
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✅ Información actualizada en el aire: ${invitado.nombre}")
                }
                // Actualizar también los estados locales
                invitadoNombre = invitado.nombre
                invitadoRol = invitado.rol
                tema = invitado.tema
            },
            onFailure = { error ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("❌ Error al actualizar: ${error.message}")
                }
            }
        )
    }

    // Agregar esta función después de ponerInvitadoAlAire()
    fun ponerAlAireDirecto() {
        if (invitadoNombre.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("⚠️ El nombre es obligatorio")
            }
            return
        }

        val updates = mapOf(
            "Invitado" to invitadoNombre,
            "Rol" to invitadoRol,
            "Tema" to tema,
            "Mostrar_Invitado" to true  // 🔥 CLAVE: Pone al aire automáticamente
        )

        firebaseRepository.saveData(
            "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
            updates,
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("⚡ AL AIRE DIRECTO: $invitadoNombre")
                }
                // Sincronizar estados locales
                lowerThirdChecked = true
                mostrarInvitado = true
            },
            onFailure = { error ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("❌ Error: ${error.message}")
                }
            }
        )
    }

    fun actualizarDatosSinAire(invitado: Invitado) {
        val updates = mapOf(
            "Invitado" to invitado.nombre,
            "Rol" to invitado.rol,
            "Tema" to invitado.tema
            // 🔥 NO incluye "Mostrar_Invitado" = solo actualiza datos
        )

        firebaseRepository.saveData(
            "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
            updates,
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("📋 Datos actualizados: ${invitado.nombre}")
                }
                // Actualizar estados locales
                invitadoNombre = invitado.nombre
                invitadoRol = invitado.rol
                tema = invitado.tema
            },
            onFailure = { error ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("❌ Error al actualizar: ${error.message}")
                }
            }
        )
    }

    // ================================
    // FUNCIONES PARA SWITCHES MEJORADOS
    // ================================

    // Función para Lower Third / Invitado
    fun updateLowerThirdWithFeedback(
        value: Boolean,
        additionalUpdates: Map<String, Any> = emptyMap()
    ) {
        coroutineScope.launch {
            lowerThirdEnabled = false
            lowerThirdProcessing = true

            try {
                updateFirebaseField("Mostrar_Invitado", value, firebaseRepository)
                additionalUpdates.forEach { (field, fieldValue) ->
                    updateFirebaseField(field, fieldValue, firebaseRepository)
                }
                delay(2000) // Ajusta según tu servidor

                lowerThirdEnabled = true
                lowerThirdChecked = value
                lowerThirdProcessing = false

                // Actualizar también el estado antiguo para compatibilidad
                mostrarInvitado = value
            } catch (e: Exception) {
                lowerThirdEnabled = true
                lowerThirdChecked = !value
                lowerThirdProcessing = false
            }
        }
    }

    // Función para Tema
    fun updateTemaWithFeedback(
        value: Boolean,
        additionalUpdates: Map<String, Any> = emptyMap()
    ) {
        coroutineScope.launch {
            temaEnabled = false
            temaProcessing = true

            try {
                updateFirebaseField("Mostrar_Tema", value, firebaseRepository)
                additionalUpdates.forEach { (field, fieldValue) ->
                    updateFirebaseField(field, fieldValue, firebaseRepository)
                }
                delay(2000)

                temaEnabled = true
                temaChecked = value
                temaProcessing = false

                // Actualizar también el estado antiguo para compatibilidad
                mostrarTema = value
            } catch (e: Exception) {
                temaEnabled = true
                temaChecked = !value
                temaProcessing = false
            }
        }
    }

    // Función para Logo
    fun updateLogoWithFeedback(value: Boolean) {
        coroutineScope.launch {
            logoEnabled = false
            logoProcessing = true

            try {
                updateFirebaseField("Mostrar_Logo", value, firebaseRepository)
                delay(2000)

                logoEnabled = true
                logoChecked = value
                logoProcessing = false

                // Actualizar también el estado antiguo para compatibilidad
                mostrarLogo = value
            } catch (e: Exception) {
                logoEnabled = true
                logoChecked = !value
                logoProcessing = false
            }
        }
    }

    // Función para Publicidad
    fun updatePublicidadWithFeedback(
        value: Boolean,
        additionalUpdates: Map<String, Any> = emptyMap()
    ) {
        coroutineScope.launch {
            publicidadEnabled = false
            publicidadProcessing = true

            try {
                updateFirebaseField("Mostrar_Publicidad", value, firebaseRepository)
                additionalUpdates.forEach { (field, fieldValue) ->
                    updateFirebaseField(field, fieldValue, firebaseRepository)
                }
                delay(2000)

                publicidadEnabled = true
                publicidadChecked = value
                publicidadProcessing = false

                // Actualizar también el estado antiguo para compatibilidad
                mostrarPublicidad = value
            } catch (e: Exception) {
                publicidadEnabled = true
                publicidadChecked = !value
                publicidadProcessing = false
            }
        }
    }

    // 🆕 Función para cancelar edición
    fun cancelarEdicion() {
        modoEdicion = false
        invitadoEnEdicion = null
        limpiarCampos()
    }

    // Función para auto-desactivar switches cuando no hay contenido
    fun autoDisableSwitch(switchType: String, newValue: Boolean) {
        when (switchType) {
            "lowerThird" -> {
                if (!newValue && lowerThirdChecked) {
                    // Desactivar Lower Third automáticamente
                    updateLowerThirdWithFeedback(false)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("🔄 Lower Third desactivado: sin nombre de invitado")
                    }
                }
            }
            "tema" -> {
                if (!newValue && temaChecked) {
                    // Desactivar Tema automáticamente
                    updateTemaWithFeedback(false)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("🔄 Mostrar Tema desactivado: sin contenido de tema")
                    }
                }
            }
        }
    }

    // Función para ejecutar realmente el poner al aire
    fun executeAiring() {
        airingButtonState = AiringButtonState.PROCESSING

        val updates = mapOf(
            "Invitado" to invitadoNombre,
            "Rol" to invitadoRol,
            "Tema" to tema,
            "Mostrar_Invitado" to true,
            // Desactivar otros elementos
            "Mostrar_Tema" to false,
            "Mostrar_SubTema" to false,
            "Mostrar_Publicidad" to false
        )

        firebaseRepository.saveData(
            "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
            updates,
            onSuccess = {
                airingButtonState = AiringButtonState.NORMAL
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✅ ¡EN VIVO! - $invitadoNombre al aire")
                }
                // Los estados se actualizarán automáticamente por el listener
            },
            onFailure = { error ->
                airingButtonState = AiringButtonState.NORMAL
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("❌ Error al enviar: ${error.message}")
                }
            }
        )
    }

    // Función para iniciar el proceso de poner al aire
    fun startAiringProcess() {
        if (invitadoNombre.trim().isEmpty()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("⚠️ Agregue un nombre de invitado primero")
            }
            return
        }

        airingButtonState = AiringButtonState.COUNTDOWN
        countdownTime = 4 // 4 segundos de gracia para cancelar

        airingJob = coroutineScope.launch {
            // Countdown de 4 segundos
            repeat(4) {
                delay(1000)
                countdownTime -= 1

                if (airingButtonState != AiringButtonState.COUNTDOWN) {
                    return@launch // Cancelado
                }
            }

            // Si llegamos aquí, el usuario no canceló
            executeAiring()
        }
    }

    // Función para cancelar el proceso de poner al aire
    fun cancelAiringProcess() {
        airingJob?.cancel()
        airingButtonState = AiringButtonState.NORMAL
        countdownTime = 0

        coroutineScope.launch {
            snackbarHostState.showSnackbar("❌ Cancelado - No se envió al aire")
        }
    }


    // ================================
    // CONFIGURACIÓN DE LISTENERS Y CARGA INICIAL (OPTIMIZADA)
    // ================================

    // Configurar listener para actualizaciones automáticas según filtro
    LaunchedEffect(filtroSeleccionado) {
        cargarInvitadosSegunFiltro(filtroSeleccionado)
    }

    // Cargar datos iniciales del perfil
    LaunchedEffect(Unit) {
        // CARGAR PERFIL (mantener funcionalidad existente)
        firebaseRepository.loadStreamData(
            "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
            onSuccess = { data ->
                nombrePerfil = data["NombrePerfil"] as? String ?: "Sin Perfil"

                // Actualizar campos de texto
                invitadoNombre = data["Invitado"] as? String ?: ""
                invitadoRol = data["Rol"] as? String ?: ""
                tema = data["Tema"] as? String ?: ""
                subTema = data["SubTema"] as? String ?: ""
                rutaLogo = data["urlLogo"] as? String ?: ""
                rutaPublicidad = data["urlImagenPublicidad"] as? String ?: ""

                // Actualizar switches booleanos antiguos (mantener compatibilidad)
                mostrarInvitado = data["Mostrar_Invitado"] as? Boolean ?: false
                mostrarTema = data["Mostrar_Tema"] as? Boolean ?: false
                mostrarSubTema = data["Mostrar_SubTema"] as? Boolean ?: false
                mostrarLogo = data["Mostrar_Logo"] as? Boolean ?: false
                mostrarPublicidad = data["Mostrar_Publicidad"] as? Boolean ?: false

                // Inicializar nuevos estados simplificados
                lowerThirdChecked = data["Mostrar_Invitado"] as? Boolean ?: false
                temaChecked = data["Mostrar_Tema"] as? Boolean ?: false
                logoChecked = data["Mostrar_Logo"] as? Boolean ?: false
                publicidadChecked = data["Mostrar_Publicidad"] as? Boolean ?: false

                // Colores
                val colorFondo1String = data["colorFondo1"] as? String
                try {
                    colorFondo1 = Color(android.graphics.Color.parseColor(colorFondo1String ?: "#FFFFFF"))
                } catch (e: IllegalArgumentException) {
                    colorFondo1 = Color.White
                }

                val colorLetra1String = data["colorLetra1"] as? String
                try {
                    colorLetra1 = Color(android.graphics.Color.parseColor(colorLetra1String ?: "#000000"))
                } catch (e: IllegalArgumentException) {
                    colorLetra1 = Color.Black
                }
            },
            onFailure = { /* Manejar error */ }
        )
    }

    // Listener en tiempo real para sincronización automática de switches
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Solo actualizar si los datos han sido cargados inicialmente
                if (nombrePerfil.isNotEmpty()) {
                    try {
                        // Sincronizar switches desde Firebase
                        val nuevoMostrarInvitado = snapshot.child("Mostrar_Invitado").getValue(Boolean::class.java) ?: false
                        val nuevoMostrarTema = snapshot.child("Mostrar_Tema").getValue(Boolean::class.java) ?: false
                        val nuevoMostrarLogo = snapshot.child("Mostrar_Logo").getValue(Boolean::class.java) ?: false
                        val nuevoMostrarPublicidad = snapshot.child("Mostrar_Publicidad").getValue(Boolean::class.java) ?: false

                        // Sincronizar campos de texto
                        val nuevoInvitadoNombre = snapshot.child("Invitado").getValue(String::class.java) ?: ""
                        val nuevoInvitadoRol = snapshot.child("Rol").getValue(String::class.java) ?: ""
                        val nuevoTema = snapshot.child("Tema").getValue(String::class.java) ?: ""
                        val nuevoSubTema = snapshot.child("SubTema").getValue(String::class.java) ?: ""

                        // Actualizar campos de texto primero
                        if (nuevoInvitadoNombre != invitadoNombre) {
                            invitadoNombre = nuevoInvitadoNombre
                        }
                        if (nuevoInvitadoRol != invitadoRol) {
                            invitadoRol = nuevoInvitadoRol
                        }
                        if (nuevoTema != tema) {
                            tema = nuevoTema
                        }
                        if (nuevoSubTema != subTema) {
                            subTema = nuevoSubTema
                        }

                        // 🆕 LÓGICA DE AUTO-DESACTIVACIÓN CONDICIONAL
                        val hasInvitadoContent = nuevoInvitadoNombre.trim().isNotEmpty()
                        val hasTemaContent = nuevoTema.trim().isNotEmpty()

                        // Actualizar switches con validación de contenido
                        if (nuevoMostrarInvitado != lowerThirdChecked && !lowerThirdProcessing) {
                            // Si el switch debe estar activado pero no hay contenido, forzar desactivación
                            if (nuevoMostrarInvitado && !hasInvitadoContent) {
                                // Auto-desactivar en Firebase si no hay contenido
                                updateFirebaseField("Mostrar_Invitado", false, firebaseRepository)
                                lowerThirdChecked = false
                                mostrarInvitado = false
                            } else {
                                lowerThirdChecked = nuevoMostrarInvitado
                                mostrarInvitado = nuevoMostrarInvitado
                            }
                        }

                        if (nuevoMostrarTema != temaChecked && !temaProcessing) {
                            // Si el switch debe estar activado pero no hay contenido, forzar desactivación
                            if (nuevoMostrarTema && !hasTemaContent) {
                                // Auto-desactivar en Firebase si no hay contenido
                                updateFirebaseField("Mostrar_Tema", false, firebaseRepository)
                                temaChecked = false
                                mostrarTema = false
                            } else {
                                temaChecked = nuevoMostrarTema
                                mostrarTema = nuevoMostrarTema
                            }
                        }

                        // Logo y Publicidad no necesitan validación de contenido
                        if (nuevoMostrarLogo != logoChecked && !logoProcessing) {
                            logoChecked = nuevoMostrarLogo
                            mostrarLogo = nuevoMostrarLogo
                        }

                        if (nuevoMostrarPublicidad != publicidadChecked && !publicidadProcessing) {
                            publicidadChecked = nuevoMostrarPublicidad
                            mostrarPublicidad = nuevoMostrarPublicidad
                        }

                        // 🆕 CANCELAR COUNTDOWN AUTOMÁTICAMENTE SI ALGUIEN MÁS PUSO AL AIRE
                        if (nuevoMostrarInvitado && airingButtonState == AiringButtonState.COUNTDOWN) {
                            // Alguien más activó el Lower Third, cancelar nuestro countdown
                            airingJob?.cancel()
                            airingButtonState = AiringButtonState.NORMAL
                            countdownTime = 0
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🔄 Otro usuario puso al aire")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("SwitchSync", "Error sincronizando switches: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SwitchSync", "Error en listener de switches: ${error.message}")
            }
        }

        // Añadir el listener en tiempo real al path de gráficos
        val databaseRef = firebaseRepository.db.child("CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS")
        databaseRef.addValueEventListener(listener)

        // Limpiar el listener cuando se destruya el componente
        onDispose {
            databaseRef.removeEventListener(listener)
        }
    }

    // ================================
    // INTERFAZ DE USUARIO
    // ================================

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Generador de Caracteres")
                        Text(
                            text = nombrePerfil,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorLetra1
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = colorFondo1,
                    titleContentColor = colorLetra1
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ================================
            // BARRA DE TABS CON ICONOS
            // ================================
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            // ================================
            // CONTENIDO SEGÚN TAB SELECCIONADO
            // ================================
            when (selectedTabIndex) {

                // 👥 TAB 1: ENTREVISTAS
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Sección de Edición Rápida (Expandible/Contraible)
                        item {
                            ExpandableEditorCard(
                                expanded = seccionEditorExpandida,
                                onExpandedChange = { seccionEditorExpandida = it },
                                invitadoNombre = invitadoNombre,
                                invitadoRol = invitadoRol,
                                tema = tema,
                                selectedPlace = selectedPlace,
                                availablePlaces = availablePlaces,
                                onInvitadoNombreChange = { invitadoNombre = it },
                                onInvitadoRolChange = { invitadoRol = it },
                                onTemaChange = { tema = it },
                                onPlaceChange = { selectedPlace = it },
                                onShowNombreDialog = { showNombreDialog = true },
                                onShowRolDialog = { showRolDialog = true },
                                onShowTemaDialog = { showTemaDialog = true },
                                onLimpiarCampos = { limpiarCampos() },
                                onAgregar = {
                                    if (invitadoNombre.isNotBlank()) {
                                        coroutineScope.launch {
                                            if (modoEdicion && invitadoEnEdicion != null) {
                                                // 🆕 MODO EDICIÓN: Actualizar invitado existente
                                                actualizarInvitado(
                                                    invitadoId = invitadoEnEdicion!!.id,
                                                    invitadoNombre = invitadoNombre,
                                                    invitadoRol = invitadoRol,
                                                    tema = tema,
                                                    selectedPlace = selectedPlace,
                                                    firebaseRepository = firebaseRepository,
                                                    onSuccess = {
                                                        cargarInvitadosSegunFiltro(filtroSeleccionado)
                                                        // 🔧 ENVOLVER en coroutineScope.launch
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("✅ Invitado actualizado: $invitadoNombre")
                                                        }
                                                        // 🆕 Resetear modo edición
                                                        modoEdicion = false
                                                        invitadoEnEdicion = null
                                                        limpiarCampos()
                                                    },
                                                    onFailure = { error ->
                                                        // 🔧 ENVOLVER en coroutineScope.launch
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("❌ Error al actualizar: $error")
                                                        }
                                                    }
                                                )
                                            } else {
                                                // 🔄 MODO AGREGAR: Crear nuevo invitado (lógica existente)
                                                guardarInvitado(
                                                    invitadoNombre = invitadoNombre,
                                                    invitadoRol = invitadoRol,
                                                    tema = tema,
                                                    selectedPlace = selectedPlace,
                                                    firebaseRepository = firebaseRepository,
                                                    onSuccess = {
                                                        cargarInvitadosSegunFiltro(filtroSeleccionado)
                                                        // 🔧 ENVOLVER en coroutineScope.launch
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("✅ Invitado agregado: $invitadoNombre")
                                                        }
                                                        limpiarCampos()
                                                    },
                                                    onFailure = { error ->
                                                        // 🔧 ENVOLVER en coroutineScope.launch
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("❌ Error al guardar: $error")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },
                                // 🆕 Nuevos parámetros
                                modoEdicion = modoEdicion,
                                onCancelarEdicion = { cancelarEdicion() },
                                // 🆕 Parámetros para botón inteligente
                                isOnAir = lowerThirdChecked, // Basado en el estado del switch Lower Third
                                airingButtonState = airingButtonState,
                                countdownTime = countdownTime,
                                onStartAiring = { startAiringProcess() },
                                onCancelAiring = { cancelAiringProcess() }
                            )
                        }

                        // Lista Unificada de Invitados con Filtros
                        item {
                            ListaUnificadaInvitados(
                                filtroSeleccionado = filtroSeleccionado,
                                onFiltroChange = { nuevoFiltro ->
                                    filtroSeleccionado = nuevoFiltro
                                },
                                listaInvitados = listaUnificadaInvitados,
                                onInvitadoSelect = { invitado ->
                                    invitadoNombre = invitado.nombre
                                    invitadoRol = invitado.rol
                                    tema = invitado.tema
                                    selectedPlace = invitado.lugar
                                    // ✅ NUEVO: Activar modo edición
                                    modoEdicion = true
                                    invitadoEnEdicion = invitado

                                },
                                onActualizarDatos = { invitado -> // ← CAMBIAR DE onPonerAlAire
                                    actualizarDatosSinAire(invitado)
                                },
                                onEliminar = { invitado ->
                                    invitadoParaEliminar = invitado
                                    showConfirmacionEliminar = true
                                }
                            )
                        }
                    }
                }

                // 📺 TAB 2: PUBLICIDAD
                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            PublicidadCard(
                                publicidadesList = publicidadesList,
                                selectedPublicidad = selectedPublicidad,
                                expandedPublicidades = expandedPublicidades,
                                rutaPublicidad = rutaPublicidad,
                                mostrarPublicidad = mostrarPublicidad,
                                onExpandedChange = { expandedPublicidades = it },
                                onPublicidadSelect = { publicidad ->
                                    selectedPublicidad = publicidad
                                    expandedPublicidades = false
                                    // Aquí podrías cargar datos de la publicidad si tienes esa función
                                }
                            )
                        }
                    }
                }

                // 🎛️ TAB 3: CONTROL DE SWITCHES
                2 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SimplifiedSwitchCard(
                                // Lower Third
                                lowerThirdEnabled = lowerThirdEnabled,
                                lowerThirdChecked = lowerThirdChecked,
                                lowerThirdProcessing = lowerThirdProcessing,
                                onLowerThirdChange = { isChecked ->
                                    // Solo permitir activación si hay contenido de invitado
                                    if (isChecked && invitadoNombre.trim().isEmpty()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("⚠️ Agregue un nombre de invitado primero")
                                        }
                                        return@SimplifiedSwitchCard
                                    }

                                    val additionalUpdates = mapOf(
                                        "Mostrar_Tema" to false,
                                        "Mostrar_SubTema" to false,
                                        "Mostrar_Publicidad" to false,
                                        "Invitado" to invitadoNombre,
                                        "Rol" to invitadoRol,
                                        "Tema" to tema
                                    )
                                    updateLowerThirdWithFeedback(isChecked, additionalUpdates)
                                },

                                // Tema
                                temaEnabled = temaEnabled,
                                temaChecked = temaChecked,
                                temaProcessing = temaProcessing,
                                onTemaChange = { isChecked ->
                                    // Solo permitir activación si hay contenido de tema
                                    if (isChecked && tema.trim().isEmpty()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("⚠️ Agregue un tema primero")
                                        }
                                        return@SimplifiedSwitchCard
                                    }

                                    val additionalUpdates = mapOf(
                                        "Mostrar_Invitado" to false,
                                        "Mostrar_SubTema" to false,
                                        "Mostrar_Publicidad" to false,
                                        "Tema" to tema
                                    )
                                    updateTemaWithFeedback(isChecked, additionalUpdates)
                                },

                                // Logo
                                logoEnabled = logoEnabled,
                                logoChecked = logoChecked,
                                logoProcessing = logoProcessing,
                                onLogoChange = { isChecked ->
                                    updateLogoWithFeedback(isChecked)
                                },

                                // Publicidad
                                publicidadEnabled = publicidadEnabled,
                                publicidadChecked = publicidadChecked,
                                publicidadProcessing = publicidadProcessing,
                                onPublicidadChange = { isChecked ->
                                    val additionalUpdates = mapOf(
                                        "Mostrar_Invitado" to false,
                                        "Mostrar_Tema" to false,
                                        "Mostrar_SubTema" to false
                                    )
                                    updatePublicidadWithFeedback(isChecked, additionalUpdates)
                                },
                                // 🆕 INFORMACIÓN SINCRONIZADA (AGREGAR ESTAS LÍNEAS)
                                invitadoNombre = invitadoNombre,
                                invitadoRol = invitadoRol,
                                tema = tema,
                                // 🆕 FUNCIÓN DE AUTO-DESACTIVACIÓN
                                onAutoDisableSwitch = ::autoDisableSwitch
                            )
                        }
                    }
                }
            }
        }
    }

    // ================================
    // DIÁLOGOS MEJORADOS
    // ================================

    if (showNombreDialog) {
        TextFieldDialog(
            title = "Configurar Nombre",
            currentValue = invitadoNombre,
            onValueChange = { invitadoNombre = it },
            onDismiss = { showNombreDialog = false },
            onSaveToFirebase = {
                updateFirebaseField("Invitado", invitadoNombre, firebaseRepository)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Nombre guardado en Firebase")
                }
                showNombreDialog = false
            },
            firebaseRepository = firebaseRepository
        )
    }

    if (showRolDialog) {
        TextFieldDialog(
            title = "Configurar Rol",
            currentValue = invitadoRol,
            onValueChange = { invitadoRol = it },
            onDismiss = { showRolDialog = false },
            onSaveToFirebase = {
                updateFirebaseField("Rol", invitadoRol, firebaseRepository)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Rol guardado en Firebase")
                }
                showRolDialog = false
            },
            firebaseRepository = firebaseRepository
        )
    }

    if (showTemaDialog) {
        TextFieldDialog(
            title = "Configurar Tema",
            currentValue = tema,
            onValueChange = { tema = it },
            onDismiss = { showTemaDialog = false },
            onSaveToFirebase = {
                updateFirebaseField("Tema", tema, firebaseRepository)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Tema guardado en Firebase")
                }
                showTemaDialog = false
            },
            firebaseRepository = firebaseRepository
        )
    }

    // Diálogo de confirmación para eliminar invitado
    if (showConfirmacionEliminar && invitadoParaEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmacionEliminar = false
                invitadoParaEliminar = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Eliminar invitado")
                }
            },
            text = {
                Column {
                    Text("¿Está seguro que desea eliminar este invitado?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "👤 ${invitadoParaEliminar?.nombre}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "💼 ${invitadoParaEliminar?.rol}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!invitadoParaEliminar?.tema.isNullOrEmpty()) {
                                Text(
                                    text = "📋 ${invitadoParaEliminar?.tema}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "📅 ${invitadoParaEliminar?.fecha}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        invitadoParaEliminar?.let { invitado ->
                            eliminarInvitado(
                                invitadoId = invitado.id,
                                firebaseRepository = firebaseRepository,
                                onSuccess = {
                                    cargarInvitadosSegunFiltro(filtroSeleccionado) // Refrescar lista
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("✅ Invitado eliminado: ${invitado.nombre}")
                                    }
                                },
                                onFailure = { error ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("❌ Error al eliminar: $error")
                                    }
                                }
                            )
                        }
                        showConfirmacionEliminar = false
                        invitadoParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmacionEliminar = false
                        invitadoParaEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🆕 Diálogo de confirmación para poner invitado al aire
    if (showConfirmacionAire && invitadoParaAire != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmacionAire = false
                invitadoParaAire = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Poner al aire",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "¿Desea enviar la información de este invitado al aire?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview del invitado
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "👤 ${invitadoParaAire?.nombre ?: ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (!invitadoParaAire?.rol.isNullOrEmpty()) {
                                Text(
                                    text = "💼 ${invitadoParaAire?.rol}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            if (!invitadoParaAire?.tema.isNullOrEmpty()) {
                                Text(
                                    text = "📋 ${invitadoParaAire?.tema}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2
                                )
                            }

                            Text(
                                text = "📍 ${invitadoParaAire?.lugar ?: "Estudio Principal"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )

                            Text(
                                text = "📅 ${invitadoParaAire?.fecha ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📡 Esta información se enviará inmediatamente al servidor de transmisión en vivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        invitadoParaAire?.let { invitado ->
                            // 🔥 AQUÍ SE CONECTA CON LA FUNCIÓN EXISTENTE
                            ponerInvitadoAlAire(invitado)
                        }
                        // Cerrar diálogo
                        showConfirmacionAire = false
                        invitadoParaAire = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Poner al aire")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmacionAire = false
                        invitadoParaAire = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ================================
// COMPOSABLES AUXILIARES MEJORADOS
// ================================

@Composable
private fun ExpandableEditorCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    invitadoNombre: String,
    invitadoRol: String,
    tema: String,
    selectedPlace: String,
    availablePlaces: List<String>,
    onInvitadoNombreChange: (String) -> Unit,
    onInvitadoRolChange: (String) -> Unit,
    onTemaChange: (String) -> Unit,
    onPlaceChange: (String) -> Unit,
    onShowNombreDialog: () -> Unit,
    onShowRolDialog: () -> Unit,
    onShowTemaDialog: () -> Unit,
    onLimpiarCampos: () -> Unit,
    onAgregar: () -> Unit,
    // 🆕 Nuevos parámetros para edición
    modoEdicion: Boolean = false,
    onCancelarEdicion: () -> Unit = {},
    // 🆕 Parámetros para el botón inteligente
    isOnAir: Boolean = false,
    airingButtonState: AiringButtonState = AiringButtonState.NORMAL,
    countdownTime: Int = 0,
    onStartAiring: () -> Unit = {},
    onCancelAiring: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            // 🎨 Cambiar color según modo
            containerColor = if (modoEdicion)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        // 🎨 Borde visual para modo edición
        border = if (modoEdicion)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con indicador de modo
            // Header con indicador de modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            isOnAir -> "🔴 EN VIVO - Editando"
                            modoEdicion -> "✏️ Editando Invitado"
                            else -> "✏️ Editor de Entrevistados"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOnAir -> Color.Red
                            modoEdicion -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    // 🆕 Indicador visual del modo
                    when {
                        isOnAir -> {
                            Text(
                                text = "Modificando información que está al aire",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        modoEdicion -> {
                            Text(
                                text = "Modificando registro existente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }

                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Contraer" else "Expandir"
                    )
                }
            }

            // Contenido expandible
            if (expanded) {



                // [MANTENER todos los campos existentes: Nombre, Rol, Tema, Lugar...]
                // Campo Nombre con botón de opciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = invitadoNombre,
                        onValueChange = onInvitadoNombreChange,
                        label = { Text("👤 Nombre *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onInvitadoNombreChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar nombre")
                    }
                    IconButton(onClick = onShowNombreDialog) {
                        Icon(Icons.Default.Settings, contentDescription = "Opciones nombre")
                    }
                }

                // Campo Rol con botón de opciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = invitadoRol,
                        onValueChange = onInvitadoRolChange,
                        label = { Text("💼 Rol") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onInvitadoRolChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar rol")
                    }
                    IconButton(onClick = onShowRolDialog) {
                        Icon(Icons.Default.Settings, contentDescription = "Opciones rol")
                    }
                }

                // Campo Tema con botón de opciones (MÁS ALTO - 2 líneas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = tema,
                        onValueChange = onTemaChange,
                        label = { Text("📋 Tema") },
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        IconButton(onClick = { onTemaChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar tema")
                        }
                        IconButton(onClick = onShowTemaDialog) {
                            Icon(Icons.Default.Settings, contentDescription = "Opciones tema")
                        }
                    }
                }

                // Campo Lugar (mantener igual)
                var expandedPlaces by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedPlace,
                        onValueChange = { /* No editable directamente */ },
                        label = { Text("📍 Lugar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedPlaces = true },
                        enabled = false,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar lugar")
                        }
                    )
                    DropdownMenu(
                        expanded = expandedPlaces,
                        onDismissRequest = { expandedPlaces = false }
                    ) {
                        availablePlaces.forEach { lugar ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        lugar,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    onPlaceChange(lugar)
                                    expandedPlaces = false
                                }
                            )
                        }
                    }
                }

                // 🔄 BOTONES MEJORADOS CON LÓGICA CONDICIONAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (modoEdicion) {
                        // 🆕 Botón Cancelar (solo en modo edición)
                        Button(
                            onClick = onCancelarEdicion,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelar")
                        }

                        // 🔄 Botón Actualizar (en modo edición)
                        Button(
                            onClick = onAgregar, // Se reutiliza pero con lógica condicional
                            modifier = Modifier.weight(1f),
                            enabled = invitadoNombre.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Actualizar")
                        }
                    } else {
                        // 🆕 NUEVA ESTRUCTURA: Column para organizar verticalmente
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre el botón superior y la fila inferior
                        ) {
                            // 🆕 BOTÓN INTELIGENTE AL AIRE
                            SmartAirButton(
                                invitadoNombre = invitadoNombre,
                                invitadoRol = invitadoRol,
                                tema = tema,
                                isOnAir = isOnAir,
                                airingButtonState = airingButtonState,
                                countdownTime = countdownTime,
                                onStartAiring = onStartAiring,
                                onCancelAiring = onCancelAiring
                            )

                            // 🔄 Botones originales (modo agregar) - en una Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onLimpiarCampos,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nuevo")
                                }

                                Button(
                                    onClick = onAgregar,
                                    modifier = Modifier.weight(1f),
                                    enabled = invitadoNombre.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }

                // Nota sobre campos obligatorios
                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ListaUnificadaInvitados(
    filtroSeleccionado: String,
    onFiltroChange: (String) -> Unit,
    listaInvitados: List<Invitado>,
    onInvitadoSelect: (Invitado) -> Unit,
    onActualizarDatos: (Invitado) -> Unit,
    onEliminar: (Invitado) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📋 Invitados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${listaInvitados.size} registros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hoy", "Mes", "Todos").forEach { filtro ->
                    FilterChip(
                        onClick = { onFiltroChange(filtro) },
                        label = { Text(filtro) },
                        selected = filtroSeleccionado == filtro,
                        leadingIcon = if (filtroSeleccionado == filtro) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de invitados
            if (listaInvitados.isEmpty()) {
                Text(
                    text = when (filtroSeleccionado) {
                        "Hoy" -> "No hay invitados para hoy"
                        "Mes" -> "No hay invitados este mes"
                        else -> "No hay invitados registrados"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // ✅ REEMPLAZA el LazyColumn problemático con esto:
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp), // Limitar altura máxima
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listaInvitados.take(10).forEach { invitado -> // Limitar a 10 para performance
                        InvitadoItemCard(
                            invitado = invitado,
                            onSelect = { onInvitadoSelect(invitado) },
                            onActualizarDatos = { onActualizarDatos(invitado) },
                            onEliminar = { onEliminar(invitado) }
                        )
                    }

                    if (listaInvitados.size > 10) {
                        Text(
                            text = "... y ${listaInvitados.size - 10} más. Usa filtros para encontrar más rápido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitadoItemCard(
    invitado: Invitado,
    onSelect: () -> Unit,
    onActualizarDatos: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del invitado
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invitado.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (invitado.rol.isNotEmpty()) {
                    Text(
                        text = "💼 ${invitado.rol}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }

                if (invitado.tema.isNotEmpty()) {
                    Text(
                        text = "📋 ${invitado.tema}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                // Información adicional: fecha y lugar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📅 ${invitado.fecha}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📍 ${invitado.lugar}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Eliminar
                IconButton(
                    onClick = onEliminar,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar invitado",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Botón Play para poner al aire
                IconButton(
                    onClick = onActualizarDatos, // ← CAMBIAR CALLBACK
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.Update, // ← CAMBIAR ÍCONO
                        contentDescription = "Actualizar datos",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Icono de seleccionar
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Seleccionar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }


        }
    }
}

@Composable
private fun SimplifiedSwitchRow(
    label: String,
    enabled: Boolean,
    checked: Boolean,
    processing: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (processing) {
                Text(
                    text = "Procesando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (processing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled && !processing
            )
        }
    }
}

@Composable
private fun TextFieldDialog(
    title: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveToFirebase: () -> Unit,
    firebaseRepository: FirebaseRepository
) {
    var tempValue by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Botón para convertir a mayúsculas
                Button(
                    onClick = {
                        tempValue = tempValue.uppercase()
                        onValueChange(tempValue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CONVERTIR A MAYÚSCULAS")
                }

                Button(
                    onClick = {
                        onValueChange(tempValue)
                        onSaveToFirebase()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Poner datos al Aire")
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Aceptar")
                }
            }
        }
    )
}



@Composable
private fun SimplifiedSwitchCard(
    // Lower Third
    lowerThirdEnabled: Boolean,
    lowerThirdChecked: Boolean,
    lowerThirdProcessing: Boolean,
    onLowerThirdChange: (Boolean) -> Unit,

    // Tema
    temaEnabled: Boolean,
    temaChecked: Boolean,
    temaProcessing: Boolean,
    onTemaChange: (Boolean) -> Unit,

    // Logo
    logoEnabled: Boolean,
    logoChecked: Boolean,
    logoProcessing: Boolean,
    onLogoChange: (Boolean) -> Unit,

    // Publicidad
    publicidadEnabled: Boolean,
    publicidadChecked: Boolean,
    publicidadProcessing: Boolean,
    onPublicidadChange: (Boolean) -> Unit,
    // 🆕 NUEVOS PARÁMETROS para mostrar datos actuales
    invitadoNombre: String = "",
    invitadoRol: String = "",
    tema: String = "",

    // 🆕 FUNCIÓN PARA DESACTIVAR SWITCHES AUTOMÁTICAMENTE
    onAutoDisableSwitch: (String, Boolean) -> Unit = { _, _ -> }
) {
    // 🆕 LÓGICA CONDICIONAL AUTOMÁTICA
    val hasInvitadoContent = invitadoNombre.trim().isNotEmpty()
    val hasTemaContent = tema.trim().isNotEmpty()

    // Calcular si los switches deben estar habilitados basándose en contenido
    val lowerThirdConditionalEnabled = lowerThirdEnabled && hasInvitadoContent
    val temaConditionalEnabled = temaEnabled && hasTemaContent

    // 🆕 EFECTO PARA DESACTIVAR SWITCHES AUTOMÁTICAMENTE CUANDO NO HAY CONTENIDO
    LaunchedEffect(hasInvitadoContent, hasTemaContent) {
        // Si no hay contenido de invitado y el lower third está activo, desactivarlo
        if (!hasInvitadoContent && lowerThirdChecked) {
            onAutoDisableSwitch("lowerThird", false)
        }

        // Si no hay contenido de tema y el switch tema está activo, desactivarlo
        if (!hasTemaContent && temaChecked) {
            onAutoDisableSwitch("tema", false)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📺 Control de Visualización",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 🆕 SECCIÓN INFORMATIVA - INFORMACIÓN ACTUAL AL AIRE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 EN VIVO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // Indicador de estado en vivo
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (lowerThirdChecked || temaChecked) Color.Red else Color.Gray,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lowerThirdChecked || temaChecked) "AL AIRE" else "FUERA DE AIRE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (lowerThirdChecked || temaChecked) Color.Red else Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    // Información actual sincronizada
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoRow(
                            label = "Invitado:",
                            value = invitadoNombre.ifEmpty { "Sin información" },
                            isEmpty = invitadoNombre.isEmpty()
                        )

                        InfoRow(
                            label = "Rol:",
                            value = invitadoRol.ifEmpty { "Sin información" },
                            isEmpty = invitadoRol.isEmpty()
                        )

                        InfoRow(
                            label = "Tema:",
                            value = tema.ifEmpty { "Sin información" },
                            isEmpty = tema.isEmpty()
                        )
                    }
                }
            }

            // 🆕 Switch Lower Third con validación de contenido
            SimplifiedSwitchRowWithValidation(
                label = "Lower Third",
                enabled = lowerThirdConditionalEnabled,
                checked = lowerThirdChecked,
                processing = lowerThirdProcessing,
                onChange = onLowerThirdChange,
                hasRequiredContent = hasInvitadoContent,
                missingContentMessage = "Requiere nombre de invitado"
            )

            // 🆕 Switch Tema con validación de contenido
            SimplifiedSwitchRowWithValidation(
                label = "Mostrar Tema",
                enabled = temaConditionalEnabled,
                checked = temaChecked,
                processing = temaProcessing,
                onChange = onTemaChange,
                hasRequiredContent = hasTemaContent,
                missingContentMessage = "Requiere tema"
            )


            // Switch Logo
            SimplifiedSwitchRow(
                label = "Mostrar Logo",
                enabled = logoEnabled,
                checked = logoChecked,
                processing = logoProcessing,
                onChange = onLogoChange
            )

            // Switch Publicidad
            SimplifiedSwitchRow(
                label = "Mostrar Publicidad",
                enabled = publicidadEnabled,
                checked = publicidadChecked,
                processing = publicidadProcessing,
                onChange = onPublicidadChange
            )
        }
    }
}

// 🆕 COMPONENTE AUXILIAR PARA MOSTRAR INFORMACIÓN
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isEmpty: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.3f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = if (isEmpty)
                MaterialTheme.colorScheme.outline
            else
                MaterialTheme.colorScheme.onSurface,
            fontStyle = if (isEmpty) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// 🆕 NUEVO COMPONENTE: Switch con validación de contenido requerido
@Composable
private fun SimplifiedSwitchRowWithValidation(
    label: String,
    enabled: Boolean,
    checked: Boolean,
    processing: Boolean,
    onChange: (Boolean) -> Unit,
    hasRequiredContent: Boolean,
    missingContentMessage: String
) {
    Column {
        SimplifiedSwitchRow(
            label = label,
            enabled = enabled,
            checked = checked,
            processing = processing,
            onChange = onChange
        )

        // 🆕 Mensaje de advertencia cuando no hay contenido
        if (!hasRequiredContent) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = missingContentMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublicidadCard(
    publicidadesList: List<String>,
    selectedPublicidad: String,
    expandedPublicidades: Boolean,
    rutaPublicidad: String,
    mostrarPublicidad: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPublicidadSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📺 Publicidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Selector de publicidad
            ExposedDropdownMenuBox(
                expanded = expandedPublicidades,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = selectedPublicidad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar publicidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPublicidades) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedPublicidades,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    publicidadesList.forEach { publicidad ->
                        DropdownMenuItem(
                            text = { Text(publicidad) },
                            onClick = { onPublicidadSelect(publicidad) }
                        )
                    }
                }
            }

            // Preview de la publicidad
            if (rutaPublicidad.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = rutaPublicidad
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Publicidad",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 4.5f)
                    )
                }
            }
        }
    }
}

// ================================
// FUNCIONES AUXILIARES OPTIMIZADAS
// ================================

// Función auxiliar para guardar entrevistado (NUEVA ESTRUCTURA OPTIMIZADA)
private fun guardarInvitado(
    invitadoNombre: String,
    invitadoRol: String,
    tema: String,
    firebaseRepository: FirebaseRepository,
    selectedPlace: String = "Estudio Principal",
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    if (invitadoNombre.isBlank()) {
        onFailure("El nombre del entrevistado es obligatorio")
        return
    }

    // Generar ID único usando timestamp + random
    val uniqueId = "inv_${System.currentTimeMillis()}_${(1000..9999).random()}"

    // Obtener fecha actual en formato YYYY-MM-DD
    val fechaActual = Calendar.getInstance()
    val fechaFormateada = "${fechaActual.get(Calendar.YEAR)}-${String.format("%02d", fechaActual.get(Calendar.MONTH) + 1)}-${String.format("%02d", fechaActual.get(Calendar.DAY_OF_MONTH))}"

    // Crear estructura optimizada
    val datosInvitado = mapOf(
        "nombre" to invitadoNombre,
        "rol" to invitadoRol,
        "tema" to tema,
        "subTema" to "", // Campo para futura expansión
        "fecha" to fechaFormateada,
        "lugar" to selectedPlace,
        "timestamp" to System.currentTimeMillis(),
        "activo" to true,
        "fechaCreacion" to System.currentTimeMillis(),
        "ultimaModificacion" to System.currentTimeMillis()
    )

    // Guardar con la nueva estructura: CLAVE_STREAM_FB/INVITADOS/{ID_UNICO}/
    firebaseRepository.saveData(
        nodePath = "CLAVE_STREAM_FB/INVITADOS/$uniqueId",
        data = datosInvitado,
        onSuccess = { onSuccess() },
        onFailure = { error -> onFailure("Error al guardar: ${error.message}") }
    )
}

// 🆕 Función para actualizar invitado existente
private fun actualizarInvitado(
    invitadoId: String,
    invitadoNombre: String,
    invitadoRol: String,
    tema: String,
    selectedPlace: String,
    firebaseRepository: FirebaseRepository,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val datosActualizados = mapOf(
        "nombre" to invitadoNombre,
        "rol" to invitadoRol,
        "tema" to tema,
        "lugar" to selectedPlace,
        "ultimaModificacion" to System.currentTimeMillis()
    )

    firebaseRepository.saveData(
        nodePath = "CLAVE_STREAM_FB/INVITADOS/$invitadoId",
        data = datosActualizados,
        onSuccess = { onSuccess() },
        onFailure = { error -> onFailure("Error al actualizar: ${error.message}") }
    )
}

// Función para eliminar invitado (soft delete)
private fun eliminarInvitado(
    invitadoId: String,
    firebaseRepository: FirebaseRepository,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val updates = mapOf(
        "activo" to false,
        "ultimaModificacion" to System.currentTimeMillis()
    )

    firebaseRepository.saveData(
        nodePath = "CLAVE_STREAM_FB/INVITADOS/$invitadoId",
        data = updates,
        onSuccess = { onSuccess() },
        onFailure = { error -> onFailure("Error al eliminar: ${error.message}") }
    )
}

// Función auxiliar para actualizar campos en Firebase (mantener compatibilidad)
private fun updateFirebaseField(
    field: String,
    value: Any,
    firebaseRepository: FirebaseRepository
) {
    val updates = mapOf(field to value)
    firebaseRepository.saveData("CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS", updates,
        onSuccess = {
            println("$field actualizado a $value en Firebase")
        },
        onFailure = { error ->
            println("Error al actualizar $field: ${error.message}")
        }
    )
}

// ================================
// SMARTAIRBUTTON CORREGIDO (SIN .first y .second)
// ================================

@Composable
fun SmartAirButton(
    invitadoNombre: String,
    invitadoRol: String,
    tema: String,
    isOnAir: Boolean,
    airingButtonState: AiringButtonState,
    countdownTime: Int,
    onStartAiring: () -> Unit,
    onCancelAiring: () -> Unit,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")

    // Animación de pulso cuando está al aire
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnAir) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // 🔧 CORREGIDO: Determinar colores y texto según estado
    val buttonColor: Color
    val contentColor: Color
    val buttonText: String
    val buttonIcon: ImageVector

    when {
        isOnAir -> {
            buttonColor = Color.Red.copy(alpha = pulseAlpha)
            contentColor = Color.White
            buttonText = "🔴 EN VIVO"
            buttonIcon = Icons.Default.Stop
        }
        airingButtonState == AiringButtonState.COUNTDOWN -> {
            buttonColor = Color(0xFFFF9800) // Color naranja directo
            contentColor = Color.White
            buttonText = "CANCELAR ($countdownTime)"
            buttonIcon = Icons.Default.Cancel
        }
        airingButtonState == AiringButtonState.PROCESSING -> {
            buttonColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            buttonText = "ENVIANDO..."
            buttonIcon = Icons.Default.CloudUpload
        }
        else -> {
            buttonColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            buttonText = "📡 AL AIRE"
            buttonIcon = Icons.Default.PlayArrow
        }
    }

    val hasRequiredContent = invitadoNombre.trim().isNotEmpty()

    Column {
        Button(
            onClick = {
                when (airingButtonState) {
                    AiringButtonState.NORMAL -> {
                        if (hasRequiredContent) {
                            onStartAiring()
                        }
                    }
                    AiringButtonState.COUNTDOWN -> {
                        onCancelAiring()
                    }
                    else -> { /* No hacer nada durante PROCESSING */ }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled && (airingButtonState != AiringButtonState.PROCESSING) &&
                    (hasRequiredContent || airingButtonState == AiringButtonState.COUNTDOWN),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor, // 🔧 CORREGIDO: Sin .first
                contentColor = contentColor,   // 🔧 CORREGIDO: Sin .second
                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (isOnAir) 8.dp else 4.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (airingButtonState == AiringButtonState.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        buttonIcon, // 🔧 CORREGIDO: Sin .second
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = buttonText, // 🔧 CORREGIDO: Sin .first
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        // Mensaje de estado/advertencia
        when {
            !hasRequiredContent && airingButtonState == AiringButtonState.NORMAL -> {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Requiere nombre de invitado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            airingButtonState == AiringButtonState.COUNTDOWN -> {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Haz clic para cancelar o espera $countdownTime segundos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            isOnAir -> {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Información transmitiendo en vivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}