// Archivo: models/PerfilStreamConfig.kt
package com.wilsoft.arki_streamconfig.models

/**
 * Configuración avanzada para perfiles de streaming
 * Integra y extiende las configuraciones existentes de LowerThirdConfig
 */
data class PerfilStreamConfig(
    // INFORMACIÓN BÁSICA DEL PERFIL
    val nombrePerfil: String = "",
    val descripcion: String = "",
    val categoria: CategoriaStream = CategoriaStream.NOTICIAS,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val version: String = "2.0",
    val activo: Boolean = true,

    // INTEGRACIÓN CON SISTEMA EXISTENTE
    val lowerThirdConfig: LowerThirdConfig = LowerThirdConfig(),

    // CONFIGURACIÓN EXPANDIDA DE COLORES (extiende los 3 colores actuales)
    val sistemaColores: SistemaColoresAvanzado = SistemaColoresAvanzado(),

    // POSICIONAMIENTO AVANZADO DE ELEMENTOS
    val posicionamiento: ConfiguracionPosicionamiento = ConfiguracionPosicionamiento(),

    // CONFIGURACIÓN ESPECÍFICA DEL INVITADO EXPANDIDA
    val invitadoConfig: InvitadoConfigAvanzado = InvitadoConfigAvanzado(),

    // CONFIGURACIÓN DE CONTENIDO DINÁMICO
    val contenidoDinamico: ContenidoDinamicoConfig = ContenidoDinamicoConfig(),

    // CONFIGURACIÓN PARA LA WEB (CameraFi App)
    val webRenderConfig: WebRenderConfig = WebRenderConfig(),

    val animaciones: ConfigAnimaciones = ConfigAnimaciones(),
)

enum class CategoriaStream(val displayName: String, val colorTema: String) {
    NOTICIAS("Noticias", "#C41E3A"),
    DEPORTES("Deportes", "#16A34A"),
    ENTRETENIMIENTO("Entretenimiento", "#9333EA"),
    CORPORATIVO("Corporativo", "#1E40AF"),
    EDUCATIVO("Educativo", "#0891B2"),
    GAMING("Gaming", "#DC2626"),
    PODCAST("Podcast", "#EA580C"),
    PERSONALIZADO("Personalizado", "#6B7280")
}

/**
 * Sistema de colores expandido que mantiene compatibilidad con el sistema actual
 */
data class SistemaColoresAvanzado(
    // MANTIENE COMPATIBILIDAD CON SISTEMA ACTUAL (colorFondo1, colorFondo2, colorFondo3)
    val colorFondo1: String = "#1066FF", // Primario
    val colorFondo2: String = "#043884", // Secundario
    val colorFondo3: String = "#F08313", // Terciario
    val colorLetra1: String = "#FFFFFF", // Texto principal
    val colorLetra2: String = "#FFFFFF", // Texto secundario
    val colorLetra3: String = "#FFFFFF", // Texto terciario

    // NUEVOS COLORES AVANZADOS
    val colorAcento: String = "#FF6B35",
    val colorExito: String = "#4CAF50",
    val colorAdvertencia: String = "#FF9800",
    val colorError: String = "#F44336",
    val colorInfo: String = "#2196F3",

    val textoDescripcion: String = "#AAAAAA",

    // FONDOS ADICIONALES
    val fondoModal: String = "#2A2A2A",
    val fondoTransparente: String = "rgba(0,0,0,0.7)",

    // GRADIENTES
    val gradientePrincipal: List<String> = listOf("#1066FF", "#043884"),
    val gradienteSecundario: List<String> = listOf("#F08313", "#FF6B35"),

    // PRESETS RÁPIDOS POR CATEGORÍA
    val presetAutomatico: Boolean = true
) {
    /**
     * Generar colores automáticamente basados en la categoría
     */
    companion object {
        fun desdeCategoria(categoria: CategoriaStream): SistemaColoresAvanzado {
            return when (categoria) {
                CategoriaStream.NOTICIAS -> SistemaColoresAvanzado(
                    colorFondo1 = "#C41E3A", // Rojo noticias
                    colorFondo2 = "#1E3A8A", // Azul corporativo
                    colorFondo3 = "#F59E0B"  // Amarillo alerta
                )
                CategoriaStream.DEPORTES -> SistemaColoresAvanzado(
                    colorFondo1 = "#16A34A", // Verde
                    colorFondo2 = "#EAB308", // Amarillo
                    colorFondo3 = "#DC2626"  // Rojo
                )
                CategoriaStream.CORPORATIVO -> SistemaColoresAvanzado(
                    colorFondo1 = "#1E40AF", // Azul corporativo
                    colorFondo2 = "#6B7280", // Gris
                    colorFondo3 = "#059669"  // Verde elegante
                )
                else -> SistemaColoresAvanzado() // Colores por defecto
            }
        }
    }
}


