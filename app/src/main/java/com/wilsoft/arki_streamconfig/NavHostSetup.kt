package com.wilsoft.arki_streamconfig

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wilsoft.arki_streamconfig.PantallaPrincipal
import com.wilsoft.arki_streamconfig.PantallaPerfiles
import com.wilsoft.arki_streamconfig.PantallaGeneradorCaracteres
import com.wilsoft.arki_streamconfig.PantallaGeneradorCaracteresAvanzada
import com.wilsoft.arki_streamconfig.PantallaSwitcherVideo
import com.wilsoft.arki_streamconfig.PantallaAgendaInvitados

@Composable
fun NavHostSetup(navController: NavHostController, firebaseRepository: FirebaseRepository) {
    NavHost(navController = navController, startDestination = NavigationRoutes.MAIN_SCREEN) {
        composable(NavigationRoutes.MAIN_SCREEN) {
            PantallaPrincipal(firebaseRepository = firebaseRepository, navController = navController)
        }
        composable(NavigationRoutes.PERFILES_SCREEN) {
            PantallaPerfiles(firebaseRepository = firebaseRepository, navController = navController)
        }
        // ✅ CAMBIO: Usar la nueva pantalla avanzada
        composable(NavigationRoutes.GENERADOR_CARACTERES_SCREEN) {
            PantallaGeneradorCaracteres(firebaseRepository = firebaseRepository, navController = navController)
        }
        // ✅ CAMBIO: Usar la nueva pantalla avanzada
        composable(NavigationRoutes.GENERADOR_CARACTERES_SCREEN) {
            PantallaGeneradorCaracteres(firebaseRepository = firebaseRepository, navController = navController)
        }

        // ✅ CAMBIO: Usar la nueva pantalla avanzada
        composable(NavigationRoutes.GENERADOR_CARACTERES_AVANZADO_SCREEN) {
            PantallaGeneradorCaracteresAvanzada(firebaseRepository = firebaseRepository, navController = navController)
        }

        composable(NavigationRoutes.SWITCHER_VIDEO_SCREEN) {
            PantallaSwitcherVideo(navController = navController)
        }
        composable(NavigationRoutes.AGENDA_INVITADOS_SCREEN) {
            PantallaAgendaInvitados(navController = navController, firebaseRepository = firebaseRepository)
        }

        //(NavigationRoutes.PUBLICIDAD_SCREEN) {
        //    PantallaPublicidad(navController = navController, firebaseRepository = firebaseRepository)
        //}
        composable(NavigationRoutes.PUBLICIDAD_SCREEN) {
            PantallaPublicidad(firebaseRepository = firebaseRepository)  // Elimina el parámetro navController
        }

        composable(NavigationRoutes.LISTA_PERFILES_SCREEN) {
            PantallaListaPerfiles(navController = navController, firebaseRepository = firebaseRepository)
        }
        composable("${NavigationRoutes.EDITAR_PERFIL_SCREEN}/{profileName}") { backStackEntry ->
            val profileName = backStackEntry.arguments?.getString("profileName")
            PantallaEditarPerfil(navController = navController, firebaseRepository = firebaseRepository, profileName = profileName)
        }


    }
}
