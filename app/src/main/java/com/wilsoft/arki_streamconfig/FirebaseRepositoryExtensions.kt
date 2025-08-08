// Archivo: FirebaseRepositoryExtensions.kt
package com.wilsoft.arki_streamconfig

import com.wilsoft.arki_streamconfig.models.*
import com.wilsoft.arki_streamconfig.utilidades.FirebaseDataStructure
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Extensiones del FirebaseRepository para el sistema avanzado de Lower Thirds
 */
class FirebaseRepositoryExtensions(private val repository: FirebaseRepository) {

    companion object {
        const val LOWER_THIRDS_PATH = "CLAVE_STREAM_FB/STREAM_LIVE/GRAFICOS"
        const val PRESETS_PATH = "CLAVE_STREAM_FB/LOWER_THIRDS_PRESETS"
        const val INVITADOS_PATH = "CLAVE_STREAM_FB/INVITADOS"
        const val LIVE_CONTROLS_PATH = "CLAVE_STREAM_FB/STREAM_LIVE/CONTROLS"
    }

    /**
     * Cargar configuración completa de Lower Thirds
     */
    fun loadLowerThirdConfig(
        onSuccess: (LowerThirdConfig) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.loadStreamData(
            path = LOWER_THIRDS_PATH,
            onSuccess = { data ->
                try {
                    val config = FirebaseDataStructure.fromLowerThirdFirebaseFormat(data)
                    onSuccess(config)
                } catch (e: Exception) {
                    onFailure(e)
                }
            },
            onFailure = onFailure
        )
    }