data class ConfigAnimaciones(
    val entradaLogo: AnimationType = AnimationType.SLIDE_IN_LEFT,
    val duracionEntrada: Int = 500,
    val delayEntreElementos: Int = 200
)

/**
 * Configuración de posicionamiento que extiende Position existente
 */
data class ConfiguracionPosicionamiento(
    // POSICIONES PREDEFINIDAS PARA ELEMENTOS PRINCIPALES
    val logoPosition: ElementPosition = ElementPosition.BOTTOM_LEFT,
    val logoCustomPosition: Position? = null,

    val textoNombrePosition: ElementPosition = ElementPosition.BOTTOM_LEFT,
    val textoNombreCustomPosition: Position? = null,

    val textoRolPosition: ElementPosition = ElementPosition.BOTTOM_LEFT,
    val textoRolCustomPosition: Position? = null,

    val temaPosition: ElementPosition = ElementPosition.BOTTOM_CENTER,
    val temaCustomPosition: Position? = null,

    val ubicacionPosition: ElementPosition = ElementPosition.TOP_RIGHT,
    val ubicacionCustomPosition: Position? = null,

    val publicidadPosition: ElementPosition = ElementPosition.BOTTOM_RIGHT,
    val publicidadCustomPosition: Position? = null,

    // CONFIGURACIÓN DE CANVAS
    val canvasResolution: CanvasResolution = CanvasResolution.HD_1080P,
    val safeMargins: Boolean = true,
    val gridSnap: Boolean = false
)

enum class ElementPosition(val displayName: String, val coordinates: Position) {
    TOP_LEFT("Superior Izquierda", Position(50, 50)),
    TOP_CENTER("Superior Centro", Position(960, 50)),
    TOP_RIGHT("Superior Derecha", Position(1870, 50)),
    MIDDLE_LEFT("Centro Izquierda", Position(50, 540)),
    MIDDLE_CENTER("Centro", Position(960, 540)),
    MIDDLE_RIGHT("Centro Derecha", Position(1870, 540)),
    BOTTOM_LEFT("Inferior Izquierda", Position(50, 1030)),
    BOTTOM_CENTER("Inferior Centro", Position(960, 1030)),
    BOTTOM_RIGHT("Inferior Derecha", Position(1870, 1030)),
    CUSTOM("Posición Personalizada", Position(0, 0))
}

enum class CanvasResolution(val displayName: String, val dimensions: Dimensions) {
    HD_720P("720p - 1280x720", Dimensions(1280, 720)),
    HD_1080P("1080p - 1920x1080", Dimensions(1920, 1080)),
    UHD_4K("4K - 3840x2160", Dimensions(3840, 2160))
}

/**
 * Configuración expandida del invitado que extiende el modelo Invitado existente
 */
