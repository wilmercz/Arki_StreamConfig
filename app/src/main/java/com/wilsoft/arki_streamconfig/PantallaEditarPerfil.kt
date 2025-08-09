// Archivo: PantallaEditarPerfil.kt
package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.wilsoft.arki_streamconfig.components.ColorPickerAdvanced
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import java.util.UUID
import androidx.activity.compose.rememberLauncherForActivityResult


import androidx.compose.foundation.clickable

import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.wilsoft.arki_streamconfig.utilidades.StorageExtensions
import com.wilsoft.arki_streamconfig.utilidades.getFileNameFromUri
import com.wilsoft.arki_streamconfig.utilidades.getFileExtensionFromUri

import com.wilsoft.arki_streamconfig.utilidades.generateFileName
import com.wilsoft.arki_streamconfig.utilidades.StorageImageItem
import android.provider.OpenableColumns
import com.wilsoft.arki_streamconfig.models.*
import com.wilsoft.arki_streamconfig.repository.PerfilStreamRepository


import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarPerfil(
    navController: NavController,
    firebaseRepository: FirebaseRepository,
    profileName: String? = null
) {
    val isNewProfile = profileName == null || profileName == "nuevo"

    // Estados del perfil
    var nombrePerfil by remember { mutableStateOf("") }
    var colorFondo1 by remember { mutableStateOf("#1066FF") }
    var colorFondo2 by remember { mutableStateOf("#F08313") }
    var colorFondo3 by remember { mutableStateOf("#103264") }
    var colorLetra1 by remember { mutableStateOf("#FFFFFF") }
    var colorLetra2 by remember { mutableStateOf("#FFFFFF") }
    var colorLetra3 by remember { mutableStateOf("#FFFFFF") }
    var urlLogo by remember { mutableStateOf("") }
    var urlImagenPublicidad by remember { mutableStateOf("") }

    // Estados UI
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(!isNewProfile) }
    var isSaving by remember { mutableStateOf(false) }

    // Estados para el selector de logos
    var showLogoSelector by remember { mutableStateOf(false) }
    var showPublicidadSelector by remember { mutableStateOf(false) }
    var availableLogos by remember { mutableStateOf<List<StorageImageItem>>(emptyList()) }
    var availablePublicidades by remember { mutableStateOf<List<StorageImageItem>>(emptyList()) }
    var isLoadingLogos by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isLoadingImages by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Launcher para seleccionar archivos del dispositivo
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                isSaving = true
                uploadProgress = 0

                // Detectar extensión original
                val extension = getFileExtensionFromUri(context.contentResolver, selectedUri)
                val fileName = generateFileName("logo", extension)

                StorageExtensions.uploadImageWithMetadata(
                    imageUri = selectedUri,
                    path = "logos",
                    fileName = fileName,
                    contentResolver = context.contentResolver, // ← NUEVO PARÁMETRO
                    onProgress = { progress ->
                        uploadProgress = progress
                    }
                ).onSuccess { downloadUrl ->
                    urlLogo = downloadUrl
                    isSaving = false
                    snackbarHostState.showSnackbar("Logo subido exitosamente (.$extension)")
                }.onFailure { error ->
                    isSaving = false
                    snackbarHostState.showSnackbar("Error: ${error.message}")
                }
            }
        }
    }

