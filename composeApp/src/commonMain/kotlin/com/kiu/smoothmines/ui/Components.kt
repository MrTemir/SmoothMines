package com.kiu.smoothmines.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kiu.smoothmines.models.Cell

@Composable
fun MineCell(
    cell: Cell,
    onClick: () -> Unit
) {
    // Анимация масштаба: от 0.8 до 1.0 при открытии
    val scale by animateFloatAsState(
        targetValue = if (cell.isRevealed) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    // Анимация появления содержимого (цифры или мины)
    val contentAlpha by animateFloatAsState(
        targetValue = if (cell.isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale) // Применяем масштаб
            .background(
                color = if (cell.isRevealed) Color.White else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.isRevealed) {
            Text(
                text = if (cell.isMine) "💣" else cell.adjacentMines.toString(),
                modifier = Modifier.alpha(contentAlpha) // Плавно проявляем текст
            )
        }
    }
}