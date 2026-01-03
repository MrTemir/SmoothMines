package com.kiu.smoothmines

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.models.initSettings
import com.kiu.smoothmines.platform.PlatformSettings
import com.kiu.smoothmines.platform.getPlatformContext
import com.kiu.smoothmines.ui.AppThemes
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.ThemeDecorations
import com.kiu.smoothmines.utils.VibrationHelper

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    val platformSettings = remember { PlatformSettings() }
    val settingsManager = remember { initSettings(platformSettings) }
    val context = getPlatformContext()
    VibrationHelper.getInstance()

    var currentScreen by remember { mutableStateOf("menu") }
    var activeConfig by remember { mutableStateOf<GameConfig?>(null) }
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }

    // Список сохранений (можно потом привязать к базе данных или файлу)
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    var themeIndex by remember { mutableIntStateOf(settingsManager.savedThemeIndex) }
    val baseTheme = AppThemes.themes[themeIndex % AppThemes.themes.size]

    // Плавная анимация цветов при смене темы
    val bgColor by animateColorAsState(baseTheme.background, tween(600))
    val accentColor by animateColorAsState(baseTheme.accent, tween(600))
    val textColor by animateColorAsState(baseTheme.textColor, tween(600))
    val cellClosedColor by animateColorAsState(baseTheme.cellClosed, tween(600))

    val animatedTheme = remember(bgColor, accentColor, textColor, cellClosedColor, baseTheme.type) {
        MinesTheme(
            name = baseTheme.name,
            type = baseTheme.type,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellClosed = cellClosedColor,
            cellOpened = Color.Transparent
        )
    }

    MaterialTheme {
        // Основной контейнер с пастельным фоном
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            // ДОБАВЛЕНО: Теперь анимации рисуются на самом нижнем слое
            ThemeDecorations(theme = animatedTheme)

            Crossfade(targetState = currentScreen, animationSpec = tween(500)) { screen ->
                when (screen) {
                    "menu" -> MenuScreen(
                                currentTheme = animatedTheme, // Теперь тема передает правильный контрастный textColor
                        onNextTheme = {
                            themeIndex = (themeIndex + 1) % AppThemes.themes.size
                            settingsManager.updateThemeIndex(themeIndex)
                        },
                        onPrevTheme = {
                            themeIndex =
                                (themeIndex - 1 + AppThemes.themes.size) % AppThemes.themes.size
                            settingsManager.updateThemeIndex(themeIndex)
                        },
                        onStartGame = { config ->
                            activeConfig = config
                            initialCells = null
                            currentScreen = "game"
                        },
                        onContinueGame = { saveData ->
                            activeConfig = saveData.config
                            initialCells = saveData.cells
                            currentScreen = "game"
                        },
                        savedGames = savedGames,
                        settingsManager = settingsManager
                    )
                    "game" -> {
                    activeConfig?.let { config ->
                        MineField(
                                rows = config.rows,
                                cols = config.cols,
                                currentTheme = animatedTheme,
                                activeConfig = config,
                                initialCells = initialCells,
                                context = context,
                                settingsManager = settingsManager,
                                onBack = { currentCells ->
                                    val newSave = SaveData(
                                        slotIndex = savedGames.size,
                                        config = config,
                                        cells = currentCells.toList(),
                                        date = "Игра ${config.minesCount} мин"
                                    )
                                    savedGames = listOf(newSave) + savedGames.take(2)
                                    currentScreen = "menu"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}