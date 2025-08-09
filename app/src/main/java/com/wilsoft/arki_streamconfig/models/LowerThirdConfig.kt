// Archivo: models/LowerThirdConfig.kt
package com.wilsoft.arki_streamconfig.models

data class LowerThirdConfig(
    val layout: LayoutConfig = LayoutConfig(),
    val logo: LogoConfig = LogoConfig(),
    val textoPrincipal: TextConfig = TextConfig(),
    val textoSecundario: TextConfig = TextConfig(),
    val tema: TextConfig = TextConfig(),
    val publicidad: PublicidadConfig = PublicidadConfig(),
    val timing: TimingConfig = TimingConfig(),
    val presets: PresetsConfig = PresetsConfig()
)

// Archivo: models/LayoutConfig.kt
data class LayoutConfig(
    val presetActivo: String = "standard",
    val safeMargins: Boolean = true,
    val canvasVirtual: Dimensions = Dimensions(1920, 1080)
)

enum class LogoMode(val displayName: String) {
    SIMPLE("Logo Simple"),
    ALIANZA("Logo Alianza"),
    ROTACION("Rotación de Logos")
}

data class LogoSimpleConfig(
    val url: String = "",
    val posicion: Position = Position(126, 982),
    val tamaño: Dimensions = Dimensions(55, 55),
    val forma: LogoShape = LogoShape.CIRCLE,
    val fondo: FondoConfig = FondoConfig(),
    val animacion: AnimationConfig = AnimationConfig()
)

data class LogoAlianzaConfig(
    val url: String = "",
    val descripcion: String = "Canal 1 & Radio 2",
    val posicion: Position = Position(126, 982),
    val tamaño: Dimensions = Dimensions(75, 55),
    val animacion: AnimationConfig = AnimationConfig()
)

data class LogoRotacionConfig(
    val logos: List<LogoRotacionItem> = emptyList(),
    val cicloContinuo: Boolean = true,
    val pausarEnHover: Boolean = false,
    val intervaloCambio: Int = 4000
)

data class LogoRotacionItem(
    val url: String,
    val nombre: String,
    val duracion: Int = 4000
)

enum class LogoShape(val displayName: String, val cssValue: String) {
    CIRCLE("Circular", "50%"),
    SQUARE("Cuadrado", "0px"),
    ROUNDED("Redondeado", "8px")
}

// Archivo: models/TextConfig.kt
data class TextConfig(
    val contenido: String = "",
    val mostrar: Boolean = true,
    val posicion: Position = Position(180, 976),
    val tipografia: TypographyConfig = TypographyConfig(),
    val fondo: FondoConfig = FondoConfig(),
    val texto: TextStyleConfig = TextStyleConfig(),
    val animacion: AnimationConfig = AnimationConfig()
)

data class TypographyConfig(
    val familia: String = "Arial",
    val tamaño: Int = 18,
    val peso: FontWeight = FontWeight.NORMAL,
    val estilo: FontStyle = FontStyle.NORMAL,
    val transformacion: TextTransform = TextTransform.NONE
)

enum class FontWeight(val value: Int, val displayName: String) {
    LIGHT(300, "Ligera"),
    NORMAL(400, "Normal"),
    MEDIUM(500, "Media"),
    SEMIBOLD(600, "Semi-Bold"),
    BOLD(700, "Negrita"),
    EXTRABOLD(800, "Extra Negrita")
}

enum class FontStyle(val displayName: String, val cssValue: String) {
    NORMAL("Normal", "normal"),
    ITALIC("Cursiva", "italic")
}

enum class TextTransform(val displayName: String, val cssValue: String) {
    NONE("Sin transformar", "none"),
    UPPERCASE("MAYÚSCULAS", "uppercase"),
    LOWERCASE("minúsculas", "lowercase"),
    CAPITALIZE("Primera Letra", "capitalize")
}

data class FondoConfig(
    val color: String = "#1066FF",
    val opacidad: Float = 1.0f,
    val padding: Padding = Padding(),
    val borderRadius: String = "0px",
    val mostrar: Boolean = true
)

data class TextStyleConfig(
    val color: String = "#FFFFFF",
    val sombra: ShadowConfig = ShadowConfig()
)

data class ShadowConfig(
    val mostrar: Boolean = false,
    val color: String = "#000000",
    val blur: Int = 2,
    val offset: Position = Position(1, 1)
)

// Archivo: models/AnimationConfig.kt
data class AnimationConfig(
    val entrada: AnimationType = AnimationType.FADE_IN,
    val salida: AnimationType = AnimationType.FADE_OUT,
    val duracion: Int = 300,
    val delay: Int = 0,
    val easing: EasingType = EasingType.EASE_IN_OUT
)

