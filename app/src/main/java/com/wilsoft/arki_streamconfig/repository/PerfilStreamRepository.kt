// Archivo: repository/PerfilStreamRepository.kt
package com.wilsoft.arki_streamconfig.repository

import com.wilsoft.arki_streamconfig.FirebaseRepository
import com.wilsoft.arki_streamconfig.models.*

/**
 * Repository específico para manejar perfiles de stream avanzados
 * Mantiene compatibilidad total con el sistema actual
 */
class PerfilStreamRepository(private val firebaseRepository: FirebaseRepository) {

    companion object {
        const val PERFILES_BASICOS_PATH = "CLAVE_STREAM_FB/PERFILES"
        const val PERFILES_AVANZADOS_PATH = "CLAVE_STREAM_FB/PERFILES_STREAM_AVANZADOS"
    }

    /**
     * Guardar perfil avanzado manteniendo compatibilidad con sistema actual
     */
    fun guardarPerfilAvanzado(
        config: PerfilStreamConfig,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val datosCompletos = PerfilStreamConfigUtils.toFirebaseFormat(config)

        // Guardar en formato actual para compatibilidad
        val datosBasicos = mapOf(
            "NombrePerfil" to config.nombrePerfil,
            "colorFondo1" to config.sistemaColores.colorFondo1,
            "colorFondo2" to config.sistemaColores.colorFondo2,
            "colorFondo3" to config.sistemaColores.colorFondo3,
            "colorLetra1" to config.sistemaColores.colorLetra1,
            "colorLetra2" to config.sistemaColores.colorLetra2,
            "colorLetra3" to config.sistemaColores.colorLetra3,
            "urlLogo" to config.lowerThirdConfig.logo.simple.url,
            "urlImagenPublicidad" to config.lowerThirdConfig.publicidad.url,
            "Invitado" to config.invitadoConfig.nombreCompleto
        )

        // Guardar en ambos formatos
        firebaseRepository.saveData(
            nodePath = "$PERFILES_BASICOS_PATH/${config.nombrePerfil}",
            data = datosBasicos,
            onSuccess = {
                // Si el básico se guarda correctamente, guardar el avanzado
                firebaseRepository.saveData(
                    nodePath = "$PERFILES_AVANZADOS_PATH/${config.nombrePerfil}",
                    data = datosCompletos,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    /**
     * Cargar perfil (primero intenta avanzado, luego básico)
     */
    fun cargarPerfil(
        nombrePerfil: String,
        onSuccess: (PerfilStreamConfig) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Intentar cargar configuración avanzada primero
        firebaseRepository.loadStreamData(
            path = "$PERFILES_AVANZADOS_PATH/$nombrePerfil",
            onSuccess = { data ->
                try {
                    val config = convertirFirebaseAPerfilAvanzado(data)
                    onSuccess(config)
                } catch (e: Exception) {
                    // Si falla, intentar cargar configuración básica
                    cargarPerfilBasicoYMigrar(nombrePerfil, onSuccess, onFailure)
                }
            },
            onFailure = {
                // Si no existe avanzado, intentar cargar básico
                cargarPerfilBasicoYMigrar(nombrePerfil, onSuccess, onFailure)
            }
        )
    }

    /**
     * Cargar perfil básico y convertir a avanzado automáticamente
     */
    private fun cargarPerfilBasicoYMigrar(
        nombrePerfil: String,
        onSuccess: (PerfilStreamConfig) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firebaseRepository.loadStreamData(
            path = "$PERFILES_BASICOS_PATH/$nombrePerfil",
            onSuccess = { data ->
                try {
                    val config = migrarPerfilBasicoAAvanzado(data)
                    onSuccess(config)
                } catch (e: Exception) {
                    onFailure(e)
                }
            },
            onFailure = onFailure
        )
    }

    /**
     * Migrar perfil básico a avanzado
     */
    private fun migrarPerfilBasicoAAvanzado(data: Map<String, Any>): PerfilStreamConfig {
        return PerfilStreamConfigUtils.migrarDesdeConfiguracionBasica(
            nombrePerfil = data["NombrePerfil"] as? String ?: "",
            colorFondo1 = data["colorFondo1"] as? String ?: "#1066FF",
            colorFondo2 = data["colorFondo2"] as? String ?: "#043884",
            colorFondo3 = data["colorFondo3"] as? String ?: "#F08313",
            colorLetra1 = data["colorLetra1"] as? String ?: "#FFFFFF",
            colorLetra2 = data["colorLetra2"] as? String ?: "#FFFFFF",
            colorLetra3 = data["colorLetra3"] as? String ?: "#FFFFFF",
            urlLogo = data["urlLogo"] as? String ?: "",
            urlImagenPublicidad = data["urlImagenPublicidad"] as? String ?: "",
            invitado = data["Invitado"] as? String ?: ""
        )
    }

    /**
     * Convertir datos de Firebase a PerfilStreamConfig
     */
    private fun convertirFirebaseAPerfilAvanzado(data: Map<String, Any>): PerfilStreamConfig {
        val configAvanzada = data["configuracionAvanzada"] as? Map<String, Any> ?: emptyMap()
        val coloresAvanzados = configAvanzada["sistemaColoresAvanzado"] as? Map<String, Any> ?: emptyMap()
        val posicionamiento = configAvanzada["posicionamiento"] as? Map<String, Any> ?: emptyMap()
        val invitadoAvanzado = configAvanzada["invitadoAvanzado"] as? Map<String, Any> ?: emptyMap()
        val contenidoDinamico = configAvanzada["contenidoDinamico"] as? Map<String, Any> ?: emptyMap()
        val webConfig = configAvanzada["webRenderConfig"] as? Map<String, Any> ?: emptyMap()

        return PerfilStreamConfig(
            nombrePerfil = data["NombrePerfil"] as? String ?: "",
            descripcion = configAvanzada["descripcion"] as? String ?: "",
            categoria = try {
                CategoriaStream.valueOf(configAvanzada["categoria"] as? String ?: "NOTICIAS")
            } catch (e: Exception) {
                CategoriaStream.NOTICIAS
            },
            fechaCreacion = configAvanzada["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
            version = configAvanzada["version"] as? String ?: "2.0",

            // Sistema de colores (mantiene compatibilidad)
            sistemaColores = SistemaColoresAvanzado(
                colorFondo1 = data["colorFondo1"] as? String ?: "#1066FF",
                colorFondo2 = data["colorFondo2"] as? String ?: "#043884",
                colorFondo3 = data["colorFondo3"] as? String ?: "#F08313",
                colorLetra1 = data["colorLetra1"] as? String ?: "#FFFFFF",
                colorLetra2 = data["colorLetra2"] as? String ?: "#FFFFFF",
                colorLetra3 = data["colorLetra3"] as? String ?: "#FFFFFF",
                colorAcento = coloresAvanzados["colorAcento"] as? String ?: "#FF6B35",
                colorExito = coloresAvanzados["colorExito"] as? String ?: "#4CAF50",
                colorAdvertencia = coloresAvanzados["colorAdvertencia"] as? String ?: "#FF9800",
                colorError = coloresAvanzados["colorError"] as? String ?: "#F44336",
                gradientePrincipal = (coloresAvanzados["gradientePrincipal"] as? List<String>) ?: listOf("#1066FF", "#043884"),
                gradienteSecundario = (coloresAvanzados["gradienteSecundario"] as? List<String>) ?: listOf("#F08313", "#FF6B35")
            ),

            // Configuración de Lower Third (usando estructura existente)
            lowerThirdConfig = LowerThirdConfig(
                logo = LogoConfig(
                    simple = LogoSimpleConfig(
                        url = data["urlLogo"] as? String ?: ""
                    )
                ),
                publicidad = PublicidadConfig(
                    url = data["urlImagenPublicidad"] as? String ?: ""
                )
            ),

            // Posicionamiento
            posicionamiento = ConfiguracionPosicionamiento(
                logoPosition = try {
                    ElementPosition.valueOf(posicionamiento["logoPosition"] as? String ?: "BOTTOM_LEFT")
                } catch (e: Exception) {
                    ElementPosition.BOTTOM_LEFT
                },
                canvasResolution = try {
                    CanvasResolution.valueOf(posicionamiento["canvasResolution"] as? String ?: "HD_1080P")
                } catch (e: Exception) {
                    CanvasResolution.HD_1080P
                },
                safeMargins = posicionamiento["safeMargins"] as? Boolean ?: true
            ),

            // Configuración del invitado
            invitadoConfig = InvitadoConfigAvanzado(
                invitadoBasico = Invitado(
                    nombre = data["Invitado"] as? String ?: "",
                    rol = "",
                    tema = ""
                ),
                nombreCompleto = invitadoAvanzado["nombreCompleto"] as? String ?: (data["Invitado"] as? String ?: ""),
                titulo = invitadoAvanzado["titulo"] as? String ?: "",
                organizacion = invitadoAvanzado["organizacion"] as? String ?: "",
                biografia = invitadoAvanzado["biografia"] as? String ?: "",
                redesSociales = (invitadoAvanzado["redesSociales"] as? Map<String, String>)?.mapKeys { entry ->
                    try {
                        RedSocial.valueOf(entry.key)
                    } catch (e: Exception) {
                        RedSocial.FACEBOOK // Default fallback
                    }
                }?.filterKeys { it != null } ?: emptyMap()
            ),

            // Contenido dinámico
            contenidoDinamico = ContenidoDinamicoConfig(
                mostrarFechaHora = contenidoDinamico["mostrarFechaHora"] as? Boolean ?: true,
                formatoFecha = try {
                    FormatoFecha.valueOf(contenidoDinamico["formatoFecha"] as? String ?: "DD_MM_YYYY")
                } catch (e: Exception) {
                    FormatoFecha.DD_MM_YYYY
                },
                formatoHora = try {
                    FormatoHora.valueOf(contenidoDinamico["formatoHora"] as? String ?: "HH_MM_24H")
                } catch (e: Exception) {
                    FormatoHora.HH_MM_24H
                },
                ciudadActual = contenidoDinamico["ciudadActual"] as? String ?: "Nueva Loja",
                provinciaActual = contenidoDinamico["provinciaActual"] as? String ?: "Sucumbíos",
                estadoStream = try {
                    EstadoStream.valueOf(contenidoDinamico["estadoStream"] as? String ?: "EN_VIVO")
                } catch (e: Exception) {
                    EstadoStream.EN_VIVO
                }
            ),

            // Configuración web
            webRenderConfig = WebRenderConfig(
                habilitado = webConfig["habilitado"] as? Boolean ?: true,
                actualizacionTiempoReal = webConfig["actualizacionTiempoReal"] as? Boolean ?: true,
                compressionLevel = try {
                    CompressionLevel.valueOf(webConfig["compressionLevel"] as? String ?: "MEDIUM")
                } catch (e: Exception) {
                    CompressionLevel.MEDIUM
                }
            )
        )
    }

    /**
     * Listar todos los perfiles (básicos y avanzados)
     */
    fun listarTodosLosPerfiles(
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firebaseRepository.loadProfiles(
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    /**
     * Generar configuración web para CameraFi App
     */
    fun generarConfiguracionWeb(
        nombrePerfil: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        cargarPerfil(
            nombrePerfil = nombrePerfil,
            onSuccess = { config ->
                val webConfig = generarJsonParaWeb(config)
                onSuccess(webConfig)
            },
            onFailure = onFailure
        )
    }

    /**
     * Generar JSON optimizado para CameraFi App
     */
    private fun generarJsonParaWeb(config: PerfilStreamConfig): String {
        return """
        {
            "streamConfig": {
                "profileName": "${config.nombrePerfil}",
                "category": "${config.categoria.displayName}",
                "version": "${config.version}",
                "timestamp": ${System.currentTimeMillis()},
                
                "colors": {
                    "primary": "${config.sistemaColores.colorFondo1}",
                    "secondary": "${config.sistemaColores.colorFondo2}",
                    "tertiary": "${config.sistemaColores.colorFondo3}",
                    "accent": "${config.sistemaColores.colorAcento}",
                    "text": {
                        "primary": "${config.sistemaColores.colorLetra1}",
                        "secondary": "${config.sistemaColores.colorLetra2}",
                        "tertiary": "${config.sistemaColores.colorLetra3}"
                    },
                    "gradients": {
                        "primary": ${config.sistemaColores.gradientePrincipal},
                        "secondary": ${config.sistemaColores.gradienteSecundario}
                    }
                },
                
                "layout": {
                    "canvas": {
                        "width": ${config.posicionamiento.canvasResolution.dimensions.width},
                        "height": ${config.posicionamiento.canvasResolution.dimensions.height}
                    },
                    "safeMargins": ${config.posicionamiento.safeMargins},
                    "positions": {
                        "logo": "${config.posicionamiento.logoPosition.name}",
                        "tema": "${config.posicionamiento.temaPosition.name}",
                        "ubicacion": "${config.posicionamiento.ubicacionPosition.name}",
                        "publicidad": "${config.posicionamiento.publicidadPosition.name}"
                    }
                },
                
                "elements": {
                    "logo": {
                        "url": "${config.lowerThirdConfig.logo.simple.url}",
                        "position": {
                            "x": ${config.posicionamiento.logoPosition.coordinates.x},
                            "y": ${config.posicionamiento.logoPosition.coordinates.y}
                        },
                        "size": {
                            "width": ${config.lowerThirdConfig.logo.simple.tamaño.width},
                            "height": ${config.lowerThirdConfig.logo.simple.tamaño.height}
                        }
                    },
                    "guest": {
                        "name": "${config.invitadoConfig.nombreCompleto}",
                        "title": "${config.invitadoConfig.titulo}",
                        "organization": "${config.invitadoConfig.organizacion}",
                        "photo": "${config.invitadoConfig.fotoUrl}",
                        "socialMedia": ${config.invitadoConfig.redesSociales.mapKeys { it.key.name }}
                    },
                    "dynamicContent": {
                        "showDateTime": ${config.contenidoDinamico.mostrarFechaHora},
                        "dateFormat": "${config.contenidoDinamico.formatoFecha.name}",
                        "timeFormat": "${config.contenidoDinamico.formatoHora.name}",
                        "location": {
                            "city": "${config.contenidoDinamico.ciudadActual}",
                            "province": "${config.contenidoDinamico.provinciaActual}",
                            "country": "${config.contenidoDinamico.paisActual}",
                            "type": "${config.contenidoDinamico.tipoUbicacion.displayName}"
                        },
                        "stream": {
                            "status": "${config.contenidoDinamico.estadoStream.displayName}",
                            "statusColor": "${config.contenidoDinamico.estadoStream.color}"
                        }
                    }
                },
                
                "rendering": {
                    "realTimeUpdates": ${config.webRenderConfig.actualizacionTiempoReal},
                    "caching": ${config.webRenderConfig.cachingEnabled},
                    "compression": "${config.webRenderConfig.compressionLevel.name}",
                    "mobileOptimized": ${config.webRenderConfig.optimizacionMobile},
                    "fallback": ${config.webRenderConfig.fallbackConfig}
                },
                
                "urls": {
                    "visualization": "${PerfilStreamConfigUtils.generarUrlVisualizacion(config.nombrePerfil)}",
                    "updates": "${config.webRenderConfig.urlBase}/updates/${config.nombrePerfil}",
                    "websocket": "${config.webRenderConfig.urlBase.replace("https://", "wss://")}/ws/${config.nombrePerfil}"
                }
            }
        }
        """.trimIndent()
    }

    /**
     * Eliminar perfil (tanto básico como avanzado)
     */
    fun eliminarPerfil(
        nombrePerfil: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Eliminar configuración básica
        firebaseRepository.saveData(
            nodePath = "$PERFILES_BASICOS_PATH/$nombrePerfil",
            data = emptyMap(),
            onSuccess = {
                // Eliminar configuración avanzada
                firebaseRepository.saveData(
                    nodePath = "$PERFILES_AVANZADOS_PATH/$nombrePerfil",
                    data = emptyMap(),
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    /**
     * Crear preset automático por categoría
     */
    fun crearPresetPorCategoria(
        categoria: CategoriaStream,
        nombrePersonalizado: String? = null,
        onSuccess: (PerfilStreamConfig) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val preset = PerfilStreamConfigUtils.generarPresetPorCategoria(categoria)
        val nombreFinal = nombrePersonalizado ?: preset.nombrePerfil

        val presetConNombre = preset.copy(nombrePerfil = nombreFinal)

        guardarPerfilAvanzado(
            config = presetConNombre,
            onSuccess = { onSuccess(presetConNombre) },
            onFailure = onFailure
        )
    }
}