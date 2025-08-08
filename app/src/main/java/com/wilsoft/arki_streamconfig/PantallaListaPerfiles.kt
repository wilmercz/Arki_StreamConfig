
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