// REEMPLAZAR el publicidadLauncher:
    val publicidadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                isSaving = true
                uploadProgress = 0

                // Detectar extensión original
                val extension = getFileExtensionFromUri(context.contentResolver, selectedUri)
                val fileName = generateFileName("publicidad", extension)

                StorageExtensions.uploadImageWithMetadata(
                    imageUri = selectedUri,
                    path = "publicidades",
                    fileName = fileName,
                    contentResolver = context.contentResolver, // ← NUEVO PARÁMETRO
                    onProgress = { progress ->
                        uploadProgress = progress
                    }
                ).onSuccess { downloadUrl ->
                    urlImagenPublicidad = downloadUrl
                    isSaving = false
                    snackbarHostState.showSnackbar("Imagen publicitaria subida exitosamente (.$extension)")
                }.onFailure { error ->
                    isSaving = false
                    snackbarHostState.showSnackbar("Error: ${error.message}")
                }
            }
        }
    }

    // Estados principales
    var perfilConfig by remember { mutableStateOf(PerfilStreamConfig()) }
    var tabSeleccionada by remember { mutableStateOf(0) }
    var mostrarPreview by remember { mutableStateOf(false) }
    var modoPresets by remember { mutableStateOf(false) }


    fun seleccionarLogoDesdeStorage() {
        showLogoSelector = true
        coroutineScope.launch {
            isLoadingImages = true
            StorageExtensions.listImagesInFolder("logos")
                .onSuccess { logos ->
                    availableLogos = logos
                    isLoadingImages = false
                }
                .onFailure {
                    isLoadingImages = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error cargando logos")
                    }
                }
        }
    }

    fun seleccionarPublicidadDesdeStorage() {
        showPublicidadSelector = true
        coroutineScope.launch {
            isLoadingImages = true
            StorageExtensions.listImagesInFolder("publicidades")
                .onSuccess { publicidades ->
                    availablePublicidades = publicidades
                    isLoadingImages = false
                }
                .onFailure {
                    isLoadingImages = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error cargando publicidades")
                    }
                }
        }
    }

    // Tabs del editor
    val tabs = listOf(
        TabInfo("General", Icons.Default.Settings, "Información básica"),
        TabInfo("Colores", Icons.Default.Palette, "Sistema de colores"),
        TabInfo("Logos", Icons.Default.Image, "Elementos de Recursos"),
        TabInfo("Posición", Icons.Default.CropFree, "Ubicación de elementos"),
        TabInfo("Animaciones", Icons.Default.Animation, "Efectos y transiciones"),
        TabInfo("Contenido", Icons.Default.TextFields, "Texto dinámico"),
        TabInfo("Streaming", Icons.Default.Videocam, "Configuración técnica"),
        TabInfo("Web", Icons.Default.Web, "Configuración para CameraFi")
    )


    // 3. MIGRADOR TEMPORAL para convertir datos básicos a avanzados
    fun migrarPerfilBasicoAAvanzado(data: Map<String, Any>): PerfilStreamConfig {
        return PerfilStreamConfig(
            nombrePerfil = data["NombrePerfil"] as? String ?: "",
            descripcion = "Perfil migrado desde formato básico",
            categoria = CategoriaStream.NOTICIAS,
            fechaCreacion = System.currentTimeMillis(),

            sistemaColores = SistemaColoresAvanzado(
                colorFondo1 = data["colorFondo1"] as? String ?: "#1066FF",
                colorFondo2 = data["colorFondo2"] as? String ?: "#043884",
                colorFondo3 = data["colorFondo3"] as? String ?: "#F08313",
                colorLetra1 = data["colorLetra1"] as? String ?: "#FFFFFF",
                colorLetra2 = data["colorLetra2"] as? String ?: "#FFFFFF",
                colorLetra3 = data["colorLetra3"] as? String ?: "#FFFFFF"
            ),

            lowerThirdConfig = LowerThirdConfig(
                logo = LogoConfig(
                    simple = LogoSimpleConfig(
                        url = data["urlLogo"] as? String ?: ""  // ← MIGRAR DESDE urlLogo
                    )
                ),
                publicidad = PublicidadConfig(
                    url = data["urlImagenPublicidad"] as? String ?: ""
                )
            ),

            // Valores por defecto para campos nuevos
            posicionamiento = ConfiguracionPosicionamiento(),
            invitadoConfig = InvitadoConfigAvanzado(),
            contenidoDinamico = ContenidoDinamicoConfig(),
            animaciones = ConfigAnimaciones(),
            webRenderConfig = WebRenderConfig()
        )
    }


    /*
    // Cargar datos si es edición
    LaunchedEffect(profileName) {
        if (!isNewProfile && profileName != null) {
            firebaseRepository.loadProfile(
                profileName = profileName,
                onSuccess = { data ->
                    nombrePerfil = data["NombrePerfil"] as? String ?: profileName
                    colorFondo1 = data["colorFondo1"] as? String ?: "#1066FF"
                    colorFondo2 = data["colorFondo2"] as? String ?: "#F08313"
                    colorFondo3 = data["colorFondo3"] as? String ?: "#103264"
                    colorLetra1 = data["colorLetra1"] as? String ?: "#FFFFFF"
                    colorLetra2 = data["colorLetra2"] as? String ?: "#FFFFFF"
                    colorLetra3 = data["colorLetra3"] as? String ?: "#FFFFFF"
                    urlLogo = data["urlLogo"] as? String ?: ""
                    urlImagenPublicidad = data["urlImagenPublicidad"] as? String ?: ""
                    isLoading = false
                },
                onFailure = { error ->
                    isLoading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error: ${error.message}")
                    }
                }
            )
        }
    }


     */

    // 4. USAR EL MIGRADOR en tu LaunchedEffect:
    LaunchedEffect(profileName) {
        if (!isNewProfile && profileName != null) {
            firebaseRepository.loadProfile(
                profileName = profileName,
                onSuccess = { data ->
                    // USAR EL MIGRADOR
                    perfilConfig = migrarPerfilBasicoAAvanzado(data)

                    println("🔍 DEBUG después de migración:")
                    println("URL Logo: '${perfilConfig.lowerThirdConfig.logo.simple.url}'")

                    isLoading = false
                },
                onFailure = { error ->
                    isLoading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error: ${error.message}")
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBarExpandida(
                titulo = if (isNewProfile) "Crear Perfil" else "Editar: ${perfilConfig.nombrePerfil}",
                onBack = { navController.navigateUp() },
                onPreview = { mostrarPreview = !mostrarPreview },
                onSave = {
                    isSaving = true
                    coroutineScope.launch {
                        val path = "CLAVE_STREAM_FB/PERFILES/${perfilConfig.nombrePerfil}"
                        val data = PerfilStreamConfigUtils.toFirebaseFormat(perfilConfig)

                        firebaseRepository.saveData(
                            nodePath = path,
                            data = data,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Perfil guardado exitosamente")
                                    isSaving = false
                                    navController.navigateUp()
                                }
                            },
                            onFailure = { error ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar: ${error.message}")
                                    isSaving = false
                                }
                            }
                        )
                    }
                },
                onPresets = { modoPresets = !modoPresets },
                isSaving = isSaving,
                showPreview = mostrarPreview
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (isLoading) {
            LoadingScreen()
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Panel principal de edición
                Column(
                    modifier = Modifier
                        .weight(if (mostrarPreview) 0.6f else 1f)
                        .fillMaxHeight()
                ) {
                    // Tabs horizontales
                    TabRowPersonalizada(
                        tabs = tabs,
                        selectedIndex = tabSeleccionada,
                        onTabSelected = { tabSeleccionada = it }
                    )

                    // Contenido del tab seleccionado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        when (tabSeleccionada) {
                            0 -> TabGeneral(perfilConfig) { perfilConfig = it }
                            1 -> TabColoresAvanzado(perfilConfig) { perfilConfig = it }
                            2 -> TabRecursos(perfilConfig) { perfilConfig = it }
                            3 -> TabPosicionamiento(perfilConfig) { perfilConfig = it }
                            4 -> TabAnimaciones(perfilConfig) { perfilConfig = it }
                            5 -> TabContenidoDinamico(perfilConfig) { perfilConfig = it }
                            6 -> TabStreaming(perfilConfig) { perfilConfig = it }
                            7 -> TabWebConfig(perfilConfig) { perfilConfig = it }
                        }
                    }
                }

                // Panel de previsualización (deslizable)
                AnimatedVisibility(
                    visible = mostrarPreview,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it })
                ) {
                    Card(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        PanelPreviewEnTiempoReal(perfilConfig)
                    }
                }
            }
        }

        // Overlay de presets
        if (modoPresets) {
            PresetsOverlay(
                onPresetSelected = { preset ->
                    perfilConfig = preset
                    modoPresets = false
                },
                onDismiss = { modoPresets = false }
            )
        }


        // Agregar estos diálogos justo antes del cierre del Scaffold
        if (showLogoSelector) {
            ImageSelectorDialog(
                title = "Seleccionar Logo",
                images = availableLogos,
                isLoading = isLoadingImages,
                currentImageUrl = urlLogo,
                onImageSelect = { storageItem ->
                    urlLogo = storageItem.downloadUrl
                    showLogoSelector = false
                },
                onDismiss = { showLogoSelector = false },
                onUploadNew = {
                    showLogoSelector = false
                    logoLauncher.launch("image/*")
                }
            )
        }

        if (showPublicidadSelector) {
            ImageSelectorDialog(
                title = "Seleccionar Imagen Publicitaria",
                images = availablePublicidades,
                isLoading = isLoadingImages,
                currentImageUrl = urlImagenPublicidad,
                onImageSelect = { storageItem ->
                    urlImagenPublicidad = storageItem.downloadUrl
                    showPublicidadSelector = false
                },
                onDismiss = { showPublicidadSelector = false },
                onUploadNew = {
                    showPublicidadSelector = false
                    publicidadLauncher.launch("image/*")
                }
            )
        }

    }
}

