// Archivo: components/CoordinateSystem.kt
package com.wilsoft.arki_streamconfig.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilsoft.arki_streamconfig.models.*
import kotlin.math.roundToInt

@Composable
fun CoordinateSystem(
    lowerThirdConfig: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedElement by remember { mutableStateOf<String?>(null) }
    var showGrid by remember { mutableStateOf(true) }
    var showSafeMargins by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📐 Sistema de Coordenadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        onClick = { showGrid = !showGrid },
                        label = { Text("Grid", fontSize = 12.sp) },
                        selected = showGrid,
                        leadingIcon = {
                            Icon(Icons.Default.GridOn, contentDescription = null)
                        }
                    )

                    FilterChip(
                        onClick = { showSafeMargins = !showSafeMargins },
                        label = { Text("Safe Zone", fontSize = 12.sp) },
                        selected = showSafeMargins,
                        leadingIcon = {
                            Icon(Icons.Default.CropFree, contentDescription = null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas de coordenadas
            CoordinateCanvas(
                config = lowerThirdConfig,
                onConfigChange = onConfigChange,
                selectedElement = selectedElement,
                onElementSelect = { selectedElement = it },
                showGrid = showGrid,
                showSafeMargins = showSafeMargins,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Panel de información del elemento seleccionado
            selectedElement?.let { element ->
                ElementInfoPanel(
                    elementName = element,
                    config = lowerThirdConfig,
                    onConfigChange = onConfigChange,
                    onClearSelection = { selectedElement = null }
                )
            }

            // Información de resolución
            ResolutionInfo(lowerThirdConfig.layout.canvasVirtual)
        }
    }
}

@Composable
private fun CoordinateCanvas(
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    selectedElement: String?,
    onElementSelect: (String) -> Unit,
    showGrid: Boolean,
    showSafeMargins: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    // Implementar lógica de drag para mover elementos
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val virtualWidth = config.layout.canvasVirtual.width as Int
                    val virtualHeight = config.layout.canvasVirtual.height as Int

                    val scaleX = virtualWidth.toFloat() / canvasWidth
                    val scaleY = virtualHeight.toFloat() / canvasHeight

                    val newX = (change.position.x * scaleX).roundToInt()
                    val newY = (change.position.y * scaleY).roundToInt()

                    selectedElement?.let { element ->
                        when (element) {
                            "logo" -> {
                                val newLogoConfig = config.logo.simple.copy(
                                    posicion = Position(newX, newY)
                                )
                                onConfigChange(
                                    config.copy(
                                        logo = config.logo.copy(simple = newLogoConfig)
                                    )
                                )
                            }
                            "textoPrincipal" -> {
                                val newTextConfig = config.textoPrincipal.copy(
                                    posicion = Position(newX, newY)
                                )
                                onConfigChange(config.copy(textoPrincipal = newTextConfig))
                            }
                            "textoSecundario" -> {
                                val newTextConfig = config.textoSecundario.copy(
                                    posicion = Position(newX, newY)
                                )
                                onConfigChange(config.copy(textoSecundario = newTextConfig))
                            }
                            "tema" -> {
                                val newTextConfig = config.tema.copy(
                                    posicion = Position(newX, newY)
                                )
                                onConfigChange(config.copy(tema = newTextConfig))
                            }
                        }
                    }
                }
            }
    ) {
        // Dibujar grid
        if (showGrid) {
            drawGrid(this, size.width, size.height)
        }

        // Dibujar safe margins
        if (showSafeMargins) {
            drawSafeMargins(this, size.width, size.height)
        }

        // Dibujar elementos
        drawElements(
            drawScope = this,
            config = config,
            canvasWidth = size.width,
            canvasHeight = size.height,
            selectedElement = selectedElement,
            onElementClick = onElementSelect
        )
    }
}

private fun drawGrid(drawScope: DrawScope, width: Float, height: Float) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val gridSpacing = 50f

    with(drawScope) {
        // Líneas verticales
        var x = gridSpacing
        while (x < width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx() // ✅ ahora funciona
            )
            x += gridSpacing
        }

        // Líneas horizontales
        var y = gridSpacing
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx() // ✅ ahora funciona
            )
            y += gridSpacing
        }
    }
}

