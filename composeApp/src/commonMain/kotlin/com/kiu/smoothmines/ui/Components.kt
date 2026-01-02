package com.kiu.smoothmines.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MineCell(
    symbol: String,
    isRevealed: Boolean,
    currentTheme: MinesTheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // ДЕЛАЕТ ЯЧЕЙКУ КВАДРАТНОЙ
            .padding(2.dp) // Крошечный отступ между ячейками
            .background(
                color = if (isRevealed) currentTheme.cellOpened else currentTheme.cellClosed,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    )  {
        if (isRevealed) {
            Text(text = symbol, color = currentTheme.textColor)
        }
    }
}