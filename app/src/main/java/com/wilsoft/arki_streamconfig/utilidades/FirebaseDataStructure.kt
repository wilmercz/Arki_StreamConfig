// Archivo: utils/FirebaseDataStructure.kt
package com.wilsoft.arki_streamconfig.utilidades

import com.wilsoft.arki_streamconfig.models.*

object FirebaseDataStructure {

    /**
     * Convierte la configuración de Lower Third a formato Firebase
     */
    fun toLowerThirdFirebaseFormat(config: LowerThirdConfig): Map<String, Any> {
        return mapOf(
            "LAYOUT" to mapOf(
                "preset_activo" to config.layout.presetActivo,
                "safe_margins" to config.layout.safeMargins,
                "canvas_virtual" to mapOf(
                    "width" to config.layout.canvasVirtual.width,
                    "height" to config.layout.canvasVirtual.height
                )
            ),
            "LOGO" to mapOf(
                "modo" to config.logo.modo.name.lowercase(),
                "mostrar" to config.logo.mostrar,
                "simple" to mapOf(
                    "url" to config.logo.simple.url,
                    "posicion" to mapOf(
                        "x" to config.logo.simple.posicion.x,
                        "y" to config.logo.simple.posicion.y
                    ),
                    "tamaño" to mapOf(
                        "width" to config.logo.simple.tamaño.width,
                        "height" to config.logo.simple.tamaño.height
                    ),
                    "forma" to config.logo.simple.forma.name.lowercase(),
                    "fondo" to mapOf(
                        "color" to config.logo.simple.fondo.color,
                        "opacidad" to config.logo.simple.fondo.opacidad,
                        "padding" to config.logo.simple.fondo.padding.top, // Simplificado
                        "mostrar" to config.logo.simple.fondo.mostrar
                    ),
                    "animacion" to mapOf(
                        "entrada" to config.logo.simple.animacion.entrada.cssClass,
                        "salida" to config.logo.simple.animacion.salida.cssClass,
                        "duracion" to config.logo.simple.animacion.duracion
                    )
                ),
                "alianza" to mapOf(
                    "url" to config.logo.alianza.url,
                    "descripcion" to config.logo.alianza.descripcion,
                    "posicion" to mapOf(
                        "x" to config.logo.alianza.posicion.x,
                        "y" to config.logo.alianza.posicion.y
                    ),
                    "tamaño" to mapOf(
                        "width" to config.logo.alianza.tamaño.width,
                        "height" to config.logo.alianza.tamaño.height
                    )
                ),
                "rotacion" to mapOf(
                    "logos" to config.logo.rotacion.logos.map { logo ->
                        mapOf(
                            "url" to logo.url,
                            "nombre" to logo.nombre,
                            "duracion" to logo.duracion
                        )
                    },
                    "ciclo_continuo" to config.logo.rotacion.cicloContinuo,
                    "pausar_en_hover" to config.logo.rotacion.pausarEnHover
                )
            ),
            "TEXTO_PRINCIPAL" to toTextConfigFirebaseFormat(config.textoPrincipal),
            "TEXTO_SECUNDARIO" to toTextConfigFirebaseFormat(config.textoSecundario),
            "TEMA" to toTextConfigFirebaseFormat(config.tema),
            "PUBLICIDAD" to mapOf(
                "mostrar" to config.publicidad.mostrar,
                "url" to config.publicidad.url,
                "posicion" to mapOf(
                    "x" to config.publicidad.posicion.x,
                    "y" to config.publicidad.posicion.y
                ),
                "tamaño" to mapOf(
                    "width" to config.publicidad.tamaño.width,
                    "height" to config.publicidad.tamaño.height
                ),
                "animacion" to mapOf(
                    "entrada" to config.publicidad.animacion.entrada.cssClass,
                    "salida" to config.publicidad.animacion.salida.cssClass,
                    "duracion" to config.publicidad.animacion.duracion
                )
            ),
            "TIMING" to mapOf(
                "duracion_display" to config.timing.duracionDisplay,
                "auto_hide" to config.timing.autoHide,
                "secuencia" to mapOf(
                    "logo_primero" to config.timing.secuencia.logoPrimero,
                    "intervalo_entre_elementos" to config.timing.secuencia.intervaloEntreElementos
                )
            ),
            "PRESETS" to mapOf(
                "actual" to config.presets.actual,
                "disponibles" to config.presets.disponibles
            )
        )
    }

