package com.kiu.smoothmines.ui

import androidx.compose.ui.graphics.Color

enum class ThemeType {
    FOREST, AFTER_RAIN, SAND_DUNE, DEEP_OCEAN, ALPINE_SUNRISE
}
data class MinesTheme(
    val name: String,
    val type: ThemeType, // Ссылка на тип из Enum
    val background: Color,
    val cellClosed: Color,
    val cellOpened: Color = Color.Transparent,
    val accent: Color,
    val textColor: Color
)
object AppThemes {
    val themes = listOf(
        MinesTheme(
            name = "Forest",
            type = ThemeType.FOREST,
            background = Color(0xFFE2E4C4),
            cellClosed = Color(0xFFC1C49B),
            accent = Color(0xFF4CAF50),
            textColor = Color(0xFF2E4D32)
        ),
        MinesTheme(
            name = "After Rain",
            type = ThemeType.AFTER_RAIN,
            background = Color(0xFF263238),
            cellClosed = Color(0xFF37474F),
            accent = Color(0xFF80CBC4),
            textColor = Color(0xFFECEFF1)
        ),
        MinesTheme(
            name = "Sand Dune",
            type = ThemeType.SAND_DUNE,
            background = Color(0xFFF5F5DC),
            cellClosed = Color(0xFFE0C097),
            accent = Color(0xFFB85C38),
            textColor = Color(0xFF5C3D2E)
        ),
        MinesTheme(
            name = "Deep Ocean",
            type = ThemeType.DEEP_OCEAN,
            background = Color(0xFF011627),
            cellClosed = Color(0xFF0B2942),
            accent = Color(0xFF00D1FF),
            textColor = Color(0xFFD6EFFF)
        ),
        // PEACH: Теплый персиковый
        MinesTheme(
            name = "PEACH",
            type = ThemeType.SAND_DUNE,
            background = Color(0xFFFFF5F2),
            accent = Color(0xFFFFB5A7),
            textColor = Color(0xFF6D5959),
            cellClosed = Color(0xFFFCD5CE)
        ),
        // SAKURA: Нежно-розовый (Вишня)
        MinesTheme(
            name = "SAKURA",
            type = ThemeType.FOREST,
            background = Color(0xFFFFF0F3),
            accent = Color(0xFFFFB7C5),
            textColor = Color(0xFF8A5A65),
            cellClosed = Color(0xFFFFD1DC)
        ),
        // MOUNTAIN: Холодный серый / Скалы
        MinesTheme(
            name = "MOUNTAIN",
            type = ThemeType.AFTER_RAIN,
            background = Color(0xFFF4F4F9),
            accent = Color(0xFFB8C1EC),
            textColor = Color(0xFF232946),
            cellClosed = Color(0xFFD4D8F0)
        ),
        // MATCHA: Глубокий пастельный зеленый
        MinesTheme(
            name = "MATCHA",
            type = ThemeType.FOREST,
            background = Color(0xFFF3F6F4),
            accent = Color(0xFFA3B18A),
            textColor = Color(0xFF3A4431),
            cellClosed = Color(0xFFDADCCB)
        ),
        // SUNFLOWER: Мягкий желтый / Солнечный
        MinesTheme(
            name = "SUNFLOWER",
            type = ThemeType.SAND_DUNE,
            background = Color(0xFFFFFDF0),
            accent = Color(0xFFF4D35E),
            textColor = Color(0xFF6B5E2E),
            cellClosed = Color(0xFFF9F1C6)
        ),// ROSE GARDEN: Классическая пастельная роза
        MinesTheme(
            name = "ROSE GARDEN",
            type = ThemeType.FOREST,
            background = Color(0xFFFFF5F7), // Едва заметный розовый отлив
            accent = Color(0xFFF497AD),     // Цвет розового бутона
            textColor = Color(0xFF5D4037),  // Мягкий кофейный для контраста
            cellClosed = Color(0xFFFCE4EC)  // Светло-розовый лепесток
        ),
        // TEA ROSE: Чайная роза (более бежево-розовая)
        MinesTheme(
            name = "TEA ROSE",
            type = ThemeType.SAND_DUNE,
            background = Color(0xFFFFF9F5),
            accent = Color(0xFFF2C6B1),
            textColor = Color(0xFF7A5C58),
            cellClosed = Color(0xFFF7E9E1)
        ),
        // LAVENDER: Успокаивающая лаванда
        MinesTheme(
            name = "LAVENDER",
            type = ThemeType.AFTER_RAIN,
            background = Color(0xFFF8F7FF),
            accent = Color(0xFFB8B8FF),
            textColor = Color(0xFF5E548E),
            cellClosed = Color(0xFFEFEBFF)
        ),
        // DEEP LAVENDER: Более насыщенная, "сумеречная" лаванда
        MinesTheme(
            name = "NIGHT LAVENDER",
            type = ThemeType.AFTER_RAIN,
            background = Color(0xFFF1F0FA),
            accent = Color(0xFF9F9FED),
            textColor = Color(0xFF48435C),
            cellClosed = Color(0xFFE2E2F5)
        ),
        // MINTY FRESH: Свежая мята
        MinesTheme(
            name = "MINTY",
            type = ThemeType.FOREST,
            background = Color(0xFFF0FFF4),
            accent = Color(0xFF9AE6B4),
            textColor = Color(0xFF276749),
            cellClosed = Color(0xFFC6F6D5)
        ),
        // SKY BLUE: Ясное небо
        MinesTheme(
            name = "CLEAR SKY",
            type = ThemeType.AFTER_RAIN,
            background = Color(0xFFF0F9FF),
            accent = Color(0xFF7CC2FE),
            textColor = Color(0xFF1E4E7A),
            cellClosed = Color(0xFFE0F2FE)
        )
    )
}


//val GlassTheme = MinesTheme(
//    name = "The Glass",
//    background = Color(0xFF121212), // Здесь лучше использовать градиент в будущем
//    cellClosed = Color.White.copy(alpha = 0.15f), // Чуть больше белого для эффекта инея
//    cellOpened = Color.White.copy(alpha = 0.05f), // Открытая ячейка почти невидима
//    accent = Color(0xFF00E5FF),
//    textColor = Color.White,
//    isGlass = true
//)