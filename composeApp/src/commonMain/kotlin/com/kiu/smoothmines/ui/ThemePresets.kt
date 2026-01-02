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
        background = Color(0xFFF5F5F5),
        cellClosed = Color(0xFFE0E0E0),
        cellOpened = Color(0xFFFFFFFF),
        textColor = Color(0xFF212121),
        accent = Color(0xFF5C7CFA)
    ),
    MinesTheme(
        name = "Dark Mode",
        background = Color(0xFF121212),
        cellClosed = Color(0xFF333333),
        cellOpened = Color(0xFF1E1E1E),
        textColor = Color(0xFFEEEEEE),
        accent = Color(0xFFBB86FC)
    ),
    MinesTheme(
        name = "Forest",
        background = Color(0xFFE8F5E9),
        cellClosed = Color(0xFFA5D6A7),
        cellOpened = Color(0xFFC8E6C9),
        textColor = Color(0xFF1B5E20),
        accent = Color(0xFF4CAF50)
    ),
    MinesTheme(
        name = "The Glass",
        background = Color(0xFF121212), // Здесь лучше использовать градиент в будущем
        cellClosed = Color.White.copy(alpha = 0.15f), // Чуть больше белого для эффекта инея
        cellOpened = Color.Black.copy(alpha = 0.2f), // Открытая ячейка почти невидима
        accent = Color(0xFF00E5FF),
        textColor = Color.White,
        isGlass = true
    )
)