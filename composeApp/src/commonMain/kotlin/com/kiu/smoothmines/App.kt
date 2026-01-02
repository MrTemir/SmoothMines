import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.kiu.smoothmines.platform.getPlatformContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.Difficulties
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.ui.MenuScreen
import com.kiu.smoothmines.ui.MineBackHandler
import com.kiu.smoothmines.ui.MineField
import com.kiu.smoothmines.ui.MinesTheme
import com.kiu.smoothmines.ui.ThemePresets
import com.kiu.smoothmines.utils.VibrationHelper


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("menu") }
    var themeIndex by remember { mutableStateOf(0) }
    val themeTarget = ThemePresets[themeIndex]

    var activeConfig by remember { mutableStateOf(Difficulties[0]) }
    var initialCells by remember { mutableStateOf<List<Cell>?>(null) }
    var savedGames by remember { mutableStateOf(listOf<SaveData>()) }

    // Анимация цветов темы
    val bgColor by animateColorAsState(themeTarget.background, tween(800))
    val accentColor by animateColorAsState(themeTarget.accent, tween(800))
    val textColor by animateColorAsState(themeTarget.textColor, tween(800))
    val cellOpenedColor by animateColorAsState(themeTarget.cellOpened, tween(800))
    val cellClosedColor by animateColorAsState(themeTarget.cellClosed, tween(800))
    val vibrationHelper = VibrationHelper.getInstance()

    val animatedTheme = remember(bgColor, accentColor, textColor, cellOpenedColor, cellClosedColor) {
        MinesTheme(
            name = themeTarget.name,
            background = bgColor,
            accent = accentColor,
            textColor = textColor,
            cellOpened = cellOpenedColor,
            cellClosed = cellClosedColor
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
                    "menu" -> MenuScreen(
                        currentTheme = animatedTheme,
                        onNextTheme = { themeIndex = (themeIndex + 1) % ThemePresets.size },
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