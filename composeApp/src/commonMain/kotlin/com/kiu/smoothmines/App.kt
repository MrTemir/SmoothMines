
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.Difficulties
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.platform.getPlatformContext
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineBackHandler
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.SettingsDialog
import com.kiu.smoothmines.ui.ThemePresets
import com.kiu.smoothmines.utils.VibrationHelper


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("menu") }
    var themeIndex by remember { mutableStateOf(0) }
    val currentTheme = ThemePresets[themeIndex]

    var activeConfig by remember { mutableStateOf(Difficulties[0]) }
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    // Анимация цветов темы
    val bgColor by animateColorAsState(currentTheme.background, tween(800))
    val accentColor by animateColorAsState(currentTheme.accent, tween(800))
    val textColor by animateColorAsState(currentTheme.textColor, tween(800))
    val cellOpenedColor by animateColorAsState(currentTheme.cellOpened, tween(800))
    val cellClosedColor by animateColorAsState(currentTheme.cellClosed, tween(800))
    val vibrationHelper = VibrationHelper.getInstance()

    val animatedTheme = remember(bgColor, accentColor, textColor, cellOpenedColor, cellClosedColor) {
        MinesTheme(
            name = currentTheme.name,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellOpened = cellOpenedColor,
            cellClosed = cellClosedColor,
            isGlass = currentTheme.isGlass
        )
    }

    fun saveGameToSlots(save: SaveData) {
        savedGames = (listOf(save) + savedGames).take(3)
    }

    if (currentScreen == "game") {
        MineBackHandler { currentScreen = "menu" }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(animatedTheme.background)) {
            Crossfade(targetState = currentScreen, animationSpec = tween(500)) { screen ->
                when (screen) {
                    "settings" -> SettingsDialog(
                        onDismiss = { currentScreen = "menu" },
                        theme = animatedTheme
                    )

                    "menu" -> MenuScreen(
                        currentTheme = animatedTheme,
                        onNextTheme = {
                            themeIndex = (themeIndex + 1) % ThemePresets.size
                        },
                        onPrevTheme = {
                            themeIndex = if (themeIndex == 0) ThemePresets.size - 1 else themeIndex - 1
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
                        savedGames = savedGames
                    )

                    "game" -> {
                        // Важно: создаем локальную константу для использования внутри лямбды onBack
                        val currentConfig = activeConfig

                        MineField(
                            rows = currentConfig.rows,
                            cols = currentConfig.cols,
                            currentTheme = animatedTheme,
                            activeConfig = currentConfig,
                            initialCells = initialCells,
                            context = getPlatformContext(),
                            onBack = { currentCells ->
                                val timeStr = "Слот ${savedGames.size + 1}"
                                val newSave = SaveData(
                                    slotIndex = savedGames.size + 1,
                                    config = currentConfig,
                                    cells = currentCells.map { it.copy() },
                                    date = timeStr
                                )
                                saveGameToSlots(newSave)
                                currentScreen = "menu"
                            }
                        )
                    }
                }
            }
        }
    }
}