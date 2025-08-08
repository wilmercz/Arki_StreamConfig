// Archivo: components/AdvancedTextEditor.kt
package com.wilsoft.arki_streamconfig.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilsoft.arki_streamconfig.models.*
import com.wilsoft.arki_streamconfig.utilidades.toComposeFontWeight
import com.wilsoft.arki_streamconfig.utilidades.toLowerThirdFontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTextEditor(
    label: String,
    textConfig: TextConfig,
    onConfigChange: (TextConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con toggle de visibilidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = textConfig.mostrar,
                    onCheckedChange = {
                        onConfigChange(textConfig.copy(mostrar = it))
                    }
                )
            }

            if (textConfig.mostrar) {
                // Contenido del texto
                OutlinedTextField(
                    value = textConfig.contenido,
                    onValueChange = {
                        onConfigChange(textConfig.copy(contenido = it))
                    },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Configuración tipográfica
                TypographySection(
                    typography = textConfig.tipografia,
                    onTypographyChange = { newTypography ->
                        onConfigChange(textConfig.copy(tipografia = newTypography))
                    }
                )

                // Configuración de posición
                PositionSection(
                    position = textConfig.posicion,
                    onPositionChange = { newPosition ->
                        onConfigChange(textConfig.copy(posicion = newPosition))
                    }
                )

                // Configuración de colores
                ColorSection(
                    fondoConfig = textConfig.fondo,
                    textStyleConfig = textConfig.texto,
                    onFondoChange = { newFondo ->
                        onConfigChange(textConfig.copy(fondo = newFondo))
                    },
                    onTextStyleChange = { newTextStyle ->
                        onConfigChange(textConfig.copy(texto = newTextStyle))
                    }
                )

                // Configuración de animación
                AnimationSection(
                    animationConfig = textConfig.animacion,
                    onAnimationChange = { newAnimation ->
                        onConfigChange(textConfig.copy(animacion = newAnimation))
                    }
                )
            }
        }
    }
}

@Composable
private fun TypographySection(
    typography: TypographyConfig,
    onTypographyChange: (TypographyConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tipografía",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Familia de fuente
            OutlinedTextField(
                value = typography.familia,
                onValueChange = {
                    onTypographyChange(typography.copy(familia = it))
                },
                label = { Text("Fuente") },
                modifier = Modifier.weight(1f)
            )

            // Tamaño
            OutlinedTextField(
                value = typography.tamaño.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { size ->
                        onTypographyChange(typography.copy(tamaño = size))
                    }
                },
                label = { Text("Tamaño") },
                modifier = Modifier.width(80.dp)
            )
        }

        // Peso de fuente
        FontWeightSelector(
            currentWeight = typography.peso,
            onWeightChange = {
                onTypographyChange(
                    typography.copy(
                        peso = it
                    )
                )
            }
        )


        // Transformación de texto
        TextTransformSelector(
            currentTransform = typography.transformacion,
            onTransformChange = {
                onTypographyChange(typography.copy(transformacion = it))
            }
        )
    }
}

@Composable
private fun FontWeightSelector(
    currentWeight: com.wilsoft.arki_streamconfig.models.FontWeight,
    onWeightChange: (com.wilsoft.arki_streamconfig.models.FontWeight) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(com.wilsoft.arki_streamconfig.models.FontWeight.values()) { weight ->
            FilterChip(
                onClick = { onWeightChange(weight) },
                label = {
                    Text(
                        text = weight.displayName,
                        fontSize = 12.sp,
                        fontWeight = weight.toComposeFontWeight() // 👈 conversión aquí
                    )
                },
                selected = currentWeight == weight
            )
        }
    }
}



@Composable
private fun TextTransformSelector(
    currentTransform: TextTransform,
    onTransformChange: (TextTransform) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(TextTransform.values()) { transform ->
            FilterChip(
                onClick = { onTransformChange(transform) },
                label = {
                    Text(
                        text = transform.displayName,
                        fontSize = 12.sp
                    )
                },
                selected = currentTransform == transform
            )
        }
    }
}

