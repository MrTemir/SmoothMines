package com.kiu.smoothmines.ui

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun ThemeDecorations(theme: MinesTheme) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Слой 1: Базовый Canvas для простых эффектов (дождь, пузырьки, горы)
        MainCanvasLayer(theme)

        // Слой 2: Сложные анимированные частицы (лепестки, пыльца, облака)
        when (theme.name) {
            "ROSE GARDEN", "TEA ROSE" -> {
                FallingParticles(count = 12, color = theme.accent.copy(alpha = 0.2f), speed = 6000)
            }
            "NIGHT LAVENDER" -> {
                FloatingPollen(count = 25, color = theme.accent.copy(alpha = 0.3f))
            }
            "MINTY" -> {
                RisingBubbles(count = 10, color = Color.White.copy(alpha = 0.25f))
            }
            "CLEAR SKY" -> {
                SoftDriftingClouds(color = Color.White.copy(alpha = 0.15f))
            }
        }
    }
}

@Composable
private fun MainCanvasLayer(theme: MinesTheme) {
    val transition = rememberInfiniteTransition(label = "ThemeAnim")

    val slowPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "SlowPhase"
    )

    val fastPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Restart),
        label = "FastPhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (theme.type) {
            ThemeType.FOREST -> drawForest(theme, fastPhase)
            ThemeType.AFTER_RAIN -> drawRain(theme, fastPhase)
            ThemeType.DEEP_OCEAN -> drawBubbles(theme, slowPhase)
            ThemeType.SAND_DUNE -> drawDunes(theme, slowPhase)
            ThemeType.ALPINE_SUNRISE -> drawMountains(theme, slowPhase)
        }
    }
}

// --- ЛОГИКА РИСОВАНИЯ (DRAW SCOPE) ---

private fun DrawScope.drawForest(theme: MinesTheme, phase: Float) {
    val color = theme.textColor.copy(alpha = 0.08f)
    drawPath(
        path = Path().apply {
            moveTo(size.width, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.7f, size.height * 0.15f, size.width * 0.6f, size.height * 0.4f)
        },
        color = color,
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawRain(theme: MinesTheme, phase: Float) {
    val color = theme.accent.copy(alpha = 0.12f)
    for (i in 0..10) {
        val x = (size.width / 10) * i
        val startY = (size.height * phase + (i * 120)) % size.height
        drawLine(
            color = color,
            start = Offset(x, startY),
            end = Offset(x - 4f, startY + 25f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawBubbles(theme: MinesTheme, phase: Float) {
    val color = theme.textColor.copy(alpha = 0.08f)
    for (i in 1..5) {
        val x = size.width * ((0.18f * i) + (phase * 0.04f))
        val y = size.height * (0.85f - (phase * 0.15f * (i % 2 + 1)))
        drawCircle(
            color = color,
            radius = 12f * (i % 2 + 1),
            center = Offset(x, y),
            style = Stroke(width = 1.5f)
        )
    }
}

private fun DrawScope.drawDunes(theme: MinesTheme, phase: Float) {
    val color = theme.textColor.copy(alpha = 0.04f)
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(-size.width * 0.1f + (phase * 40), size.height * 0.82f),
        size = Size(size.width * 1.3f, size.height * 0.35f)
    )
}

private fun DrawScope.drawMountains(theme: MinesTheme, phase: Float) {
    val color = theme.textColor.copy(alpha = 0.04f)
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(size.width * 0.25f, size.height * 0.75f)
        lineTo(size.width * 0.5f, size.height * 0.88f)
        lineTo(size.width * 0.85f, size.height * 0.68f)
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

// --- КОМПОНЕНТЫ ЧАСТИЦ (COMPOSABLES) ---

@Composable
fun FallingParticles(count: Int, color: Color, speed: Int) {
    val transition = rememberInfiniteTransition(label = "Falling")
    repeat(count) { i ->
        val startDelay = i * 800
        val xPos = remember { (0..100).random().toFloat() / 100f }

        val yOffset by transition.animateFloat(
            initialValue = -100f,
            targetValue = 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween(speed, delayMillis = startDelay, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "y"
        )

        val rotation by transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(speed * 2, easing = LinearEasing)),
            label = "rot"
        )

        Canvas(Modifier.fillMaxSize()) {
            val pSize = 35f
            rotate(rotation, pivot = Offset(size.width * xPos, yOffset)) {
                drawOval(
                    color = color,
                    topLeft = Offset(size.width * xPos, yOffset),
                    size = Size(pSize, pSize * 1.4f)
                )
            }
        }
    }
}

@Composable
fun FloatingPollen(count: Int, color: Color) {
    val transition = rememberInfiniteTransition(label = "Pollen")
    repeat(count) { i ->
        val xOffset = remember { (0..100).random().toFloat() / 100f }
        val yOffset = remember { (0..100).random().toFloat() / 100f }

        val drift by transition.animateFloat(
            initialValue = -30f, targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500 + i * 200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "drift"
        )

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = color,
                radius = 5f,
                center = Offset(size.width * xOffset + drift, size.height * yOffset + drift)
            )
        }
    }
}

@Composable
fun RisingBubbles(count: Int, color: Color) {
    val transition = rememberInfiniteTransition(label = "Bubbles")
    repeat(count) { i ->
        val xPos = remember { (0..100).random().toFloat() / 100f }
        val duration = remember { (5000..8000).random() }

        val yPos by transition.animateFloat(
            initialValue = 1.1f,
            targetValue = -0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, delayMillis = i * 400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "y"
        )

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = color,
                radius = 18f,
                center = Offset(size.width * xPos, size.height * yPos),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun SoftDriftingClouds(color: Color) {
    val transition = rememberInfiniteTransition(label = "Clouds")
    val xAnim by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart),
        label = "x"
    )

    Canvas(Modifier.fillMaxSize()) {
        drawOval(
            color = color,
            topLeft = Offset(size.width * xAnim, size.height * 0.12f),
            size = Size(size.width * 0.7f, size.height * 0.18f)
        )
    }
}