@Composable
private fun BasicTab(
    nombrePerfil: String,
    onNombrePerfilChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información Básica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nombrePerfil,
                        onValueChange = onNombrePerfilChange,
                        label = { Text("Nombre del Perfil") },
                        placeholder = { Text("Ej: Canal Noticias") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "El nombre del perfil debe ser único y descriptivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ColorsTabRediseñado(
    colorFondo1: String,
    colorFondo2: String,
    colorFondo3: String,
    colorLetra1: String,
    colorLetra2: String,
    colorLetra3: String,
    onColorFondo1Change: (String) -> Unit,
    onColorFondo2Change: (String) -> Unit,
    onColorFondo3Change: (String) -> Unit,
    onColorLetra1Change: (String) -> Unit,
    onColorLetra2Change: (String) -> Unit,
    onColorLetra3Change: (String) -> Unit
) {
    var fondoExpanded by remember { mutableStateOf(true) }
    var letraExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = { fondoExpanded = !fondoExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 Colores de Fondo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = fondoExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Spacer(Modifier.height(8.dp))
                            ColorPickerAdvanced("Principal", colorFondo1, onColorFondo1Change, Modifier.fillMaxWidth())
                            ColorPickerAdvanced("Secundario", colorFondo2, onColorFondo2Change, Modifier.fillMaxWidth())
                            ColorPickerAdvanced("Terciario", colorFondo3, onColorFondo3Change, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                onClick = { letraExpanded = !letraExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 Colores de Texto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = letraExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Spacer(Modifier.height(8.dp))
                            ColorPickerAdvanced("Principal", colorLetra1, onColorLetra1Change, Modifier.fillMaxWidth())
                            ColorPickerAdvanced("Secundario", colorLetra2, onColorLetra2Change, Modifier.fillMaxWidth())
                            ColorPickerAdvanced("Terciario", colorLetra3, onColorLetra3Change, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun ImageSelectorDialog(
    title: String,
    images: List<StorageImageItem>,
    isLoading: Boolean,
    currentImageUrl: String = "",
    onImageSelect: (StorageImageItem) -> Unit,
    onDismiss: () -> Unit,
    onUploadNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title)
                TextButton(onClick = onUploadNew) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Subir Nueva")
                }
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay imágenes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onUploadNew) {
                            Text("Subir la primera imagen")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { storageItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onImageSelect(storageItem) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (storageItem.downloadUrl == currentImageUrl) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = storageItem.downloadUrl,
                                    contentDescription = storageItem.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = storageItem.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = storageItem.getFormattedSize(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = storageItem.getFormattedDate(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (storageItem.downloadUrl == currentImageUrl) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Seleccionar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun PreviewTab(
    nombrePerfil: String,
    colorFondo1: String,
    colorFondo2: String,
    colorLetra1: String,
    urlLogo: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (urlLogo.isNotEmpty()) {
                            AsyncImage(
                                model = urlLogo,
                                contentDescription = "Logo preview",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(colorFondo1)),
                                        RoundedCornerShape(20.dp)
                                    )
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(android.graphics.Color.parseColor(colorFondo1)),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (nombrePerfil.isNotEmpty()) nombrePerfil else "Nombre Invitado",
                                    color = Color(android.graphics.Color.parseColor(colorLetra1)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(android.graphics.Color.parseColor(colorFondo2)),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Cargo del Invitado",
                                    color = Color(android.graphics.Color.parseColor(colorLetra1)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// Función para guardar el perfil (reutilizada)
private fun saveProfile(
    firebaseRepository: FirebaseRepository,
    nombrePerfil: String,
    colorFondo1: String,
    colorFondo2: String,
    colorFondo3: String,
    colorLetra1: String,
    colorLetra2: String,
    colorLetra3: String,
    urlLogo: String,
    urlImagenPublicidad: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val perfilData = mapOf(
        "NombrePerfil" to nombrePerfil,
        "colorFondo1" to colorFondo1,
        "colorFondo2" to colorFondo2,
        "colorFondo3" to colorFondo3,
        "colorLetra1" to colorLetra1,
        "colorLetra2" to colorLetra2,
        "colorLetra3" to colorLetra3,
        "urlLogo" to urlLogo,
        "urlImagenPublicidad" to urlImagenPublicidad
    )

    firebaseRepository.saveData(
        "CLAVE_STREAM_FB/PERFILES/$nombrePerfil",
        perfilData,
        onSuccess = onSuccess,
        onFailure = onFailure
    )
}

@Composable
fun TabRecursos(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    // Estados necesarios para los launchers (deben estar en el composable padre)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Estados para carga
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var showLogoSelector by remember { mutableStateOf(false) }
    var showPublicidadSelector by remember { mutableStateOf(false) }
    var availableLogos by remember { mutableStateOf<List<StorageImageItem>>(emptyList()) }
    var availablePublicidades by remember { mutableStateOf<List<StorageImageItem>>(emptyList()) }
    var isLoadingImages by remember { mutableStateOf(false) }

   var isUploadingLogoAliado by remember { mutableStateOf(false) }
    var uploadProgressLogoAliado by remember { mutableStateOf(0) }

    // ✅ NUEVOS ESTADOS PARA LOGOS ALIADOS (agregar después de los existentes)
    var showLogoAliadoSelector by remember { mutableStateOf(false) }
    var logoAliadoEnEdicion by remember { mutableStateOf<Int?>(null) } // índice del logo en edición
    var availableLogosAliados by remember { mutableStateOf<List<StorageImageItem>>(emptyList()) }


    // ✅ NUEVAS FUNCIONES PARA LOGOS ALIADOS
    fun seleccionarLogoAliadoDesdeStorage(indice: Int) {
        logoAliadoEnEdicion = indice
        showLogoAliadoSelector = true
        coroutineScope.launch {
            isLoadingImages = true
            StorageExtensions.listImagesInFolder("logos")
                .onSuccess { logos ->
                    availableLogosAliados = logos
                    isLoadingImages = false
                }
                .onFailure {
                    isLoadingImages = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error cargando logos aliados")
                    }
                }
        }
    }

    // Funciones para seleccionar desde storage
    fun seleccionarLogoDesdeStorage() {
        showLogoSelector = true
        coroutineScope.launch {
            isLoadingImages = true
            StorageExtensions.listImagesInFolder("logos")
                .onSuccess { logos ->
                    availableLogos = logos
                    isLoadingImages = false
                }
                .onFailure {
                    isLoadingImages = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error cargando logos")
                    }
                }
        }
    }

    fun seleccionarPublicidadDesdeStorage() {
        showPublicidadSelector = true
        coroutineScope.launch {
            isLoadingImages = true
            StorageExtensions.listImagesInFolder("publicidades")
                .onSuccess { publicidades ->
                    availablePublicidades = publicidades
                    isLoadingImages = false
                }
                .onFailure {
                    isLoadingImages = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error cargando publicidades")
                    }
                }
        }
    }

    // Launcher para logo
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                isUploading = true
                uploadProgress = 0

                val extension = getFileExtensionFromUri(context.contentResolver, selectedUri)
                val fileName = generateFileName("logo", extension)

                StorageExtensions.uploadImageWithMetadata(
                    imageUri = selectedUri,
                    path = "logos",
                    fileName = fileName,
                    contentResolver = context.contentResolver,
                    onProgress = { progress -> uploadProgress = progress }
                ).onSuccess { downloadUrl ->
                    // ACTUALIZAR PerfilStreamConfig
                    val newLogo = perfil.lowerThirdConfig.logo.simple.copy(url = downloadUrl)
                    val newLogoConfig = perfil.lowerThirdConfig.logo.copy(simple = newLogo)
                    val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
                    onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))

                    isUploading = false
                    snackbarHostState.showSnackbar("Logo subido exitosamente")
                }.onFailure { error ->
                    isUploading = false
                    snackbarHostState.showSnackbar("Error: ${error.message}")
                }
            }
        }
    }

    // Launcher para publicidad
    val publicidadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                isUploading = true
                uploadProgress = 0

                val extension = getFileExtensionFromUri(context.contentResolver, selectedUri)
                val fileName = generateFileName("publicidad", extension)

                StorageExtensions.uploadImageWithMetadata(
                    imageUri = selectedUri,
                    path = "publicidades",
                    fileName = fileName,
                    contentResolver = context.contentResolver,
                    onProgress = { progress -> uploadProgress = progress }
                ).onSuccess { downloadUrl ->
                    // ACTUALIZAR PerfilStreamConfig
                    val newPublicidad = perfil.lowerThirdConfig.publicidad.copy(url = downloadUrl)
                    val newLowerThird = perfil.lowerThirdConfig.copy(publicidad = newPublicidad)
                    onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))

                    isUploading = false
                    snackbarHostState.showSnackbar("Imagen publicitaria subida exitosamente")
                }.onFailure { error ->
                    isUploading = false
                    snackbarHostState.showSnackbar("Error: ${error.message}")
                }
            }
        }
    }


    // ✅ NUEVO LAUNCHER PARA LOGOS ALIADOS (agregar después de los existentes)
    // ✅ NUEVO LAUNCHER PARA LOGOS ALIADOS
    val logoAliadoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            logoAliadoEnEdicion?.let { indice ->
                coroutineScope.launch {
                    isUploadingLogoAliado = true
                    uploadProgressLogoAliado = 0

                    val extension = getFileExtensionFromUri(context.contentResolver, selectedUri)
                    val fileName = generateFileName("logo_aliado_$indice", extension)

                    StorageExtensions.uploadImageWithMetadata(
                        imageUri = selectedUri,
                        path = "logos",
                        fileName = fileName,
                        contentResolver = context.contentResolver,
                        onProgress = { progress -> uploadProgressLogoAliado = progress }
                    ).onSuccess { downloadUrl ->
                        // Actualizar lista de logos aliados
                        val logosActuales = perfil.lowerThirdConfig.logo.logosAliados.logos.toMutableList()
                        while (logosActuales.size <= indice) {
                            logosActuales.add(LogoAliadoItem())
                        }
                        logosActuales[indice] = logosActuales[indice].copy(url = downloadUrl)

                        val newLogosConfig = perfil.lowerThirdConfig.logo.logosAliados.copy(logos = logosActuales)
                        val newLogoConfig = perfil.lowerThirdConfig.logo.copy(logosAliados = newLogosConfig)
                        val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
                        onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))

                        isUploadingLogoAliado = false
                        snackbarHostState.showSnackbar("Logo aliado ${indice + 1} subido")
                        logoAliadoEnEdicion = null
                    }.onFailure { error ->
                        isUploadingLogoAliado = false
                        snackbarHostState.showSnackbar("Error: ${error.message}")
                        logoAliadoEnEdicion = null
                    }
                }
            }
        }
    }

    // USAR LA FUNCIÓN ResourcesTabCompleta QUE YA EXISTE
    ResourcesTabCompleta(
        // Parámetros existentes
        urlLogo = perfil.lowerThirdConfig.logo.simple.url,
        urlImagenPublicidad = perfil.lowerThirdConfig.publicidad.url,
        onUrlLogoChange = { newUrl ->
            val newLogo = perfil.lowerThirdConfig.logo.simple.copy(url = newUrl)
            val newLogoConfig = perfil.lowerThirdConfig.logo.copy(simple = newLogo)
            val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
            onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
        },
        onUrlImagenPublicidadChange = { newUrl ->
            val newPublicidad = perfil.lowerThirdConfig.publicidad.copy(url = newUrl)
            val newLowerThird = perfil.lowerThirdConfig.copy(publicidad = newPublicidad)
            onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
        },
        onSeleccionarLogoDispositivo = { logoLauncher.launch("image/*") },
        onSeleccionarLogoStorage = { seleccionarLogoDesdeStorage() },
        onSeleccionarPublicidadDispositivo = { publicidadLauncher.launch("image/*") },
        onSeleccionarPublicidadStorage = { seleccionarPublicidadDesdeStorage() },
        isUploading = isUploading,
        uploadProgress = uploadProgress,

        // ✅ NUEVOS PARÁMETROS PARA LOGOS ALIADOS
        logosAliadosHabilitado = perfil.lowerThirdConfig.logo.logosAliados.habilitado,
        logosAliados = perfil.lowerThirdConfig.logo.logosAliados.logos.map { it.url },
        onLogosAliadosHabilitadoChange = { enabled ->
            val newLogosConfig = perfil.lowerThirdConfig.logo.logosAliados.copy(habilitado = enabled)
            val newLogoConfig = perfil.lowerThirdConfig.logo.copy(logosAliados = newLogosConfig)
            val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
            onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
        },
        onLogoAliadoChange = { indice, nuevaUrl ->
            val logosActuales = perfil.lowerThirdConfig.logo.logosAliados.logos.toMutableList()
            while (logosActuales.size <= indice) {
                logosActuales.add(LogoAliadoItem())
            }
            logosActuales[indice] = logosActuales[indice].copy(url = nuevaUrl)

            val newLogosConfig = perfil.lowerThirdConfig.logo.logosAliados.copy(logos = logosActuales)
            val newLogoConfig = perfil.lowerThirdConfig.logo.copy(logosAliados = newLogosConfig)
            val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
            onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
        },
        onSeleccionarLogoAliadoDispositivo = { indice ->
            logoAliadoEnEdicion = indice
            logoAliadoLauncher.launch("image/*")
        },
        onSeleccionarLogoAliadoStorage = { indice -> seleccionarLogoAliadoDesdeStorage(indice) },
        onEliminarLogoAliado = { indice ->
            val logosActuales = perfil.lowerThirdConfig.logo.logosAliados.logos.toMutableList()
            if (indice < logosActuales.size) {
                logosActuales.removeAt(indice)
                val newLogosConfig = perfil.lowerThirdConfig.logo.logosAliados.copy(logos = logosActuales)
                val newLogoConfig = perfil.lowerThirdConfig.logo.copy(logosAliados = newLogosConfig)
                val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
                onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
            }
        },
        isUploadingLogoAliado = isUploadingLogoAliado,
        uploadProgressLogoAliado = uploadProgressLogoAliado,
        logoAliadoEnEdicion = logoAliadoEnEdicion
    )

    // Diálogos (igual que en tu código original)
    if (showLogoSelector) {
        ImageSelectorDialog(
            title = "Seleccionar Logo",
            images = availableLogos,
            isLoading = isLoadingImages,
            currentImageUrl = perfil.lowerThirdConfig.logo.simple.url,
            onImageSelect = { storageItem ->
                val newLogo = perfil.lowerThirdConfig.logo.simple.copy(url = storageItem.downloadUrl)
                val newLogoConfig = perfil.lowerThirdConfig.logo.copy(simple = newLogo)
                val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
                onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
                showLogoSelector = false
            },
            onDismiss = { showLogoSelector = false },
            onUploadNew = {
                showLogoSelector = false
                logoLauncher.launch("image/*")
            }
        )
    }

    if (showPublicidadSelector) {
        ImageSelectorDialog(
            title = "Seleccionar Imagen Publicitaria",
            images = availablePublicidades,
            isLoading = isLoadingImages,
            currentImageUrl = perfil.lowerThirdConfig.publicidad.url,
            onImageSelect = { storageItem ->
                val newPublicidad = perfil.lowerThirdConfig.publicidad.copy(url = storageItem.downloadUrl)
                val newLowerThird = perfil.lowerThirdConfig.copy(publicidad = newPublicidad)
                onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
                showPublicidadSelector = false
            },
            onDismiss = { showPublicidadSelector = false },
            onUploadNew = {
                showPublicidadSelector = false
                publicidadLauncher.launch("image/*")
            }
        )
    }

    // ✅ NUEVO DIÁLOGO PARA LOGOS ALIADOS
    if (showLogoAliadoSelector) {
        ImageSelectorDialog(
            title = "Seleccionar Logo Aliado ${(logoAliadoEnEdicion ?: 0) + 1}",
            images = availableLogos,
            isLoading = isLoadingImages,
            currentImageUrl = logoAliadoEnEdicion?.let { indice ->
                perfil.lowerThirdConfig.logo.logosAliados.logos.getOrNull(indice)?.url ?: ""
            } ?: "",
            onImageSelect = { storageItem ->
                logoAliadoEnEdicion?.let { indice ->
                    val logosActuales = perfil.lowerThirdConfig.logo.logosAliados.logos.toMutableList()
                    while (logosActuales.size <= indice) {
                        logosActuales.add(LogoAliadoItem())
                    }
                    logosActuales[indice] = logosActuales[indice].copy(url = storageItem.downloadUrl)

                    val newLogosConfig = perfil.lowerThirdConfig.logo.logosAliados.copy(logos = logosActuales)
                    val newLogoConfig = perfil.lowerThirdConfig.logo.copy(logosAliados = newLogosConfig)
                    val newLowerThird = perfil.lowerThirdConfig.copy(logo = newLogoConfig)
                    onPerfilChange(perfil.copy(lowerThirdConfig = newLowerThird))
                }
                showLogoAliadoSelector = false
                logoAliadoEnEdicion = null
            },
            onDismiss = {
                showLogoAliadoSelector = false
                logoAliadoEnEdicion = null
            },
            onUploadNew = {
                showLogoAliadoSelector = false
                logoAliadoLauncher.launch("image/*")
            }
        )
    }

}


