// Archivo: PantallaPublicidad.kt - Versión Mejorada (Con funciones organizadas)
package com.wilsoft.arki_streamconfig

// Importaciones de Jetpack Compose para UI
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.foundation.Image

// Importaciones de corutinas para manejar operaciones asíncronas
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

// Importaciones de Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

// Para selección de archivos
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.util.UUID

// Importa las clases para los íconos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Importaciones para usar componentes existentes
import com.wilsoft.arki_streamconfig.utilidades.StorageExtensions
import com.wilsoft.arki_streamconfig.utilidades.StorageImageItem
import com.wilsoft.arki_streamconfig.components.AdvancedImageSelector
// AGREGAR en la parte superior del archivo, junto con las otras importaciones:
import androidx.compose.material.icons.filled.LibraryAdd

// ================================
// FUNCIONES DE FIREBASE (GLOBALES)
// ================================

// Función para cargar las publicidades asociadas a un perfil
fun loadPublicidadesForProfile(
    profile: String,
    onSuccess: (List<String>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val database = FirebaseDatabase.getInstance()
    // CORREGIDA: Ruta correcta según la estructura de Firebase mostrada
    val publicidadesRef = database.reference.child("CLAVE_STREAM_FB").child("PUBLICIDADES").child(profile)


    publicidadesRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val publicidades = snapshot.children.map { it.key ?: "" }.filter { it.isNotEmpty() }
                onSuccess(publicidades)
                println("Publicidades encontradas para $profile: $publicidades")
            } else {
                onSuccess(emptyList())
                println("No se encontraron publicidades para el perfil: $profile")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onFailure(Exception(error.message))
        }
    })
}

// Función para cargar los datos de una publicidad específica desde Firebase
fun loadPublicidadData(
    profile: String,  // Nombre del perfil seleccionado
    publicidad: String,  // Nombre de la publicidad seleccionada
    onSuccess: (Map<String, Any>) -> Unit,  // Callback de éxito que recibe los datos de la publicidad
    onFailure: (Exception) -> Unit  // Callback en caso de fallo
) {
    // Referencia a la ruta correcta según la estructura de Firebase
    val publicidadRef = FirebaseDatabase.getInstance().reference
        .child("CLAVE_STREAM_FB")
        .child("PUBLICIDADES")
        .child(profile)
        .child(publicidad)  // Accedemos a la clave de la publicidad

    // Escuchar los datos de Firebase (solo una vez)
    publicidadRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                // Crear un mapa para almacenar los datos de la publicidad
                val publicidadData = mutableMapOf<String, Any>()

                // Extraer los valores de los campos de la publicidad
                val nombrePublicidad = snapshot.child("nombre").getValue(String::class.java)
                val rutaImagen = snapshot.child("ruta").getValue(String::class.java)
                val urlVideo = snapshot.child("urlVideo").getValue(String::class.java) // NUEVO CAMPO
                val fechaInicial = snapshot.child("fechaInicial").getValue(String::class.java)
                val fechaFinal = snapshot.child("fechaFinal").getValue(String::class.java)
                val guion = snapshot.child("guion").getValue(String::class.java)
                val id = snapshot.child("id").getValue(String::class.java) // NUEVO CAMPO
                val fechaCreacion = snapshot.child("fechaCreacion").getValue(Long::class.java) // NUEVO CAMPO

                // Agregar los datos al mapa si no son nulos
                nombrePublicidad?.let { publicidadData["nombre"] = it }
                rutaImagen?.let { publicidadData["ruta"] = it }
                urlVideo?.let { publicidadData["urlVideo"] = it } // NUEVO CAMPO
                fechaInicial?.let { publicidadData["fechaInicial"] = it }
                fechaFinal?.let { publicidadData["fechaFinal"] = it }
                guion?.let { publicidadData["guion"] = it }
                id?.let { publicidadData["id"] = it } // NUEVO CAMPO
                fechaCreacion?.let { publicidadData["fechaCreacion"] = it } // NUEVO CAMPO

                println("CARGANDO PUBLICIDAD '$publicidad': Imagen=$rutaImagen, Video=$urlVideo")
                // Llamar al callback de éxito con los datos de la publicidad
                onSuccess(publicidadData)
            } else {
                // Manejo cuando la publicidad no existe
                println("La publicidad '$publicidad' no existe para el perfil '$profile'")
                onFailure(Exception("La publicidad '$publicidad' no existe"))
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Llamar al callback de error en caso de fallo
            onFailure(Exception(error.message))
        }
    })
}

