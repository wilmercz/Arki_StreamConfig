// Archivo: utils/LowerThirdUtils.kt
package com.wilsoft.arki_streamconfig.utilidades

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.wilsoft.arki_streamconfig.models.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object LowerThirdUtils {

    /**
     * Validar configuración de Lower Third
     */
    fun validateConfiguration(config: LowerThirdConfig): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Validar logo
        if (config.logo.mostrar) {
            when (config.logo.modo) {
                LogoMode.SIMPLE -> {
                    if (config.logo.simple.url.isEmpty()) {
                        errors.add("URL del logo simple es requerida cuando el logo está habilitado")
                    }
                    if (!isValidUrl(config.logo.simple.url)) {
                        warnings.add("URL del logo simple no parece válida")
                    }
                }
                LogoMode.ALIANZA -> {
                    if (config.logo.alianza.url.isEmpty()) {
                        errors.add("URL del logo de alianza es requerida")
                    }
                }
                LogoMode.ROTACION -> {
                    if (config.logo.rotacion.logos.isEmpty()) {
                        errors.add("Se requiere al menos un logo para la rotación")
                    }
                    config.logo.rotacion.logos.forEach { logo ->
                        if (logo.url.isEmpty()) {
                            errors.add("URL vacía en logo de rotación: ${logo.nombre}")
                        }
                    }
                }
            }
        }

        // Validar textos
        if (config.textoPrincipal.mostrar && config.textoPrincipal.contenido.isEmpty()) {
            warnings.add("Texto principal está habilitado pero vacío")
        }

        if (config.textoSecundario.mostrar && config.textoSecundario.contenido.isEmpty()) {
            warnings.add("Texto secundario está habilitado pero vacío")
        }

        // Validar colores
        listOf(
            config.textoPrincipal.fondo.color,
            config.textoPrincipal.texto.color,
            config.textoSecundario.fondo.color,
            config.textoSecundario.texto.color,
            config.tema.fondo.color,
            config.tema.texto.color,
            config.logo.simple.fondo.color
        ).forEach { color ->
            if (!isValidHexColor(color)) {
                errors.add("Color inválido: $color")
            }
        }

        // Validar posiciones dentro del canvas
        val canvasWidth = config.layout.canvasVirtual.width as Int
        val canvasHeight = config.layout.canvasVirtual.height as Int

        listOf(
            "Logo" to config.logo.simple.posicion,
            "Texto Principal" to config.textoPrincipal.posicion,
            "Texto Secundario" to config.textoSecundario.posicion,
            "Tema" to config.tema.posicion,
            "Publicidad" to config.publicidad.posicion
        ).forEach { (name, position) ->
            if (position.x < 0 || position.x > canvasWidth) {
                warnings.add("$name: posición X (${'$'}{position.x}) fuera del canvas")
            }
            if (position.y < 0 || position.y > canvasHeight) {
                warnings.add("$name: posición Y (${'$'}{position.y}) fuera del canvas")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Optimizar configuración para rendimiento
     */
    fun optimizeConfiguration(config: LowerThirdConfig): LowerThirdConfig {
        return config.copy(
            // Optimizar duraciones de animación
            textoPrincipal = optimizeTextAnimations(config.textoPrincipal),
            textoSecundario = optimizeTextAnimations(config.textoSecundario),
            tema = optimizeTextAnimations(config.tema),

            // Optimizar logo
            logo = config.logo.copy(
                simple = config.logo.simple.copy(
                    animacion = optimizeAnimationConfig(config.logo.simple.animacion)
                )
            ),

            // Ajustar timing global
            timing = config.timing.copy(
                duracionDisplay = minOf(config.timing.duracionDisplay, 15000), // Max 15 segundos
                secuencia = config.timing.secuencia.copy(
                    intervaloEntreElementos = maxOf(config.timing.secuencia.intervaloEntreElementos, 50) // Min 50ms
                )
            )
        )
    }

    private fun optimizeTextAnimations(textConfig: TextConfig): TextConfig {
        return textConfig.copy(
            animacion = optimizeAnimationConfig(textConfig.animacion)
        )
    }

    private fun optimizeAnimationConfig(animConfig: AnimationConfig): AnimationConfig {
        return animConfig.copy(
            duracion = when {
                animConfig.duracion < 100 -> 100 // Mínimo 100ms
                animConfig.duracion > 2000 -> 2000 // Máximo 2s
                else -> animConfig.duracion
            },
            delay = when {
                animConfig.delay < 0 -> 0
                animConfig.delay > 1000 -> 1000 // Máximo 1s de delay
                else -> animConfig.delay
            }
        )
    }

    /**
     * Generar configuración responsiva basada en resolución
     */
    fun generateResponsiveConfig(
        baseConfig: LowerThirdConfig,
        targetWidth: Int,
        targetHeight: Int
    ): LowerThirdConfig {
        val baseWidth = baseConfig.layout.canvasVirtual.width as Int
        val baseHeight = baseConfig.layout.canvasVirtual.height as Int

        val scaleX = targetWidth.toFloat() / baseWidth
        val scaleY = targetHeight.toFloat() / baseHeight
        val avgScale = (scaleX + scaleY) / 2

        return baseConfig.copy(
            layout = baseConfig.layout.copy(
                canvasVirtual = Dimensions(targetWidth, targetHeight)
            ),

            logo = baseConfig.logo.copy(
                simple = baseConfig.logo.simple.copy(
                    posicion = Position(
                        x = (baseConfig.logo.simple.posicion.x * scaleX).roundToInt(),
                        y = (baseConfig.logo.simple.posicion.y * scaleY).roundToInt()
                    ),
                    tamaño = Dimensions(
                        width = ((baseConfig.logo.simple.tamaño.width as Int) * avgScale).roundToInt(),
                        height = ((baseConfig.logo.simple.tamaño.height as Int) * avgScale).roundToInt()
                    )
                )
            ),

            textoPrincipal = scaleTextConfig(baseConfig.textoPrincipal, scaleX, scaleY, avgScale),
            textoSecundario = scaleTextConfig(baseConfig.textoSecundario, scaleX, scaleY, avgScale),
            tema = scaleTextConfig(baseConfig.tema, scaleX, scaleY, avgScale),

            publicidad = baseConfig.publicidad.copy(
                posicion = Position(
                    x = (baseConfig.publicidad.posicion.x * scaleX).roundToInt(),
                    y = (baseConfig.publicidad.posicion.y * scaleY).roundToInt()
                ),
                tamaño = when (val height = baseConfig.publicidad.tamaño.height) {
                    is Int -> Dimensions(
                        width = baseConfig.publicidad.tamaño.width,
                        height = (height * avgScale).roundToInt()
                    )
                    else -> baseConfig.publicidad.tamaño
                }
            )
        )
    }

    private fun scaleTextConfig(textConfig: TextConfig, scaleX: Float, scaleY: Float, avgScale: Float): TextConfig {
        return textConfig.copy(
            posicion = Position(
                x = (textConfig.posicion.x * scaleX).roundToInt(),
                y = (textConfig.posicion.y * scaleY).roundToInt()
            ),
            tipografia = textConfig.tipografia.copy(
                tamaño = (textConfig.tipografia.tamaño * avgScale).roundToInt()
            ),
            fondo = textConfig.fondo.copy(
                padding = Padding(
                    top = (textConfig.fondo.padding.top * avgScale).roundToInt(),
                    right = (textConfig.fondo.padding.right * avgScale).roundToInt(),
                    bottom = (textConfig.fondo.padding.bottom * avgScale).roundToInt(),
                    left = (textConfig.fondo.padding.left * avgScale).roundToInt()
                )
            )
        )
    }

    /**
     * Exportar configuración a diferentes formatos
     */
    fun exportToOBS(config: LowerThirdConfig): String {
        return """
        {
          "obs_lower_third_config": {
            "version": "2.0",
            "timestamp": "${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}",
            "canvas": {
              "width": ${config.layout.canvasVirtual.width},
              "height": ${config.layout.canvasVirtual.height}
            },
            "elements": {
              "logo": {
                "enabled": ${config.logo.mostrar},
                "source": "${config.logo.simple.url}",
                "x": ${config.logo.simple.posicion.x},
                "y": ${config.logo.simple.posicion.y},
                "width": ${config.logo.simple.tamaño.width},
                "height": ${config.logo.simple.tamaño.height}
              },
              "main_text": {
                "enabled": ${config.textoPrincipal.mostrar},
                "text": "${config.textoPrincipal.contenido}",
                "font_family": "${config.textoPrincipal.tipografia.familia}",
                "font_size": ${config.textoPrincipal.tipografia.tamaño},
                "font_weight": ${config.textoPrincipal.tipografia.peso.value},
                "color": "${config.textoPrincipal.texto.color}",
                "background_color": "${config.textoPrincipal.fondo.color}",
                "x": ${config.textoPrincipal.posicion.x},
                "y": ${config.textoPrincipal.posicion.y}
              },
              "secondary_text": {
                "enabled": ${config.textoSecundario.mostrar},
                "text": "${config.textoSecundario.contenido}",
                "font_family": "${config.textoSecundario.tipografia.familia}",
                "font_size": ${config.textoSecundario.tipografia.tamaño},
                "color": "${config.textoSecundario.texto.color}",
                "background_color": "${config.textoSecundario.fondo.color}",
                "x": ${config.textoSecundario.posicion.x},
                "y": ${config.textoSecundario.posicion.y}
              }
            }
          }
        }
        """.trimIndent()
    }

    fun exportToCSS(config: LowerThirdConfig): String {
        return """
        /* Lower Third CSS Export - Generated ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())} */
        
        .lower-third-container {
            position: absolute;
            bottom: 0;
            left: 0;
            width: 100%;
            height: 200px;
            pointer-events: none;
            z-index: 1000;
        }
        
        .logo-element {
            position: absolute;
            left: ${config.logo.simple.posicion.x}px;
            bottom: ${1080 - config.logo.simple.posicion.y}px;
            width: ${config.logo.simple.tamaño.width}px;
            height: ${config.logo.simple.tamaño.height}px;
            background-color: ${config.logo.simple.fondo.color};
            opacity: ${config.logo.simple.fondo.opacidad};
            border-radius: ${config.logo.simple.forma.cssValue};
            display: ${if (config.logo.mostrar) "block" else "none"};
        }
        
        .main-text-element {
            position: absolute;
            left: ${config.textoPrincipal.posicion.x}px;
            bottom: ${1080 - config.textoPrincipal.posicion.y}px;
            font-family: ${config.textoPrincipal.tipografia.familia};
            font-size: ${config.textoPrincipal.tipografia.tamaño}px;
            font-weight: ${config.textoPrincipal.tipografia.peso.value};
            color: ${config.textoPrincipal.texto.color};
            background-color: ${config.textoPrincipal.fondo.color};
            padding: ${config.textoPrincipal.fondo.padding.top}px ${config.textoPrincipal.fondo.padding.right}px ${config.textoPrincipal.fondo.padding.bottom}px ${config.textoPrincipal.fondo.padding.left}px;
            border-radius: ${config.textoPrincipal.fondo.borderRadius};
            text-transform: ${config.textoPrincipal.tipografia.transformacion.cssValue};
            display: ${if (config.textoPrincipal.mostrar) "block" else "none"};
        }
        
        .secondary-text-element {
            position: absolute;
            left: ${config.textoSecundario.posicion.x}px;
            bottom: ${1080 - config.textoSecundario.posicion.y}px;
            font-family: ${config.textoSecundario.tipografia.familia};
            font-size: ${config.textoSecundario.tipografia.tamaño}px;
            font-weight: ${config.textoSecundario.tipografia.peso.value};
            color: ${config.textoSecundario.texto.color};
            background-color: ${config.textoSecundario.fondo.color};
            padding: ${config.textoSecundario.fondo.padding.top}px ${config.textoSecundario.fondo.padding.right}px ${config.textoSecundario.fondo.padding.bottom}px ${config.textoSecundario.fondo.padding.left}px;
            border-radius: ${config.textoSecundario.fondo.borderRadius};
            text-transform: ${config.textoSecundario.tipografia.transformacion.cssValue};
            display: ${if (config.textoSecundario.mostrar) "block" else "none"};
        }
        """.trimIndent()
    }

    /**
     * Importar configuración desde diferentes formatos
     */
    fun importFromJSON(jsonString: String): LowerThirdConfig? {
        return try {
            // Implementar parser JSON personalizado o usar library
            PresetTemplates.getPresetEstandar() // Placeholder
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calcular contraste de colores para accesibilidad
     */
    fun calculateColorContrast(color1: String, color2: String): Double {
        return try {
            val c1 = parseHexColor(color1)
            val c2 = parseHexColor(color2)

            val l1 = calculateLuminance(c1)
            val l2 = calculateLuminance(c2)

            val lighter = maxOf(l1, l2)
            val darker = minOf(l1, l2)

            (lighter + 0.05) / (darker + 0.05)
        } catch (e: Exception) {
            1.0
        }
    }

    private fun calculateLuminance(color: Color): Double {
        val r = if (color.red <= 0.03928) color.red / 12.92 else Math.pow((color.red + 0.055) / 1.055, 2.4)
        val g = if (color.green <= 0.03928) color.green / 12.92 else Math.pow((color.green + 0.055) / 1.055, 2.4)
        val b = if (color.blue <= 0.03928) color.blue / 12.92 else Math.pow((color.blue + 0.055) / 1.055, 2.4)

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun parseHexColor(hex: String): Color {
        val colorInt = android.graphics.Color.parseColor(hex)
        return Color(colorInt)
    }

    /**
     * Generar recomendaciones de mejora
     */
    fun generateRecommendations(config: LowerThirdConfig): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        // Verificar contraste de colores
        val mainTextContrast = calculateColorContrast(
            config.textoPrincipal.fondo.color,
            config.textoPrincipal.texto.color
        )

        if (mainTextContrast < 4.5) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.ACCESSIBILITY,
                    priority = Priority.HIGH,
                    title = "Contraste insuficiente en texto principal",
                    description = "El contraste actual es ${String.format("%.1f", mainTextContrast)}:1, se recomienda al menos 4.5:1 para mejor legibilidad.",
                    action = "Ajustar colores de fondo o texto"
                )
            )
        }

        // Verificar tamaños de fuente
        if (config.textoPrincipal.tipografia.tamaño < 16) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.USABILITY,
                    priority = Priority.MEDIUM,
                    title = "Tamaño de fuente pequeño",
                    description = "El texto principal usa ${config.textoPrincipal.tipografia.tamaño}px, se recomienda al menos 16px para mejor legibilidad.",
                    action = "Aumentar tamaño de fuente"
                )
            )
        }

        // Verificar duraciones de animación
        val totalAnimationTime = config.textoPrincipal.animacion.duracion +
                config.textoSecundario.animacion.duracion +
                config.tema.animacion.duracion

        if (totalAnimationTime > 2000) {
            recommendations.add(
                Recommendation(
                    type = RecommendationType.PERFORMANCE,
                    priority = Priority.LOW,
                    title = "Animaciones muy lentas",
                    description = "Las animaciones toman ${totalAnimationTime}ms total, considera reducir para mejor experiencia.",
                    action = "Reducir duración de animaciones"
                )
            )
        }

        // Verificar posicionamiento
        val canvasWidth = config.layout.canvasVirtual.width as Int
        val safeZone = canvasWidth * 0.05 // 5% del ancho como zona segura

        listOf(
            "Texto Principal" to config.textoPrincipal.posicion,
            "Texto Secundario" to config.textoSecundario.posicion
        ).forEach { (name, position) ->
            if (position.x < safeZone) {
                recommendations.add(
                    Recommendation(
                        type = RecommendationType.LAYOUT,
                        priority = Priority.MEDIUM,
                        title = "$name muy cerca del borde",
                        description = "El elemento está a ${position.x}px del borde izquierdo, considera moverlo hacia la zona segura.",
                        action = "Mover elemento hacia el centro"
                    )
                )
            }
        }

        return recommendations
    }

    /**
     * Funciones auxiliares de validación
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URL(url)
            url.startsWith("http://") || url.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }

    fun isValidHexColor(color: String): Boolean {
        val regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex()
        return color.matches(regex)
    }

    /**
     * Generar ID único para configuraciones
     */
    fun generateConfigId(): String {
        return "lt_config_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Formatear duración para display
     */
    fun formatDuration(milliseconds: Int): String {
        return when {
            milliseconds < 1000 -> "${milliseconds}ms"
            milliseconds < 60000 -> "${milliseconds / 1000}s"
            else -> "${milliseconds / 60000}m ${(milliseconds % 60000) / 1000}s"
        }
    }
}

