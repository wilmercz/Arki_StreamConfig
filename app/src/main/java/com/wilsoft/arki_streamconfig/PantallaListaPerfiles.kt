
// Archivo: PantallaListaPerfiles.kt
package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaPerfiles(
    navController: NavController,
    firebaseRepository: FirebaseRepository
) {
    var perfilesList by remember { mutableStateOf(listOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf("") }
    var showLiveDialog by remember { mutableStateOf(false) }
    var profileToLive by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cargar perfiles al inicio
    LaunchedEffect(Unit) {
        firebaseRepository.loadProfiles(
            onSuccess = { perfiles ->
                perfilesList = perfiles
                isLoading = false
            },
            onFailure = { exception ->
                isLoading = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${exception.message}")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎨 Gestión de Perfiles",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("${NavigationRoutes.EDITAR_PERFIL_SCREEN}/nuevo")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("${NavigationRoutes.EDITAR_PERFIL_SCREEN}/nuevo")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear perfil")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (perfilesList.isEmpty()) {
                EmptyProfilesState(
                    onCreateProfile = {
                        navController.navigate("${NavigationRoutes.EDITAR_PERFIL_SCREEN}/nuevo")
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(perfilesList) { perfil ->
                        ProfileCard(
                            profileName = perfil,
                            onEdit = {
                                navController.navigate("${NavigationRoutes.EDITAR_PERFIL_SCREEN}/$perfil")
                            },
                            onDelete = {
                                profileToDelete = perfil
                                showDeleteDialog = true
                            },
                            onSetLive = {
                                profileToLive = perfil
                                showLiveDialog = true
                            },
                            firebaseRepository = firebaseRepository
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar el perfil '$profileToDelete'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        firebaseRepository.deleteProfile(
                            profileName = profileToDelete,
                            onSuccess = {
                                perfilesList = perfilesList.filter { it != profileToDelete }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Perfil eliminado exitosamente")
                                }
                                showDeleteDialog = false
                            },
                            onFailure = { error ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error: ${error.message}")
                                }
                                showDeleteDialog = false
                            }
                        )
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para poner en vivo
    if (showLiveDialog) {
        AlertDialog(
            onDismissRequest = { showLiveDialog = false },
            title = { Text("Poner en vivo") },
            text = { Text("¿Deseas activar el perfil '$profileToLive' para transmisión en vivo?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        copyProfileToStreamLive(profileToLive, firebaseRepository)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Perfil '$profileToLive' activado en vivo")
                        }
                        showLiveDialog = false
                    }
                ) {
                    Text("Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profileName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetLive: () -> Unit,
    firebaseRepository: FirebaseRepository
) {
    var profileData by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(profileName) {
        firebaseRepository.loadProfile(
            profileName = profileName,
            onSuccess = { data -> profileData = data },
            onFailure = { /* Handle error */ }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del perfil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    profileData?.let { data ->
                        Text(
                            text = "Logo: ${if ((data["urlLogo"] as? String)?.isNotEmpty() == true) "✅" else "❌"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Preview de colores
                profileData?.let { data ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ColorPreview(
                            color = data["colorFondo1"] as? String ?: "#1066FF",
                            size = 20.dp
                        )
                        ColorPreview(
                            color = data["colorFondo2"] as? String ?: "#F08313",
                            size = 20.dp
                        )
                        ColorPreview(
                            color = data["colorLetra1"] as? String ?: "#FFFFFF",
                            size = 20.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSetLive,
                modifier = Modifier
                    .fillMaxWidth() // ✅ Esto garantiza que el botón ocupe toda la fila
                    .height(48.dp)  // Opcional: altura consistente
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Poner en Vivo")
            }

        }
    }
}

@Composable
private fun ColorPreview(
    color: String,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = try {
                    Color(android.graphics.Color.parseColor(color))
                } catch (e: Exception) {
                    Color.Gray
                },
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

@Composable
private fun EmptyProfilesState(
    onCreateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay perfiles creados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Crea tu primer perfil para empezar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCreateProfile) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Primer Perfil")
        }
    }
}


// FUNCIÓN SIMPLIFICADA Y SIN ERRORES: copyProfileToStreamLive
fun copyProfileToStreamLive(profile: String, firebaseRepository: FirebaseRepository) {
    firebaseRepository.loadProfile(profile, onSuccess = { profileData ->

        // ✅ DATOS BÁSICOS (los que ya se copiaban)
        val streamLiveData = mutableMapOf<String, Any>(
            "NombrePerfil" to (profileData["NombrePerfil"] ?: profile),

            // COLORES BÁSICOS
            "colorFondo1" to (profileData["colorFondo1"] ?: "#1066FF"),
            "colorFondo2" to (profileData["colorFondo2"] ?: "#FFFFFF"),
            "colorFondo3" to (profileData["colorFondo3"] ?: "#CCCCCC"),
            "colorLetra1" to (profileData["colorLetra1"] ?: "#FFFFFF"),
            "colorLetra2" to (profileData["colorLetra2"] ?: "#000000"),
            "colorLetra3" to (profileData["colorLetra3"] ?: "#333333"),

            // RECURSOS BÁSICOS
            "urlLogo" to (profileData["urlLogo"] ?: ""),
            "urlImagenPublicidad" to (profileData["urlImagenPublicidad"] ?: "")
        )

        // ✅ CONFIGURACIÓN DE CONTENIDO AVANZADA (anteriormente comentada)
        streamLiveData.apply {
            // CONTENIDO DE INVITADOS
            put("Invitado", profileData["Invitado"] ?: "")
            put("Rol", profileData["Rol"] ?: "")
            put("Tema", profileData["Tema"] ?: "")
            put("SubTema", profileData["SubTema"] ?: "")

            // ESTADOS DE VISUALIZACIÓN (Lower Thirds)
            put("Mostrar_Invitado", profileData["Mostrar_Invitado"] ?: false)
            put("Mostrar_Tema", profileData["Mostrar_Tema"] ?: false)
            put("Mostrar_SubTema", profileData["Mostrar_SubTema"] ?: false)
            put("Mostrar_Logo", profileData["Mostrar_Logo"] ?: false)
            put("Mostrar_Publicidad", profileData["Mostrar_Publicidad"] ?: false)

            // COMPATIBILIDAD CON NOMBRES ALTERNATIVOS
            put("GraficoInvitado", profileData["GraficoInvitado"] ?: profileData["Mostrar_Invitado"] ?: false)
            put("GraficoTema", profileData["GraficoTema"] ?: profileData["Mostrar_Tema"] ?: false)
            put("GraficoLogo", profileData["GraficoLogo"] ?: profileData["Mostrar_Logo"] ?: false)
            put("GraficoPublicidad", profileData["GraficoPublicidad"] ?: profileData["Mostrar_Publicidad"] ?: false)
            put("GraficoSubtema", profileData["GraficoSubtema"] ?: profileData["Mostrar_SubTema"] ?: false)

            // ✅ CONFIGURACIÓN BÁSICA DE POSICIONES Y TAMAÑOS
            put("logo_posicionX", profileData["logo_posicionX"] ?: 1870)
            put("logo_posicionY", profileData["logo_posicionY"] ?: 50)
            put("logo_ancho", profileData["logo_ancho"] ?: 120)
            put("logo_alto", profileData["logo_alto"] ?: 80)

            put("publicidad_posicionX", profileData["publicidad_posicionX"] ?: 1870)
            put("publicidad_posicionY", profileData["publicidad_posicionY"] ?: 1030)
            put("publicidad_ancho", profileData["publicidad_ancho"] ?: 300)
            put("publicidad_alto", profileData["publicidad_alto"] ?: 200)

            put("tema_posicionX", profileData["tema_posicionX"] ?: 960)
            put("tema_posicionY", profileData["tema_posicionY"] ?: 1030)

            put("invitado_posicionX", profileData["invitado_posicionX"] ?: 50)
            put("invitado_posicionY", profileData["invitado_posicionY"] ?: 1030)

            // ✅ CONFIGURACIÓN DE TIPOGRAFÍAS
            put("textoPrincipal_familia", profileData["textoPrincipal_familia"] ?: "Inter")
            put("textoPrincipal_tamano", profileData["textoPrincipal_tamano"] ?: "24")
            put("textoPrincipal_peso", profileData["textoPrincipal_peso"] ?: "BOLD")

            put("textoSecundario_familia", profileData["textoSecundario_familia"] ?: "Inter")
            put("textoSecundario_tamano", profileData["textoSecundario_tamano"] ?: "18")
            put("textoSecundario_peso", profileData["textoSecundario_peso"] ?: "MEDIUM")

            // ✅ CONFIGURACIÓN DE ANIMACIONES
            put("animacion_entrada", profileData["animacion_entrada"] ?: "SLIDE_IN_LEFT")
            put("animacion_salida", profileData["animacion_salida"] ?: "SLIDE_OUT_LEFT")
            put("animacion_duracion", profileData["animacion_duracion"] ?: 500)
            put("animacion_habilitada", profileData["animacion_habilitada"] ?: true)

            // ✅ CONFIGURACIÓN DE CANVAS
            put("canvas_resolucion", profileData["canvas_resolucion"] ?: "HD_1080P")
            put("canvas_safeMargins", profileData["canvas_safeMargins"] ?: true)
            put("canvas_gridSnap", profileData["canvas_gridSnap"] ?: false)

            // ✅ LOGOS ALIADOS
            put("logosAliados_habilitado", profileData["logosAliados_habilitado"] ?: false)
            profileData["logosAliados_lista"]?.let { logosList ->
                put("logosAliados_lista", logosList)
            }

            // ✅ CONFIGURACIÓN ADICIONAL DE PUBLICIDADES
            profileData["publicidades"]?.let { publicidadesData ->
                put("publicidades_perfil", publicidadesData)
            }

            profileData["publicidadSeleccionada"]?.let { publicidadSel ->
                put("publicidadSeleccionada", publicidadSel)
            }

            // ✅ CONFIGURACIÓN DE INVITADOS
            profileData["invitadosConfig"]?.let { invitadosConfigData ->
                put("invitados_config", invitadosConfigData)
            }

            // ✅ CONFIGURACIÓN DE TRANSPARENCIAS Y OPACIDADES
            put("logo_opacidad", profileData["logo_opacidad"] ?: 1.0)
            put("publicidad_opacidad", profileData["publicidad_opacidad"] ?: 1.0)
            put("tema_opacidad", profileData["tema_opacidad"] ?: 0.9)
            put("invitado_opacidad", profileData["invitado_opacidad"] ?: 0.9)

            // ✅ CONFIGURACIÓN DE BORDES Y ESTILOS
            put("borderRadius", profileData["borderRadius"] ?: "8px")
            put("borderWidth", profileData["borderWidth"] ?: "0")
            put("borderColor", profileData["borderColor"] ?: "#FFFFFF")

            // ✅ CONFIGURACIÓN DE SOMBRAS
            put("shadow_enabled", profileData["shadow_enabled"] ?: true)
            put("shadow_color", profileData["shadow_color"] ?: "rgba(0,0,0,0.3)")
            put("shadow_blur", profileData["shadow_blur"] ?: 4)
            put("shadow_offsetX", profileData["shadow_offsetX"] ?: 2)
            put("shadow_offsetY", profileData["shadow_offsetY"] ?: 2)
        }

        // ✅ METADATOS DEL PERFIL
        streamLiveData.putAll(mapOf(
            "perfil_version" to (profileData["version"] ?: "1.0"),
            "perfil_fechaCreacion" to (profileData["fechaCreacion"] ?: System.currentTimeMillis()),
            "perfil_fechaActivacion" to System.currentTimeMillis(),
            "perfil_activo" to true,
            "perfil_tipo" to (profileData["tipo"] ?: "stream"),
            "perfil_descripcion" to (profileData["descripcion"] ?: "")
        ))

        // 🚀 GUARDAR EN STREAM_LIVE con toda la configuración avanzada
        firebaseRepository.saveData("CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS", streamLiveData,
            onSuccess = {
                println("✅ Perfil '$profile' activado en vivo con configuración COMPLETA")
                println("📊 Campos copiados: ${streamLiveData.size}")
                println("🎨 Colores: ${streamLiveData["colorFondo1"]}, ${streamLiveData["colorLetra1"]}")
                println("🖼️ Logo: ${streamLiveData["urlLogo"]}")
                println("📺 Publicidad: ${streamLiveData["urlImagenPublicidad"]}")
            },
            onFailure = { exception ->
                println("❌ Error al poner el perfil en vivo: ${exception.message}")
            }
        )
    }, onFailure = { exception ->
        println("❌ Error al cargar el perfil: ${exception.message}")
    })
}

// ✅ FUNCIÓN AUXILIAR: Verificar qué se copió
fun verifyStreamLiveData(firebaseRepository: FirebaseRepository) {
    firebaseRepository.loadStreamData("CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS",
        onSuccess = { data ->
            println("🔍 VERIFICACIÓN - DATOS EN STREAM_LIVE:")
            println("═══════════════════════════════════════")

            // Mostrar datos básicos
            println("📋 BÁSICOS:")
            println("  - Perfil: ${data["NombrePerfil"]}")
            println("  - Color Fondo 1: ${data["colorFondo1"]}")
            println("  - Color Letra 1: ${data["colorLetra1"]}")

            // Mostrar contenido
            println("\n📝 CONTENIDO:")
            println("  - Invitado: ${data["Invitado"]}")
            println("  - Rol: ${data["Rol"]}")
            println("  - Tema: ${data["Tema"]}")

            // Mostrar estados
            println("\n🎛️ ESTADOS:")
            println("  - Mostrar Invitado: ${data["Mostrar_Invitado"]}")
            println("  - Mostrar Tema: ${data["Mostrar_Tema"]}")
            println("  - Mostrar Logo: ${data["Mostrar_Logo"]}")

            // Mostrar configuración avanzada
            println("\n⚙️ CONFIGURACIÓN AVANZADA:")
            println("  - Animación Entrada: ${data["animacion_entrada"]}")
            println("  - Canvas Resolución: ${data["canvas_resolucion"]}")
            println("  - Logos Aliados: ${data["logosAliados_habilitado"]}")

            println("\n📊 TOTAL DE CAMPOS: ${data.size}")
            println("═══════════════════════════════════════")
        },
        onFailure = { error ->
            println("❌ Error verificando datos: ${error.message}")
        }
    )
}