data class InvitadoConfigAvanzado(
    // MANTIENE COMPATIBILIDAD CON SISTEMA ACTUAL
    val invitadoBasico: Invitado = Invitado("", "", ""),

    // INFORMACIÓN EXPANDIDA
    val nombreCompleto: String = "",
    val titulo: String = "",
    val organizacion: String = "",
    val biografia: String = "",
    val fotoUrl: String = "",
    val ciudadOrigen: String = "",
    val paisOrigen: String = "",

    // REDES SOCIALES
    val redesSociales: Map<RedSocial, String> = emptyMap(),
    val mostrarRedesEnVivo: Boolean = false,

    // CONFIGURACIÓN DE VISUALIZACIÓN
    val mostrarFoto: Boolean = false,
    val posicionFoto: ElementPosition = ElementPosition.TOP_LEFT,
    val tamañoFoto: TamañoElemento = TamañoElemento.PEQUENO
)

enum class RedSocial(val displayName: String, val icon: String) {
    FACEBOOK("Facebook", "facebook"),
    TWITTER("Twitter/X", "twitter"),
    INSTAGRAM("Instagram", "instagram"),
    YOUTUBE("YouTube", "youtube"),
    TIKTOK("TikTok", "tiktok"),
    LINKEDIN("LinkedIn", "linkedin")
}

enum class TamañoElemento(val displayName: String, val pixels: Int) {
    EXTRA_PEQUENO("Extra Pequeño", 30),
    PEQUENO("Pequeño", 50),
    MEDIANO("Mediano", 70),
    GRANDE("Grande", 100),
    EXTRA_GRANDE("Extra Grande", 150)
}

/**
 * Configuración de contenido que se actualiza automáticamente
 */
data class ContenidoDinamicoConfig(
    // FECHA Y HORA
    val mostrarFechaHora: Boolean = true,
    val formatoFecha: FormatoFecha = FormatoFecha.DD_MM_YYYY,
    val formatoHora: FormatoHora = FormatoHora.HH_MM_24H,
    val zonaHoraria: String = "America/Guayaquil", // Ecuador
    val posicionFechaHora: ElementPosition = ElementPosition.TOP_RIGHT,

    // UBICACIÓN GEOGRÁFICA
    val mostrarUbicacion: Boolean = true,
    val ciudadActual: String = "Nueva Loja",
    val provinciaActual: String = "Sucumbíos",
    val paisActual: String = "Ecuador",
    val tipoUbicacion: TipoUbicacion = TipoUbicacion.LOCAL,

    // INFORMACIÓN DEL PROGRAMA
    val nombrePrograma: String = "",
    val tipoPrograma: String = "",
    val numeroEpisodio: String = "",
    val temporada: String = "",

    // ESTADO DEL STREAM
    val estadoStream: EstadoStream = EstadoStream.EN_VIVO,
    val mostrarContadorEspectadores: Boolean = false,
    val hashtagsActivos: List<String> = emptyList(),

    // INFORMACIÓN CONTEXTUAL
    val clima: ClimaConfig = ClimaConfig(),
    val noticias: NoticiasConfig = NoticiasConfig()
)

enum class FormatoFecha {
    DD_MM_YYYY, MM_DD_YYYY, YYYY_MM_DD, DD_MMM_YYYY
}

enum class FormatoHora {
    HH_MM_12H, HH_MM_24H, HH_MM_SS_12H, HH_MM_SS_24H
}


enum class TipoUbicacion(val displayName: String) {
    LOCAL("Local - Sucumbíos"),
    REGIONAL("Regional - Ecuador"),
    NACIONAL("Nacional - Ecuador"),
    INTERNACIONAL("Internacional")
}

enum class EstadoStream(val displayName: String, val color: String) {
    EN_VIVO("🔴 EN VIVO", "#DC2626"),
    PROXIMAMENTE("🕐 PRÓXIMAMENTE", "#F59E0B"),
    REPETICION("📹 REPETICIÓN", "#6B7280"),
    PAUSADO("⏸️ PAUSADO", "#9333EA"),
    FINALIZADO("✅ FINALIZADO", "#16A34A")
}

