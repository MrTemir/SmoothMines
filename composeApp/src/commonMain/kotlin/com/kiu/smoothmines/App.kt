package com.kiu.smoothmines

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.models.initSettings
import com.kiu.smoothmines.platform.PlatformSettings
import com.kiu.smoothmines.platform.getPlatformContext
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.ThemePresets
import com.kiu.smoothmines.utils.VibrationHelper

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    // 1. Инициализация платформенных штук
    val platformSettings = remember { PlatformSettings() }
    val settingsManager = remember { initSettings(platformSettings) }
    val context = getPlatformContext()
    val vibrationHelper = VibrationHelper.getInstance()

    // 2. Состояния экранов и данных
    var currentScreen by remember { mutableStateOf("menu") }
    var activeConfig by remember { mutableStateOf<GameConfig?>(null) } // ТОЛЬКО ОДНО ОБЪЯВЛЕНИЕ
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    // 3. Управление темой
    var themeIndex by remember { mutableIntStateOf(settingsManager.savedThemeIndex) }
    val baseTheme = ThemePresets[themeIndex]

    // Инициализация вибрации
    DisposableEffect(Unit) {
        vibrationHelper.initialize(context)
        onDispose { }
    }

    // Анимация цветов темы
    val animationDuration = settingsManager.animationSpeed.toInt().coerceIn(100, 1000)
    val bgColor by animateColorAsState(baseTheme.background, tween(animationDuration))
    val accentColor by animateColorAsState(baseTheme.accent, tween(animationDuration))
    val textColor by animateColorAsState(baseTheme.textColor, tween(animationDuration))
    val cellOpenedColor by animateColorAsState(baseTheme.cellOpened, tween(animationDuration))
    val cellClosedColor by animateColorAsState(baseTheme.cellClosed, tween(animationDuration))

    val animatedTheme = remember(bgColor, accentColor, textColor, cellOpenedColor, cellClosedColor, baseTheme.isGlass) {
        MinesTheme(
            name = baseTheme.name,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellOpened = cellOpenedColor,
            cellClosed = cellClosedColor,
            isGlass = baseTheme.isGlass
        )
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (baseTheme.isGlass) {
                        Brush.verticalGradient(listOf(Color(0xFF141E30), Color(0xFF243B55)))
                    } else {
                        SolidColor(bgColor)
                    }
                )
        ) {
            Crossfade(targetState = currentScreen, animationSpec = tween(500)) { screen ->
                when (screen) {
                    "menu" -> MenuScreen(
                        currentTheme = animatedTheme,
                        onNextTheme = {
                            val newIndex = (themeIndex + 1) % ThemePresets.size
                            themeIndex = newIndex
                            settingsManager.updateThemeIndex(newIndex)
                        },
                        onPrevTheme = {
                            // Логика "назад": если индекс стал -1, он перепрыгнет на конец списка
                            val newIndex = (themeIndex - 1 + ThemePresets.size) % ThemePresets.size
                            themeIndex = newIndex
                            settingsManager.updateThemeIndex(newIndex)
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
                        val config = activeConfig
                        if (config != null) {
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
                                        slotIndex = savedGames.size + 1,
                                        config = config,
                                        cells = currentCells.map { it.copy() },
                                        date = "Слот ${savedGames.size + 1}"
                                    )
                                    savedGames = savedGames + newSave
                                    currentScreen = "menu"
                                }
                            )
                        } else {
                            // Если конфиг потерялся, возвращаемся в меню
                            currentScreen = "menu"
                        }
                    }
                }
            }
        }
    }
}