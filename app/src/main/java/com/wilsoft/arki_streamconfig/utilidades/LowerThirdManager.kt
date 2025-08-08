// Archivo: utils/LowerThirdManager.kt
package com.wilsoft.arki_streamconfig.utilidades

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.wilsoft.arki_streamconfig.FirebaseRepositoryExtensions
import com.wilsoft.arki_streamconfig.models.*
import com.wilsoft.arki_streamconfig.models.Invitado

/**
 * Manager principal para el sistema de Lower Thirds
 * Centraliza toda la lógica de gestión y coordinación
 */
class LowerThirdManager(
    private val firebaseExtensions: FirebaseRepositoryExtensions,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _currentConfig = MutableStateFlow(PresetTemplates.getPresetEstandar())
    val currentConfig: StateFlow<LowerThirdConfig> = _currentConfig.asStateFlow()

    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    private val _validationResult = MutableStateFlow<ValidationResult?>(null)
    val validationResult: StateFlow<ValidationResult?> = _validationResult.asStateFlow()

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    private var configHistory = mutableListOf<LowerThirdConfig>()
    private var currentHistoryIndex = -1

    init {
        // Configurar observadores
        setupObservers()

        // Cargar configuración inicial
        loadInitialConfiguration()
    }

    private fun setupObservers() {
        // Observar cambios de configuración para validación automática
        scope.launch {
            currentConfig
                .debounce(500) // Evitar validaciones excesivas
                .collect { config ->
                    validateConfiguration(config)
                    generateRecommendations(config)
                }
        }

        // Observar cambios en tiempo real desde Firebase
        scope.launch {
            firebaseExtensions.observeLowerThirdConfig()
                .catch { e ->
                    println("Error observing Firebase config: ${e.message}")
                }
                .collect { config ->
                    _currentConfig.value = config
                }
        }
    }

    private fun loadInitialConfiguration() {
        scope.launch {
            firebaseExtensions.loadLowerThirdConfig(
                onSuccess = { config ->
                    _currentConfig.value = config
                    addToHistory(config)
                },
                onFailure = { error ->
                    println("Error loading initial config: ${error.message}")
                    // Usar configuración por defecto
                    val defaultConfig = PresetTemplates.getPresetEstandar()
                    _currentConfig.value = defaultConfig
                    addToHistory(defaultConfig)
                }
            )
        }
    }

    /**
     * Actualizar configuración
     */
    fun updateConfiguration(config: LowerThirdConfig) {
        val optimizedConfig = LowerThirdUtils.optimizeConfiguration(config)
        _currentConfig.value = optimizedConfig
        addToHistory(optimizedConfig)

        // Guardar en Firebase
        scope.launch {
            firebaseExtensions.saveLowerThirdConfig(
                config = optimizedConfig,
                onSuccess = {
                    println("Configuration saved successfully")
                },
                onFailure = { error ->
                    println("Error saving configuration: ${error.message}")
                }
            )
        }
    }

    /**
     * Cambiar preset
     */
    fun switchPreset(presetName: String, onComplete: (Boolean) -> Unit = {}) {
        scope.launch {
            firebaseExtensions.switchPreset(
                presetName = presetName,
                onSuccess = {
                    onComplete(true)
                },
                onFailure = { error ->
                    println("Error switching preset: ${error.message}")
                    onComplete(false)
                }
            )
        }
    }

    /**
     * Controlar visibilidad
     */
    fun setVisibility(visible: Boolean, onComplete: (Boolean) -> Unit = {}) {
        _isVisible.value = visible

        scope.launch {
            firebaseExtensions.setLowerThirdVisibility(
                isVisible = visible,
                onSuccess = {
                    // Log del evento
                    firebaseExtensions.logUsageEvent(
                        action = if (visible) "show" else "hide",
                        presetUsed = _currentConfig.value.presets.actual
                    )
                    onComplete(true)
                },
                onFailure = { error ->
                    println("Error setting visibility: ${error.message}")
                    _isVisible.value = !visible // Revertir estado
                    onComplete(false)
                }
            )
        }
    }

    /**
     * Actualización rápida de contenido
     */
    fun updateTextContent(
        textoPrincipal: String? = null,
        textoSecundario: String? = null,
        tema: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        scope.launch {
            firebaseExtensions.updateTextContent(
                textoPrincipal = textoPrincipal,
                textoSecundario = textoSecundario,
                tema = tema,
                onSuccess = {
                    // Actualizar configuración local
                    val updatedConfig = _currentConfig.value.copy(
                        textoPrincipal = textoPrincipal?.let {
                            _currentConfig.value.textoPrincipal.copy(contenido = it, mostrar = true)
                        } ?: _currentConfig.value.textoPrincipal,

                        textoSecundario = textoSecundario?.let {
                            _currentConfig.value.textoSecundario.copy(contenido = it, mostrar = true)
                        } ?: _currentConfig.value.textoSecundario,

                        tema = tema?.let {
                            _currentConfig.value.tema.copy(contenido = it, mostrar = true)
                        } ?: _currentConfig.value.tema
                    )

                    _currentConfig.value = updatedConfig
                    addToHistory(updatedConfig)
                    onComplete(true)
                },
                onFailure = { error ->
                    println("Error updating text content: ${error.message}")
                    onComplete(false)
                }
            )
        }
    }

    /**
     * Aplicar invitado seleccionado
     */
    fun applyInvitado(invitado: Invitado) {
        updateTextContent(
            textoPrincipal = invitado.nombre,
            textoSecundario = invitado.rol,
            tema = if (invitado.tema.isNotEmpty()) invitado.tema else null
        )
    }

    /**
     * Sistema de historial
     */
    private fun addToHistory(config: LowerThirdConfig) {
        // Remover configuraciones futuras si estamos en el medio del historial
        if (currentHistoryIndex < configHistory.size - 1) {
            configHistory = configHistory.subList(0, currentHistoryIndex + 1).toMutableList()
        }

        configHistory.add(config)
        currentHistoryIndex = configHistory.size - 1

        // Limitar historial a 50 configuraciones
        if (configHistory.size > 50) {
            configHistory.removeAt(0)
            currentHistoryIndex--
        }
    }

    fun canUndo(): Boolean = currentHistoryIndex > 0

    fun canRedo(): Boolean = currentHistoryIndex < configHistory.size - 1

    fun undo(): Boolean {
        if (!canUndo()) return false

        currentHistoryIndex--
        _currentConfig.value = configHistory[currentHistoryIndex]
        return true
    }

    fun redo(): Boolean {
        if (!canRedo()) return false

        currentHistoryIndex++
        _currentConfig.value = configHistory[currentHistoryIndex]
        return true
    }

    /**
     * Validación y recomendaciones
     */
    private fun validateConfiguration(config: LowerThirdConfig) {
        val result = LowerThirdUtils.validateConfiguration(config)
        _validationResult.value = result
    }

    private fun generateRecommendations(config: LowerThirdConfig) {
        val recommendations = LowerThirdUtils.generateRecommendations(config)
        _recommendations.value = recommendations
    }

    /**
     * Backup y restauración
     */
    fun createBackup(backupName: String, onComplete: (Boolean) -> Unit) {
        scope.launch {
            firebaseExtensions.backupConfiguration(
                config = _currentConfig.value,
                backupName = backupName,
                onSuccess = {
                    onComplete(true)
                },
                onFailure = { error ->
                    println("Error creating backup: ${error.message}")
                    onComplete(false)
                }
            )
        }
    }

    fun restoreFromBackup(backupName: String, onComplete: (Boolean, LowerThirdConfig?) -> Unit) {
        scope.launch {
            firebaseExtensions.restoreFromBackup(
                backupName = backupName,
                onSuccess = { config ->
                    _currentConfig.value = config
                    addToHistory(config)
                    onComplete(true, config)
                },
                onFailure = { error ->
                    println("Error restoring backup: ${error.message}")
                    onComplete(false, null)
                }
            )
        }
    }

    /**
     * Exportación
     */
    fun exportToOBS(): String {
        return LowerThirdUtils.exportToOBS(_currentConfig.value)
    }

    fun exportToCSS(): String {
        return LowerThirdUtils.exportToCSS(_currentConfig.value)
    }

    /**
     * Estadísticas
     */
    fun getUsageStats(onResult: (Map<String, Any>) -> Unit) {
        scope.launch {
            firebaseExtensions.getLowerThirdStats(
                onSuccess = onResult,
                onFailure = { error ->
                    println("Error getting stats: ${error.message}")
                    onResult(emptyMap())
                }
            )
        }
    }

    /**
     * Limpiar recursos
     */
    fun cleanup() {
        // Limpiar cualquier recurso o listener activo
        configHistory.clear()
        currentHistoryIndex = -1
    }
}