private fun drawSafeMargins(drawScope: DrawScope, width: Float, height: Float) {
    val safeMarginColor = Color.Yellow.copy(alpha = 0.5f)

    with(drawScope) {
        val marginSize = 20.dp.toPx()

        drawRect(
            color = safeMarginColor,
            topLeft = Offset(marginSize, marginSize),
            size = androidx.compose.ui.geometry.Size(
                width - 2 * marginSize,
                height - 2 * marginSize
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

private fun drawElements(
    drawScope: DrawScope,
    config: LowerThirdConfig,
    canvasWidth: Float,
    canvasHeight: Float,
    selectedElement: String?,
    onElementClick: (String) -> Unit
) {
    val virtualWidth = config.layout.canvasVirtual.width as Int
    val virtualHeight = config.layout.canvasVirtual.height as Int

    val scaleX = canvasWidth / virtualWidth
    val scaleY = canvasHeight / virtualHeight

    with(drawScope) {
        // Dibujar logo
        if (config.logo.mostrar) {
            val logoPos = config.logo.simple.posicion
            val logoSize = config.logo.simple.tamaño
            val screenX = logoPos.x * scaleX
            val screenY = logoPos.y * scaleY
            val screenWidth = (logoSize.width as Int) * scaleX
            val screenHeight = (logoSize.height as Int) * scaleY

            drawRect(
                color = if (selectedElement == "logo") Color.Red else Color.Blue,
                topLeft = Offset(screenX, screenY),
                size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Texto principal
        if (config.textoPrincipal.mostrar) {
            val textPos = config.textoPrincipal.posicion
            val screenX = textPos.x * scaleX
            val screenY = textPos.y * scaleY
            val estimatedWidth = config.textoPrincipal.contenido.length * 10 * scaleX
            val estimatedHeight = config.textoPrincipal.tipografia.tamaño * scaleY

            drawRect(
                color = if (selectedElement == "textoPrincipal") Color.Red else Color.Green,
                topLeft = Offset(screenX, screenY),
                size = androidx.compose.ui.geometry.Size(estimatedWidth, estimatedHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Texto secundario
        if (config.textoSecundario.mostrar) {
            val textPos = config.textoSecundario.posicion
            val screenX = textPos.x * scaleX
            val screenY = textPos.y * scaleY
            val estimatedWidth = config.textoSecundario.contenido.length * 8 * scaleX
            val estimatedHeight = config.textoSecundario.tipografia.tamaño * scaleY

            drawRect(
                color = if (selectedElement == "textoSecundario") Color.Red else Color.Magenta,
                topLeft = Offset(screenX, screenY),
                size = androidx.compose.ui.geometry.Size(estimatedWidth, estimatedHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Tema
        if (config.tema.mostrar) {
            val textPos = config.tema.posicion
            val screenX = textPos.x * scaleX
            val screenY = textPos.y * scaleY
            val estimatedWidth = config.tema.contenido.length * 12 * scaleX
            val estimatedHeight = config.tema.tipografia.tamaño * scaleY

            drawRect(
                color = if (selectedElement == "tema") Color.Red else Color.Cyan,
                topLeft = Offset(screenX, screenY),
                size = androidx.compose.ui.geometry.Size(estimatedWidth, estimatedHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}


@Composable
private fun ElementInfoPanel(
    elementName: String,
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    onClearSelection: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${getElementDisplayName(elementName)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            val position = getElementPosition(elementName, config)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = position.x.toString(),
                    onValueChange = { newX ->
                        newX.toIntOrNull()?.let { x ->
                            updateElementPosition(elementName, Position(x, position.y), config, onConfigChange)
                        }
                    },
                    label = { Text("X") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = position.y.toString(),
                    onValueChange = { newY ->
                        newY.toIntOrNull()?.let { y ->
                            updateElementPosition(elementName, Position(position.x, y), config, onConfigChange)
                        }
                    },
                    label = { Text("Y") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResolutionInfo(canvasVirtual: Dimensions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AspectRatio,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Resolución: ${canvasVirtual.width} x ${canvasVirtual.height}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getElementDisplayName(elementName: String): String = when (elementName) {
    "logo" -> "Logo"
    "textoPrincipal" -> "Texto Principal"
    "textoSecundario" -> "Texto Secundario"
    "tema" -> "Tema"
    "publicidad" -> "Publicidad"
    else -> elementName
}

private fun getElementPosition(elementName: String, config: LowerThirdConfig): Position = when (elementName) {
    "logo" -> config.logo.simple.posicion
    "textoPrincipal" -> config.textoPrincipal.posicion
    "textoSecundario" -> config.textoSecundario.posicion
    "tema" -> config.tema.posicion
    else -> Position(0, 0)
}

private fun updateElementPosition(
    elementName: String,
    newPosition: Position,
    config: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit
) {
    when (elementName) {
        "logo" -> {
            val newLogoConfig = config.logo.simple.copy(posicion = newPosition)
            onConfigChange(config.copy(logo = config.logo.copy(simple = newLogoConfig)))
        }
        "textoPrincipal" -> {
            onConfigChange(config.copy(textoPrincipal = config.textoPrincipal.copy(posicion = newPosition)))
        }
        "textoSecundario" -> {
            onConfigChange(config.copy(textoSecundario = config.textoSecundario.copy(posicion = newPosition)))
        }
        "tema" -> {
            onConfigChange(config.copy(tema = config.tema.copy(posicion = newPosition)))
        }
    }
}

// Archivo: components/PresetsManager.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsManager(
    currentConfig: LowerThirdConfig,
    onConfigChange: (LowerThirdConfig) -> Unit,
    onSavePreset: (String, LowerThirdConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(currentConfig.presets.actual) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎨 Gestión de Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showSaveDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 12.sp)
                }
            }

            // Selector de presets
            Text(
                text = "Preset Actual:",
                style = MaterialTheme.typography.titleSmall
            )

            PresetGrid(
                presets = PresetTemplates.getAllPresets(),
                currentPreset = selectedPreset,
                onPresetSelect = { presetName ->
                    selectedPreset = presetName
                    val newConfig = PresetTemplates.getAllPresets()[presetName]
                    newConfig?.let { config ->
                        onConfigChange(config.copy(
                            presets = currentConfig.presets.copy(actual = presetName)
                        ))
                    }
                }
            )

            // Información del preset actual
            currentConfig.presets.disponibles[selectedPreset]?.let { description ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📝 $selectedPreset",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Diálogo para guardar preset
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar Preset") },
            text = {
                Column {
                    Text("Ingresa un nombre para el nuevo preset:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Nombre del preset") },
                        placeholder = { Text("Ej: Mi Preset Personalizado") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSavePreset(newPresetName, currentConfig)
                            showSaveDialog = false
                            newPresetName = ""
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        newPresetName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PresetGrid(
    presets: Map<String, LowerThirdConfig>,
    currentPreset: String,
    onPresetSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (name, config) ->
                    PresetCard(
                        name = name,
                        config = config,
                        isSelected = name == currentPreset,
                        onClick = { onPresetSelect(name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Rellenar espacio si solo hay un elemento en la fila
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    name: String,
    config: LowerThirdConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (name) {
                        "estandar" -> Icons.Default.Settings
                        "noticias" -> Icons.Default.NewReleases
                        "deportes" -> Icons.Default.SportsFootball
                        "corporativo" -> Icons.Default.Business
                        else -> Icons.Default.Palette
                    },
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview visual miniatura
            PresetPreview(
                config = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun PresetPreview(
    config: LowerThirdConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // Simulación visual del preset
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Logo simulado
            if (config.logo.mostrar) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            Color(android.graphics.Color.parseColor(config.logo.simple.fondo.color)),
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Texto simulado
            if (config.textoPrincipal.mostrar) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(60.dp)
                        .background(
                            Color(android.graphics.Color.parseColor(config.textoPrincipal.fondo.color)),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}