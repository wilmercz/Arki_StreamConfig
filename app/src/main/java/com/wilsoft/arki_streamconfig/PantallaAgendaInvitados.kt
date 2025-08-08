package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.wilsoft.arki_streamconfig.DatePicker
import com.wilsoft.arki_streamconfig.Invitado
import androidx.compose.ui.unit.sp  // Importar la unidad sp

//fun PantallaPerfiles(firebaseRepository: FirebaseRepository, navController: NavController, scope: CoroutineScope = rememberCoroutineScope()) {
@Composable
fun PantallaAgendaInvitados(navController: NavController, firebaseRepository: FirebaseRepository) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var invitadosList by remember { mutableStateOf<List<Invitado>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentInvitado by remember { mutableStateOf<Invitado?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var invitadoToDelete by remember { mutableStateOf<Invitado?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun loadInvitadosForDate(date: Calendar) {
        val dateStr = "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-${date.get(Calendar.DAY_OF_MONTH)}"
        firebaseRepository.loadStreamData("CLAVE_STREAM_FB/INVITADOS/$dateStr",
            onSuccess = { data ->
                val invitados = data.map { (key, value) ->
                    val invitadoMap = value as Map<String, Any>
                    Invitado(
                        nombre = key,
                        rol = invitadoMap["Rol"] as? String ?: "",
                        tema = invitadoMap["Tema"] as? String ?: "",
                        subTema = invitadoMap["SubTema"] as? String ?: "",
                        graficoInvitado = invitadoMap["GraficoInvitado"] as? Boolean ?: false
                    )
                }
                invitadosList = invitados
            },
            onFailure = {
                invitadosList = emptyList()
            }
        )
    }

    LaunchedEffect(selectedDate) {
        loadInvitadosForDate(selectedDate)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text(text = "Selecciona una Fecha:", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            DatePicker(selectedDate) { newDate ->
                selectedDate = newDate
                loadInvitadosForDate(selectedDate)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (invitadosList.isEmpty()) {
                    item { Text(text = "No hay invitados para la fecha seleccionada.") }
                } else {
                    items(invitadosList) { invitado ->
                        InvitadoRow(
                            invitado = invitado,
                            onEditClick = {
                                currentInvitado = invitado
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                invitadoToDelete = invitado
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        MostrarFormularioInvitado(
            navController = navController,
            firebaseRepository = firebaseRepository,
            selectedDate = selectedDate,
            invitado = null,
            onSaveSuccess = {
                showCreateDialog = false
                loadInvitadosForDate(selectedDate)
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showEditDialog) {
        MostrarFormularioInvitado(
            navController = navController,
            firebaseRepository = firebaseRepository,
            selectedDate = selectedDate,
            invitado = currentInvitado,
            onSaveSuccess = {
                showEditDialog = false
                loadInvitadosForDate(selectedDate)
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteConfirmation) {
        MostrarConfirmacionEliminar(
            nombre = invitadoToDelete?.nombre ?: "",
            onConfirm = {
                coroutineScope.launch {
                    invitadoToDelete?.let { invitado ->
                        firebaseRepository.deleteProfile(invitado.nombre,
                            onSuccess = {
                                loadInvitadosForDate(selectedDate)
                            },
                            onFailure = { error ->
                                println("Error al eliminar: ${error.message}")
                            }
                        )
                    }
                }
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }
}

@Composable
fun InvitadoRow(invitado: Invitado, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = invitado.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = "Rol: ${invitado.rol}", fontSize = 12.sp)
            Text(text = "Tema: ${invitado.tema}", fontSize = 10.sp)
        }
        Row {
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
fun MostrarConfirmacionEliminar(
    nombre: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que deseas eliminar a $nombre?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Sí")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@Composable
fun MostrarFormularioInvitado(
    navController: NavController,
    firebaseRepository: FirebaseRepository,
    selectedDate: Calendar,
    invitado: Invitado?,
    onSaveSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(invitado?.nombre ?: "") }
    var rol by remember { mutableStateOf(invitado?.rol ?: "") }
    var tema by remember { mutableStateOf(invitado?.tema ?: "") }
    var subTema by remember { mutableStateOf(invitado?.subTema ?: "") }
    var graficoInvitado by remember { mutableStateOf(invitado?.graficoInvitado ?: false) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (invitado == null) "Crear Invitado" else "Editar Invitado") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Invitado") })
                TextField(value = rol, onValueChange = { rol = it }, label = { Text("Rol del Invitado") })
                TextField(value = tema, onValueChange = { tema = it }, label = { Text("Tema") })
                TextField(value = subTema, onValueChange = { subTema = it }, label = { Text("SubTema") })

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Switch(
                        checked = graficoInvitado,
                        onCheckedChange = { graficoInvitado = it }
                    )
                    Text(text = "Mostrar gráfico del invitado", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dateStr = "${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(Calendar.DAY_OF_MONTH)}"
                val newInvitado = mapOf(
                    "Rol" to rol,
                    "Tema" to tema,
                    "SubTema" to subTema,
                    "GraficoInvitado" to graficoInvitado
                )

                coroutineScope.launch {
                    firebaseRepository.saveData("CLAVE_STREAM_FB/INVITADOS/$dateStr/$nombre", newInvitado,
                        onSuccess = {
                            onSaveSuccess()
                            onDismiss()
                        },
                        onFailure = { error -> println("Error al guardar: ${error.message}") }
                    )
                }
            }) {
                Text(text = if (invitado == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}



