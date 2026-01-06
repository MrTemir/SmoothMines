package com.kiu.smoothmines.ui

import androidx.compose.ui.graphics.Color

enum class ThemeType {
    DEFAULT, FOREST, SAKURA, LAVENDER, AFTER_RAIN, DEEP_OCEAN, SAND_DUNE, ALPINE_SUNRISE, SPACE
}

data class MinesTheme(
    val name: String,
    val background: Color,
    val accent: Color,
    val textColor: Color,
    val cellClosed: Color,
    val type: ThemeType = ThemeType.DEFAULT,
    val isDark: Boolean = false,
    // Настройка прозрачности "стекла" для каждой темы
    val glassAlpha: Float = if (isDark) 0.1f else 0.3f
)

object ThemePresets {
    val allThemes = listOf(
        MinesTheme(
            name = "Forest Matcha",
            background = Color(0xFFF1F8E9),
            accent = Color(0xFF81C784),
            textColor = Color(0xFF1B5E20),
            cellClosed = Color(0xFFC8E6C9),
            type = ThemeType.FOREST
        ),
        MinesTheme(
            name = "Sakura Bloom",
            background = Color(0xFFFFF5F7),
            accent = Color(0xFFFFB7C5),
            textColor = Color(0xFF884D59),
            cellClosed = Color(0xFFFFDDE5),
            type = ThemeType.SAKURA
        ),
        MinesTheme(
            name = "Lavender Dream",
            background = Color(0xFFF3E5F5),
            accent = Color(0xFFCE93D8),
            textColor = Color(0xFF4A148C),
            cellClosed = Color(0xFFE1BEE7),
            type = ThemeType.LAVENDER
        ),
        MinesTheme(
            name = "After Rain",
            background = Color(0xFFE0F7FA),
            accent = Color(0xFF4DD0E1),
            textColor = Color(0xFF006064),
            cellClosed = Color(0xFFB2EBF2),
            type = ThemeType.AFTER_RAIN
        ),
        MinesTheme(
            name = "Minty Ocean",
            background = Color(0xFFE0F2F1),
            accent = Color(0xFF4DB6AC),
            textColor = Color(0xFF004D40),
            cellClosed = Color(0xFFB2DFDB),
            type = ThemeType.DEEP_OCEAN
        ),
        MinesTheme(
            name = "Sand Dune",
            background = Color(0xFFFFF3E0),
            accent = Color(0xFFFFB74D),
            textColor = Color(0xFFE65100),
            cellClosed = Color(0xFFFFE0B2),
            type = ThemeType.SAND_DUNE
        ),
        MinesTheme(
            name = "Alpine Sunrise",
            background = Color(0xFFE8EAF6),
            accent = Color(0xFF7986CB),
            textColor = Color(0xFF1A237E),
            cellClosed = Color(0xFFC5CAE9),
            type = ThemeType.ALPINE_SUNRISE
        ),
        MinesTheme(
            name = "Deep Space",
            background = Color(0xFF02040A),
            accent = Color(0xFF3F51B5),
            textColor = Color(0xFFC5CAE9),
            cellClosed = Color(0xFF1A1C2E),
            type = ThemeType.SPACE,
            isDark = true,
            glassAlpha = 0.15f
        )
    )

    fun getDefaultTheme(): MinesTheme = allThemes[0]
}