fun savePublicidad(
    profile: String,  // Nombre del perfil seleccionado
    nombre: String,  // Nombre de la publicidad (obligatorio)
    imageUrl: String?,  // URL de la imagen publicitaria (opcional)
    videoUrl: String?,  // URL del video publicitario (opcional) - NUEVO CAMPO
    fechaInicial: String?,  // Fecha inicial de la publicidad (opcional)
    fechaFinal: String?,  // Fecha final de la publicidad (opcional)
    guion: String?,  // Guion o contenido de la publicidad (opcional)
    onSuccess: () -> Unit,  // Callback en caso de éxito
    onFailure: (Exception) -> Unit  // Callback en caso de fallo
) {
    // Verificar que el campo de nombre no esté vacío
    if (profile.isBlank() || nombre.isBlank()) {
        onFailure(Exception("El campo nombre no puede estar vacío"))
        return
    }

    // Crear datos con la estructura correcta, incluyendo urlVideo
    val publicidadData = mapOf(
        "nombre" to nombre,
        "ruta" to (imageUrl ?: ""),  // URL de la imagen
        "urlVideo" to (videoUrl ?: ""),  // URL del video - NUEVO CAMPO
        "fechaInicial" to (fechaInicial ?: ""),
        "fechaFinal" to (fechaFinal ?: ""),
        "guion" to (guion ?: ""),
        "id" to "${profile}_${nombre}_${System.currentTimeMillis()}", // ID único - NUEVO CAMPO
        "fechaCreacion" to System.currentTimeMillis() // Timestamp de creación - NUEVO CAMPO
    )

    // Referencia a la ruta correcta según la estructura de Firebase
    val database = FirebaseDatabase.getInstance()
    val publicidadRef = database.reference
        .child("CLAVE_STREAM_FB")
        .child("PUBLICIDADES")
        .child(profile)
        .child(nombre)  // Usamos el nombre de la publicidad como clave

    // Guardar los datos en Firebase
    publicidadRef.setValue(publicidadData).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            println("Publicidad '$nombre' guardada exitosamente en perfil '$profile'")
            onSuccess()
        } else {
            println("Error guardando publicidad: ${task.exception?.message}")
            task.exception?.let { onFailure(it) }
        }
    }
}

fun deletePublicidad(
    profile: String,  // Nombre del perfil seleccionado
    publicidad: String,  // Nombre de la publicidad a eliminar
    onSuccess: () -> Unit,  // Callback en caso de éxito
    onFailure: (Exception) -> Unit  // Callback en caso de fallo
) {
    // Referencia a la ruta de Firebase donde se encuentra la publicidad a eliminar
    val publicidadRef = FirebaseDatabase.getInstance().reference
        .child("CLAVE_STREAM_FB")
        .child("PUBLICIDADES")
        .child(profile)
        .child(publicidad)  // Accedemos a la clave de la publicidad

    // Verificar si la publicidad realmente existe antes de intentar eliminarla
    publicidadRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                // Si la publicidad existe, procedemos a eliminarla
                publicidadRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Si la operación de borrado fue exitosa, llamar al callback de éxito
                        onSuccess()
                    } else {
                        // Si ocurrió un error, llamar al callback de fallo con la excepción
                        task.exception?.let { onFailure(it) }
                    }
                }
            } else {
                // Manejar el caso en que la publicidad no existe
                println("La publicidad '$publicidad' no existe en el perfil '$profile'")
                onFailure(Exception("La publicidad '$publicidad' no existe en el perfil '$profile'"))
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onFailure(Exception(error.message))
        }
    })
}

