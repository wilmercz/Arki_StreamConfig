package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.PlayArrow
import com.wilsoft.arki_streamconfig.FirebaseRepository.FirebasePaths.basePath
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfiles(firebaseRepository: FirebaseRepository, navController: NavController, scope: CoroutineScope = rememberCoroutineScope()) {
    // Variables de estado
    var selectedProfile by remember { mutableStateOf("") }  // Almacena el perfil seleccionado
    var perfilesList by remember { mutableStateOf(listOf<String>()) }  // Lista de perfiles cargada desde Firebase
    var isEditing by remember { mutableStateOf(false) }  // Controla si se está editando un perfil
    var nombrePerfil by remember { mutableStateOf("") }  // Nombre del perfil en edición
    //var sPerfil by remember { mutableStateOf("") }  // Nombre del perfil en edición
    var colorFondo1 by remember { mutableStateOf("#FFFFFF") }  // Colores asociados al perfil
    var colorFondo2 by remember { mutableStateOf("#CCCCCC") }  // Color fondo adicional
    var colorFondo3 by remember { mutableStateOf("#AAAAAA") }  // Color fondo adicional
    var colorLetra1 by remember { mutableStateOf("#000000") }
    var colorLetra2 by remember { mutableStateOf("#333333") }  // Color letra adicional
    var colorLetra3 by remember { mutableStateOf("#444444") }  // Color letra adicional
    var urlLogo by remember { mutableStateOf("") }  // URL del logo del perfil
    var urlImagenPublicidad by remember { mutableStateOf("") }  // URL de imagen publicitaria del perfil
    var showSnackbar by remember { mutableStateOf(false) }  // Control para mostrar el mensaje de éxito

    val snackbarHostState = remember { SnackbarHostState() }
    // Agregar el estado para mostrar el diálogo de confirmación
    var showDialog by remember { mutableStateOf(false) }

    // Corutina para cargar perfiles desde Firebase al inicio
    LaunchedEffect(Unit) {
        firebaseRepository.loadProfiles(onSuccess = { perfiles ->
            perfilesList = perfiles  // Almacena la lista de perfiles
        }, onFailure = { exception ->
            println("Error cargando perfiles: ${exception.message}")
        })
    }

    // Interfaz de usuario
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Caja del menú desplegable para seleccionar un perfil
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            // Para cada perfil en la lista de perfiles, creamos un item de la lista
            items(perfilesList) { perfil ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            selectedProfile = perfil  // Al hacer clic, seleccionamos el perfil

                            //cargar datos actualmente
                            // Llamar a loadProfileData con las funciones de actualización de estado
                            loadProfileData(
                                perfil,
                                firebaseRepository,
                                setNombrePerfil = { nombrePerfil = it },
                                setUrlLogo = { urlLogo = it },
                                setUrlImagenPublicidad = { urlImagenPublicidad = it },
                                setColorFondo1 = { colorFondo1 = it },  // Actualiza el colorFondo1
                                setColorFondo2 = { colorFondo2 = it },  // Actualiza el colorFondo2
                                setColorFondo3 = { colorFondo3 = it },  // Actualiza el colorFondo3
                                setColorLetra1 = { colorLetra1 = it },  // Actualiza el colorLetra1
                                setColorLetra2 = { colorLetra2 = it },  // Actualiza el colorLetra2
                                setColorLetra3 = { colorLetra3 = it }   // Actualiza el colorLetra3
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = perfil, style = MaterialTheme.typography.bodyLarge)
                    // Si necesitas agregar botones como Editar o Eliminar, se pueden colocar aquí
                }
            }
        }

        // Mostrar botones de acción si hay un perfil seleccionado
        if (selectedProfile.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar perfil")
                }

                Button(onClick = { deleteProfile(firebaseRepository, selectedProfile) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar perfil")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar perfil")
                }
            }

            // Botón para poner en vivo
            Button(
                onClick = {
                    showDialog = true // Mostrar el diálogo de confirmación
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Poner en Vivo")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Poner en Vivo")
            }

