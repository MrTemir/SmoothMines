package com.kiu.smoothmines.ui

import androidx.compose.ui.graphics.Color

data class MinesTheme(
    val name: String,
    val background: Color,
    val cellClosed: Color,
    val cellOpened: Color,
    val textColor: Color,
    val accent: Color,
    val isGlass: Boolean = false
)
val GlassTheme = MinesTheme(
    name = "The Glass",
    background = Color(0xFF121212), // Здесь лучше использовать градиент в будущем
    cellClosed = Color.White.copy(alpha = 0.15f), // Чуть больше белого для эффекта инея
    cellOpened = Color.White.copy(alpha = 0.05f), // Открытая ячейка почти невидима
    accent = Color(0xFF00E5FF),
    textColor = Color.White,
    isGlass = true
)
val ThemePresets = listOf(
    MinesTheme(
        name = "The Clean One",
        background = Color(0xFFE2E3C0),
        cellClosed = Color(0xFF5CABBD),
        cellOpened = Color(0xFFFFFFFF),
        textColor = Color(0xFF58583A),
        accent = Color(0xFF5C7CFA)
    ),
    MinesTheme(
        name = "Dark Mode",
        background = Color(0xFF000000),
        cellClosed = Color(0xFF403E3E),
        cellOpened = Color(0xFF242222),
        textColor = Color(0xFFEEEEEE),
        accent = Color(0xFF3C393E)
    ),
    MinesTheme(
        name = "Forest",
        background = Color(0xFFBDDEC1),
        cellClosed = Color(0xFFA5D6A7),
        cellOpened = Color(0xFFC8E6C9),
        textColor = Color(0xFF1B5E20),
        accent = Color(0xFF4CAF50)
    ),
    MinesTheme(
        name = "The Glass",
        background = Color(0xADD8E6),
        cellClosed = Color(0xFF333333).copy(alpha = 0.7f),
        cellOpened = Color.Transparent,
        accent = Color(0xFF7ABACE),
        textColor = Color.White,
        isGlass = true
    )
)