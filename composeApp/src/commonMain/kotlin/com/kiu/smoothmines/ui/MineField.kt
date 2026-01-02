package com.kiu.smoothmines.ui

import com.kiu.smoothmines.utils.VibrationHelper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.logic.MinesweeperEngine
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.globalSettings
import com.kiu.smoothmines.ui.MinesTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

// --- ЛОГИКА ВИБРАЦИИ ---
@Composable
private fun triggerVibration(context: Any) {
    // Get the instance of VibrationHelper
    val vibrationHelper = VibrationHelper.getInstance()

    // Initialize with context - the actual implementation will handle the platform-specific part
    vibrationHelper.initialize(context)

    // Trigger the vibration
    vibrationHelper.triggerVibration()
}

// --- ОСНОВНОЕ ИГРОВОЕ ПОЛЕ ---
@Composable
fun MineField(
    rows: Int,
    cols: Int,
    currentTheme: MinesTheme,
    activeConfig: GameConfig,
    initialCells: List<Cell>? = null,
    context: Any,  // Keep this for other potential uses
    vibrationHelper: VibrationHelper = VibrationHelper.getInstance(),
    onBack: (List<Cell>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val minesCount = remember(activeConfig) { activeConfig.minesCount }
    val engine = remember(activeConfig) { MinesweeperEngine(rows, cols, minesCount) }

    val cells = remember(activeConfig) {
        mutableStateListOf<Cell>().apply {
            if (!initialCells.isNullOrEmpty()) addAll(initialCells)
            else addAll(engine.generateBoard())
        }
    }

    var isFirstClick by remember { mutableStateOf(initialCells == null) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isFlagMode by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf("playing") }

    val animProgress = remember { mutableStateMapOf<Int, Animatable<Float, *>>() }

    fun animateReveal(index: Int, delayMs: Long) {
        scope.launch {
            delay(delayMs)
            if (index in cells.indices && !cells[index].isRevealed) {
                cells[index] = cells[index].copy(isRevealed = true)
                animProgress[index] = Animatable(0.8f).apply {
                    animateTo(1f, tween(300))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(currentTheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offset.x, translationY = offset.y
                ),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                modifier = Modifier.size(40.dp * cols, 40.dp * rows),
                userScrollEnabled = false
            ) {
                itemsIndexed(cells) { index, cell ->
                    AnimatedCellView(
                        cell = cell,
                        currentTheme = currentTheme,
                        progress = animProgress[index]?.value ?: 1f,
                        onClick = {
                            if (gameState != "playing" || cell.isRevealed) return@AnimatedCellView

                            if (isFlagMode) {
                                cells[index] = cell.copy(isFlagged = !cell.isFlagged)
                                vibrationHelper.triggerVibration()
                            } else if (!cell.isFlagged) {
                                if (isFirstClick) {
                                    engine.placeMines(cells, cell)
                                    isFirstClick = false
                                }

                                if (cell.isMine) {
                                    gameState = "lost"
                                    vibrationHelper.triggerVibration()
                                    cells.forEachIndexed { i, c ->
                                        if (c.isMine) {
                                            // Рассчитываем строку для анимации "волны"
                                            val currentRow = i / cols
                                            val clickedRow = index / cols
                                            animateReveal(i, (abs(currentRow - clickedRow) * 40L))
                                        }
                                    }
                                } else {
                                    engine.revealEmptyCells(cells, cell)
                                    cells.forEachIndexed { i, c ->
                                        if (c.isRevealed && !animProgress.containsKey(i)) {
                                            animateReveal(i, 0)
                                        }
                                    }
                                }

                                if (cells.count { !it.isRevealed && !it.isMine } == 0) {
                                    gameState = "won"
                                }
                            }
                        }
                    )
                }
            }
        }

        // Интерфейс
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onBack(cells.toList()) }, colors = ButtonDefaults.buttonColors(currentTheme.accent)) {
                Text("МЕНЮ", color = currentTheme.background, fontWeight = FontWeight.Bold)
            }
            val flagsCount = cells.count { it.isFlagged }
            Text("💣 ${max(0, minesCount - flagsCount)}", color = currentTheme.textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
            ModeToggle(isFlagMode, { isFlagMode = it }, currentTheme)
        }

        if (gameState != "playing") {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (gameState == "won") "ПОБЕДА!" else "БУМ!", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = {
                        cells.clear(); cells.addAll(engine.generateBoard())
                        animProgress.clear(); isFirstClick = true; gameState = "playing"
                    }, colors = ButtonDefaults.buttonColors(currentTheme.accent)) {
                        Text("НОВАЯ ИГРА", color = Color.White)
                    }
                }
            }
        }
    }
}

// --- ЯЧЕЙКА ---
@Composable
private fun AnimatedCellView(
    cell: Cell,
    currentTheme: MinesTheme,
    progress: Float,
    onClick: () -> Unit
) {
    val bgColor = when {
        cell.isRevealed -> currentTheme.cellOpened
        cell.isFlagged -> currentTheme.accent
        else -> currentTheme.cellClosed
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = progress
                scaleY = progress
            }
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isRevealed) {
            val text = if (cell.isMine) "💣" else if (cell.adjacentMines > 0) "${cell.adjacentMines}" else ""
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    color = if (cell.isMine) Color.Unspecified else getNumberColor(cell.adjacentMines),
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            val dotSize by animateDpAsState(if (cell.isFlagged) 16.dp else 4.dp)
            Box(Modifier.size(dotSize).background(if (cell.isFlagged) Color.White else currentTheme.accent.copy(0.4f), CircleShape))
        }

        if (globalSettings.showBorders) {
            Canvas(Modifier.fillMaxSize()) {
                val s = 0.5.dp.toPx()
                drawLine(Color.Black.copy(0.1f), Offset(size.width, 0f), Offset(size.width, size.height), s)
                drawLine(Color.Black.copy(0.1f), Offset(0f, size.height), Offset(size.width, size.height), s)
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
private fun getNumberColor(number: Int): Color {
    return when (number) {
        1 -> Color(0xFF2196F3); 2 -> Color(0xFF4CAF50); 3 -> Color(0xFFF44336)
        4 -> Color(0xFF3F51B5); 5 -> Color(0xFF795548); else -> Color.Gray
    }
}

@Composable
fun ModeToggle(isFlagMode: Boolean, onModeChange: (Boolean) -> Unit, theme: MinesTheme) {
    val indicatorOffset by animateDpAsState(targetValue = if (isFlagMode) 22.dp else (-22).dp)
    Box(
        modifier = Modifier.width(100.dp).height(48.dp)
            .background(theme.cellClosed, RoundedCornerShape(24.dp))
            .clickable { onModeChange(!isFlagMode) },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.offset(x = indicatorOffset).size(40.dp).background(theme.accent, CircleShape))
        Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
            Text("💣", fontSize = 18.sp); Text("🚩", fontSize = 18.sp)
        }
    }
}