// Data classes para utilidades
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
) {
    fun hasIssues(): Boolean = errors.isNotEmpty() || warnings.isNotEmpty()

    fun getSummary(): String {
        return when {
            errors.isNotEmpty() -> "❌ ${errors.size} errores, ${warnings.size} advertencias"
            warnings.isNotEmpty() -> "⚠️ ${warnings.size} advertencias"
            else -> "✅ Configuración válida"
        }
    }
}

data class Recommendation(
    val type: RecommendationType,
    val priority: Priority,
    val title: String,
    val description: String,
    val action: String
)

enum class RecommendationType(val displayName: String, val icon: String) {
    ACCESSIBILITY("Accesibilidad", "♿"),
    USABILITY("Usabilidad", "👤"),
    PERFORMANCE("Rendimiento", "⚡"),
    LAYOUT("Diseño", "📐"),
    CONTENT("Contenido", "📝")
}

enum class Priority(val displayName: String, val color: String) {
    HIGH("Alta", "#FF5722"),
    MEDIUM("Media", "#FF9800"),
    LOW("Baja", "#4CAF50")
}


fun com.wilsoft.arki_streamconfig.models.FontWeight.toComposeFontWeight(): androidx.compose.ui.text.font.FontWeight {
    return when (this) {
        com.wilsoft.arki_streamconfig.models.FontWeight.LIGHT -> androidx.compose.ui.text.font.FontWeight.Light
        com.wilsoft.arki_streamconfig.models.FontWeight.NORMAL -> androidx.compose.ui.text.font.FontWeight.Normal
        com.wilsoft.arki_streamconfig.models.FontWeight.MEDIUM -> androidx.compose.ui.text.font.FontWeight.Medium
        com.wilsoft.arki_streamconfig.models.FontWeight.SEMIBOLD -> androidx.compose.ui.text.font.FontWeight.SemiBold
        com.wilsoft.arki_streamconfig.models.FontWeight.BOLD -> androidx.compose.ui.text.font.FontWeight.Bold
        com.wilsoft.arki_streamconfig.models.FontWeight.EXTRABOLD -> androidx.compose.ui.text.font.FontWeight.ExtraBold
    }
}

fun androidx.compose.ui.text.font.FontWeight.toLowerThirdFontWeight(): com.wilsoft.arki_streamconfig.models.FontWeight {
    return when (this) {
        androidx.compose.ui.text.font.FontWeight.Light -> com.wilsoft.arki_streamconfig.models.FontWeight.LIGHT
        androidx.compose.ui.text.font.FontWeight.Normal -> com.wilsoft.arki_streamconfig.models.FontWeight.NORMAL
        androidx.compose.ui.text.font.FontWeight.Medium -> com.wilsoft.arki_streamconfig.models.FontWeight.MEDIUM
        androidx.compose.ui.text.font.FontWeight.SemiBold -> com.wilsoft.arki_streamconfig.models.FontWeight.SEMIBOLD
        androidx.compose.ui.text.font.FontWeight.Bold -> com.wilsoft.arki_streamconfig.models.FontWeight.BOLD
        androidx.compose.ui.text.font.FontWeight.ExtraBold -> com.wilsoft.arki_streamconfig.models.FontWeight.EXTRABOLD
        else -> com.wilsoft.arki_streamconfig.models.FontWeight.NORMAL
    }
}

