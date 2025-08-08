package com.wilsoft.arki_streamconfig

import androidx.compose.foundation.layout.* // Importación para el manejo de layout (Column, Row, Spacer)
import androidx.compose.material3.* // Importación del nuevo Material3
import androidx.compose.runtime.* // Para el manejo de estado (remember, mutableStateOf)
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // Manejo de dimensiones (dp)
import androidx.navigation.NavController
import android.util.Log // Para el manejo de logs
//import com.wilsoft.arki_streamconfig.FirebaseRepository // Importar nuestro repositorio de Firebase
import androidx.compose.ui.text.AnnotatedString // Para el manejo de texto anotado (portapapeles)
import androidx.compose.ui.platform.LocalClipboardManager // Para manejar el portapapeles
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons // Importar íconos de Material
import androidx.compose.material.icons.filled.ContentCopy  // Ícono para "Copiar"
import androidx.compose.material.icons.filled.ContentPaste  // Ícono para "Pegar"
import androidx.compose.material.icons.filled.Clear  // Ícono para "Limpiar"
import androidx.compose.material.icons.filled.Save  // Ícono para "Guardar"
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
// Importa para manejar la visibilidad
//import androidx.compose.foundation.Image
//import androidx.compose.ui.res.painterResource // Para cargar la imagen del logotipo
//import androidx.compose.material.icons.filled.Person  // Ícono de Persona
//import androidx.compose.material.icons.filled.BarChart  // Ícono de gráfico (o puedes usar otro según el caso)
//import androidx.compose.material.icons.filled.Save  // Ícono de guardar (si lo usas en otros lugares)
//import androidx.compose.ui.graphics.vector.ImageVector  // Para manejar los íconos en vectores
import androidx.compose.material.icons.filled.ExpandLess  // Ícono para colapsar
import androidx.compose.material.icons.filled.ExpandMore  // Ícono para expandir
import androidx.compose.material.icons.filled.Camera  // Ícono de cámara
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.app.Activity
import com.wilsoft.arki_streamconfig.utilidades.SimpleOrientationHelper
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun PantallaPrincipal(navController: NavController, firebaseRepository: FirebaseRepository) {
    // Controla si la sección del acordeón está expandida o no
    var isExpanded by remember { mutableStateOf(false) }

    // Variables de estado para los datos que se leerán y escribirán en Firebase
    var transmissionKey by remember { mutableStateOf("") }
    var rtpServer by remember { mutableStateOf("") }
    var lowerThirdLink by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Variables para indicar si los datos han sido leídos remotamente
    var isKeyReadRemotely by remember { mutableStateOf(false) }
    var isDescriptionRead by remember { mutableStateOf(false) }
    var isServerRead by remember { mutableStateOf(false) }
    var isTitleRead by remember { mutableStateOf(false) }

    // Copiar y pegar desde el portapapeles
    val clipboardManager = LocalClipboardManager.current

    // Funciones para copiar, pegar y limpiar los valores
    //val onCopyKey = { clipboardManager.setText(AnnotatedString(transmissionKey)) }
    val onCopyKey = {
        // Copiar el valor de "Clave de transmisión" al portapapeles
        clipboardManager.setText(AnnotatedString(transmissionKey))

        // Actualizar en Firebase que el campo "Clave_Estado" ha sido leído (true)
        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Clave_Estado" to true), onSuccess = {
            Log.d("Firebase", "Clave_Estado actualizado a true")
        }, onFailure = { error ->
            Log.e("Firebase", "Error al actualizar Clave_Estado: ${error.message}")
        })

        // Actualizar el estado local para reflejar el cambio en la interfaz
        //isKeyReadRemotely = true
    }

    val onPasteKey = { transmissionKey = clipboardManager.getText()?.text ?: "" }
    val onClearKey = { transmissionKey = "" }

    //val onCopyRTPM = { clipboardManager.setText(AnnotatedString(rtpServer)) }
    val onCopyRTPM = {
        // Copiar el valor de "Servidor RTPM" al portapapeles
        clipboardManager.setText(AnnotatedString(rtpServer))

        // Actualizar en Firebase que el campo "Servidor_Estado" ha sido leído (true)
        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Servidor_Estado" to true), onSuccess = {
            Log.d("Firebase", "Servidor_Estado actualizado a true")
        }, onFailure = { error ->
            Log.e("Firebase", "Error al actualizar Servidor_Estado: ${error.message}")
        })

        // Actualizar el estado local para reflejar el cambio en la interfaz
        //isServerRead = true
    }

    val onPasteRTPM = { rtpServer = clipboardManager.getText()?.text ?: "" }
    val onClearRTPM = { rtpServer = "" }

    //val onCopyLowerThird = { clipboardManager.setText(AnnotatedString(lowerThirdLink)) }
    val onCopyLowerThird = {
        // Copiar el valor de "Lower Third" al portapapeles
        clipboardManager.setText(AnnotatedString(lowerThirdLink))

        // Actualizar en Firebase que el campo "Lower1_Estado" ha sido leído (true)
        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Lower1_Estado" to true), onSuccess = {
            Log.d("Firebase", "Lower1_Estado actualizado a true")
        }, onFailure = { error ->
            Log.e("Firebase", "Error al actualizar Lower1_Estado: ${error.message}")
        })

        // No tienes variable local para el ícono de Lower Third, pero podrías agregarla si es necesario
    }

    val onPasteLowerThird = { lowerThirdLink = clipboardManager.getText()?.text ?: "" }
    val onClearLowerThird = { lowerThirdLink = "" }

    //val onCopyTitle = { clipboardManager.setText(AnnotatedString(title)) }
    val onCopyTitle = {
        // Copiar el valor de "Título" al portapapeles
        clipboardManager.setText(AnnotatedString(title))

        // Actualizar en Firebase que el campo "Titulo_Estado" ha sido leído (true)
        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Titulo_Estado" to true), onSuccess = {
            Log.d("Firebase", "Titulo_Estado actualizado a true")
        }, onFailure = { error ->
            Log.e("Firebase", "Error al actualizar Titulo_Estado: ${error.message}")
        })

        // Actualizar el estado local para reflejar el cambio en la interfaz
        //isTitleRead = true
    }

    val onPasteTitle = { title = clipboardManager.getText()?.text ?: "" }
    val onClearTitle = { title = "" }

    //val onCopyDescription = { clipboardManager.setText(AnnotatedString(description)) }
    val onCopyDescription = {
        // Copiar el valor de "Descripción" al portapapeles
        clipboardManager.setText(AnnotatedString(description))

        // Actualizar en Firebase que el campo "Descripcion_Estado" ha sido leído (true)
        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Descripcion_Estado" to true), onSuccess = {
            Log.d("Firebase", "Descripcion_Estado actualizado a true")
        }, onFailure = { error ->
            Log.e("Firebase", "Error al actualizar Descripcion_Estado: ${error.message}")
        })

        // Actualizar el estado local para reflejar el cambio en la interfaz
        //isDescriptionRead = true
    }

    val onPasteDescription = { description = clipboardManager.getText()?.text ?: "" }
    val onClearDescription = { description = "" }

    var isDataLoaded by remember { mutableStateOf(false) }


    // Cargar datos desde Firebase cuando la pantalla se carga
    LaunchedEffect(Unit) {
        firebaseRepository.loadStreamData(FirebaseRepository.basePath, onSuccess = { data ->
            Log.d("FirebaseSuccess", "Datos cargados: $data")

            // Asignar los valores obtenidos de Firebase a las variables de estado
            transmissionKey = data["Clave"] as? String ?: ""
            description = data["Descripcion"] as? String ?: ""
            lowerThirdLink = data["Lower1"] as? String ?: ""
            rtpServer = data["Servidor"] as? String ?: ""
            title = data["Titulo"] as? String ?: ""

            // Estados de "leído remotamente"
            isKeyReadRemotely = (data["Clave_Estado"] as? Long ?: 0L) == 1L
            isDescriptionRead = (data["Descripcion_Estado"] as? Long ?: 0L) == 1L
            isServerRead = (data["Servidor_Estado"] as? Long ?: 0L) == 1L
            isTitleRead = (data["Titulo_Estado"] as? Long ?: 0L) == 1L
            // Indicar que los datos se han cargado
            isDataLoaded = true
        }, onFailure = { exception ->
            Log.e("FirebaseError", "Error al cargar datos: ${exception.message}")
        })


        // Agregar el listener en tiempo real después de la carga inicial
        // Escuchar cambios en tiempo real
        //firebaseRepository.db.child(FirebaseRepository.basePath) // Acceso al nodo específico
        //    .addValueEventListener(object : ValueEventListener {
        //        override fun onDataChange(snapshot: DataSnapshot) {
                    // Actualizar las variables de estado con los datos de Firebase
        //           transmissionKey = snapshot.child("Clave").getValue(String::class.java) ?: ""
         //           description = snapshot.child("Descripcion").getValue(String::class.java) ?: ""
         //           lowerThirdLink = snapshot.child("Lower1").getValue(String::class.java) ?: ""
         //           rtpServer = snapshot.child("Servidor").getValue(String::class.java) ?: ""
         //           title = snapshot.child("Titulo").getValue(String::class.java) ?: ""

                    // Verificar si el estado de "Clave_Estado" ha cambiado antes de actualizar
         //          val newKeyReadState = snapshot.child("Clave_Estado").getValue(Long::class.java) == 1L
          //          if (newKeyReadState != isKeyReadRemotely) {
          //              isKeyReadRemotely = newKeyReadState
          //          }

                    // Verificar si el estado de "Descripcion_Estado" ha cambiado antes de actualizar
          //          val newDescriptionReadState = snapshot.child("Descripcion_Estado").getValue(Long::class.java) == 1L
          //          if (newDescriptionReadState != isDescriptionRead) {
           //             isDescriptionRead = newDescriptionReadState
           //         }

                    // Verificar si el estado de "Servidor_Estado" ha cambiado antes de actualizar
          //          val newServerReadState = snapshot.child("Servidor_Estado").getValue(Long::class.java) == 1L
          //          if (newServerReadState != isServerRead) {
          //              isServerRead = newServerReadState
          //          }

                    // Verificar si el estado de "Titulo_Estado" ha cambiado antes de actualizar
          //          val newTitleReadState = snapshot.child("Titulo_Estado").getValue(Long::class.java) == 1L
          //          if (newTitleReadState != isTitleRead) {
          //              isTitleRead = newTitleReadState
          //          }
          //      }

          //      override fun onCancelled(error: DatabaseError) {
          //          Log.e("FirebaseError", "Error al escuchar los datos: ${error.message}")
          //      }
          //  })
        //FIN ESCUCHA TIEMPO REAL

    }


