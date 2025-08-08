// Archivo: PantallaGeneradorCaracteresAvanzada.kt
package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.*
import com.wilsoft.arki_streamconfig.components.*
import com.wilsoft.arki_streamconfig.models.*
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGeneradorCaracteresAvanzada(
    navController: NavController,
    firebaseRepository: FirebaseRepository
) {
    // Estados principales
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var invitadosList by remember { mutableStateOf<List<Invitado>>(emptyList()) }
    var lowerThirdConfig by remember { mutableStateOf(PresetTemplates.getPresetEstandar()) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cargar configuración desde Firebase al inicio
    LaunchedEffect(Unit) {
        isLoading = true
        loadLowerThirdConfigFromFirebase(
            firebaseRepository = firebaseRepository,
            onSuccess = { config ->
                lowerThirdConfig = config
                isLoading = false
            },
            onFailure = { error ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${error.message}")
                }
                isLoading = false
            }
        )

        // Cargar invitados
        loadInvitadosForDate(selectedDate, firebaseRepository) { invitados ->
            invitadosList = invitados
        }
    }

    // Escuchar cambios en Firebase en tiempo real
    LaunchedEffect(Unit) {
        setupRealtimeListener(firebaseRepository) { newConfig ->
            lowerThirdConfig = newConfig
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lower Thirds Avanzado",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { isPreviewMode = !isPreviewMode }
                    ) {
                        Icon(
                            if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (isPreviewMode) "Modo Edición" else "Modo Preview"
                        )
                    }

                    IconButton(
                        onClick = {
                            saveLowerThirdConfigToFirebase(
                                config = lowerThirdConfig,
                                firebaseRepository = firebaseRepository,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Configuración guardada exitosamente")
                                    }
                                },
                                onFailure = { error ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error: ${error.message}")
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (isPreviewMode) {
                // Modo Preview
                PreviewMode(
                    config = lowerThirdConfig,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                // Modo Edición
                EditionMode(
                    config = lowerThirdConfig,
                    onConfigChange = { newConfig ->
                        lowerThirdConfig = newConfig
                    },
                    invitadosList = invitadosList,
                    selectedDate = selectedDate,
                    onDateChange = { newDate ->
                        selectedDate = newDate
                        loadInvitadosForDate(newDate, firebaseRepository) { invitados ->
                            invitadosList = invitados
                        }
                    },
                    selectedTab = selectedTab,
                    onTabChange = { selectedTab = it },
                    firebaseRepository = firebaseRepository,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PreviewMode(
    config: LowerThirdConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Fondo negro simulando el stream
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Aquí iría el WebView con el preview real
            // Por ahora, mostramos una simulación
            PreviewSimulation(
                config = config,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        // Controles de preview
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "🎬 Vista Previa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Activar Lower Third */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Activar", fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Desactivar Lower Third */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desactivar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PreviewSimulation(
    config: LowerThirdConfig,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        // Logo
        if (config.logo.mostrar) {
            Box(
                modifier = Modifier
                    .offset(
                        x = (config.logo.simple.posicion.x / 10).dp,
                        y = (config.logo.simple.posicion.y / 10).dp
                    )
                    .size(
                        width = ((config.logo.simple.tamaño.width as Int) / 10).dp,
                        height = ((config.logo.simple.tamaño.height as Int) / 10).dp
                    )
                    .background(
                        Color(android.graphics.Color.parseColor(config.logo.simple.fondo.color)),
                        androidx.compose.foundation.shape.CircleShape
                    )
            )
        }

        // Texto Principal
        if (config.textoPrincipal.mostrar && config.textoPrincipal.contenido.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .offset(
                        x = (config.textoPrincipal.posicion.x / 10).dp,
                        y = (config.textoPrincipal.posicion.y / 10).dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(android.graphics.Color.parseColor(config.textoPrincipal.fondo.color))
                )
            ) {
                Text(
                    text = config.textoPrincipal.contenido,
                    color = Color(android.graphics.Color.parseColor(config.textoPrincipal.texto.color)),
                    fontSize = (config.textoPrincipal.tipografia.tamaño / 2).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Texto Secundario
        if (config.textoSecundario.mostrar && config.textoSecundario.contenido.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .offset(
                        x = (config.textoSecundario.posicion.x / 10).dp,
                        y = (config.textoSecundario.posicion.y / 10).dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(android.graphics.Color.parseColor(config.textoSecundario.fondo.color))
                )
            ) {
                Text(
                    text = config.textoSecundario.contenido,
                    color = Color(android.graphics.Color.parseColor(config.textoSecundario.texto.color)),
                    fontSize = (config.textoSecundario.tipografia.tamaño / 2).sp,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditionMode(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    invitadosList: List<Invitado>,
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    firebaseRepository: FirebaseRepository,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Pestañas principales
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabChange(0) },
                text = { Text("📝 Contenido") },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabChange(1) },
                text = { Text("🎨 Diseño") },
                icon = { Icon(Icons.Default.Palette, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { onTabChange(2) },
                text = { Text("📐 Posición") },
                icon = { Icon(Icons.Default.AspectRatio, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { onTabChange(3) },
                text = { Text("🎭 Presets") },
                icon = { Icon(Icons.Default.Palette, contentDescription = null) }
            )
        }

        // Contenido de las pestañas
        when (selectedTab) {
            0 -> ContentTab(
                config = config,
                onConfigChange = onConfigChange,
                invitadosList = invitadosList,
                selectedDate = selectedDate,
                onDateChange = onDateChange,
                firebaseRepository = firebaseRepository
            )
            1 -> DesignTab(
                config = config,
                onConfigChange = onConfigChange
            )
            2 -> PositionTab(
                config = config,
                onConfigChange = onConfigChange
            )
            3 -> PresetsTab(
                config = config,
                onConfigChange = onConfigChange,
                firebaseRepository = firebaseRepository
            )
        }
    }
}

@Composable
private fun ContentTab(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    invitadosList: List<Invitado>,
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit,
    firebaseRepository: FirebaseRepository
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de fecha y invitados
        item {
            InvitadosSection(
                selectedDate = selectedDate,
                invitadosList = invitadosList,
                onDateChange = onDateChange,
                onInvitadoSelect = { invitado ->
                    onConfigChange(
                        config.copy(
                            textoPrincipal = config.textoPrincipal.copy(contenido = invitado.nombre),
                            textoSecundario = config.textoSecundario.copy(contenido = invitado.rol),
                            tema = config.tema.copy(contenido = invitado.tema)
                        )
                    )
                }
            )
        }

        // Configuración de contenido rápido
        item {
            QuickContentSection(
                config = config,
                onConfigChange = onConfigChange
            )
        }

        // Estados de visibilidad
        item {
            VisibilityControlsSection(
                config = config,
                onConfigChange = onConfigChange
            )
        }
    }
}

@Composable
private fun DesignTab(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Configuración del logo
        item {
            LogoModeSelector(
                logoConfig = config.logo,
                onConfigChange = { newLogoConfig ->
                    onConfigChange(config.copy(logo = newLogoConfig))
                }
            )
        }

        // Configuración de textos
        item {
            AdvancedTextEditor(
                label = "Texto Principal",
                textConfig = config.textoPrincipal,
                onConfigChange = { newTextConfig ->
                    onConfigChange(config.copy(textoPrincipal = newTextConfig))
                }
            )
        }

        item {
            AdvancedTextEditor(
                label = "Texto Secundario",
                textConfig = config.textoSecundario,
                onConfigChange = { newTextConfig ->
                    onConfigChange(config.copy(textoSecundario = newTextConfig))
                }
            )
        }

        item {
            AdvancedTextEditor(
                label = "Tema",
                textConfig = config.tema,
                onConfigChange = { newTextConfig ->
                    onConfigChange(config.copy(tema = newTextConfig))
                }
            )
        }
    }
}

@Composable
private fun PositionTab(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CoordinateSystem(
                lowerThirdConfig = config,
                onConfigChange = onConfigChange
            )
        }
    }
}

@Composable
private fun PresetsTab(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    firebaseRepository: FirebaseRepository
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PresetsManager(
                currentConfig = config,
                onConfigChange = onConfigChange,
                onSavePreset = { name, newConfig ->
                    // Implementar guardado de preset personalizado
                    saveCustomPresetToFirebase(name, newConfig, firebaseRepository)
                }
            )
        }
    }
}

@Composable
private fun InvitadosSection(
    selectedDate: Calendar,
    invitadosList: List<Invitado>,
    onDateChange: (Calendar) -> Unit,
    onInvitadoSelect: (Invitado) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 Invitados del Día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                DatePicker(selectedDate) { newDate ->
                    onDateChange(newDate)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (invitadosList.isEmpty()) {
                Text(
                    text = "No hay invitados para la fecha seleccionada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invitadosList) { invitado ->
                        Card(
                            onClick = { onInvitadoSelect(invitado) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = invitado.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = invitado.rol,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (invitado.tema.isNotEmpty()) {
                                    Text(
                                        text = "Tema: ${invitado.tema}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickContentSection(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit
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
                text = "✏️ Edición Rápida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = config.textoPrincipal.contenido,
                onValueChange = { newContent ->
                    onConfigChange(
                        config.copy(
                            textoPrincipal = config.textoPrincipal.copy(contenido = newContent)
                        )
                    )
                },
                label = { Text("Texto Principal") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = config.textoSecundario.contenido,
                onValueChange = { newContent ->
                    onConfigChange(
                        config.copy(
                            textoSecundario = config.textoSecundario.copy(contenido = newContent)
                        )
                    )
                },
                label = { Text("Texto Secundario") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = config.tema.contenido,
                onValueChange = { newContent ->
                    onConfigChange(
                        config.copy(
                            tema = config.tema.copy(contenido = newContent)
                        )
                    )
                },
                label = { Text("Tema") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VisibilityControlsSection(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "👁️ Controles de Visibilidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Logo")
                Switch(
                    checked = config.logo.mostrar,
                    onCheckedChange = { isChecked ->
                        onConfigChange(
                            config.copy(
                                logo = config.logo.copy(mostrar = isChecked)
                            )
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Texto Principal")
                Switch(
                    checked = config.textoPrincipal.mostrar,
                    onCheckedChange = { isChecked ->
                        onConfigChange(
                            config.copy(
                                textoPrincipal = config.textoPrincipal.copy(mostrar = isChecked)
                            )
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Texto Secundario")
                Switch(
                    checked = config.textoSecundario.mostrar,
                    onCheckedChange = { isChecked ->
                        onConfigChange(
                            config.copy(
                                textoSecundario = config.textoSecundario.copy(mostrar = isChecked)
                            )
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tema")
                Switch(
                    checked = config.tema.mostrar,
                    onCheckedChange = { isChecked ->
                        onConfigChange(
                            config.copy(
                                tema = config.tema.copy(mostrar = isChecked)
                            )
                        )
                    }
                )
            }
        }
    }
}

// Funciones auxiliares para Firebase
private fun loadLowerThirdConfigFromFirebase(
    firebaseRepository: FirebaseRepository,
    onSuccess: (LowerThirdConfig) -> Unit,
    onFailure: (Exception) -> Unit
) {
    firebaseRepository.loadStreamData(
        "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
        onSuccess = { data ->
            val config = parseLowerThirdConfigFromFirebase(data)
            onSuccess(config)
        },
        onFailure = onFailure
    )
}

private fun saveLowerThirdConfigToFirebase(
    config: LowerThirdConfig,
    firebaseRepository: FirebaseRepository,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val firebaseData = convertLowerThirdConfigToFirebaseFormat(config)
    firebaseRepository.saveData(
        "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
        firebaseData,
        onSuccess = onSuccess,
        onFailure = onFailure
    )
}

private fun setupRealtimeListener(
    firebaseRepository: FirebaseRepository,
    onConfigUpdate: (LowerThirdConfig) -> Unit
) {
    // Implementar listener en tiempo real
}

private fun loadInvitadosForDate(
    date: Calendar,
    firebaseRepository: FirebaseRepository,
    onSuccess: (List<Invitado>) -> Unit
) {
    val dateStr = "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-${date.get(Calendar.DAY_OF_MONTH)}"
    firebaseRepository.loadStreamData(
        "CLAVE_STREAM_FB/INVITADOS/$dateStr",
        onSuccess = { data ->
            val invitados = data.map { (key, value) ->
                val invitadoMap = value as Map<String, Any>
                Invitado(
                    nombre = key,
                    rol = invitadoMap["Rol"] as? String ?: "",
                    tema = invitadoMap["Tema"] as? String ?: "",
                    subTema = invitadoMap["SubTema"] as? String ?: ""
                )
            }
            onSuccess(invitados)
        },
        onFailure = { onSuccess(emptyList()) }
    )
}

private fun saveCustomPresetToFirebase(
    name: String,
    config: LowerThirdConfig,
    firebaseRepository: FirebaseRepository
) {
    // Implementar guardado de preset personalizado
}

private fun parseLowerThirdConfigFromFirebase(data: Map<String, Any>): LowerThirdConfig {
    // Parsear configuración desde Firebase
    return PresetTemplates.getPresetEstandar() // Placeholder
}

private fun convertLowerThirdConfigToFirebaseFormat(config: LowerThirdConfig): Map<String, Any> {
    // Convertir configuración a formato Firebase
    return mapOf<String, Any>() // Placeholder
}