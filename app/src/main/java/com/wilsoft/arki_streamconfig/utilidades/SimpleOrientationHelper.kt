package com.wilsoft.arki_streamconfig.utilidades


import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.SharedPreferences

/**
 * Helper para controlar orientación y engañar a Facebook Live
 *
 * FUNCIONAMIENTO:
 * - Fuerza orientación SENSOR_LANDSCAPE en la Activity
 * - Facebook detecta "landscape" pero no bloquea porque el sistema
 *   mantiene ciertos flags que le hacen pensar que seguimos en portrait
 * - Persiste el estado entre sesiones de la app
 */
class SimpleOrientationHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "orientation_control_prefs"
        private const val KEY_ORIENTATION_ACTIVE = "orientation_control_active"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Verifica si el control de orientación está activo
     * @return Boolean - true si está activo, false si está inactivo
     */
    fun isOrientationControlActive(): Boolean {
        return prefs.getBoolean(KEY_ORIENTATION_ACTIVE, false)
    }

    /**
     * Activa o desactiva el control de orientación
     * @param active Boolean - true para activar, false para desactivar
     */
    fun setOrientationControl(active: Boolean) {
        // Guardar estado en SharedPreferences
        prefs.edit()
            .putBoolean(KEY_ORIENTATION_ACTIVE, active)
            .apply()

        // Aplicar orientación inmediatamente
        if (active) {
            enableFacebookBypassMode()
        } else {
            enableNormalRotation()
        }
    }

    /**
     * Modo bypass para Facebook:
     * Fuerza landscape pero mantiene compatibilidad con Facebook
     */
    private fun enableFacebookBypassMode() {
        if (context is Activity) {
            // CLAVE: SENSOR_LANDSCAPE permite rotación pero Facebook no lo detecta como "bloqueado"
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    /**
     * Restaura rotación automática normal
     */
    private fun enableNormalRotation() {
        if (context is Activity) {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    /**
     * Aplica la configuración guardada a una Activity
     * Llamar desde onCreate() de Activities
     * @param activity Activity donde aplicar la configuración
     */
    fun applyToActivity(activity: Activity) {
        if (isOrientationControlActive()) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }
}