data class ClimaConfig(
    val mostrar: Boolean = false,
    val ciudad: String = "Nueva Loja",
    val unidadTemperatura: UnidadTemperatura = UnidadTemperatura.CELSIUS,
    val posicion: ElementPosition = ElementPosition.TOP_RIGHT
)

enum class UnidadTemperatura { CELSIUS, FAHRENHEIT }

data class NoticiasConfig(
    val mostrarTicker: Boolean = false,
    val velocidadTicker: VelocidadTicker = VelocidadTicker.NORMAL,
    val posicionTicker: ElementPosition = ElementPosition.BOTTOM_CENTER
)

enum class VelocidadTicker { LENTA, NORMAL, RAPIDA }

/**
 * Configuración específica para renderizado web (CameraFi App)
 */
data class WebRenderConfig(
    val habilitado: Boolean = true,
    val urlBase: String = "https://tu-dominio.com/stream-graphics",
    val actualizacionTiempoReal: Boolean = true,
    val cachingEnabled: Boolean = true,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val maxFileSize: Int = 2048, // KB
    val formatoSalida: FormatoSalida = FormatoSalida.HTML_CSS_JS,
    val optimizacionMobile: Boolean = true,
    val fallbackConfig: Boolean = true // Si falla, usar configuración básica
)

enum class CompressionLevel { LOW, MEDIUM, HIGH }
enum class FormatoSalida { HTML_CSS_JS, JSON_CONFIG, BOTH }

/**
 * Funciones de utilidad para conversión y compatibilidad
 */
object PerfilStreamConfigUtils {

    /**
     * Migrar de configuración básica actual a configuración avanzada
     */
    fun migrarDesdeConfiguracionBasica(
        nombrePerfil: String,
        colorFondo1: String,
        colorFondo2: String,
        colorFondo3: String,
        colorLetra1: String,
        colorLetra2: String,
        colorLetra3: String,
        urlLogo: String,
        urlImagenPublicidad: String,
        invitado: String
    ): PerfilStreamConfig {

        val sistemaColores = SistemaColoresAvanzado(
            colorFondo1 = colorFondo1,
            colorFondo2 = colorFondo2,
            colorFondo3 = colorFondo3,
            colorLetra1 = colorLetra1,
            colorLetra2 = colorLetra2,
            colorLetra3 = colorLetra3
        )

        val lowerThirdConfig = LowerThirdConfig(
            logo = LogoConfig(
                simple = LogoSimpleConfig(url = urlLogo)
            ),
            publicidad = PublicidadConfig(url = urlImagenPublicidad)
        )

        val invitadoConfig = InvitadoConfigAvanzado(
            invitadoBasico = Invitado(nombre = invitado, rol = "", tema = ""),
            nombreCompleto = invitado
        )

        return PerfilStreamConfig(
            nombrePerfil = nombrePerfil,
            descripcion = "Migrado desde configuración básica",
            sistemaColores = sistemaColores,
            lowerThirdConfig = lowerThirdConfig,
            invitadoConfig = invitadoConfig
        )
    }