    private fun toTextConfigFirebaseFormat(textConfig: TextConfig): Map<String, Any> {
        return mapOf(
            "contenido" to textConfig.contenido,
            "mostrar" to textConfig.mostrar,
            "posicion" to mapOf(
                "x" to textConfig.posicion.x,
                "y" to textConfig.posicion.y
            ),
            "tipografia" to mapOf(
                "familia" to textConfig.tipografia.familia,
                "tamaño" to textConfig.tipografia.tamaño,
                "peso" to textConfig.tipografia.peso.value,
                "estilo" to textConfig.tipografia.estilo.cssValue,
                "transformacion" to textConfig.tipografia.transformacion.cssValue
            ),
            "fondo" to mapOf(
                "color" to textConfig.fondo.color,
                "opacidad" to textConfig.fondo.opacidad,
                "padding" to mapOf(
                    "top" to textConfig.fondo.padding.top,
                    "right" to textConfig.fondo.padding.right,
                    "bottom" to textConfig.fondo.padding.bottom,
                    "left" to textConfig.fondo.padding.left
                ),
                "border_radius" to textConfig.fondo.borderRadius
            ),
            "texto" to mapOf(
                "color" to textConfig.texto.color,
                "sombra" to mapOf(
                    "mostrar" to textConfig.texto.sombra.mostrar,
                    "color" to textConfig.texto.sombra.color,
                    "blur" to textConfig.texto.sombra.blur,
                    "offset" to mapOf(
                        "x" to textConfig.texto.sombra.offset.x,
                        "y" to textConfig.texto.sombra.offset.y
                    )
                )
            ),
            "animacion" to mapOf(
                "entrada" to textConfig.animacion.entrada.cssClass,
                "salida" to textConfig.animacion.salida.cssClass,
                "duracion" to textConfig.animacion.duracion,
                "delay" to textConfig.animacion.delay,
                "easing" to textConfig.animacion.easing.cssValue
            )
        )
    }

    /**
     * Convierte datos de Firebase a configuración de Lower Third
     */
    fun fromLowerThirdFirebaseFormat(data: Map<String, Any>): LowerThirdConfig {
        return LowerThirdConfig(
            layout = parseLayoutConfig(data["LAYOUT"] as? Map<String, Any> ?: emptyMap()),
            logo = parseLogoConfig(data["LOGO"] as? Map<String, Any> ?: emptyMap()),
            textoPrincipal = parseTextConfig(data["TEXTO_PRINCIPAL"] as? Map<String, Any> ?: emptyMap()),
            textoSecundario = parseTextConfig(data["TEXTO_SECUNDARIO"] as? Map<String, Any> ?: emptyMap()),
            tema = parseTextConfig(data["TEMA"] as? Map<String, Any> ?: emptyMap()),
            publicidad = parsePublicidadConfig(data["PUBLICIDAD"] as? Map<String, Any> ?: emptyMap()),
            timing = parseTimingConfig(data["TIMING"] as? Map<String, Any> ?: emptyMap()),
            presets = parsePresetsConfig(data["PRESETS"] as? Map<String, Any> ?: emptyMap())
        )
    }

    private fun parseLayoutConfig(data: Map<String, Any>): LayoutConfig {
        val canvasData = data["canvas_virtual"] as? Map<String, Any> ?: emptyMap()
        return LayoutConfig(
            presetActivo = data["preset_activo"] as? String ?: "standard",
            safeMargins = data["safe_margins"] as? Boolean ?: true,
            canvasVirtual = Dimensions(
                width = canvasData["width"] as? Int ?: 1920,
                height = canvasData["height"] as? Int ?: 1080
            )
        )
    }

    private fun parseLogoConfig(data: Map<String, Any>): LogoConfig {
        val modoStr = data["modo"] as? String ?: "simple"
        val modo = LogoMode.values().find { it.name.lowercase() == modoStr } ?: LogoMode.SIMPLE

        return LogoConfig(
            modo = modo,
            mostrar = data["mostrar"] as? Boolean ?: true,
            simple = parseLogoSimpleConfig(data["simple"] as? Map<String, Any> ?: emptyMap()),
            alianza = parseLogoAlianzaConfig(data["alianza"] as? Map<String, Any> ?: emptyMap()),
            rotacion = parseLogoRotacionConfig(data["rotacion"] as? Map<String, Any> ?: emptyMap())
        )
    }