// Configuración de la escucha en tiempo real
    DisposableEffect(isDataLoaded) {
        // Solo agrega el listener si los datos ya han sido cargados
        if (isDataLoaded) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    transmissionKey = snapshot.child("Clave").getValue(String::class.java) ?: ""
                    description = snapshot.child("Descripcion").getValue(String::class.java) ?: ""
                    lowerThirdLink = snapshot.child("Lower1").getValue(String::class.java) ?: ""
                    rtpServer = snapshot.child("Servidor").getValue(String::class.java) ?: ""
                    title = snapshot.child("Titulo").getValue(String::class.java) ?: ""

                    //POR AHORA NO PUEDO PONER ATUALIZA EN LA APP, LEIDO O NO LEIDO
                //PORQUE SE CUELGA LA APP
                // Actualización condicional de los estados de "leído/no leído"
                    //val newKeyReadState = snapshot.child("Clave_Estado").getValue(Long::class.java) == 1L
                    //if (newKeyReadState != isKeyReadRemotely) {
                    //    isKeyReadRemotely = newKeyReadState
                    //}
                   // val newDescriptionReadState = snapshot.child("Descripcion_Estado").getValue(Long::class.java) == 1L
                   // if (newDescriptionReadState != isDescriptionRead) {
                   //     isDescriptionRead = newDescriptionReadState
                    //}
                    //val newServerReadState = snapshot.child("Servidor_Estado").getValue(Long::class.java) == 1L
                    //if (newServerReadState != isServerRead) {
                    //    isServerRead = newServerReadState
                    //}
                    //val newTitleReadState = snapshot.child("Titulo_Estado").getValue(Long::class.java) == 1L
                    //if (newTitleReadState != isTitleRead) {
                    //    isTitleRead = newTitleReadState
                    //}
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error al escuchar los datos: ${error.message}")
                }
            }
            // Añadir el listener en tiempo real
            firebaseRepository.db.child(FirebaseRepository.basePath)
                .addValueEventListener(listener)

            // Eliminar el listener cuando el componente se destruya
            onDispose {
                firebaseRepository.db.child(FirebaseRepository.basePath)
                    .removeEventListener(listener)
            }
        } else {
            // Si no hay listener que añadir, devolvemos un `onDispose {}` vacío
            onDispose {}
        }
    }

    if (!isDataLoaded) {
        // Mostrar un círculo de progreso mientras los datos se cargan
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
    // Mostrar la interfaz principal cuando los datos se han cargado
    // Sección de la pantalla principal con un scroll
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Añadir desplazamiento
    ) {
        // --- LOGOTIPO --- //
        // El logotipo será visible solo cuando el acordeón esté colapsado
        if (!isExpanded) {
            Image(
                painter = painterResource(id = R.drawable.mi_logo), // Coloca tu imagen aquí
                contentDescription = "Logotipo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp) // Suficiente espacio alrededor para un aspecto profesional
                    .height(100.dp) // Tamaño fijo para el logotipo
            )
        }

        // --- CONTROL DE ORIENTACIÓN FACEBOOK - SIEMPRE VISIBLE --- //
        SimpleOrientationControl()

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN ACORDEÓN PARA INTERACTUAR CON FIREBASE --- //

        Button(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,  // Ícono de expansión/colapso
                contentDescription = if (isExpanded) "Ver menos" else "Ver más",
                modifier = Modifier.size(28.dp)  // Tamaño del ícono
            )
            Spacer(modifier = Modifier.width(8.dp))  // Espacio entre ícono y texto
            Text(text = if (isExpanded) "Ver menos" else "Configuración de Stream Live")
        }

        if (isExpanded) {
            // --- Contenedor principal con cards individuales --- //
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp) // Aumentar altura para acomodar cards
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {

                // SECCIÓN 1: CONFIGURACIÓN DE STREAM
                Text(
                    text = "⚡ CONFIGURACIÓN DE STREAM",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Campo "Clave de transmisión" - Card de solo lectura
                StreamInfoCard(
                    label = "Clave de Transmisión",
                    value = transmissionKey,
                    isReadRemotely = isKeyReadRemotely,
                    onCopy = onCopyKey,
                    onPaste = onPasteKey, // ¡Agregado!
                    onSave = { newValue ->
                        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Clave" to newValue), onSuccess = {
                            Log.d("Firebase", "Clave guardada exitosamente")
                        }, onFailure = { exception ->
                            Log.e("Firebase", "Error al guardar Clave", exception)
                        })
                    }, // ¡Agregado!
                    icon = Icons.Default.Key,
                    backgroundColor = if (isKeyReadRemotely) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo "Servidor RTPM" - Card de solo lectura
                StreamInfoCard(
                    label = "Servidor RTMP",
                    value = rtpServer,
                    isReadRemotely = isServerRead,
                    onCopy = onCopyRTPM,
                    onPaste = onPasteRTPM, // ¡Agregado!
                    onSave = { newValue ->
                        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Servidor" to newValue), onSuccess = {
                            Log.d("Firebase", "Servidor guardado exitosamente")
                        }, onFailure = { exception ->
                            Log.e("Firebase", "Error al guardar Servidor", exception)
                        })
                    }, // ¡Agregado!
                    icon = Icons.Default.Cloud,
                    backgroundColor = if (isServerRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN 2: GRÁFICOS Y CONTENIDO
                Text(
                    text = "🎨 GRÁFICOS Y CONTENIDO",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.secondary
                )

                // Campo "Lower Third" - Card de solo lectura
                StreamInfoCard(
                    label = "Lower Third",
                    value = lowerThirdLink,
                    isReadRemotely = false,
                    onCopy = onCopyLowerThird,
                    onPaste = onPasteLowerThird, // ¡Agregado!
                    onSave = { newValue ->
                        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Lower1" to newValue), onSuccess = {
                            Log.d("Firebase", "Lower Third guardado exitosamente")
                        }, onFailure = { exception ->
                            Log.e("Firebase", "Error al guardar Lower Third", exception)
                        })
                    }, // ¡Agregado!
                    icon = Icons.Default.TextFields,
                    backgroundColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo "Título" - Card editable
                StreamEditableCard(
                    label = "Título",
                    value = title,
                    onValueChange = { title = it },
                    isReadRemotely = isTitleRead,
                    onCopy = onCopyTitle,
                    onPaste = onPasteTitle,
                    onClear = onClearTitle,
                    onSave = { newValue ->
                        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Titulo" to newValue), onSuccess = {
                            Log.d("Firebase", "Título guardado exitosamente")
                        }, onFailure = { exception ->
                            Log.e("Firebase", "Error al guardar Título", exception)
                        })
                    },
                    icon = Icons.Default.Title,
                    backgroundColor = if (isTitleRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo "Descripción" - Card editable
                StreamEditableCard(
                    label = "Descripción",
                    value = description,
                    onValueChange = { description = it },
                    isReadRemotely = isDescriptionRead,
                    onCopy = onCopyDescription,
                    onPaste = onPasteDescription,
                    onClear = onClearDescription,
                    onSave = { newValue ->
                        firebaseRepository.saveData(FirebaseRepository.basePath, mapOf("Descripcion" to newValue), onSuccess = {
                            Log.d("Firebase", "Descripción guardada exitosamente")
                        }, onFailure = { exception ->
                            Log.e("Firebase", "Error al guardar Descripción", exception)
                        })
                    },
                    icon = Icons.Default.Description,
                    backgroundColor = if (isDescriptionRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            }
        }

        // --- FIN DE SECCIÓN ACORDEÓN --- //

        // --- BOTONES DE NAVEGACIÓN --- //
        Button(
            onClick = { navController.navigate(NavigationRoutes.GENERADOR_CARACTERES_SCREEN) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(80.dp)  // Aumentar la altura para acomodar el ícono y el texto en formato vertical
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centrar el ícono y el texto horizontalmente
                verticalArrangement = Arrangement.Center  // Centrar verticalmente dentro del botón
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,  // Ícono de TV (pantalla)
                    contentDescription = "Generador de Caracteres",
                    modifier = Modifier.size(32.dp)  // Tamaño del ícono
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el ícono y el texto
                Text("Generador de Caracteres")  // Texto debajo del ícono
            }
        }


        //Button(
        //    onClick = { navController.navigate(NavigationRoutes.GENERADOR_CARACTERES_SCREEN) },
        //    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        //) {
        //    Text("Generador de Caracteres")
        //}


        Button(
            onClick = { navController.navigate(NavigationRoutes.SWITCHER_VIDEO_SCREEN) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(80.dp)  // Aumentar la altura para acomodar el ícono y el texto en formato vertical
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centrar contenido horizontalmente
                verticalArrangement = Arrangement.Center  // Centrar contenido verticalmente
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,  // Ícono de cámara
                    contentDescription = "Switcher de Video",
                    modifier = Modifier.size(32.dp)  // Tamaño del ícono
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el ícono y el texto
                Text("Switcher de Video")  // Texto debajo del ícono
            }
        }



        //Button(
        //     onClick = { navController.navigate(NavigationRoutes.SWITCHER_VIDEO_SCREEN) },
        //    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        //) {
        //    Icon(
        //        imageVector = Icons.Default.Camera,  // Cambiar a ícono de cámara
        //       contentDescription = "Switcher de Video",
        //       modifier = Modifier.size(24.dp)
        //  )
        //  Spacer(modifier = Modifier.width(8.dp))  // Espacio entre el ícono y el texto
        //  Text("Switcher de Video")
        //}



        //Button(
        //    onClick = { navController.navigate(NavigationRoutes.SWITCHER_VIDEO_SCREEN) },
        //    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        // ) {
        //     Text("Switcher de Video")
        // }

        Button(
            onClick = { navController.navigate(NavigationRoutes.LISTA_PERFILES_SCREEN) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(80.dp)  // Aumentar la altura para acomodar el ícono y el texto en formato vertical
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centrar contenido horizontalmente
                verticalArrangement = Arrangement.Center  // Centrar contenido verticalmente
            ) {
                Icon(
                    imageVector = Icons.Default.Person,  // Ícono de persona
                    contentDescription = "Gestión de Perfiles",
                    modifier = Modifier.size(32.dp)  // Tamaño del ícono
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el ícono y el texto
                Text("Gestión de Perfiles")  // Texto debajo del ícono
            }
        }

        //Button(
        //    onClick = { navController.navigate(NavigationRoutes.PERFILES_SCREEN) },
        //    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        //) {
        //    Icon(
        //        imageVector = Icons.Default.Person,  // Ícono de persona
        //        contentDescription = "Gestión de Perfiles",
        //        modifier = Modifier.size(24.dp)
        //    )
        //    Spacer(modifier = Modifier.width(8.dp))  // Espacio entre el ícono y el texto
        //    Text("Gestión de Perfiles")
        //}




        Button(
            onClick = { navController.navigate(NavigationRoutes.AGENDA_INVITADOS_SCREEN) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(80.dp)  // Aumentar la altura para acomodar el ícono y el texto en formato vertical
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centrar el ícono y el texto horizontalmente
                verticalArrangement = Arrangement.Center  // Centrar verticalmente dentro del botón
            ) {
                Icon(
                    imageVector = Icons.Default.Event,  // Ícono de persona
                    contentDescription = "Agenda de Invitados",
                    modifier = Modifier.size(32.dp)  // Tamaño del ícono
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el ícono y el texto
                Text("Agenda de Invitados")  // Texto debajo del ícono
            }
        }

        //Button(
        //    onClick = { navController.navigate(NavigationRoutes.AGENDA_INVITADOS_SCREEN) },
        //    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        //) {
        //    Text("Agenda de Invitados")
        //}

        Button(
            onClick = { navController.navigate(NavigationRoutes.PUBLICIDAD_SCREEN) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(80.dp)  // Aumentar la altura para acomodar el ícono y el texto en formato vertical
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centrar el ícono y el texto horizontalmente
                verticalArrangement = Arrangement.Center  // Centrar verticalmente dentro del botón
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,  // Ícono de monetización
                    contentDescription = "Gestión de Publicidad",
                    modifier = Modifier.size(32.dp)  // Tamaño del ícono
                )
                Spacer(modifier = Modifier.height(4.dp))  // Espacio entre el ícono y el texto
                Text("Gestión de Publicidad")  // Texto debajo del ícono
            }
        }
    }
    }
}

