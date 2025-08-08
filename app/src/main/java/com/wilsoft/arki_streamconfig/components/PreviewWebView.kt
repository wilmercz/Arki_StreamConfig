// Archivo: components/PreviewWebView.kt
package com.wilsoft.arki_streamconfig.components

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.wilsoft.arki_streamconfig.models.LowerThirdConfig
import com.wilsoft.arki_streamconfig.utilidades.FirebaseDataStructure
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun PreviewWebView(
    config: LowerThirdConfig,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isWebViewReady by remember { mutableStateOf(false) }

    // Actualizar configuración cuando cambie
    LaunchedEffect(config, isWebViewReady) {
        if (isWebViewReady) {
            webView?.let { wv ->
                updateLowerThirdConfig(wv, config)
            }
        }
    }

    // Actualizar visibilidad cuando cambie
    LaunchedEffect(isVisible, isWebViewReady) {
        if (isWebViewReady) {
            webView?.let { wv ->
                setLowerThirdVisibility(wv, isVisible)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Barra de controles
            PreviewControlBar(
                isVisible = isVisible,
                onVisibilityToggle = onVisibilityChange,
                onRefresh = {
                    webView?.let { wv ->
                        wv.reload()
                    }
                },
                onFullscreen = {
                    // Implementar pantalla completa
                }
            )

            // WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webView = this
                        setupWebView(this) {
                            isWebViewReady = true
                        }
                        loadPreviewHTML(this)
                    }
                }
            )
        }
    }
}

@Composable
private fun PreviewControlBar(
    isVisible: Boolean,
    onVisibilityToggle: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onFullscreen: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎬 Vista Previa en Tiempo Real",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onVisibilityToggle(!isVisible) }
                ) {
                    Icon(
                        if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isVisible) "Ocultar" else "Mostrar",
                        tint = if (isVisible)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }

                IconButton(onClick = onFullscreen) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Pantalla completa")
                }
            }
        }
    }
}

private fun setupWebView(webView: WebView, onReady: () -> Unit) {
    webView.apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onReady()
            }
        }

        addJavascriptInterface(
            PreviewWebInterface(),
            "AndroidInterface"
        )
    }
}