// Función existente para subir imagen (mantener la que ya tienes)
fun uploadImageToFirebase(
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("GRAFICOS/publicidades/${UUID.randomUUID()}.jpg")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

// ================================
// COMPONENTE PRINCIPAL
// ================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPublicidad(
    firebaseRepository: FirebaseRepository
) {
    // Variables de estado existentes
    var selectedProfile by remember { mutableStateOf("") }
    var perfilesList by remember { mutableStateOf(listOf<String>()) }
    var publicidadesList by remember { mutableStateOf(listOf<String>()) }
    var selectedPublicidad by remember { mutableStateOf("") }
    var nombrePublicidad by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") } // NUEVO CAMPO para video
    var fechaInicial by remember { mutableStateOf("") }
    var fechaFinal by remember { mutableStateOf("") }
    var guionPublicidad by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Nuevas variables para la galería de storage
    var showImageSelector by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher para seleccionar imagen del dispositivo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            uploadProgress = 0

            // Usar la función existente uploadImageToFirebase
            uploadImageToFirebase(
                imageUri = it,
                onSuccess = { downloadUrl ->
                    imageUrl = downloadUrl
                    isUploading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Imagen subida exitosamente")
                    }
                },
                onFailure = { exception ->
                    isUploading = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error subiendo imagen: ${exception.message}")
                    }
                }
            )
        }
    }

    var showSpotSelector by remember { mutableStateOf(false) }
    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    var spotPendienteGuardar by remember { mutableStateOf<SpotSeleccionado?>(null) }


    // Función para mostrar el selector de imágenes de storage
    fun mostrarSelectorStorage() {
        showImageSelector = true
    }

    // 4. AGREGAR FUNCIÓN PARA LIMPIAR FORMULARIO:
    fun limpiarFormulario() {
        nombrePublicidad = ""
        imageUrl = ""
        videoUrl = ""
        fechaInicial = ""
        fechaFinal = ""
        guionPublicidad = ""
        isEditing = false
        selectedPublicidad = ""
    }

    // 3. AGREGAR NUEVA FUNCIÓN PARA GUARDAR:
    fun guardarSpotEnFirebase(spotSeleccionado: SpotSeleccionado) {
        if (selectedProfile.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error: No hay perfil seleccionado")
            }
            return
        }

        savePublicidad(
            profile = selectedProfile,
            nombre = spotSeleccionado.titulo,
            imageUrl = spotSeleccionado.urlImagen,
            videoUrl = "", // Spots no tienen video por defecto
            fechaInicial = spotSeleccionado.fechaInicio,
            fechaFinal = spotSeleccionado.fechaFinal,
            guion = "Importado desde spot: ${spotSeleccionado.titulo}",
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "✅ Publicidad '${spotSeleccionado.titulo}' guardada exitosamente"
                    )
                }

                // Recargar lista de publicidades automáticamente
                loadPublicidadesForProfile(
                    profile = selectedProfile,
                    onSuccess = { publicidades ->
                        publicidadesList = publicidades
                    },
                    onFailure = { exception ->
                        println("Error recargando lista: ${exception.message}")
                    }
                )

                // Limpiar campos después de guardar
                limpiarFormulario()
            },
            onFailure = { exception ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "❌ Error guardando: ${exception.message}"
                    )
                }
            }
        )
    }

    fun handleSpotSeleccionado(spotSeleccionado: SpotSeleccionado) {
        // Llenar campos UI primero
        nombrePublicidad = spotSeleccionado.titulo
        fechaInicial = spotSeleccionado.fechaInicio
        fechaFinal = spotSeleccionado.fechaFinal
        imageUrl = spotSeleccionado.urlImagen

        // Guardar spot para confirmación
        spotPendienteGuardar = spotSeleccionado

        // Verificar si ya existe una publicidad con este nombre
        val publicidadYaExiste = publicidadesList.contains(spotSeleccionado.titulo)

        if (publicidadYaExiste) {
            // Si ya existe, mostrar diálogo de confirmación
            showConfirmSaveDialog = true
        } else {
            // Si no existe, guardar directamente
            guardarSpotEnFirebase(spotSeleccionado)
        }
    }


    // Cargar perfiles al iniciar
    LaunchedEffect(Unit) {
        firebaseRepository.loadProfiles(
            onSuccess = { profiles ->
                perfilesList = profiles
            },
            onFailure = { exception ->
                println("Error cargando perfiles: ${exception.message}")
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📺 Gestión de Publicidades",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de selección de perfil
            ProfileSelectionCard(
                selectedProfile = selectedProfile,
                perfilesList = perfilesList,
                onProfileSelected = { profile ->
                    selectedProfile = profile
                    loadPublicidadesForProfile(
                        profile = profile,
                        onSuccess = { publicidades ->
                            publicidadesList = publicidades
                        },
                        onFailure = { exception ->
                            println("Error cargando publicidades: ${exception.message}")
                        }
                    )
                }
            )

            // Card de lista de publicidades (solo si hay perfil seleccionado)
            if (selectedProfile.isNotEmpty()) {
                PublicidadesListCard(
                    publicidadesList = publicidadesList,
                    selectedPublicidad = selectedPublicidad,
                    onPublicidadSelected = { publicidad ->
                        selectedPublicidad = publicidad
                        // Cargar datos de la publicidad seleccionada usando función existente
                        loadPublicidadData(
                            profile = selectedProfile,
                            publicidad = publicidad,
                            onSuccess = { data ->
                                nombrePublicidad = data["nombre"] as? String ?: ""
                                imageUrl = data["ruta"] as? String ?: ""
                                videoUrl = data["urlVideo"] as? String ?: "" // NUEVO CAMPO
                                fechaInicial = data["fechaInicial"] as? String ?: ""
                                fechaFinal = data["fechaFinal"] as? String ?: ""
                                guionPublicidad = data["guion"] as? String ?: ""
                                isEditing = true
                            },
                            onFailure = { exception ->
                                println("Error cargando datos de la publicidad: ${exception.message}")
                            }
                        )
                    },
                    onEditClick = { isEditing = true },
                    onDeleteClick = { publicidad ->
                        // Usar función existente de eliminar
                        deletePublicidad(
                            profile = selectedProfile,
                            publicidad = publicidad,
                            onSuccess = {
                                publicidadesList = publicidadesList.filter { it != publicidad }
                                if (selectedPublicidad == publicidad) {
                                    selectedPublicidad = ""
                                    isEditing = false
                                }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Publicidad eliminada")
                                }
                            },
                            onFailure = { exception ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error eliminando: ${exception.message}")
                                }
                            }
                        )
                    }
                )

                // Botón para crear nueva publicidad
                OutlinedButton(
                    onClick = {
                        nombrePublicidad = ""
                        imageUrl = ""
                        videoUrl = "" // NUEVO CAMPO
                        fechaInicial = ""
                        fechaFinal = ""
                        guionPublicidad = ""
                        isEditing = true
                        selectedPublicidad = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva publicidad")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear nueva publicidad")
                }
            }

            // NUEVA SECCIÓN: Botón para cargar desde spots existentes
            if (selectedProfile.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LibraryAdd,
                                contentDescription = "Cargar desde spots",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cargar desde Spots Existentes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Selecciona un spot existente con imagen para llenar automáticamente los campos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = { showSpotSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar Spots con Imágenes")
                        }
                    }
                }
            }

            // Formulario de edición/creación
            if (isEditing) {
                PublicidadFormCard(
                    nombrePublicidad = nombrePublicidad,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl, // NUEVO CAMPO
                    fechaInicial = fechaInicial,
                    fechaFinal = fechaFinal,
                    guionPublicidad = guionPublicidad,
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    onNombreChange = { nombrePublicidad = it },
                    onVideoUrlChange = { videoUrl = it }, // NUEVO CALLBACK
                    onFechaInicialChange = { fechaInicial = it },
                    onFechaFinalChange = { fechaFinal = it },
                    onGuionChange = { guionPublicidad = it },
                    onSelectFromDevice = { launcher.launch("image/*") },
                    onSelectFromStorage = { mostrarSelectorStorage() },
                    onClearImage = { imageUrl = "" },
                    onSave = {
                        if (nombrePublicidad.isNotBlank()) {
                            // Usar función existente para guardar
                            savePublicidad(
                                profile = selectedProfile,
                                nombre = nombrePublicidad,
                                imageUrl = imageUrl.takeIf { it.isNotEmpty() },
                                videoUrl = videoUrl.takeIf { it.isNotEmpty() }, // NUEVO CAMPO
                                fechaInicial = fechaInicial.takeIf { it.isNotEmpty() },
                                fechaFinal = fechaFinal.takeIf { it.isNotEmpty() },
                                guion = guionPublicidad.takeIf { it.isNotEmpty() },
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Publicidad guardada exitosamente")
                                    }
                                    isEditing = false
                                    selectedPublicidad = ""
                                    // Recargar lista
                                    loadPublicidadesForProfile(
                                        profile = selectedProfile,
                                        onSuccess = { publicidades ->
                                            publicidadesList = publicidades
                                        },
                                        onFailure = { exception ->
                                            println("Error recargando publicidades: ${exception.message}")
                                        }
                                    )
                                },
                                onFailure = { exception ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error guardando: ${exception.message}")
                                    }
                                }
                            )
                        }
                    },
                    onCancel = {
                        isEditing = false
                        selectedPublicidad = ""
                        nombrePublicidad = ""
                        imageUrl = ""
                        videoUrl = "" // NUEVO CAMPO
                        fechaInicial = ""
                        fechaFinal = ""
                        guionPublicidad = ""
                    }
                )
            }
        }
    }


    // Diálogo selector de imágenes de storage usando componente existente
    if (showImageSelector) {
        AdvancedImageSelector(
            title = "Galería de Publicidades",
            folderPath = "publicidades",
            currentImageUrl = imageUrl,
            onImageSelected = { downloadUrl ->
                imageUrl = downloadUrl
                showImageSelector = false
            },
            onDismiss = { showImageSelector = false },
            onUploadNew = {
                showImageSelector = false
                launcher.launch("image/*")
            }
        )
    }

    // NUEVO DIÁLOGO: Selector de spots existentes
    if (showSpotSelector) {
        PantallaPublicidadExistente(
            onSpotSeleccionado = { spotSeleccionado ->
                handleSpotSeleccionado(spotSeleccionado)
            },
            onDismiss = { showSpotSelector = false }
        )
    }

    if (showConfirmSaveDialog && spotPendienteGuardar != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmSaveDialog = false
                spotPendienteGuardar = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Guardado")
                }
            },
            text = {
                Column {
                    Text(
                        text = "¿Deseas guardar este spot como nueva publicidad?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "📺 ${spotPendienteGuardar!!.titulo}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📅 ${spotPendienteGuardar!!.fechaInicio} - ${spotPendienteGuardar!!.fechaFinal}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "🔗 Imagen incluida",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val yaExiste = publicidadesList.contains(spotPendienteGuardar!!.titulo)
                    if (yaExiste) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Información",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ya existe una publicidad con este nombre. Se sobrescribirá.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showConfirmSaveDialog = false
                            spotPendienteGuardar = null
                            limpiarFormulario()
                        }
                    ) {
                        Text("Cancelar")
                    }
                    FilledTonalButton(
                        onClick = {
                            spotPendienteGuardar?.let { spot ->
                                guardarSpotEnFirebase(spot)
                            }
                            showConfirmSaveDialog = false
                            spotPendienteGuardar = null
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            }
        )
    }

}