@Composable
private fun PositionSection(
    position: Position,
    onPositionChange: (Position) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Posición",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = position.x.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { x ->
                        onPositionChange(position.copy(x = x))
                    }
                },
                label = { Text("X") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = position.y.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { y ->
                        onPositionChange(position.copy(y = y))
                    }
                },
                label = { Text("Y") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ColorSection(
    fondoConfig: FondoConfig,
    textStyleConfig: TextStyleConfig,
    onFondoChange: (FondoConfig) -> Unit,
    onTextStyleChange: (TextStyleConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Colores",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Color de fondo
            ColorPickerAdvanced(
                label = "Fondo",
                color = fondoConfig.color,
                onColorChange = {
                    onFondoChange(fondoConfig.copy(color = it))
                },
                modifier = Modifier.weight(1f)
            )

            // Color de texto
            ColorPickerAdvanced(
                label = "Texto",
                color = textStyleConfig.color,
                onColorChange = {
                    onTextStyleChange(textStyleConfig.copy(color = it))
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Opacidad del fondo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Opacidad:",
                modifier = Modifier.width(80.dp)
            )
            Slider(
                value = fondoConfig.opacidad,
                onValueChange = {
                    onFondoChange(fondoConfig.copy(opacidad = it))
                },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(fondoConfig.opacidad * 100).toInt()}%",
                modifier = Modifier.width(50.dp)
            )
        }

        // Border radius
        OutlinedTextField(
            value = fondoConfig.borderRadius,
            onValueChange = {
                onFondoChange(fondoConfig.copy(borderRadius = it))
            },
            label = { Text("Border Radius") },
            placeholder = { Text("Ej: 10px, 50%, 0px 20px 20px 0px") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AnimationSection(
    animationConfig: AnimationConfig,
    onAnimationChange: (AnimationConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Animaciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animación de entrada
            AnimationTypeSelector(
                label = "Entrada",
                currentAnimation = animationConfig.entrada,
                onAnimationChange = {
                    onAnimationChange(animationConfig.copy(entrada = it))
                },
                modifier = Modifier.weight(1f)
            )

            // Animación de salida
            AnimationTypeSelector(
                label = "Salida",
                currentAnimation = animationConfig.salida,
                onAnimationChange = {
                    onAnimationChange(animationConfig.copy(salida = it))
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Duración
            OutlinedTextField(
                value = animationConfig.duracion.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { duration ->
                        onAnimationChange(animationConfig.copy(duracion = duration))
                    }
                },
                label = { Text("Duración (ms)") },
                modifier = Modifier.weight(1f)
            )

            // Delay
            OutlinedTextField(
                value = animationConfig.delay.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { delay ->
                        onAnimationChange(animationConfig.copy(delay = delay))
                    }
                },
                label = { Text("Delay (ms)") },
                modifier = Modifier.weight(1f)
            )
        }

        // Easing
        EasingTypeSelector(
            currentEasing = animationConfig.easing,
            onEasingChange = {
                onAnimationChange(animationConfig.copy(easing = it))
            }
        )
    }
}

// Archivo: components/LogoModeSelector.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoModeSelector(
    logoConfig: LogoConfig,
    onConfigChange: (LogoConfig) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "🏷️ Configuración de Logo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = logoConfig.mostrar,
                    onCheckedChange = {
                        onConfigChange(logoConfig.copy(mostrar = it))
                    }
                )
            }

            if (logoConfig.mostrar) {
                // Selector de modo
                Text(
                    text = "Modo de Logo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LogoMode.values()) { mode ->
                        FilterChip(
                            onClick = { onConfigChange(logoConfig.copy(modo = mode)) },
                            label = { Text(mode.displayName) },
                            selected = logoConfig.modo == mode,
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mode) {
                                        LogoMode.SIMPLE -> Icons.Default.Circle
                                        LogoMode.ALIANZA -> Icons.Default.Group
                                        LogoMode.ROTACION -> Icons.Default.RotateRight
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Configuración específica por modo
                when (logoConfig.modo) {
                    LogoMode.SIMPLE -> {
                        LogoSimpleConfiguration(
                            config = logoConfig.simple,
                            onConfigChange = { newConfig ->
                                onConfigChange(logoConfig.copy(simple = newConfig))
                            }
                        )
                    }
                    LogoMode.ALIANZA -> {
                        LogoAlianzaConfiguration(
                            config = logoConfig.alianza,
                            onConfigChange = { newConfig ->
                                onConfigChange(logoConfig.copy(alianza = newConfig))
                            }
                        )
                    }
                    LogoMode.ROTACION -> {
                        LogoRotacionConfiguration(
                            config = logoConfig.rotacion,
                            onConfigChange = { newConfig ->
                                onConfigChange(logoConfig.copy(rotacion = newConfig))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoSimpleConfiguration(
    config: LogoSimpleConfig,
    onConfigChange: (LogoSimpleConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // URL del logo
        OutlinedTextField(
            value = config.url,
            onValueChange = { onConfigChange(config.copy(url = it)) },
            label = { Text("URL del Logo") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tamaño
            OutlinedTextField(
                value = config.tamaño.width.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { width ->
                        onConfigChange(config.copy(tamaño = config.tamaño.copy(width = width)))
                    }
                },
                label = { Text("Ancho") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = config.tamaño.height.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { height ->
                        onConfigChange(config.copy(tamaño = config.tamaño.copy(height = height)))
                    }
                },
                label = { Text("Alto") },
                modifier = Modifier.weight(1f)
            )
        }

        // Forma del logo
        Text(
            text = "Forma:",
            style = MaterialTheme.typography.titleSmall
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(LogoShape.values()) { shape ->
                FilterChip(
                    onClick = { onConfigChange(config.copy(forma = shape)) },
                    label = { Text(shape.displayName) },
                    selected = config.forma == shape
                )
            }
        }

        // Color de fondo del logo
        ColorPickerAdvanced(
            label = "Color de Fondo",
            color = config.fondo.color,
            onColorChange = {
                onConfigChange(config.copy(fondo = config.fondo.copy(color = it)))
            }
        )
    }
}

@Composable
private fun LogoAlianzaConfiguration(
    config: LogoAlianzaConfig,
    onConfigChange: (LogoAlianzaConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = config.url,
            onValueChange = { onConfigChange(config.copy(url = it)) },
            label = { Text("URL del Logo de Alianza") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = config.descripcion,
            onValueChange = { onConfigChange(config.copy(descripcion = it)) },
            label = { Text("Descripción de la Alianza") },
            placeholder = { Text("Canal 1 & Radio 2") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LogoRotacionConfiguration(
    config: LogoRotacionConfig,
    onConfigChange: (LogoRotacionConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logos en Rotación:")

            IconButton(
                onClick = {
                    val newLogo = LogoRotacionItem("", "Nuevo Logo", 4000)
                    onConfigChange(config.copy(logos = config.logos + newLogo))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar logo")
            }
        }

        config.logos.forEachIndexed { index, logo ->
            LogoRotacionItem(
                logo = logo,
                onLogoChange = { newLogo ->
                    val updatedLogos = config.logos.toMutableList()
                    updatedLogos[index] = newLogo
                    onConfigChange(config.copy(logos = updatedLogos))
                },
                onRemove = {
                    val updatedLogos = config.logos.toMutableList()
                    updatedLogos.removeAt(index)
                    onConfigChange(config.copy(logos = updatedLogos))
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ciclo continuo:")
            Switch(
                checked = config.cicloContinuo,
                onCheckedChange = {
                    onConfigChange(config.copy(cicloContinuo = it))
                }
            )
        }
    }
}

@Composable
private fun LogoRotacionItem(
    logo: LogoRotacionItem,
    onLogoChange: (LogoRotacionItem) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = logo.nombre,
                    onValueChange = { onLogoChange(logo.copy(nombre = it)) },
                    label = { Text("Nombre") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            OutlinedTextField(
                value = logo.url,
                onValueChange = { onLogoChange(logo.copy(url = it)) },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = logo.duracion.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { duration ->
                        onLogoChange(logo.copy(duracion = duration))
                    }
                },
                label = { Text("Duración (ms)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Archivo: components/ColorPickerAdvanced.kt
@Composable
fun ColorPickerAdvanced(
    label: String,
    color: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview del color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(color))
                        } catch (e: Exception) {
                            Color.Gray
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
            )

            // Input de texto para el color
            OutlinedTextField(
                value = color,
                onValueChange = onColorChange,
                placeholder = { Text("#000000") },
                modifier = Modifier.weight(1f)
            )
        }

        // Colores predefinidos
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                listOf(
                    "#1066FF", "#F08313", "#103264",
                    "#C41E3A", "#228B22", "#FF6B6B",
                    "#4ECDC4", "#45B7D1", "#96CEB4",
                    "#FFEAA7", "#DDA0DD", "#98D8C8"
                )
            ) { presetColor ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color(android.graphics.Color.parseColor(presetColor)),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable { onColorChange(presetColor) }
                )
            }
        }
    }
}

// Archivo: components/AnimationTypeSelector.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimationTypeSelector(
    label: String,
    currentAnimation: AnimationType,
    onAnimationChange: (AnimationType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentAnimation.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AnimationType.values().forEach { animation ->
                DropdownMenuItem(
                    text = { Text(animation.displayName) },
                    onClick = {
                        onAnimationChange(animation)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EasingTypeSelector(
    currentEasing: EasingType,
    onEasingChange: (EasingType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentEasing.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Easing") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            EasingType.values().forEach { easing ->
                DropdownMenuItem(
                    text = { Text(easing.displayName) },
                    onClick = {
                        onEasingChange(easing)
                        expanded = false
                    }
                )
            }
        }
    }
}