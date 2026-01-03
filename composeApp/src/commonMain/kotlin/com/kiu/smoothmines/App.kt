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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.Difficulties
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.models.initSettings
import com.kiu.smoothmines.platform.PlatformSettings
import com.kiu.smoothmines.platform.getPlatformContext
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.ThemePresets
import com.kiu.smoothmines.utils.VibrationHelper.Companion.getInstance

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    // Initialize settings
    val context = getPlatformContext()
    val platformSettings = remember { PlatformSettings(context) }
    val settingsManager = remember { initSettings(platformSettings) }
    
    // Initialize vibration helper with settings
    val vibrationHelper = getInstance()
    DisposableEffect(Unit) {
        vibrationHelper.initialize(context)
        onDispose { }
    }
    
    var currentScreen by remember { mutableStateOf("menu") }
    var themeIndex by remember { mutableStateOf(0) }

    // Берем оригинал темы для логики (isGlass и названия)
    val baseTheme = ThemePresets[themeIndex]

    // Состояния для анимации (оставляем, они делают переходы красивыми)
    val animationDuration = settingsManager.animationSpeed.coerceAtLeast(100L).coerceAtMost(1000L)
    val bgColor by animateColorAsState(baseTheme.background, tween(animationDuration.toInt()))
    val accentColor by animateColorAsState(baseTheme.accent, tween(800))
    val textColor by animateColorAsState(baseTheme.textColor, tween(800))
    val cellOpenedColor by animateColorAsState(baseTheme.cellOpened, tween(800))
    val cellClosedColor by animateColorAsState(baseTheme.cellClosed, tween(800))

    // Собираем анимированную тему
    val animatedTheme = remember(bgColor, accentColor, textColor, cellOpenedColor, cellClosedColor, baseTheme.isGlass) {
        MinesTheme(
            name = baseTheme.name,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellOpened = cellOpenedColor,
            cellClosed = cellClosedColor,
            isGlass = baseTheme.isGlass // Обязательно передаем флаг стекла
        )
    }

    // Остальные переменные без изменений...
    var activeConfig by remember { mutableStateOf(Difficulties[0]) }
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    MaterialTheme {
        // Устанавливаем фон для всего приложения
        Box(modifier = Modifier
            .fillMaxSize()
            .background(animatedTheme.background)
        ) {
            Crossfade(targetState = currentScreen, animationSpec = tween(500)) { screen ->
                when (screen) {
                    "menu" -> MenuScreen(
                        currentTheme = animatedTheme,
                        onNextTheme = { themeIndex = (themeIndex + 1) % ThemePresets.size },
                        onPrevTheme = { themeIndex = (themeIndex - 1 + ThemePresets.size) % ThemePresets.size },
                        onStartGame = { config ->
                            activeConfig = config
                            initialCells = null
                            // Даем анимации цвета шанс завершиться или стабилизироваться
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
                        // Важно: создаем локальную константу для использования внутри лямбды onBack
                        val currentConfig = activeConfig

                        MineField(
                            rows = currentConfig.rows,
                            cols = currentConfig.cols,
                            currentTheme = ThemePresets[0],
                            activeConfig = currentConfig,
                            initialCells = initialCells,
                            context = getPlatformContext(),
                            settingsManager = settingsManager,
                            onBack = { currentCells ->
                                val timeStr = "Слот ${savedGames.size + 1}"
                                val newSave = SaveData(
                                    slotIndex = savedGames.size + 1,
                                    config = currentConfig,
                                    cells = currentCells.map { it.copy() },
                                    date = timeStr
                                )
                                savedGames = savedGames + newSave
                                currentScreen = "menu"
                            }
                        )
                    }
                }
            }
        }
    }
}