enum class AnimationType(val displayName: String, val cssClass: String) {
    FADE_IN("Aparecer", "fadeIn"),
    FADE_OUT("Desvanecer", "fadeOut"),
    SLIDE_IN_LEFT("Deslizar Izquierda", "slideInLeft"),
    SLIDE_OUT_LEFT("Deslizar Salir Izquierda", "slideOutLeft"),
    SLIDE_IN_RIGHT("Deslizar Derecha", "slideInRight"),
    SLIDE_OUT_RIGHT("Deslizar Salir Derecha", "slideOutRight"),
    SLIDE_IN_TOP("Deslizar Arriba", "slideInTop"),
    SLIDE_OUT_TOP("Deslizar Salir Arriba", "slideOutTop"),
    SLIDE_IN_BOTTOM("Deslizar Abajo", "slideInBottom"),
    SLIDE_OUT_BOTTOM("Deslizar Salir Abajo", "slideOutBottom"),
    ZOOM_IN("Acercar", "zoomIn"),
    ZOOM_OUT("Alejar", "zoomOut")
}

enum class EasingType(val displayName: String, val cssValue: String) {
    LINEAR("Linear", "linear"),
    EASE("Ease", "ease"),
    EASE_IN("Ease In", "ease-in"),
    EASE_OUT("Ease Out", "ease-out"),
    EASE_IN_OUT("Ease In-Out", "ease-in-out"),
    CUBIC_BEZIER("Cubic Bezier", "cubic-bezier(0.25, 0.46, 0.45, 0.94)")
}

// Archivo: models/PublicidadConfig.kt
data class PublicidadConfig(
    val mostrar: Boolean = false,
    val url: String = "",
    val posicion: Position = Position(6, 1010),
    val tamaño: Dimensions = Dimensions("auto", 70),
    val animacion: AnimationConfig = AnimationConfig()
)

// Archivo: models/TimingConfig.kt
data class TimingConfig(
    val duracionDisplay: Int = 7000,
    val autoHide: Boolean = true,
    val secuencia: SecuenciaConfig = SecuenciaConfig()
)

data class SecuenciaConfig(
    val logoPrimero: Boolean = true,
    val intervaloEntreElementos: Int = 100
)

// Archivo: models/PresetConfig.kt
data class PresetsConfig(
    val actual: String = "estandar",
    val disponibles: Map<String, String> = mapOf(
        "estandar" to "Configuración estándar broadcast",
        "noticias" to "Optimizado para noticias",
        "deportes" to "Diseño dinámico deportivo",
        "corporativo" to "Estilo corporativo minimalista"
    )
)

// Archivo: models/CommonTypes.kt
data class Position(
    val x: Int,
    val y: Int
)

data class Dimensions(
    val width: Any, // Puede ser Int o String ("auto")
    val height: Any // Puede ser Int o String ("auto")
) {
    constructor(width: Int, height: Int) : this(width as Any, height as Any)
    constructor(width: String, height: Int) : this(width as Any, height as Any)
    constructor(width: Int, height: String) : this(width as Any, height as Any)
}

data class Padding(
    val top: Int = 7,
    val right: Int = 30,
    val bottom: Int = 7,
    val left: Int = 30
)

// Archivo: models/PresetTemplates.kt
object PresetTemplates {