// NUEVA FUNCIÓN: Para campos que solo muestran información (Clave, Servidor, Lower Third)
@Composable
fun StreamInfoCard(
    label: String,
    value: String,
    isReadRemotely: Boolean,
    onCopy: () -> Unit,
    onPaste: () -> Unit, // ¡Agregado el parámetro onPaste!
    onSave: (String) -> Unit, // ¡Agregado el parámetro onSave!
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val clipboardManager = LocalClipboardManager.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp) // Reducido de 16dp a 12dp
        ) {
            // Header compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp), // Reducido de 24dp
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Indicador compacto
                if (isReadRemotely) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Copiado",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Texto con botón PEGAR a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón PEGAR compacto a la derecha
                FilledTonalButton(
                    onClick = {
                        val clipboardText = clipboardManager.getText()?.text
                        if (!clipboardText.isNullOrEmpty()) {
                            onPaste()
                            onSave(clipboardText)
                        }
                    },
                    modifier = Modifier
                        .size(32.dp), // Botón cuadrado pequeño
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Pegar",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Texto en caja compacta
                Text(
                    text = if (value.isNotEmpty()) value else "No configurado",
                    style = MaterialTheme.typography.bodySmall, // Más pequeño
                    color = if (value.isNotEmpty())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f) // Ocupa el espacio disponible
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp), // Reducido padding
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Solo botón COPIAR - más compacto
            FilledTonalButton(
                onClick = onCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp) // Altura más pequeña
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("COPIAR", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


// FUNCIÓN MODIFICADA: Para campos editables (Título y Descripción)
@Composable
fun StreamEditableCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isReadRemotely: Boolean,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit,
    onSave: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val clipboardManager = LocalClipboardManager.current
    var isSaveEnabled by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con ícono, label y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Indicador de estado "leído/no leído"
                if (isReadRemotely) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Copiado",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COPIADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de texto EDITABLE
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    isSaveEnabled = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingrese $label") },
                minLines = if (label.contains("Descripción")) 2 else 1,
                maxLines = if (label.contains("Descripción")) 4 else 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón Copiar
                FilledTonalButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Pegar
                FilledTonalButton(
                    onClick = {
                        val clipboardText = clipboardManager.getText()?.text
                        if (!clipboardText.isNullOrEmpty()) {
                            onPaste()
                            onSave(clipboardText)
                            isSaveEnabled = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Pegar",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Limpiar
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}




@Composable
fun StreamConfigCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isReadRemotely: Boolean,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit,
    onSave: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val clipboardManager = LocalClipboardManager.current
    var isSaveEnabled by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con ícono, label y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Indicador de estado "leído/no leído"
                if (isReadRemotely) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Copiado",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COPIADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de texto
            OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    isSaveEnabled = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingrese $label") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción en una fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón Copiar
                FilledTonalButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(20.dp) // Aumenté ligeramente el tamaño del ícono
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Pegar
                FilledTonalButton(
                    onClick = {
                        val clipboardText = clipboardManager.getText()?.text
                        if (!clipboardText.isNullOrEmpty()) {
                            onPaste()
                            onSave(clipboardText)
                            isSaveEnabled = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Pegar",
                        modifier = Modifier.size(20.dp) // Aumenté ligeramente el tamaño del ícono
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Limpiar
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        modifier = Modifier.size(20.dp) // Aumenté ligeramente el tamaño del ícono
                    )
                }
            }
        }
    }
}


// --- TransmissionField Composable --- //
@Composable
fun TransmissionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isReadRemotely: Boolean,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit, // Función para limpiar el campo
    onSave: (String) -> Unit // Función para guardar en Firebase
) {
    val clipboardManager = LocalClipboardManager.current // Administrador del portapapeles
    var isSaveEnabled by remember { mutableStateOf(false) } // Controlar si el botón Guardar está habilitado

    Column(modifier = Modifier.padding(16.dp)) {
        // Etiqueta del campo
        Text(text = label, style = MaterialTheme.typography.bodyLarge)

        // Caja de texto que permite modificar el valor
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                isSaveEnabled = true // Habilitar el botón "Guardar" cuando se modifica el texto
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fila de botones
        Row(verticalAlignment = Alignment.CenterVertically) {

            //Usar solo un IconButton
            //IconButton(onClick = onCopy) {
            //    Icon(
            //        imageVector = Icons.Default.ContentCopy,  // Ícono de copiar
            //        contentDescription = "Copiar",
            //        modifier = Modifier.size(24.dp)  // Tamaño adecuado del ícono
            //    )
            //}

            TextButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,  // Ícono de copiar
                    contentDescription = "Copiar",
                    modifier = Modifier.size(24.dp)  // Tamaño del ícono
                )
            }

            TextButton(onClick = {
                val clipboardText = clipboardManager.getText()?.text // Obtener texto del portapapeles
                if (!clipboardText.isNullOrEmpty()) {
                    onPaste()  // Pegar el texto en la caja de texto
                    onSave(clipboardText)  // Guardar el texto en Firebase
                    isSaveEnabled = false // Deshabilitar el botón "Guardar" después de pegar y guardar
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,  // Ícono de pegar
                    contentDescription = "Pegar",
                    modifier = Modifier.size(24.dp)  // Ajusta el tamaño del ícono
                )
            }

            TextButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Clear,  // Ícono de limpiar (X)
                    contentDescription = "Limpiar",
                    modifier = Modifier.size(24.dp)  // Ajusta el tamaño del ícono
                )
            }

            TextButton(
                onClick = { onSave(value); isSaveEnabled = false }, // Guardar el valor y deshabilitar el botón
                enabled = isSaveEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Save,  // Ícono de guardar
                    contentDescription = "Guardar",
                    modifier = Modifier.size(24.dp)  // Tamaño del ícono
                )
            }
            // Ícono que indica si ha sido leído remotamente
            Image(
                painter = if (isReadRemotely) painterResource(id = R.drawable.icon_read)
                else painterResource(id = R.drawable.icon_unread),
                contentDescription = "Leído remotamente",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun SimpleOrientationControl() {
    val context = LocalContext.current
    val orientationHelper = remember { SimpleOrientationHelper(context) }
    var isActive by remember { mutableStateOf(orientationHelper.isOrientationControlActive()) }
    var isExpanded by remember { mutableStateOf(false) } // Estado para controlar expansión

    // Card principal con color dinámico según estado
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer  // Azul cuando activo
            else
                MaterialTheme.colorScheme.surface           // Gris cuando inactivo
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FILA PRINCIPAL COMPACTA: Ícono + Estado + Switch + Botón Expandir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna izquierda: Ícono + Estado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Ícono con color dinámico
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Control de Orientación",
                        modifier = Modifier.size(28.dp),
                        tint = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Información compacta
                    Column {
                        Text(
                            text = "🔄 Facebook Horizontal",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )

                        // Estado compacto con indicador
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isActive) Color.Green else Color.Gray,
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (isActive) "ACTIVO" else "Inactivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isActive)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }

                // Columna derecha: Switch + Botón Expandir
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch principal
                    Switch(
                        checked = isActive,
                        onCheckedChange = { checked ->
                            orientationHelper.setOrientationControl(checked)
                            isActive = checked

                            if (context is Activity) {
                                orientationHelper.applyToActivity(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón para expandir/colapsar instrucciones
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Ocultar detalles" else "Ver detalles",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // SECCIÓN EXPANDIBLE: Instrucciones detalladas (solo cuando isExpanded = true)
            if (isExpanded && isActive) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MODO BYPASS FACEBOOK ACTIVO",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📱 PASOS PARA TRANSMITIR:\n" +
                                    "1. Gira tu teléfono → horizontal\n" +
                                    "2. Abre Facebook → Live\n" +
                                    "3. Facebook detecta 'portrait' → ✅ permite transmisión\n" +
                                    "4. ¡Transmites en horizontal sin restricciones!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón de acceso rápido a Facebook
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = context.packageManager
                                        .getLaunchIntentForPackage("com.facebook.katana")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback: abrir en navegador o Play Store
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Abrir Facebook",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📱 Abrir Facebook Live")
                        }
                    }
                }
            }

            // INFORMACIÓN MÍNIMA cuando está expandido pero inactivo
            else if (isExpanded && !isActive) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💡 Activa el switch para transmitir horizontal en Facebook",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