@Composable
fun LogoAliadoSlot(
    index: Int,
    logoUrl: String,
    onSubirImagen: () -> Unit,
    onSeleccionarStorage: () -> Unit,
    onEliminar: () -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (logoUrl.isNotEmpty())
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (logoUrl.isNotEmpty()) {
                // Logo cargado
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Logo aliado ${index + 1}",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Logo ${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Botones de acción compactos
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onSeleccionarStorage,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Cambiar",
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                // Slot vacío
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$uploadProgress%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar logo",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Logo ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Botones de acción compactos
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = onSubirImagen,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = "Subir",
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            IconButton(
                                onClick = onSeleccionarStorage,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = "Storage",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ResourcesTabCompleta(
    urlLogo: String,
    urlImagenPublicidad: String,
    onUrlLogoChange: (String) -> Unit,
    onUrlImagenPublicidadChange: (String) -> Unit,
    onSeleccionarLogoDispositivo: () -> Unit,
    onSeleccionarLogoStorage: () -> Unit,
    onSeleccionarPublicidadDispositivo: () -> Unit,
    onSeleccionarPublicidadStorage: () -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Int = 0,
    // ✅ NUEVOS PARÁMETROS PARA LOGOS ALIADOS
    logosAliadosHabilitado: Boolean = false,
    logosAliados: List<String> = emptyList(), // Lista de URLs
    onLogosAliadosHabilitadoChange: (Boolean) -> Unit = {},
    onLogoAliadoChange: (Int, String) -> Unit = { _, _ -> }, // índice, nueva URL
    onSeleccionarLogoAliadoDispositivo: (Int) -> Unit = {},
    onSeleccionarLogoAliadoStorage: (Int) -> Unit = {},
    onEliminarLogoAliado: (Int) -> Unit = {},
    isUploadingLogoAliado: Boolean = false,
    uploadProgressLogoAliado: Int = 0,
    logoAliadoEnEdicion: Int? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🖼️ Recursos Visuales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (isUploading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    progress = uploadProgress / 100f,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$uploadProgress%",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // SECCIÓN LOGO
                    Text(
                        text = "🏷️ Logo del Canal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    // Vista previa del Logo (CORREGIDA - NO CIRCULAR)
                    if (urlLogo.isNotEmpty()) {
                        Card(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            AsyncImage(
                                model = urlLogo,
                                contentDescription = "Logo actual",
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center
                            )
                        }

                        // Label pequeño informativo
                        Text(
                            text = "✅ Logo cargado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Card(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Sin logo",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Sin logo seleccionado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    // Botones para seleccionar logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSeleccionarLogoDispositivo,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subir")
                            }
                        }

                        OutlinedButton(
                            onClick = onSeleccionarLogoStorage,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Galería")
                            }
                        }

                        if (urlLogo.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onUrlLogoChange("") },
                                enabled = !isUploading,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Limpiar")
                                }
                            }
                        }
                    }


                    /*
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECCIÓN PUBLICIDAD
                    Text(
                        text = "💰 Imagen Publicitaria",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    // Vista previa de la imagen publicitaria
                    if (urlImagenPublicidad.isNotEmpty()) {
                        AsyncImage(
                            model = urlImagenPublicidad,
                            contentDescription = "Publicidad actual",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = "✅ Imagen publicitaria cargada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Sin imagen publicitaria",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Sin imagen publicitaria seleccionada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Botones para seleccionar publicidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSeleccionarPublicidadDispositivo,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Subir")
                        }

                        OutlinedButton(
                            onClick = onSeleccionarPublicidadStorage,
                            modifier = Modifier.weight(1f),
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería")
                        }

                        if (urlImagenPublicidad.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onUrlImagenPublicidadChange("") },
                                enabled = !isUploading,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    }

                     */
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ Recomendaciones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Logos: formato PNG con fondo transparente (200x200px)\n" +
                                "• Publicidad: formato PNG/JPG (400x100px)\n" +
                                "• 'Subir' sube desde tu dispositivo\n" +
                                "• 'Galería' muestra imágenes ya guardadas en Firebase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ✅ NUEVA SECCIÓN - Logos Aliados
        item {
                    SectionCard("🤝 Logos Aliados") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Toggle principal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mostrar logos aliados",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Exhibe logos de medios aliados o patrocinadores",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = logosAliadosHabilitado,
                                    onCheckedChange = onLogosAliadosHabilitadoChange
                                )
                            }

                            // Solo mostrar controles si está habilitado
                            if (logosAliadosHabilitado) {
                                Text(
                                    text = "Configurar logos aliados (${logosAliados.filter { it.isNotEmpty() }.size}/10)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                // Grid 2x5 de logos aliados
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    for (row in 0..1) { // 2 filas
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            for (col in 0..3) { // 5 columnas
                                                val index = row * 5 + col
                                                val logoUrl = logosAliados.getOrNull(index) ?: ""

                                                LogoAliadoSlot(
                                                    index = index,
                                                    logoUrl = logoUrl,
                                                    onSubirImagen = { onSeleccionarLogoAliadoDispositivo(index) },
                                                    onSeleccionarStorage = { onSeleccionarLogoAliadoStorage(index) },
                                                    onEliminar = { onEliminarLogoAliado(index) },
                                                    isUploading = isUploadingLogoAliado && logoAliadoEnEdicion == index,
                                                    uploadProgress = if (logoAliadoEnEdicion == index) uploadProgressLogoAliado else 0,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Información adicional
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Los logos aliados aparecerán en la esquina inferior derecha durante la transmisión",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
    }
}






















@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarExpandida(
    titulo: String,
    onBack: () -> Unit,
    onPreview: () -> Unit,
    onSave: () -> Unit,
    onPresets: () -> Unit,
    isSaving: Boolean,
    showPreview: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Editor Avanzado de Perfiles",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        },
        actions = {
            // Botón Presets
            IconButton(onClick = onPresets) {
                Icon(Icons.Default.AutoAwesome, "Presets")
            }

            // Botón Preview
            IconButton(onClick = onPreview) {
                Icon(
                    if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    "Preview"
                )
            }

            // Botón Exportar
            IconButton(onClick = { /* Implementar exportar */ }) {
                Icon(Icons.Default.FileDownload, "Exportar")
            }

            // Botón Guardar
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guardar")
            }
        }
    )
}

@Composable
fun TabRowPersonalizada(
    tabs: List<TabInfo>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.title,
                        tint = if (selectedIndex == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selectedIndex == index) {
                        Text(
                            text = tab.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabGeneral(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Información Básica") {
                OutlinedTextField(
                    value = perfil.nombrePerfil,
                    onValueChange = { onPerfilChange(perfil.copy(nombrePerfil = it)) },
                    label = { Text("Nombre del Perfil") },
                    leadingIcon = { Icon(Icons.Default.Label, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = perfil.descripcion,
                    onValueChange = { onPerfilChange(perfil.copy(descripcion = it)) },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        item {
            SectionCard("Categoría del Stream") {
                Text(
                    "Selecciona el tipo de transmisión para aplicar configuraciones optimizadas:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CategoriaStream.values()) { categoria ->
                        CategoryChip(
                            categoria = categoria,
                            isSelected = perfil.categoria == categoria,
                            onClick = {
                                onPerfilChange(perfil.copy(categoria = categoria))
                                // Aplicar colores automáticos de la categoría
                                val coloresCategoria = SistemaColoresAvanzado.desdeCategoria(categoria)
                                onPerfilChange(perfil.copy(sistemaColores = coloresCategoria))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabColoresAvanzado(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Paleta Principal") {
                ColorGrid(
                    colores = listOf(
                        "Primario" to perfil.sistemaColores.colorFondo1,
                        "Secundario" to perfil.sistemaColores.colorFondo2,
                        "Terciario" to perfil.sistemaColores.colorFondo3,
                        "Acento" to perfil.sistemaColores.colorAcento
                    ),
                    onColorChange = { index, color ->
                        val newColores = when (index) {
                            0 -> perfil.sistemaColores.copy(colorFondo1 = color)
                            1 -> perfil.sistemaColores.copy(colorFondo2 = color)
                            2 -> perfil.sistemaColores.copy(colorFondo3 = color)
                            3 -> perfil.sistemaColores.copy(colorAcento = color)
                            else -> perfil.sistemaColores
                        }
                        onPerfilChange(perfil.copy(sistemaColores = newColores))
                    }
                )
            }
        }

        item {
            SectionCard("Colores de Texto") {
                ColorGrid(
                    colores = listOf(
                        "Texto Principal" to perfil.sistemaColores.colorLetra1,
                        "Texto Secundario" to perfil.sistemaColores.colorLetra2,
                        "Texto Terciario" to perfil.sistemaColores.colorLetra3,
                        "Texto Descripción" to perfil.sistemaColores.textoDescripcion
                    ),
                    onColorChange = { index, color ->
                        val newColores = when (index) {
                            0 -> perfil.sistemaColores.copy(colorLetra1 = color)
                            1 -> perfil.sistemaColores.copy(colorLetra2 = color)
                            2 -> perfil.sistemaColores.copy(colorLetra3 = color)
                            3 -> perfil.sistemaColores.copy(textoDescripcion = color)
                            else -> perfil.sistemaColores
                        }
                        onPerfilChange(perfil.copy(sistemaColores = newColores))
                    }
                )
            }
        }

        item {
            SectionCard("Presets de Color") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        listOf(
                            "Corporativo" to listOf("#1E40AF", "#6B7280", "#059669", "#F59E0B"),
                            "Noticias" to listOf("#C41E3A", "#1E3A8A", "#F59E0B", "#DC2626"),
                            "Deportes" to listOf("#16A34A", "#EAB308", "#DC2626", "#0891B2"),
                            "Gaming" to listOf("#7C3AED", "#EC4899", "#10B981", "#F59E0B")
                        )
                    ) { (nombre, colores) ->
                        PresetColorCard(
                            nombre = nombre,
                            colores = colores,
                            onClick = {
                                val newColores = perfil.sistemaColores.copy(
                                    colorFondo1 = colores[0],
                                    colorFondo2 = colores[1],
                                    colorFondo3 = colores[2],
                                    colorAcento = colores[3]
                                )
                                onPerfilChange(perfil.copy(sistemaColores = newColores))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabPosicionamiento(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Resolución del Canvas") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CanvasResolution.values().forEach { resolucion ->
                        FilterChip(
                            selected = perfil.posicionamiento.canvasResolution == resolucion,
                            onClick = {
                                val newPosicionamiento = perfil.posicionamiento.copy(canvasResolution = resolucion)
                                onPerfilChange(perfil.copy(posicionamiento = newPosicionamiento))
                            },
                            label = { Text(resolucion.displayName) }
                        )
                    }
                }
            }
        }

        item {
            SectionCard("Posicionamiento de Elementos") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElementPositionSelector(
                        label = "Logo",
                        currentPosition = perfil.posicionamiento.logoPosition,
                        onPositionChange = {
                            val newPosicionamiento = perfil.posicionamiento.copy(logoPosition = it)
                            onPerfilChange(perfil.copy(posicionamiento = newPosicionamiento))
                        }
                    )

                    ElementPositionSelector(
                        label = "Texto Principal",
                        currentPosition = perfil.posicionamiento.textoNombrePosition,
                        onPositionChange = {
                            val newPosicionamiento = perfil.posicionamiento.copy(textoNombrePosition = it)
                            onPerfilChange(perfil.copy(posicionamiento = newPosicionamiento))
                        }
                    )

                    ElementPositionSelector(
                        label = "Ubicación",
                        currentPosition = perfil.posicionamiento.ubicacionPosition,
                        onPositionChange = {
                            val newPosicionamiento = perfil.posicionamiento.copy(ubicacionPosition = it)
                            onPerfilChange(perfil.copy(posicionamiento = newPosicionamiento))
                        }
                    )

                    ElementPositionSelector(
                        label = "Publicidad",
                        currentPosition = perfil.posicionamiento.publicidadPosition,
                        onPositionChange = {
                            val newPosicionamiento = perfil.posicionamiento.copy(publicidadPosition = it)
                            onPerfilChange(perfil.copy(posicionamiento = newPosicionamiento))
                        }
                    )
                }
            }
        }

        item {
            SectionCard("Vista Previa de Posicionamiento") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Simular canvas de posicionamiento
                        CanvasPreview(perfil)
                    }
                }
            }
        }
    }
}

@Composable
fun TabAnimaciones(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Animaciones de Entrada") {
                AnimationTypeGrid(
                    title = "Selecciona el tipo de animación para la entrada de elementos:",
                    selectedAnimation = perfil.animaciones.entradaLogo,
                    onAnimationSelected = {
                        val newAnimaciones = perfil.animaciones.copy(entradaLogo = it)
                        onPerfilChange(perfil.copy(animaciones = newAnimaciones))
                    }
                )
            }
        }

        item {
            SectionCard("Configuración de Timing") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = perfil.animaciones.duracionEntrada.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { duracion ->
                                val newAnimaciones = perfil.animaciones.copy(duracionEntrada = duracion)
                                onPerfilChange(perfil.copy(animaciones = newAnimaciones))
                            }
                        },
                        label = { Text("Duración (ms)") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = perfil.animaciones.delayEntreElementos.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { delay ->
                                val newAnimaciones = perfil.animaciones.copy(delayEntreElementos = delay)
                                onPerfilChange(perfil.copy(animaciones = newAnimaciones))
                            }
                        },
                        label = { Text("Delay (ms)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SectionCard("Vista Previa de Animación") {
                Button(
                    onClick = { /* Implementar preview de animación */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reproducir Animación de Prueba")
                }
            }
        }
    }
}

@Composable
fun TabContenidoDinamico(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Información del Invitado") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = perfil.invitadoConfig.nombreCompleto,
                        onValueChange = {
                            val newInvitado = perfil.invitadoConfig.copy(nombreCompleto = it)
                            onPerfilChange(perfil.copy(invitadoConfig = newInvitado))
                        },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = perfil.invitadoConfig.titulo,
                        onValueChange = {
                            val newInvitado = perfil.invitadoConfig.copy(titulo = it)
                            onPerfilChange(perfil.copy(invitadoConfig = newInvitado))
                        },
                        label = { Text("Título/Cargo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = perfil.invitadoConfig.organizacion,
                        onValueChange = {
                            val newInvitado = perfil.invitadoConfig.copy(organizacion = it)
                            onPerfilChange(perfil.copy(invitadoConfig = newInvitado))
                        },
                        label = { Text("Organización") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            SectionCard("Fecha y Ubicación") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = perfil.contenidoDinamico.mostrarFechaHora,
                            onCheckedChange = {
                                val newContenido = perfil.contenidoDinamico.copy(mostrarFechaHora = it)
                                onPerfilChange(perfil.copy(contenidoDinamico = newContenido))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mostrar fecha y hora automática")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = perfil.contenidoDinamico.ciudadActual,
                            onValueChange = {
                                val newContenido = perfil.contenidoDinamico.copy(ciudadActual = it)
                                onPerfilChange(perfil.copy(contenidoDinamico = newContenido))
                            },
                            label = { Text("Ciudad") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = perfil.contenidoDinamico.provinciaActual,
                            onValueChange = {
                                val newContenido = perfil.contenidoDinamico.copy(provinciaActual = it)
                                onPerfilChange(perfil.copy(contenidoDinamico = newContenido))
                            },
                            label = { Text("Provincia") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabStreaming(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Configuración Técnica") {
                Text("Configuración optimizada para streaming profesional")
                // Implementar configuraciones técnicas
            }
        }
    }
}

@Composable
fun TabWebConfig(
    perfil: PerfilStreamConfig,
    onPerfilChange: (PerfilStreamConfig) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionCard("Configuración para CameraFi App") {
                Text("URL generada: ${PerfilStreamConfigUtils.generarUrlVisualizacion(perfil.nombrePerfil)}")

                Button(
                    onClick = { /* Generar configuración web */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Web, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar Configuración Web")
                }
            }
        }
    }
}

// Componentes auxiliares
data class TabInfo(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val description: String)

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando configuración...")
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun CategoryChip(
    categoria: CategoriaStream,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(categoria.displayName) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else null
    )
}

@Composable
fun ColorGrid(
    colores: List<Pair<String, String>>,
    onColorChange: (Int, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        itemsIndexed(colores) { index, (nombre, color) ->
            ColorPickerCard(
                name = nombre,
                color = color,
                onColorChange = { onColorChange(index, it) }
            )
        }
    }
}

@Composable
fun ColorPickerCard(
    name: String,
    color: String,
    onColorChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Implementar selector de color */ }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color(android.graphics.Color.parseColor(color)),
                        shape = CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PresetColorCard(
    nombre: String,
    colores: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nombre,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                colores.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(android.graphics.Color.parseColor(color)))
                    )
                }
            }
        }
    }
}