    /**
     * Convertir a formato compatible con Firebase actual
     */
    fun toFirebaseFormat(config: PerfilStreamConfig): Map<String, Any> {
        return mapOf(
            // MANTIENE FORMATO ACTUAL PARA COMPATIBILIDAD
            "NombrePerfil" to config.nombrePerfil,
            "colorFondo1" to config.sistemaColores.colorFondo1,
            "colorFondo2" to config.sistemaColores.colorFondo2,
            "colorFondo3" to config.sistemaColores.colorFondo3,
            "colorLetra1" to config.sistemaColores.colorLetra1,
            "colorLetra2" to config.sistemaColores.colorLetra2,
            "colorLetra3" to config.sistemaColores.colorLetra3,
            "urlLogo" to config.lowerThirdConfig.logo.simple.url,
            "urlImagenPublicidad" to config.lowerThirdConfig.publicidad.url,
            "Invitado" to config.invitadoConfig.nombreCompleto,

            // NUEVOS CAMPOS AVANZADOS
            "configuracionAvanzada" to mapOf(
                "version" to config.version,
                "categoria" to config.categoria.name,
                "descripcion" to config.descripcion,
                "fechaCreacion" to config.fechaCreacion,
                "sistemaColoresAvanzado" to mapOf(
                    "colorAcento" to config.sistemaColores.colorAcento,
                    "colorExito" to config.sistemaColores.colorExito,
                    "colorAdvertencia" to config.sistemaColores.colorAdvertencia,
                    "colorError" to config.sistemaColores.colorError,
                    "gradientePrincipal" to config.sistemaColores.gradientePrincipal,
                    "gradienteSecundario" to config.sistemaColores.gradienteSecundario
                ),
                "posicionamiento" to mapOf(
                    "logoPosition" to config.posicionamiento.logoPosition.name,
                    "canvasResolution" to config.posicionamiento.canvasResolution.name,
                    "safeMargins" to config.posicionamiento.safeMargins
                ),
                "invitadoAvanzado" to mapOf(
                    "nombreCompleto" to config.invitadoConfig.nombreCompleto,
                    "titulo" to config.invitadoConfig.titulo,
                    "organizacion" to config.invitadoConfig.organizacion,
                    "biografia" to config.invitadoConfig.biografia,
                    "redesSociales" to config.invitadoConfig.redesSociales.mapKeys { it.key.name }
                ),
                "contenidoDinamico" to mapOf(
                    "mostrarFechaHora" to config.contenidoDinamico.mostrarFechaHora,
                    "formatoFecha" to config.contenidoDinamico.formatoFecha.name,
                    "formatoHora" to config.contenidoDinamico.formatoHora.name,
                    "ciudadActual" to config.contenidoDinamico.ciudadActual,
                    "provinciaActual" to config.contenidoDinamico.provinciaActual,
                    "estadoStream" to config.contenidoDinamico.estadoStream.name
                ),
                "webRenderConfig" to mapOf(
                    "habilitado" to config.webRenderConfig.habilitado,
                    "actualizacionTiempoReal" to config.webRenderConfig.actualizacionTiempoReal,
                    "compressionLevel" to config.webRenderConfig.compressionLevel.name
                )
            )
        )
    }

    /**
     * Generar URL única para visualización web
     */
    fun generarUrlVisualizacion(nombrePerfil: String): String {
        val perfilSlug = nombrePerfil.lowercase()
            .replace(" ", "-")
            .replace(Regex("[^a-z0-9-]"), "")
        return "https://tu-dominio.com/stream-graphics/${perfilSlug}"
    }

    /**
     * Generar presets automáticos basados en categoría
     */
    fun generarPresetPorCategoria(categoria: CategoriaStream): PerfilStreamConfig {
        val coloresBase = SistemaColoresAvanzado.desdeCategoria(categoria)

        return PerfilStreamConfig(
            nombrePerfil = "Preset ${categoria.displayName}",
            descripcion = "Configuración optimizada para ${categoria.displayName}",
            categoria = categoria,
            sistemaColores = coloresBase,
            posicionamiento = when (categoria) {
                CategoriaStream.NOTICIAS -> ConfiguracionPosicionamiento(
                    logoPosition = ElementPosition.TOP_LEFT,
                    temaPosition = ElementPosition.BOTTOM_CENTER,
                    ubicacionPosition = ElementPosition.TOP_RIGHT
                )
                CategoriaStream.DEPORTES -> ConfiguracionPosicionamiento(
                    logoPosition = ElementPosition.BOTTOM_LEFT,
                    temaPosition = ElementPosition.TOP_CENTER,
                    ubicacionPosition = ElementPosition.BOTTOM_RIGHT
                )
                else -> ConfiguracionPosicionamiento()
            }
        )
    }
}