// Mostrar el diálogo si showDialog es verdadero
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false }, // Cerrar el diálogo si se cancela
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirmLiveStream(profile = selectedProfile, firebaseRepository = firebaseRepository) // Acción al confirmar
                                showDialog = false // Cerrar el diálogo
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false } // Cerrar el diálogo sin hacer nada
                        ) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Confirmar acción") },
                    text = { Text("¿Estás seguro de que deseas poner en vivo el perfil seleccionado?") }
                )
            }

            //fin codigo de poner al aire

        }

        // Botón para crear un nuevo perfil, siempre visible
        Button(
            onClick = {
                startNewProfile(
                    setEditing = { isEditing = it },
                    setNombrePerfil = { nombrePerfil = it },
                    setColorFondo1 = { colorFondo1 = it },
                    setColorFondo2 = { colorFondo2 = it },
                    setColorFondo3 = { colorFondo3 = it },
                    setColorLetra1 = { colorLetra1 = it },
                    setColorLetra2 = { colorLetra2 = it },
                    setColorLetra3 = { colorLetra3 = it },
                    setUrlLogo = { urlLogo = it },
                    setUrlImagenPublicidad = { urlImagenPublicidad = it }
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo perfil")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear nuevo perfil")
        }

        // Formulario de edición o creación, visible solo si se está editando
        if (isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())  // Habilitar scroll vertical
                    .padding(top = 16.dp)
            ) {
                // Campos para el perfil
                Text("Nombre del perfil")
                BasicTextField(
                    value = nombrePerfil,
                    onValueChange = { nombrePerfil = it },
                    textStyle = TextStyle(fontSize = 18.sp, color = if (nombrePerfil.isEmpty()) Color.Gray else Color.Black),  // Tamaño 18sp y color gris si está vacío
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Fondo 1")
                BasicTextField(
                    value = colorFondo1,
                    onValueChange = { colorFondo1 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isValidHexColor(colorFondo1)) Color(android.graphics.Color.parseColor(colorFondo1))
                            else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Fondo 2")
                BasicTextField(
                    value = colorFondo2,
                    onValueChange = { colorFondo2 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isValidHexColor(colorFondo2)) Color(android.graphics.Color.parseColor(colorFondo2))
                            else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Fondo 3")
                BasicTextField(
                    value = colorFondo3,
                    onValueChange = { colorFondo3 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isValidHexColor(colorFondo3)) Color(android.graphics.Color.parseColor(colorFondo3))
                            else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Letra 1")
                BasicTextField(
                    value = colorLetra1,
                    onValueChange = { colorLetra1 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isValidHexColor(colorLetra1)) Color(android.graphics.Color.parseColor(colorLetra1))
                            else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Letra 2")
                BasicTextField(
                    value = colorLetra2,
                    onValueChange = { colorLetra2 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isValidHexColor(colorLetra2)) Color(android.graphics.Color.parseColor(colorLetra2))
                                else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Letra 3")
                BasicTextField(
                    value = colorLetra3,
                    onValueChange = { colorLetra3 = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isValidHexColor(colorLetra3)) Color(android.graphics.Color.parseColor(colorLetra3))
                        else Color.White  // Fondo blanco si no es válido
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("URL del logo")
                BasicTextField(value = urlLogo, onValueChange = { urlLogo = it }, textStyle = TextStyle(fontSize = 18.sp))
                print("RECUPERANDO LOGO RUTA: $urlLogo")
                Spacer(modifier = Modifier.height(16.dp))
                Text("URL de imagen de publicidad")
                BasicTextField(value = urlImagenPublicidad, onValueChange = { urlImagenPublicidad = it }, textStyle = TextStyle(fontSize = 18.sp))

                // Botones de acción: Guardar o Cancelar la edición
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = {
                        if (nombrePerfil.isNotBlank()) {  // Validar que el nombre del perfil no esté vacío
                            saveProfile(firebaseRepository, nombrePerfil, colorFondo1, colorFondo2, colorFondo3, colorLetra1, colorLetra2, colorLetra3, urlLogo, urlImagenPublicidad)

                            scope.launch {
                                snackbarHostState.showSnackbar("Perfil guardado exitosamente")  // Mostrar mensaje de éxito
                            }

                            isEditing = false  // Hacer invisible el formulario después de guardar

                            // Limpiar los campos del formulario
                            nombrePerfil = ""
                            colorFondo1 = "#FFFFFF"
                            colorFondo2 = "#CCCCCC"
                            colorFondo3 = "#AAAAAA"
                            colorLetra1 = "#000000"
                            colorLetra2 = "#333333"
                            colorLetra3 = "#444444"
                            urlLogo = ""
                            urlImagenPublicidad = ""

                            refreshProfilesList(firebaseRepository)  // Actualizar la lista de perfiles
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor, ingrese un nombre de perfil válido")  // Mostrar mensaje de error si el nombre está vacío
                            }
                        }
                    }) {
                        Text("Guardar")
                    }



                    Button(onClick = { cancelEdit() }) {
                        Text("Cancelar")
                    }
                }


                Spacer(modifier = Modifier.height(100.dp))  // Espacio adicional para evitar bloqueo por el teclado
            }
        }

        SnackbarHost(hostState = snackbarHostState)  // Host para mostrar el mensaje de éxito
    }
}




fun confirmLiveStream(profile: String, firebaseRepository: FirebaseRepository) {
    if (profile.isNotEmpty()) {
        copyProfileToStreamLive(profile = profile, firebaseRepository = firebaseRepository)
    } else {
        println("No hay un perfil seleccionado.")
    }
}



// Función para validar colores hexadecimales
fun isValidHexColor(color: String): Boolean {
    val regex = "^#[0-9A-Fa-f]{6}$".toRegex()  // Patrón para #RRGGBB
    return color.matches(regex)
}


fun startNewProfile(
    setEditing: (Boolean) -> Unit,
    setNombrePerfil: (String) -> Unit,
    setColorFondo1: (String) -> Unit,
    setColorFondo2: (String) -> Unit,
    setColorFondo3: (String) -> Unit,
    setColorLetra1: (String) -> Unit,
    setColorLetra2: (String) -> Unit,
    setColorLetra3: (String) -> Unit,
    setUrlLogo: (String) -> Unit,
    setUrlImagenPublicidad: (String) -> Unit
) {
    // Establecer isEditing en true para mostrar el formulario
    setEditing(true)

    // Vaciar los campos del formulario para un nuevo perfil
    setNombrePerfil("")
    setColorFondo1("#FFFFFF")  // Color de fondo por defecto
    setColorFondo2("#CCCCCC")  // Segundo color de fondo por defecto
    setColorFondo3("#AAAAAA")  // Tercer color de fondo por defecto
    setColorLetra1("#000000")  // Color de letra por defecto
    setColorLetra2("#333333")  // Segundo color de letra por defecto
    setColorLetra3("#444444")  // Tercer color de letra por defecto
    setUrlLogo("")  // URL del logo vacía
    setUrlImagenPublicidad("")  // URL de imagen publicitaria vacía
}


// Funciones auxiliares
fun loadProfileData(
    profile: String,
    firebaseRepository: FirebaseRepository,
    setNombrePerfil: (String) -> Unit,  // Función para actualizar el nombre del perfil
    setUrlLogo: (String) -> Unit,  // Función para actualizar la URL del logo
    setUrlImagenPublicidad: (String) -> Unit,  // Función para actualizar la URL de imagen publicitaria
    setColorFondo1: (String) -> Unit,  // Nuevos parámetros
    setColorFondo2: (String) -> Unit,
    setColorFondo3: (String) -> Unit,
    setColorLetra1: (String) -> Unit,
    setColorLetra2: (String) -> Unit,
    setColorLetra3: (String) -> Unit
) {
    firebaseRepository.loadProfile(profile, onSuccess = { data ->
        // Actualizar los estados usando las funciones pasadas como parámetros
        setNombrePerfil(data["NombrePerfil"] as? String ?: "")
        setUrlLogo(data["urlLogo"] as? String ?: "")
        setUrlImagenPublicidad(data["urlImagenPublicidad"] as? String ?: "")
        // Asignar los colores
        setColorFondo1(data["colorFondo1"] as? String ?: "#FFFFFF")
        setColorFondo2(data["colorFondo2"] as? String ?: "#CCCCCC")
        setColorFondo3(data["colorFondo3"] as? String ?: "#AAAAAA")
        setColorLetra1(data["colorLetra1"] as? String ?: "#000000")
        setColorLetra2(data["colorLetra2"] as? String ?: "#333333")
        setColorLetra3(data["colorLetra3"] as? String ?: "#444444")
    }, onFailure = { exception ->
        println("Error al cargar el perfil: ${exception.message}")
    })
}

//fun loadProfileData(profile: String, firebaseRepository: FirebaseRepository) {
    // Cargar datos del perfil seleccionado desde Firebase
 //   firebaseRepository.loadProfile(profile, onSuccess = { data ->
        // Aquí se asignan los datos del perfil seleccionado
 //       println("Datos recuperados del perfil $profile: $data")

 //   }, onFailure = { exception ->
        // Manejo de errores
 //   })
//}

fun resetFields(
    setNombrePerfil: (String) -> Unit,
    setColorFondo1: (String) -> Unit,
    setColorFondo2: (String) -> Unit,
    setColorFondo3: (String) -> Unit,
    setColorLetra1: (String) -> Unit,
    setColorLetra2: (String) -> Unit,
    setColorLetra3: (String) -> Unit,
    setUrlLogo: (String) -> Unit,
    setUrlImagenPublicidad: (String) -> Unit
) {
    setNombrePerfil("")
    setColorFondo1("#FFFFFF")
    setColorFondo2("#CCCCCC")
    setColorFondo3("#AAAAAA")
    setColorLetra1("#000000")
    setColorLetra2("#333333")
    setColorLetra3("#444444")
    setUrlLogo("")
    setUrlImagenPublicidad("")
}

fun saveProfile(
    firebaseRepository: FirebaseRepository,
    NombrePerfil: String,
    colorFondo1: String,
    colorFondo2: String,
    colorFondo3: String,
    colorLetra1: String,
    colorLetra2: String,
    colorLetra3: String,
    urlLogo: String,
    urlImagenPublicidad: String
) {
    // Guardar el perfil en Firebase, incluyendo el nombre del perfil como subclave "perfil"
    val perfilData = mapOf(
        "NombrePerfil" to NombrePerfil,  // Añadir el nombre del perfil como subclave
        "colorFondo1" to colorFondo1,
        "colorFondo2" to colorFondo2,
        "colorFondo3" to colorFondo3,
        "colorLetra1" to colorLetra1,
        "colorLetra2" to colorLetra2,
        "colorLetra3" to colorLetra3,
        "urlLogo" to urlLogo,
        "urlImagenPublicidad" to urlImagenPublicidad
    )

    // Guardar los datos del perfil en la ruta especificada en Firebase
    firebaseRepository.saveData("CLAVE_STREAM_FB/PERFILES/$NombrePerfil", perfilData, onSuccess = {
        println("Perfil guardado exitosamente")
    }, onFailure = { exception ->
        println("Error al guardar el perfil: ${exception.message}")
    })
}

fun cancelEdit() {
    // Cancelar la edición del perfil
}

fun deleteProfile(firebaseRepository: FirebaseRepository, profile: String) {
    // Eliminar el perfil seleccionado de Firebase
    firebaseRepository.deleteProfile(profile, onSuccess = {
        // Manejo de éxito
    }, onFailure = { exception ->
        // Manejo de errores
    })
}

fun refreshProfilesList(firebaseRepository: FirebaseRepository) {
    // Actualiza la lista de perfiles después de guardar un nuevo perfil
    firebaseRepository.loadProfiles(onSuccess = { perfiles ->
        // Aquí actualiza la lista de perfiles
    }, onFailure = { exception ->
        println("Error al actualizar la lista de perfiles: ${exception.message}")
    })
}
