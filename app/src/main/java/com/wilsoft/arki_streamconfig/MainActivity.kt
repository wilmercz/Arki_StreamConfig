package com.wilsoft.arki_streamconfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.wilsoft.arki_streamconfig.ui.theme.Arki_StreamConfigTheme
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.wilsoft.arki_streamconfig.utilidades.SimpleOrientationHelper

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth  // Instancia de FirebaseAuth
    private val firebaseRepository = FirebaseRepository() // Crear una instancia de FirebaseRepository
    private lateinit var orientationHelper: SimpleOrientationHelper  // NUEVO: Helper de orientación

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Autenticación anónima
        signInAnonymously()

        setContent {
            // Establecer el tema y la navegación
            Arki_StreamConfigTheme {
                val navController = rememberNavController()  // Controlador de navegación
                NavHostSetup(navController = navController, firebaseRepository = firebaseRepository) // Configuración del NavHost
            }
        }
    }

    // Función para autenticar anónimamente con Firebase
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autenticación exitosa
                    Log.d("FirebaseAuth", "Autenticación anónima exitosa")
                } else {
                    // Error en la autenticación
                    Log.e("FirebaseAuth", "Error en la autenticación anónima", task.exception)
                }
            }
    }
}
