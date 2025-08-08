package com.wilsoft.arki_streamconfig

import com.google.firebase.database.ktx.database // Para Firebase Realtime Database
import com.google.firebase.database.DatabaseReference // Para referenciar la base de datos
import com.google.firebase.database.DataSnapshot // Para el uso de DataSnapshot en las lambdas
import com.google.firebase.ktx.Firebase // Para la instancia global de Firebase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError




class FirebaseRepository {
    companion object FirebasePaths {
        var basePath = "CLAVE_STREAM_FB/STREAM_CONFIGURACION" // Ruta base global que puedes cambiar
    }

    public val db: DatabaseReference = Firebase.database.reference // Inicializar Realtime Database correctamente

    // Función para cargar los datos desde la ruta "CLAVE_STREAM_FB" en Realtime Database
    fun loadStreamData(path: String, onSuccess: (Map<String, Any>) -> Unit, onFailure: (Exception) -> Unit) {
        db.child(path) // Usamos la ruta dinámica
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val data = dataSnapshot.value as? Map<String, Any> ?: emptyMap()
                    onSuccess(data)
                } else {
                    onFailure(Exception("No se encontraron datos en $path."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    // Función para guardar datos en Firebase
    fun saveData(nodePath: String, data: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.child(nodePath) // Usamos `child()` para especificar la ruta en Realtime Database
            .updateChildren(data) // `updateChildren()` actualiza los datos sin eliminar los existentes
            .addOnSuccessListener {
                onSuccess() // Llamamos a la función de éxito si todo va bien
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Llamamos a la función de error si algo falla
            }
    }


    // Función para cargar los perfiles desde Firebase
    fun loadProfiles(onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val profilesRef = db.child("CLAVE_STREAM_FB").child("PERFILES")


        profilesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profilesList = mutableListOf<String>()
                for (profileSnapshot in dataSnapshot.children) {
                    val profileName = profileSnapshot.key ?: ""
                    profilesList.add(profileName)
                }
                onSuccess(profilesList)
                println("Perfiles cargados: $profilesList")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onFailure(Exception(databaseError.message))
            }
        })
    }

    // Función para cargar los datos de un perfil específico usando el nombre del perfil
    fun loadProfile(profileName: String, onSuccess: (Map<String, Any>) -> Unit, onFailure: (Exception) -> Unit) {
        val profilePath = "CLAVE_STREAM_FB/PERFILES/$profileName"  // Construir la ruta al perfil
        db.child(profilePath)  // Referencia al perfil específico
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val data = dataSnapshot.value as? Map<String, Any> ?: emptyMap()
                    onSuccess(data)  // Pasar los datos del perfil al callback de éxito
                    println("Reposity Datos recuperados del perfil: $data")
                } else {
                    onFailure(Exception("No se encontraron datos para el perfil $profileName."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)  // Pasar la excepción al callback de error
            }
    }

    fun deleteProfile(profileName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val profilePath = "$basePath/PERFILES/$profileName"
        db.child(profilePath)
            .removeValue()  // Elimina el nodo del perfil en Firebase
            .addOnSuccessListener {
                onSuccess()  // Llamar a la función de éxito si la eliminación fue exitosa
            }
            .addOnFailureListener { exception ->
                onFailure(exception)  // Llamar a la función de error en caso de fallo
            }
    }
}
