package com.kiu.smoothmines

import SaveData
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.initSettings
import com.kiu.smoothmines.platform.PlatformSettings
import com.kiu.smoothmines.platform.getPlatformContext
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.ThemeDecorations
import com.kiu.smoothmines.ui.ThemePresets
import com.kiu.smoothmines.utils.SettingsManager
import com.kiu.smoothmines.utils.VibrationHelper


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    val platformSettings = remember { PlatformSettings() }
    // Явно указываем тип SettingsManager
    val settingsManager: SettingsManager = remember { initSettings(platformSettings) }
    val context = getPlatformContext()

    // Инициализация вибрации через безопасный вызов
    LaunchedEffect(context) {
        VibrationHelper.getInstance().initialize(context)
    }

    var currentScreen by remember { mutableStateOf("menu") }
    var activeConfig by remember { mutableStateOf<GameConfig?>(null) }
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    val allThemes = ThemePresets.allThemes
    var themeIndex by remember { mutableIntStateOf(settingsManager.savedThemeIndex) }
    val baseTheme = allThemes[themeIndex % allThemes.size]

    // Анимация цветов
    val bgColor by animateColorAsState(baseTheme.background, tween(800))
    val accentColor by animateColorAsState(baseTheme.accent, tween(800))
    val textColor by animateColorAsState(baseTheme.textColor, tween(800))
    val cellClosedColor by animateColorAsState(baseTheme.cellClosed, tween(800))

    // Создаем объект анимированной темы
    val animatedTheme = remember(bgColor, accentColor, textColor, cellClosedColor, baseTheme.name) {
        MinesTheme(
            name = baseTheme.name,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellClosed = cellClosedColor,
            type = baseTheme.type,
            isDark = baseTheme.isDark
        )
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedTheme.background)
        ) {
            ThemeDecorations(theme = animatedTheme)

            Crossfade(targetState = currentScreen, animationSpec = tween(500)) { screen ->
                when (screen) {
                    "menu" -> MenuScreen(
                        currentTheme = animatedTheme,
                        onNextTheme = {
                            themeIndex = (themeIndex + 1) % allThemes.size
                            settingsManager.updateThemeIndex(themeIndex)
                        },
                        onPrevTheme = {
                            themeIndex = (themeIndex - 1 + allThemes.size) % allThemes.size
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
                                // Передаем время из сохранения, если оно есть, иначе 0
                                initialTime = if (initialCells != null) savedGames.firstOrNull()?.timeSeconds ?: 0 else 0,
                                context = context,
                                settingsManager = settingsManager,
                                onBack = { cells, time, isGameOver ->
                                    if (isGameOver) {
                                        savedGames = emptyList()
                                        initialCells = null
                                    } else {
                                        // Сохраняем текущее состояние ячеек и время
                                        val newSave = SaveData(
                                            config = config,
                                            cells = cells.toList(),
                                            timeSeconds = time,
                                            date = "Игра ${config.minesCount} мин"
                                        )
                                        savedGames = listOf(newSave)
                                    }
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