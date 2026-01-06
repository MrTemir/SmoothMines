package com.kiu.smoothmines.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ThemeDecorations(theme: MinesTheme) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (theme.type) {
            ThemeType.SAKURA -> FallingPetalsBackground(theme, Color(0xFFFFB7C5))
            ThemeType.LAVENDER -> FallingPetalsBackground(theme, Color(0xFFE6E6FA))
            ThemeType.FOREST -> FallingPetalsBackground(theme, Color(0xFFD4E157)) // Матча-листочки
            ThemeType.SPACE -> SpaceBackground(theme)
            ThemeType.AFTER_RAIN -> RainBackground(theme)
            ThemeType.ALPINE_SUNRISE -> MountainBackground(theme)
            ThemeType.SAND_DUNE -> DesertBackground(theme)
            ThemeType.DEEP_OCEAN -> OceanBackground(theme)
            else -> DefaultGradient(theme)
        }
    }
}

@Composable
fun SpaceBackground(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "Space")
    val nebulaAlpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "nebula"
    )

    // Генерируем звезды один раз для темы Space
    val stars = remember { List(80) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF02040A)) // Глубокий космос

        // Отрисовка звезд
        stars.forEach { pos ->
            drawCircle(
                color = Color.White.copy(alpha = Random.nextFloat() * 0.4f + 0.2f),
                radius = 1.dp.toPx(),
                center = Offset(pos.x * size.width, pos.y * size.height)
            )
        }

        // Плывущие туманности
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(theme.accent.copy(alpha = nebulaAlpha), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                radius = size.width * 0.8f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4A148C).copy(alpha = nebulaAlpha * 0.7f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.7f),
                radius = size.width * 0.6f
            )
        )
    }
}

@Composable
fun RainBackground(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "Rain")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rain_progress"
    )

    val rainDrops = remember { List(40) { Random.nextFloat() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        rainDrops.forEachIndexed { i, startX ->
            val p = (progress + (i.toFloat() / rainDrops.size)) % 1f
            val x = startX * size.width
            val y = p * size.height
            drawLine(
                color = theme.accent.copy(alpha = 0.2f),
                start = Offset(x, y),
                end = Offset(x - 2.dp.toPx(), y + 15.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MountainBackground(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "Mountains")
    val move1 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(25000, easing = LinearEasing)), label = "m1")
    val move2 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(45000, easing = LinearEasing)), label = "m2")

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawMountainLayer(move2, size, theme.accent.copy(alpha = 0.08f), 0.5f)
        drawMountainLayer(move1, size, theme.accent.copy(alpha = 0.12f), 0.7f)
    }
}

@Composable
fun FallingPetalsBackground(theme: MinesTheme, color: Color) {
    val transition = rememberInfiniteTransition(label = "Petals")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "petals"
    )

    val petals = remember { List(20) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        petals.forEachIndexed { i, pos ->
            val p = (progress + (i.toFloat() / petals.size)) % 1f
            val y = p * size.height
            val x = (pos.x * size.width) + sin(p * Math.PI * 4).toFloat() * 40f

            rotate(degrees = p * 360f + (i * 15), pivot = Offset(x, y)) {
                drawOval(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(x, y),
                    size = Size(10.dp.toPx(), 16.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun OceanBackground(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "Ocean")
    val wave by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "waves"
    )

    val bubbles = remember { List(12) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        bubbles.forEachIndexed { i, pos ->
            val p = (wave + (i.toFloat() / bubbles.size)) % 1f
            val y = (1f - p) * size.height
            val x = pos.x * size.width + sin(p * 10f) * 15f

            drawCircle(
                color = theme.accent.copy(alpha = 0.2f),
                center = Offset(x, y),
                radius = 8.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun DesertBackground(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "Desert")
    val drift by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "sand"
    )
    val sand = remember { List(30) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        sand.forEach { p ->
            val x = ((p.x + drift) % 1f) * size.width
            val y = p.y * size.height
            drawRect(
                color = theme.accent.copy(alpha = 0.2f),
                topLeft = Offset(x, y),
                size = Size(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun DefaultGradient(theme: MinesTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.background, theme.accent.copy(alpha = 0.05f))
            )
        )
    }
}

// Утилита для отрисовки гор
private fun DrawScope.drawMountainLayer(progress: Float, size: Size, color: Color, heightMult: Float) {
    val path = Path().apply {
        moveTo(0f, size.height)
        for (x in 0..(size.width.toInt()) step 40) {
            val relX = x.toFloat() / size.width
            val h = size.height * heightMult + sin((relX + progress) * 2 * Math.PI).toFloat() * 40f
            lineTo(x.toFloat(), h)
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}