    fun getPresetEstandar(): LowerThirdConfig = LowerThirdConfig(
        layout = LayoutConfig(
            presetActivo = "estandar",
            safeMargins = true,
            canvasVirtual = Dimensions(1920, 1080)
        ),
        logo = LogoConfig(
            modo = LogoMode.SIMPLE,
            mostrar = true,
            simple = LogoSimpleConfig(
                posicion = Position(126, 982),
                tamaño = Dimensions(55, 55),
                forma = LogoShape.CIRCLE,
                fondo = FondoConfig(
                    color = "#1066FF",
                    opacidad = 0.8f,
                    padding = Padding(10, 10, 10, 10),
                    borderRadius = "50%"
                )
            )
        ),
        textoPrincipal = TextConfig(
            posicion = Position(180, 976),
            tipografia = TypographyConfig(
                familia = "Arial",
                tamaño = 18,
                peso = FontWeight.BOLD,
                transformacion = TextTransform.UPPERCASE
            ),
            fondo = FondoConfig(
                color = "#1066FF",
                padding = Padding(7, 30, 7, 30),
                borderRadius = "0px 20px 20px 0px"
            ),
            animacion = AnimationConfig(
                entrada = AnimationType.SLIDE_IN_LEFT,
                duracion = 600,
                delay = 100
            )
        ),
        textoSecundario = TextConfig(
            posicion = Position(180, 1000),
            tipografia = TypographyConfig(
                familia = "Arial",
                tamaño = 12,
                peso = FontWeight.MEDIUM,
                transformacion = TextTransform.UPPERCASE
            ),
            fondo = FondoConfig(
                color = "#F08313",
                padding = Padding(5, 35, 5, 40),
                borderRadius = "0px 0px 30px 0px"
            ),
            animacion = AnimationConfig(
                entrada = AnimationType.SLIDE_IN_TOP,
                duracion = 300,
                delay = 250
            )
        ),
        tema = TextConfig(
            posicion = Position(180, 976),
            tipografia = TypographyConfig(
                familia = "Arial",
                tamaño = 19,
                peso = FontWeight.NORMAL
            ),
            fondo = FondoConfig(
                color = "#103264",
                padding = Padding(18, 35, 18, 35),
                borderRadius = "0px 30px 30px 0px"
            ),
            animacion = AnimationConfig(
                entrada = AnimationType.SLIDE_IN_LEFT,
                duracion = 600,
                delay = 100
            )
        )
    )

    fun getPresetNoticias(): LowerThirdConfig = getPresetEstandar().copy(
        textoPrincipal = getPresetEstandar().textoPrincipal.copy(
            fondo = getPresetEstandar().textoPrincipal.fondo.copy(color = "#C41E3A"),
            tipografia = getPresetEstandar().textoPrincipal.tipografia.copy(tamaño = 16)
        ),
        textoSecundario = getPresetEstandar().textoSecundario.copy(
            fondo = getPresetEstandar().textoSecundario.fondo.copy(color = "#8B0000")
        )
    )

    fun getPresetDeportes(): LowerThirdConfig = getPresetEstandar().copy(
        textoPrincipal = getPresetEstandar().textoPrincipal.copy(
            fondo = getPresetEstandar().textoPrincipal.fondo.copy(color = "#228B22"),
            animacion = getPresetEstandar().textoPrincipal.animacion.copy(
                entrada = AnimationType.ZOOM_IN,
                duracion = 400
            )
        ),
        textoSecundario = getPresetEstandar().textoSecundario.copy(
            fondo = getPresetEstandar().textoSecundario.fondo.copy(color = "#32CD32")
        )
    )

    fun getPresetCorporativo(): LowerThirdConfig = getPresetEstandar().copy(
        textoPrincipal = getPresetEstandar().textoPrincipal.copy(
            fondo = getPresetEstandar().textoPrincipal.fondo.copy(
                color = "#2F4F4F",
                borderRadius = "4px"
            ),
            tipografia = getPresetEstandar().textoPrincipal.tipografia.copy(
                peso = FontWeight.MEDIUM,
                transformacion = TextTransform.NONE
            )
        ),
        textoSecundario = getPresetEstandar().textoSecundario.copy(
            fondo = getPresetEstandar().textoSecundario.fondo.copy(
                color = "#696969",
                borderRadius = "4px"
            )
        )
    )

    fun getAllPresets(): Map<String, LowerThirdConfig> = mapOf(
        "estandar" to getPresetEstandar(),
        "noticias" to getPresetNoticias(),
        "deportes" to getPresetDeportes(),
        "corporativo" to getPresetCorporativo()
    )
}

/**
 * Configuración para logos aliados
 */
data class LogosAliadosConfig(
    val habilitado: Boolean = false,
    val logos: List<LogoAliadoItem> = emptyList()
)

/**
 * Item individual de logo aliado
 */
data class LogoAliadoItem(
    val id: String = "",
    val nombre: String = "",
    val url: String = "",
    val activo: Boolean = true,
    val orden: Int = 0
)

// ============================================================================
// 2. MODIFICAR LogoConfig EXISTENTE EN LowerThirdConfig.kt
// ============================================================================

// REEMPLAZAR la data class LogoConfig existente por esta:
data class LogoConfig(
    val modo: LogoMode = LogoMode.SIMPLE,
    val mostrar: Boolean = true,
    val simple: LogoSimpleConfig = LogoSimpleConfig(),
    val alianza: LogoAlianzaConfig = LogoAlianzaConfig(),
    val rotacion: LogoRotacionConfig = LogoRotacionConfig(),
    // ✅ AGREGAR ESTE CAMPO NUEVO:
    val logosAliados: LogosAliadosConfig = LogosAliadosConfig()
)