private fun loadPreviewHTML(webView: WebView) {
    val htmlContent = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lower Thirds Preview</title>
            <style>
                ${getLowerThirdCSS()}
            </style>
        </head>
        <body>
            <div id="preview-container">
                <div id="stream-background">
                    <!-- Simulación de video de fondo -->
                    <div class="video-placeholder">
                        <h2>STREAM EN VIVO</h2>
                        <p>Vista previa del Lower Third</p>
                    </div>
                </div>
                
                <!-- Lower Third Container -->
                <div id="lower-third-container" class="lower-third-hidden">
                    <!-- Logo -->
                    <div id="logo-element" class="lt-element">
                        <div id="logo-background">
                            <img id="logo-image" src="" alt="Logo" />
                        </div>
                    </div>

                    <!-- Texto Principal -->
                    <div id="texto-principal-element" class="lt-element">
                        <div id="texto-principal-background">
                            <span id="texto-principal-content"></span>
                        </div>
                    </div>

                    <!-- Texto Secundario -->
                    <div id="texto-secundario-element" class="lt-element">
                        <div id="texto-secundario-background">
                            <span id="texto-secundario-content"></span>
                        </div>
                    </div>

                    <!-- Tema -->
                    <div id="tema-element" class="lt-element">
                        <div id="tema-background">
                            <span id="tema-content"></span>
                        </div>
                    </div>

                    <!-- Publicidad -->
                    <div id="publicidad-element" class="lt-element">
                        <img id="publicidad-image" src="" alt="Publicidad" />
                    </div>
                </div>
            </div>

            <script>
                ${getLowerThirdJavaScript()}
            </script>
        </body>
        </html>
    """.trimIndent()

    webView.loadDataWithBaseURL(
        "file:///android_asset/",
        htmlContent,
        "text/html",
        "UTF-8",
        null
    )
}

private fun updateLowerThirdConfig(webView: WebView, config: LowerThirdConfig) {
    val configJson = try {
        Json.encodeToString(FirebaseDataStructure.toLowerThirdFirebaseFormat(config))
    } catch (e: Exception) {
        "{}"
    }

    val javascript = """
        if (typeof updateConfiguration === 'function') {
            updateConfiguration($configJson);
        }
    """.trimIndent()

    webView.evaluateJavascript(javascript, null)
}

private fun setLowerThirdVisibility(webView: WebView, isVisible: Boolean) {
    val javascript = """
        if (typeof setVisibility === 'function') {
            setVisibility($isVisible);
        }
    """.trimIndent()

    webView.evaluateJavascript(javascript, null)
}

class PreviewWebInterface {
    @JavascriptInterface
    fun log(message: String) {
        println("WebView Log: $message")
    }

    @JavascriptInterface
    fun onAnimationComplete(elementId: String, animationType: String) {
        println("Animation Complete: $elementId - $animationType")
    }
}

private fun getLowerThirdCSS(): String = """
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        font-family: Arial, sans-serif;
        overflow: hidden;
        background: #000;
    }

    #preview-container {
        position: relative;
        width: 100vw;
        height: 100vh;
        background: #000;
    }

    #stream-background {
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, #1e3c72, #2a5298);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .video-placeholder {
        text-align: center;
        color: white;
        opacity: 0.3;
    }

    .video-placeholder h2 {
        font-size: 3rem;
        margin-bottom: 1rem;
    }

    .video-placeholder p {
        font-size: 1.5rem;
    }

    #lower-third-container {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 200px;
        pointer-events: none;
        z-index: 1000;
    }

    .lower-third-hidden {
        opacity: 0;
    }

    .lower-third-visible {
        opacity: 1;
        transition: opacity 0.3s ease-in-out;
    }

    .lt-element {
        position: absolute;
        transition: all 0.3s ease-in-out;
    }

    .lt-element.hidden {
        opacity: 0;
        transform: translateX(-100%);
    }

    .lt-element.visible {
        opacity: 1;
        transform: translateX(0);
    }

    /* Logo Styles */
    #logo-element {
        z-index: 1001;
    }

    #logo-background {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        overflow: hidden;
    }

    #logo-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    /* Text Element Base Styles */
    .text-element-background {
        display: inline-block;
        color: white;
        font-weight: bold;
        white-space: nowrap;
        box-shadow: 0 2px 10px rgba(0,0,0,0.3);
    }

    /* Texto Principal */
    #texto-principal-element {
        z-index: 1002;
    }

    #texto-principal-background {
        extend: .text-element-background;
    }

    /* Texto Secundario */
    #texto-secundario-element {
        z-index: 1003;
    }

    #texto-secundario-background {
        extend: .text-element-background;
    }

    /* Tema */
    #tema-element {
        z-index: 1004;
    }

    #tema-background {
        extend: .text-element-background;
    }

    /* Publicidad */
    #publicidad-element {
        z-index: 1005;
    }

    #publicidad-image {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(0,0,0,0.3);
    }

    /* Animaciones */
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }

    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }

    @keyframes slideInLeft {
        from { transform: translateX(-100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }

    @keyframes slideOutLeft {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(-100%); opacity: 0; }
    }

    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }

    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }

    @keyframes slideInTop {
        from { transform: translateY(-100%); opacity: 0; }
        to { transform: translateY(0); opacity: 1; }
    }

    @keyframes slideOutTop {
        from { transform: translateY(0); opacity: 1; }
        to { transform: translateY(-100%); opacity: 0; }
    }

    @keyframes slideInBottom {
        from { transform: translateY(100%); opacity: 0; }
        to { transform: translateY(0); opacity: 1; }
    }

    @keyframes slideOutBottom {
        from { transform: translateY(0); opacity: 1; }
        to { transform: translateY(100%); opacity: 0; }
    }

    @keyframes zoomIn {
        from { transform: scale(0); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }

    @keyframes zoomOut {
        from { transform: scale(1); opacity: 1; }
        to { transform: scale(0); opacity: 0; }
    }

    /* Animation Classes */
    .animate-fadeIn {
        animation: fadeIn var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-fadeOut {
        animation: fadeOut var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideInLeft {
        animation: slideInLeft var(--animation-duration, 600ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideOutLeft {
        animation: slideOutLeft var(--animation-duration, 600ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideInRight {
        animation: slideInRight var(--animation-duration, 600ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideOutRight {
        animation: slideOutRight var(--animation-duration, 600ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideInTop {
        animation: slideInTop var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideOutTop {
        animation: slideOutTop var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideInBottom {
        animation: slideInBottom var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-slideOutBottom {
        animation: slideOutBottom var(--animation-duration, 300ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-zoomIn {
        animation: zoomIn var(--animation-duration, 400ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    .animate-zoomOut {
        animation: zoomOut var(--animation-duration, 400ms) var(--animation-easing, ease-in-out) var(--animation-delay, 0ms) forwards;
    }

    /* Responsive adjustments */
    @media (max-width: 768px) {
        #lower-third-container {
            height: 150px;
        }
        
        .text-element-background {
            font-size: 0.8em;
            padding: 0.4em 0.8em;
        }
        
        #logo-background {
            width: 40px;
            height: 40px;
        }
    }
""".trimIndent()

private fun getLowerThirdJavaScript(): String = """
    class LowerThirdManager {
        constructor() {
            this.config = null;
            this.isVisible = false;
            this.elements = {
                container: document.getElementById('lower-third-container'),
                logo: document.getElementById('logo-element'),
                logoBackground: document.getElementById('logo-background'),
                logoImage: document.getElementById('logo-image'),
                textoPrincipal: document.getElementById('texto-principal-element'),
                textoPrincipalBackground: document.getElementById('texto-principal-background'),
                textoPrincipalContent: document.getElementById('texto-principal-content'),
                textoSecundario: document.getElementById('texto-secundario-element'),
                textoSecundarioBackground: document.getElementById('texto-secundario-background'),
                textoSecundarioContent: document.getElementById('texto-secundario-content'),
                tema: document.getElementById('tema-element'),
                temaBackground: document.getElementById('tema-background'),
                temaContent: document.getElementById('tema-content'),
                publicidad: document.getElementById('publicidad-element'),
                publicidadImage: document.getElementById('publicidad-image')
            };
            
            this.animationTimeouts = new Map();
            this.init();
        }

        init() {
            console.log('Lower Third Manager initialized');
            if (typeof AndroidInterface !== 'undefined') {
                AndroidInterface.log('Lower Third Manager initialized');
            }
        }

        updateConfiguration(configData) {
            this.config = configData;
            console.log('Configuration updated:', configData);
            
            if (typeof AndroidInterface !== 'undefined') {
                AndroidInterface.log('Configuration updated');
            }

            this.applyConfiguration();
        }

        applyConfiguration() {
            if (!this.config) return;

            // Aplicar configuración del logo
            this.applyLogoConfiguration();
            
            // Aplicar configuración de textos
            this.applyTextConfiguration('TEXTO_PRINCIPAL', this.elements.textoPrincipal, this.elements.textoPrincipalBackground, this.elements.textoPrincipalContent);
            this.applyTextConfiguration('TEXTO_SECUNDARIO', this.elements.textoSecundario, this.elements.textoSecundarioBackground, this.elements.textoSecundarioContent);
            this.applyTextConfiguration('TEMA', this.elements.tema, this.elements.temaBackground, this.elements.temaContent);
            
            // Aplicar configuración de publicidad
            this.applyPublicidadConfiguration();
        }

        applyLogoConfiguration() {
            const logoConfig = this.config.LOGO;
            if (!logoConfig) return;

            const element = this.elements.logo;
            const background = this.elements.logoBackground;
            const image = this.elements.logoImage;

            // Posición
            if (logoConfig.simple && logoConfig.simple.posicion) {
                element.style.left = logoConfig.simple.posicion.x + 'px';
                element.style.bottom = (1080 - logoConfig.simple.posicion.y) + 'px';
            }

            // Tamaño
            if (logoConfig.simple && logoConfig.simple.tamaño) {
                const size = logoConfig.simple.tamaño;
                background.style.width = size.width + 'px';
                background.style.height = size.height + 'px';
            }

            // Fondo
            if (logoConfig.simple && logoConfig.simple.fondo) {
                const fondo = logoConfig.simple.fondo;
                background.style.backgroundColor = fondo.color;
                background.style.opacity = fondo.opacidad;
            }

            // URL de la imagen
            if (logoConfig.simple && logoConfig.simple.url) {
                image.src = logoConfig.simple.url;
                image.style.display = 'block';
            }

            // Visibilidad
            element.style.display = logoConfig.mostrar ? 'block' : 'none';
        }

        applyTextConfiguration(configKey, element, background, content) {
            const textConfig = this.config[configKey];
            if (!textConfig) return;

            // Contenido
            if (textConfig.contenido) {
                content.textContent = textConfig.contenido;
            }

            // Posición
            if (textConfig.posicion) {
                element.style.left = textConfig.posicion.x + 'px';
                element.style.bottom = (1080 - textConfig.posicion.y) + 'px';
            }

            // Tipografía
            if (textConfig.tipografia) {
                const tipo = textConfig.tipografia;
                content.style.fontFamily = tipo.familia || 'Arial';
                content.style.fontSize = (tipo.tamaño || 18) + 'px';
                content.style.fontWeight = tipo.peso || 400;
                content.style.fontStyle = tipo.estilo || 'normal';
                content.style.textTransform = tipo.transformacion || 'none';
            }

            // Fondo
            if (textConfig.fondo) {
                const fondo = textConfig.fondo;
                background.style.backgroundColor = fondo.color;
                background.style.opacity = fondo.opacidad || 1;
                background.style.borderRadius = fondo.border_radius || '0px';
                
                if (fondo.padding) {
                    const p = fondo.padding;
                    background.style.padding = `${'$'}{p.top}px ${'$'}{p.right}px ${'$'}{p.bottom}px ${'$'}{p.left}px`;
                }
            }

            // Color de texto
            if (textConfig.texto && textConfig.texto.color) {
                content.style.color = textConfig.texto.color;
            }

            // Sombra de texto
            if (textConfig.texto && textConfig.texto.sombra && textConfig.texto.sombra.mostrar) {
                const sombra = textConfig.texto.sombra;
                const offsetX = sombra.offset ? sombra.offset.x : 1;
                const offsetY = sombra.offset ? sombra.offset.y : 1;
                content.style.textShadow = `${'$'}{offsetX}px ${'$'}{offsetY}px ${'$'}{sombra.blur}px ${'$'}{sombra.color}`;
            }

            // Visibilidad
            element.style.display = textConfig.mostrar ? 'block' : 'none';
        }

        applyPublicidadConfiguration() {
            const pubConfig = this.config.PUBLICIDAD;
            if (!pubConfig) return;

            const element = this.elements.publicidad;
            const image = this.elements.publicidadImage;

            // Posición
            if (pubConfig.posicion) {
                element.style.left = pubConfig.posicion.x + 'px';
                element.style.bottom = (1080 - pubConfig.posicion.y) + 'px';
            }

            // Tamaño
            if (pubConfig.tamaño) {
                if (pubConfig.tamaño.width !== 'auto') {
                    image.style.width = pubConfig.tamaño.width + 'px';
                }
                if (pubConfig.tamaño.height !== 'auto') {
                    image.style.height = pubConfig.tamaño.height + 'px';
                }
            }

            // URL
            if (pubConfig.url) {
                image.src = pubConfig.url;
            }

            // Visibilidad
            element.style.display = pubConfig.mostrar ? 'block' : 'none';
        }

        setVisibility(visible) {
            this.isVisible = visible;
            
            if (visible) {
                this.showLowerThird();
            } else {
                this.hideLowerThird();
            }
        }

        showLowerThird() {
            if (!this.config) return;

            this.elements.container.classList.remove('lower-third-hidden');
            this.elements.container.classList.add('lower-third-visible');

            const timing = this.config.TIMING || {};
            const secuencia = timing.secuencia || {};
            
            let delay = 0;
            const intervalo = secuencia.intervalo_entre_elementos || 100;

            // Mostrar elementos en secuencia
            if (secuencia.logo_primero && this.config.LOGO && this.config.LOGO.mostrar) {
                this.showElement('logo', this.config.LOGO.simple, delay);
                delay += intervalo;
            }

            if (this.config.TEXTO_PRINCIPAL && this.config.TEXTO_PRINCIPAL.mostrar) {
                this.showElement('textoPrincipal', this.config.TEXTO_PRINCIPAL, delay);
                delay += intervalo;
            }

            if (this.config.TEXTO_SECUNDARIO && this.config.TEXTO_SECUNDARIO.mostrar) {
                this.showElement('textoSecundario', this.config.TEXTO_SECUNDARIO, delay);
                delay += intervalo;
            }

            if (this.config.TEMA && this.config.TEMA.mostrar) {
                this.showElement('tema', this.config.TEMA, delay);
                delay += intervalo;
            }

            if (this.config.PUBLICIDAD && this.config.PUBLICIDAD.mostrar) {
                this.showElement('publicidad', this.config.PUBLICIDAD, delay);
            }

            // Auto-hide si está configurado
            if (timing.auto_hide && timing.duracion_display) {
                setTimeout(() => {
                    this.hideLowerThird();
                }, timing.duracion_display);
            }
        }

        hideLowerThird() {
            // Ocultar todos los elementos
            Object.keys(this.elements).forEach(key => {
                if (key !== 'container') {
                    this.hideElement(key, this.config ? this.config[key.toUpperCase()] || {} : {});
                }
            });

            // Ocultar contenedor después de las animaciones
            setTimeout(() => {
                this.elements.container.classList.remove('lower-third-visible');
                this.elements.container.classList.add('lower-third-hidden');
            }, 600);
        }

        showElement(elementKey, config, delay = 0) {
            const element = this.elements[elementKey];
            if (!element) return;

            const animationConfig = config.animacion || {};
            const duration = animationConfig.duracion || 300;
            const easing = animationConfig.easing || 'ease-in-out';
            const animationType = animationConfig.entrada || 'fadeIn';

            // Limpiar animaciones previas
            this.clearElementAnimations(element);

            // Configurar variables CSS para la animación
            element.style.setProperty('--animation-duration', duration + 'ms');
            element.style.setProperty('--animation-easing', easing);
            element.style.setProperty('--animation-delay', delay + 'ms');

            setTimeout(() => {
                element.classList.remove('hidden');
                element.classList.add('visible');
                element.classList.add(`animate-${'$'}{animationType}`);

                // Limpiar clase de animación después de que termine
                setTimeout(() => {
                    element.classList.remove(`animate-${'$'}{animationType}`);
                    
                    if (typeof AndroidInterface !== 'undefined') {
                        AndroidInterface.onAnimationComplete(elementKey, animationType);
                    }
                }, duration);
            }, delay);
        }

        hideElement(elementKey, config, delay = 0) {
            const element = this.elements[elementKey];
            if (!element) return;

            const animationConfig = config.animacion || {};
            const duration = animationConfig.duracion || 300;
            const easing = animationConfig.easing || 'ease-in-out';
            const animationType = animationConfig.salida || 'fadeOut';

            // Limpiar animaciones previas
            this.clearElementAnimations(element);

            // Configurar variables CSS para la animación
            element.style.setProperty('--animation-duration', duration + 'ms');
            element.style.setProperty('--animation-easing', easing);
            element.style.setProperty('--animation-delay', delay + 'ms');

            setTimeout(() => {
                element.classList.add(`animate-${'$'}{animationType}`);

                // Ocultar elemento después de que termine la animación
                setTimeout(() => {
                    element.classList.remove('visible');
                    element.classList.add('hidden');
                    element.classList.remove(`animate-${'$'}{animationType}`);
                    
                    if (typeof AndroidInterface !== 'undefined') {
                        AndroidInterface.onAnimationComplete(elementKey, animationType);
                    }
                }, duration);
            }, delay);
        }

        clearElementAnimations(element) {
            const animationClasses = Array.from(element.classList).filter(className => 
                className.startsWith('animate-')
            );
            element.classList.remove(...animationClasses);
        }

        // Funciones de rotación de logos
        startLogoRotation() {
            if (!this.config || !this.config.LOGO || this.config.LOGO.modo !== 'rotacion') {
                return;
            }

            const rotationConfig = this.config.LOGO.rotacion;
            if (!rotationConfig || !rotationConfig.logos || rotationConfig.logos.length === 0) {
                return;
            }

            let currentIndex = 0;
            const logos = rotationConfig.logos;

            const rotateLogos = () => {
                if (!rotationConfig.ciclo_continuo && currentIndex >= logos.length) {
                    return;
                }

                const currentLogo = logos[currentIndex % logos.length];
                this.elements.logoImage.src = currentLogo.url;

                currentIndex++;
                
                setTimeout(rotateLogos, currentLogo.duracion || 4000);
            };

            // Comenzar la rotación
            rotateLogos();
        }
    }

    // Instancia global del manager
    const lowerThirdManager = new LowerThirdManager();

    // Funciones globales para interfaz con Android
    function updateConfiguration(config) {
        lowerThirdManager.updateConfiguration(config);
    }

    function setVisibility(visible) {
        lowerThirdManager.setVisibility(visible);
    }

    function showLowerThird() {
        lowerThirdManager.showLowerThird();
    }

    function hideLowerThird() {
        lowerThirdManager.hideLowerThird();
    }

    // Inicialización cuando el DOM esté listo
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Lower Third system ready');
        if (typeof AndroidInterface !== 'undefined') {
            AndroidInterface.log('Lower Third system ready');
        }
    });
""".trimIndent()