@Composable
fun ElementPositionSelector(
    label: String,
    currentPosition: ElementPosition,
    onPositionChange: (ElementPosition) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ElementPosition.values().filter { it != ElementPosition.CUSTOM }) { position ->
                FilterChip(
                    selected = currentPosition == position,
                    onClick = { onPositionChange(position) },
                    label = {
                        Text(
                            text = position.displayName.split(" ").map { it.first() }.joinToString(""),
                            fontSize = 10.sp
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AnimationTypeGrid(
    title: String,
    selectedAnimation: AnimationType,
    onAnimationSelected: (AnimationType) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        // SOLUCIÓN CORREGIDA para el error de AnimationType.values()

        @Composable
        fun AnimationTypeGrid(
            title: String,
            selectedAnimation: AnimationType,
            onAnimationSelected: (AnimationType) -> Unit
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // OPCIÓN 1: Usar items con lista (Compose versión reciente)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    // CAMBIO AQUÍ: Usar el tamaño del array y acceder por índice
                    items(AnimationType.values().size) { index ->
                        val animation = AnimationType.values()[index]
                        FilterChip(
                            selected = selectedAnimation == animation,
                            onClick = { onAnimationSelected(animation) },
                            label = {
                                Text(
                                    text = animation.displayName,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun CanvasPreview(perfil: PerfilStreamConfig) {
    // Implementar preview del canvas con posiciones
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            "Canvas Preview",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun PanelPreviewEnTiempoReal(perfil: PerfilStreamConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Vista Previa en Tiempo Real",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Implementar preview real del perfil
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(android.graphics.Color.parseColor(perfil.sistemaColores.colorFondo1))
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Preview: ${perfil.nombrePerfil}",
                    color = Color(android.graphics.Color.parseColor(perfil.sistemaColores.colorLetra1))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información del perfil
        Text("Categoría: ${perfil.categoria.displayName}")
        Text("Resolución: ${perfil.posicionamiento.canvasResolution.displayName}")
        Text("Animación: ${perfil.animaciones.entradaLogo.displayName}")
    }
}

@Composable
fun PresetsOverlay(
    onPresetSelected: (PerfilStreamConfig) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .clickable { }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Seleccionar Preset",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(CategoriaStream.values()) { categoria ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val preset = PerfilStreamConfigUtils.generarPresetPorCategoria(categoria)
                                    onPresetSelected(preset)
                                }
                        ) {
                            Text(
                                "Preset ${categoria.displayName}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}