    /**
     * Guardar configuración completa de Lower Thirds
     */
    fun saveLowerThirdConfig(
        config: LowerThirdConfig,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firebaseData = FirebaseDataStructure.toLowerThirdFirebaseFormat(config)
        repository.saveData(
            nodePath = LOWER_THIRDS_PATH,
            data = firebaseData,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Escuchar cambios en tiempo real de la configuración
     */
    fun observeLowerThirdConfig(): Flow<LowerThirdConfig> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val data = snapshot.value as? Map<String, Any> ?: emptyMap()
                    val config = FirebaseDataStructure.fromLowerThirdFirebaseFormat(data)
                    trySend(config)
                } catch (e: Exception) {
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        repository.db.child(LOWER_THIRDS_PATH).addValueEventListener(listener)

        awaitClose {
            repository.db.child(LOWER_THIRDS_PATH).removeEventListener(listener)
        }
    }

    /**
     * Guardar preset personalizado
     */
    fun saveCustomPreset(
        name: String,
        config: LowerThirdConfig,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val presetData = mapOf(
            "nombre" to name,
            "descripcion" to description,
            "configuracion" to FirebaseDataStructure.toLowerThirdFirebaseFormat(config),
            "fechaCreacion" to System.currentTimeMillis(),
            "version" to "2.0"
        )

        repository.saveData(
            nodePath = "$PRESETS_PATH/$name",
            data = presetData,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Cargar presets personalizados
     */
    fun loadCustomPresets(
        onSuccess: (Map<String, LowerThirdConfig>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.loadStreamData(
            path = PRESETS_PATH,
            onSuccess = { data ->
                try {
                    val presets = mutableMapOf<String, LowerThirdConfig>()
                    data.forEach { (name, presetData) ->
                        val preset = presetData as Map<String, Any>
                        val configData = preset["configuracion"] as? Map<String, Any> ?: emptyMap()
                        val config = FirebaseDataStructure.fromLowerThirdFirebaseFormat(configData)
                        presets[name] = config
                    }
                    onSuccess(presets)
                } catch (e: Exception) {
                    onFailure(e)
                }
            },
            onFailure = onFailure
        )
    }

    /**
     * Eliminar preset personalizado
     */
    fun deleteCustomPreset(
        name: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.db.child("$PRESETS_PATH/$name")
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Activar/Desactivar Lower Third en tiempo real
     */
    fun setLowerThirdVisibility(
        isVisible: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val controlData = mapOf(
            "lowerThirdActive" to isVisible,
            "timestamp" to System.currentTimeMillis(),
            "action" to if (isVisible) "show" else "hide"
        )

        repository.saveData(
            nodePath = LIVE_CONTROLS_PATH,
            data = controlData,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Actualizar solo el contenido de texto
     */
    fun updateTextContent(
        textoPrincipal: String? = null,
        textoSecundario: String? = null,
        tema: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mutableMapOf<String, Any>()

        textoPrincipal?.let {
            updates["TEXTO_PRINCIPAL/contenido"] = it
            updates["TEXTO_PRINCIPAL/mostrar"] = true
        }

        textoSecundario?.let {
            updates["TEXTO_SECUNDARIO/contenido"] = it
            updates["TEXTO_SECUNDARIO/mostrar"] = true
        }

        tema?.let {
            updates["TEMA/contenido"] = it
            updates["TEMA/mostrar"] = true
        }

        if (updates.isNotEmpty()) {
            repository.saveData(
                nodePath = LOWER_THIRDS_PATH,
                data = updates,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        } else {
            onSuccess()
        }
    }

    /**
     * Cambiar preset activo
     */
    fun switchPreset(
        presetName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val presetConfig = when (presetName) {
            "estandar" -> PresetTemplates.getPresetEstandar()
            "noticias" -> PresetTemplates.getPresetNoticias()
            "deportes" -> PresetTemplates.getPresetDeportes()
            "corporativo" -> PresetTemplates.getPresetCorporativo()
            else -> {
                // Cargar preset personalizado
                loadCustomPresets(
                    onSuccess = { customPresets ->
                        customPresets[presetName]?.let { config ->
                            saveLowerThirdConfig(config, onSuccess, onFailure)
                        } ?: onFailure(Exception("Preset no encontrado: $presetName"))
                    },
                    onFailure = onFailure
                )
                return
            }
        }

        val updatedConfig = presetConfig.copy(
            presets = presetConfig.presets.copy(actual = presetName)
        )

        saveLowerThirdConfig(updatedConfig, onSuccess, onFailure)
    }

    /**
     * Obtener estadísticas de uso
     */
    fun getLowerThirdStats(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.loadStreamData(
            path = "CLAVE_STREAM_FB/STATS/LOWER_THIRDS",
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Registrar evento de uso
     */
    fun logUsageEvent(
        action: String,
        presetUsed: String,
        duration: Long? = null
    ) {
        val eventData = mapOf(
            "action" to action,
            "preset" to presetUsed,
            "timestamp" to System.currentTimeMillis(),
            "duration" to (duration ?: 0)
        )

        val eventId = "event_${System.currentTimeMillis()}"
        repository.saveData(
            nodePath = "CLAVE_STREAM_FB/STATS/LOWER_THIRDS/EVENTS/$eventId",
            data = eventData,
            onSuccess = { /* Log silencioso */ },
            onFailure = { /* Log silencioso */ }
        )
    }

    /**
     * Backup de configuración
     */
    fun backupConfiguration(
        config: LowerThirdConfig,
        backupName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val backupData = mapOf(
            "name" to backupName,
            "timestamp" to System.currentTimeMillis(),
            "version" to "2.0",
            "configuration" to FirebaseDataStructure.toLowerThirdFirebaseFormat(config)
        )

        repository.saveData(
            nodePath = "CLAVE_STREAM_FB/BACKUPS/LOWER_THIRDS/$backupName",
            data = backupData,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Restaurar desde backup
     */
    fun restoreFromBackup(
        backupName: String,
        onSuccess: (LowerThirdConfig) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.loadStreamData(
            path = "CLAVE_STREAM_FB/BACKUPS/LOWER_THIRDS/$backupName",
            onSuccess = { backupData ->
                try {
                    val configData = backupData["configuration"] as? Map<String, Any> ?: emptyMap()
                    val config = FirebaseDataStructure.fromLowerThirdFirebaseFormat(configData)
                    onSuccess(config)
                } catch (e: Exception) {
                    onFailure(e)
                }
            },
            onFailure = onFailure
        )
    }
}