// ================================
// COMPONENTES UI (PRIVADOS)
// ================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSelectionCard(
    selectedProfile: String,
    perfilesList: List<String>,
    onProfileSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seleccionar Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedProfile,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Perfil") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    perfilesList.forEach { perfil ->
                        DropdownMenuItem(
                            text = { Text(perfil) },
                            onClick = {
                                onProfileSelected(perfil)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublicidadesListCard(
    publicidadesList: List<String>,
    selectedPublicidad: String,
    onPublicidadSelected: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var expandedPublicidades by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publicidades Disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (publicidadesList.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedPublicidades,
                    onExpandedChange = { expandedPublicidades = !expandedPublicidades }
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
                        onDismissRequest = { expandedPublicidades = false }
                    ) {
                        publicidadesList.forEach { publicidad ->
                            DropdownMenuItem(
                                text = { Text(publicidad) },
                                onClick = {
                                    onPublicidadSelected(publicidad)
                                    expandedPublicidades = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                }
                            )
                        }
                    }
                }

                // Botones de acción si hay publicidad seleccionada
                if (selectedPublicidad.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }

                        OutlinedButton(
                            onClick = { onDeleteClick(selectedPublicidad) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay publicidades para este perfil",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PublicidadFormCard(
    nombrePublicidad: String,
    imageUrl: String,
    videoUrl: String, // NUEVO CAMPO
    fechaInicial: String,
    fechaFinal: String,
    guionPublicidad: String,
    isUploading: Boolean,
    uploadProgress: Int,
    onNombreChange: (String) -> Unit,
    onVideoUrlChange: (String) -> Unit, // NUEVO CALLBACK
    onFechaInicialChange: (String) -> Unit,
    onFechaFinalChange: (String) -> Unit,
    onGuionChange: (String) -> Unit,
    onSelectFromDevice: () -> Unit,
    onSelectFromStorage: () -> Unit,
    onClearImage: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (nombrePublicidad.isEmpty()) "Nueva Publicidad" else "Editar Publicidad",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Campo nombre
            OutlinedTextField(
                value = nombrePublicidad,
                onValueChange = onNombreChange,
                label = { Text("Nombre de la publicidad") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = null)
                }
            )

            // Sección de imagen
            ImageSection(
                imageUrl = imageUrl,
                isUploading = isUploading,
                uploadProgress = uploadProgress,
                onSelectFromDevice = onSelectFromDevice,
                onSelectFromStorage = onSelectFromStorage,
                onClearImage = onClearImage
            )

            // NUEVO CAMPO: URL del video
            OutlinedTextField(
                value = videoUrl,
                onValueChange = onVideoUrlChange,
                label = { Text("URL del Video (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null)
                },
                placeholder = { Text("https://ejemplo.com/video.mp4") }
            )

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fechaInicial,
                    onValueChange = onFechaInicialChange,
                    label = { Text("Fecha inicial") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = fechaFinal,
                    onValueChange = onFechaFinalChange,
                    label = { Text("Fecha final") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(Icons.Default.Event, contentDescription = null)
                    }
                )
            }

            // Guión
            OutlinedTextField(
                value = guionPublicidad,
                onValueChange = onGuionChange,
                label = { Text("Guión publicitario") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                }
            )

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancelar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = nombrePublicidad.isNotBlank()
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun ImageSection(
    imageUrl: String,
    isUploading: Boolean,
    uploadProgress: Int,
    onSelectFromDevice: () -> Unit,
    onSelectFromStorage: () -> Unit,
    onClearImage: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Imagen Publicitaria",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        // Vista previa de la imagen
        if (imageUrl.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Vista previa de publicidad",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "✅ Imagen cargada correctamente",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sin imagen seleccionada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Progreso de subida
        if (isUploading) {
            LinearProgressIndicator(
                progress = uploadProgress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Subiendo... $uploadProgress%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Botones de selección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSelectFromDevice,
                modifier = Modifier.weight(1f),
                enabled = !isUploading,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Subir",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            OutlinedButton(
                onClick = onSelectFromStorage,
                modifier = Modifier.weight(1f),
                enabled = !isUploading,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Galería",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (imageUrl.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearImage,
                    enabled = !isUploading,
                    contentPadding = PaddingValues(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Limpiar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Información de ayuda
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "💡 Recomendaciones",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "• Formato: PNG/JPG (recomendado 400x100px)\n• 'Subir' sube desde tu dispositivo\n• 'Galería' muestra imágenes guardadas en Firebase",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}