    private fun parseLogoSimpleConfig(data: Map<String, Any>): LogoSimpleConfig {
        val posicionData = data["posicion"] as? Map<String, Any> ?: emptyMap()
        val tamañoData = data["tamaño"] as? Map<String, Any> ?: emptyMap()
        val fondoData = data["fondo"] as? Map<String, Any> ?: emptyMap()
        val animacionData = data["animacion"] as? Map<String, Any> ?: emptyMap()

        val formaStr = data["forma"] as? String ?: "circle"
        val forma = LogoShape.values().find { it.name.lowercase() == formaStr } ?: LogoShape.CIRCLE

        return LogoSimpleConfig(
            url = data["url"] as? String ?: "",
            posicion = Position(
                x = posicionData["x"] as? Int ?: 126,
                y = posicionData["y"] as? Int ?: 982
            ),
            tamaño = Dimensions(
                width = tamañoData["width"] as? Int ?: 55,
                height = tamañoData["height"] as? Int ?: 55
            ),
            forma = forma,
            fondo = FondoConfig(
                color = fondoData["color"] as? String ?: "#1066FF",
                opacidad = (fondoData["opacidad"] as? Number)?.toFloat() ?: 0.8f,
                padding = Padding(
                    top = fondoData["padding"] as? Int ?: 10,
                    right = fondoData["padding"] as? Int ?: 10,
                    bottom = fondoData["padding"] as? Int ?: 10,
                    left = fondoData["padding"] as? Int ?: 10
                ),
                mostrar = fondoData["mostrar"] as? Boolean ?: true
            ),
            animacion = parseAnimationConfig(animacionData)
        )
    }

    private fun parseLogoAlianzaConfig(data: Map<String, Any>): LogoAlianzaConfig {
        val posicionData = data["posicion"] as? Map<String, Any> ?: emptyMap()
        val tamañoData = data["tamaño"] as? Map<String, Any> ?: emptyMap()

        return LogoAlianzaConfig(
            url = data["url"] as? String ?: "",
            descripcion = data["descripcion"] as? String ?: "Canal 1 & Radio 2",
            posicion = Position(
                x = posicionData["x"] as? Int ?: 126,
                y = posicionData["y"] as? Int ?: 982
            ),
            tamaño = Dimensions(
                width = tamañoData["width"] as? Int ?: 75,
                height = tamañoData["height"] as? Int ?: 55
            )
        )
    }

    private fun parseLogoRotacionConfig(data: Map<String, Any>): LogoRotacionConfig {
        val logosData = data["logos"] as? List<Map<String, Any>> ?: emptyList()
        val logos = logosData.map { logoData ->
            LogoRotacionItem(
                url = logoData["url"] as? String ?: "",
                nombre = logoData["nombre"] as? String ?: "",
                duracion = logoData["duracion"] as? Int ?: 4000
            )
        }

        return LogoRotacionConfig(
            logos = logos,
            cicloContinuo = data["ciclo_continuo"] as? Boolean ?: true,
            pausarEnHover = data["pausar_en_hover"] as? Boolean ?: false
        )
    }

    private fun parseTextConfig(data: Map<String, Any>): TextConfig {
        val posicionData = data["posicion"] as? Map<String, Any> ?: emptyMap()
        val tipografiaData = data["tipografia"] as? Map<String, Any> ?: emptyMap()
        val fondoData = data["fondo"] as? Map<String, Any> ?: emptyMap()
        val textoData = data["texto"] as? Map<String, Any> ?: emptyMap()
        val animacionData = data["animacion"] as? Map<String, Any> ?: emptyMap()

        return TextConfig(
            contenido = data["contenido"] as? String ?: "",
            mostrar = data["mostrar"] as? Boolean ?: true,
            posicion = Position(
                x = posicionData["x"] as? Int ?: 180,
                y = posicionData["y"] as? Int ?: 976
            ),
            tipografia = parseTypographyConfig(tipografiaData),
            fondo = parseFondoConfig(fondoData),
            texto = parseTextStyleConfig(textoData),
            animacion = parseAnimationConfig(animacionData)
        )
    }

    private fun parseTypographyConfig(data: Map<String, Any>): TypographyConfig {
        val pesoValue = data["peso"] as? Int ?: 400
        val peso = FontWeight.values().find { it.value == pesoValue } ?: FontWeight.NORMAL

        val estiloStr = data["estilo"] as? String ?: "normal"
        val estilo = FontStyle.values().find { it.cssValue == estiloStr } ?: FontStyle.NORMAL

        val transformacionStr = data["transformacion"] as? String ?: "none"
        val transformacion = TextTransform.values().find { it.cssValue == transformacionStr } ?: TextTransform.NONE

        return TypographyConfig(
            familia = data["familia"] as? String ?: "Arial",
            tamaño = data["tamaño"] as? Int ?: 18,
            peso = peso,
            estilo = estilo,
            transformacion = transformacion
        )
    }

    private fun parseFondoConfig(data: Map<String, Any>): FondoConfig {
        val paddingData = data["padding"] as? Map<String, Any> ?: emptyMap()

        return FondoConfig(
            color = data["color"] as? String ?: "#1066FF",
            opacidad = (data["opacidad"] as? Number)?.toFloat() ?: 1.0f,
            padding = Padding(
                top = paddingData["top"] as? Int ?: 7,
                right = paddingData["right"] as? Int ?: 30,
                bottom = paddingData["bottom"] as? Int ?: 7,
                left = paddingData["left"] as? Int ?: 30
            ),
            borderRadius = data["border_radius"] as? String ?: "0px"
        )
    }

    private fun parseTextStyleConfig(data: Map<String, Any>): TextStyleConfig {
        val sombraData = data["sombra"] as? Map<String, Any> ?: emptyMap()
        val offsetData = sombraData["offset"] as? Map<String, Any> ?: emptyMap()

        return TextStyleConfig(
            color = data["color"] as? String ?: "#FFFFFF",
            sombra = ShadowConfig(
                mostrar = sombraData["mostrar"] as? Boolean ?: false,
                color = sombraData["color"] as? String ?: "#000000",
                blur = sombraData["blur"] as? Int ?: 2,
                offset = Position(
                    x = offsetData["x"] as? Int ?: 1,
                    y = offsetData["y"] as? Int ?: 1
                )
            )
        )
    }

    private fun parseAnimationConfig(data: Map<String, Any>): AnimationConfig {
        val entradaStr = data["entrada"] as? String ?: "fadeIn"
        val entrada = AnimationType.values().find { it.cssClass == entradaStr } ?: AnimationType.FADE_IN

        val salidaStr = data["salida"] as? String ?: "fadeOut"
        val salida = AnimationType.values().find { it.cssClass == salidaStr } ?: AnimationType.FADE_OUT

        val easingStr = data["easing"] as? String ?: "ease-in-out"
        val easing = EasingType.values().find { it.cssValue == easingStr } ?: EasingType.EASE_IN_OUT

        return AnimationConfig(
            entrada = entrada,
            salida = salida,
            duracion = data["duracion"] as? Int ?: 300,
            delay = data["delay"] as? Int ?: 0,
            easing = easing
        )
    }

    private fun parsePublicidadConfig(data: Map<String, Any>): PublicidadConfig {
        val posicionData = data["posicion"] as? Map<String, Any> ?: emptyMap()
        val tamañoData = data["tamaño"] as? Map<String, Any> ?: emptyMap()
        val animacionData = data["animacion"] as? Map<String, Any> ?: emptyMap()

        return PublicidadConfig(
            mostrar = data["mostrar"] as? Boolean ?: false,
            url = data["url"] as? String ?: "",
            posicion = Position(
                x = posicionData["x"] as? Int ?: 6,
                y = posicionData["y"] as? Int ?: 1010
            ),
            tamaño = Dimensions(
                width = tamañoData["width"] ?: "auto",
                height = tamañoData["height"] as? Int ?: 70
            ),
            animacion = parseAnimationConfig(animacionData)
        )
    }

    private fun parseTimingConfig(data: Map<String, Any>): TimingConfig {
        val secuenciaData = data["secuencia"] as? Map<String, Any> ?: emptyMap()

        return TimingConfig(
            duracionDisplay = data["duracion_display"] as? Int ?: 7000,
            autoHide = data["auto_hide"] as? Boolean ?: true,
            secuencia = SecuenciaConfig(
                logoPrimero = secuenciaData["logo_primero"] as? Boolean ?: true,
                intervaloEntreElementos = secuenciaData["intervalo_entre_elementos"] as? Int ?: 100
            )
        )
    }

    private fun parsePresetsConfig(data: Map<String, Any>): PresetsConfig {
        @Suppress("UNCHECKED_CAST")
        val disponibles = data["disponibles"] as? Map<String, String> ?: mapOf(
            "estandar" to "Configuración estándar broadcast",
            "noticias" to "Optimizado para noticias",
            "deportes" to "Diseño dinámico deportivo",
            "corporativo" to "Estilo corporativo minimalista"
        )

        return PresetsConfig(
            actual = data["actual"] as? String ?: "estandar",
            disponibles = disponibles
        )
    }
}

