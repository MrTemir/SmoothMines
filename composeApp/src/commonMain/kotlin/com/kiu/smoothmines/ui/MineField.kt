package com.kiu.smoothmines.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.logic.MinesweeperEngine
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.utils.SettingsManager
import com.kiu.smoothmines.utils.VibrationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import smoothmines.composeapp.generated.resources.Res
import smoothmines.composeapp.generated.resources.flag_ic
import smoothmines.composeapp.generated.resources.mine_ic
import kotlin.math.max

@Composable
fun MineField(
    rows: Int,
    cols: Int,
    currentTheme: MinesTheme,
    activeConfig: GameConfig,
    initialCells: List<Cell>? = null,
    initialTime: Int = 0,
    context: Any,
    vibrationHelper: VibrationHelper = VibrationHelper.getInstance(),
    settingsManager: SettingsManager,
    onBack: (List<Cell>, Int, Boolean) -> Unit
) {
    LaunchedEffect(context) { vibrationHelper.initialize(context) }

    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    // --- СОСТОЯНИЕ ИГРЫ ---
    val engine = remember(activeConfig) { MinesweeperEngine(rows, cols, activeConfig.minesCount) }
    val cells = remember(activeConfig) {
        mutableStateListOf<Cell>().apply {
            if (!initialCells.isNullOrEmpty()) addAll(initialCells)
            else addAll(engine.generateBoard())
        }
    }

    var isFirstClick by remember { mutableStateOf(initialCells == null) }
    var isFlagMode by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf("playing") }
    var explodedMineIndex by remember { mutableStateOf(-1) }
    var timeSeconds by remember { mutableStateOf(initialTime) }
    var isTimerActive by remember { mutableStateOf(!isFirstClick && initialCells != null) }

    // --- КАМЕРА (Scale/Offset) ---
    val scale = remember { Animatable(max(1f, cols / 9f)) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scope.launch {
            val newScale = (scale.value * zoomChange).coerceIn(0.5f, 5f)
            scale.snapTo(newScale)
            offset.snapTo(offset.value + offsetChange)
        }
    }

    fun zoomOutToOverview() {
        scope.launch {
            launch { scale.animateTo(max(1f, cols / 9f), tween(800)) }
            launch { offset.animateTo(Offset.Zero, tween(800)) }
        }
    }

    // --- ТАЙМЕР ---
    LaunchedEffect(isTimerActive, gameState) {
        if (isTimerActive && gameState == "playing") {
            while (true) {
                delay(1000L)
                timeSeconds++
            }
        }
    }

    // --- ЛОГИКА КЛИКА И ПОБЕДЫ ---
    suspend fun handleCellClick(idx: Int) {
        val targetCell = cells[idx]
        if (targetCell.isRevealed || targetCell.isFlagged || gameState != "playing") return

        if (targetCell.isMine) {
            withContext(Dispatchers.Main) {
                explodedMineIndex = idx
                gameState = "lost"
                isTimerActive = false
                vibrationHelper.triggerVibration()
                zoomOutToOverview()
            }
            // Анимация открытия всех мин
            cells.indices.filter { cells[it].isMine }.forEach { mIdx ->
                delay(30)
                cells[mIdx] = cells[mIdx].copy(isRevealed = true)
            }
        } else {
            engine.revealEmptyCells(targetCell, cells) { }

            // ПРОВЕРКА ПОБЕДЫ: если все НЕ-мины открыты
            val hasWon = cells.all { it.isMine || it.isRevealed }
            if (hasWon) {
                withContext(Dispatchers.Main) {
                    gameState = "won"
                    isTimerActive = false
                    vibrationHelper.triggerVibration()
                    zoomOutToOverview()
                }
            }
        }
    }

    // --- ВЕРСТКА ---
    Box(modifier = Modifier.fillMaxSize()) {
        MovingGradientBackground(currentTheme)

        // Игровое поле
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        translationX = offset.value.x,
                        translationY = offset.value.y
                    )
                    .appleGlass(currentTheme)
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    modifier = Modifier.width((if (cols >= 21) 34.dp else 44.dp) * cols),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(if(settingsManager.showBorders) 1.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(if(settingsManager.showBorders) 1.dp else 0.dp)
                ) {
                    itemsIndexed(items = cells, key = { _, cell -> cell.id }) { index, cell ->
                        val shape = calculateCellStatusShape(index, cols, cells)

                        AnimatedCellView(
                            cell = cell,
                            theme = currentTheme,
                            shape = shape,
                            index = index,
                            cols = cols,
                            cells = cells,
                            gameState = gameState,
                            settingsManager = settingsManager,
                            vibrationHelper = vibrationHelper,
                            isExplodedMine = (index == explodedMineIndex),
                            onClick = {
                                if (gameState != "playing") return@AnimatedCellView

                                if (cell.isRevealed) {
                                    // АККОРД
                                    val adjIdxs = engine.getAdjacentIndexes(index)
                                    if (adjIdxs.count { cells[it].isFlagged } == cell.adjacentMines) {
                                        scope.launch(Dispatchers.Default) {
                                            adjIdxs.forEach { handleCellClick(it) }
                                        }
                                    }
                                } else {
                                    scope.launch(Dispatchers.Default) {
                                        if (isFlagMode) {
                                            cells[index] = cell.copy(isFlagged = !cell.isFlagged)
                                            if (settingsManager.vibrationEnabled) vibrationHelper.triggerVibration()
                                        } else {
                                            if (cell.isFlagged) return@launch
                                            if (isFirstClick) {
                                                isFirstClick = false
                                                engine.placeMines(cells, cell, safeRadius = 2)
                                                withContext(Dispatchers.Main) { isTimerActive = true }
                                            }
                                            handleCellClick(index)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Интерфейс
        UIOverlay(
            cells = cells,
            config = activeConfig,
            theme = currentTheme,
            timeSeconds = timeSeconds,
            onSettingsClick = { showSettings = true },
            onBackClick = { onBack(cells, timeSeconds, gameState != "playing") },
            isFlagMode = isFlagMode,
            onModeChange = { isFlagMode = it }
        )

        // Поздравление при победе
        if (gameState == "won") {
            GameOverActionButtons(currentTheme,
                onBack = { onBack(cells, timeSeconds, true) },
                onRetry = { /* логика перезапуска */ }
            )
        }

        if (showSettings) {
            SettingsDialog({ showSettings = false }, currentTheme, settingsManager)
        }
    }
}
// --- SNAPSHOT FOR MINI-MAP ---

@Composable
fun GameSnapshot(cells: List<Cell>, cols: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val rows = cells.size / cols
        val cellW = size.width / cols
        val cellH = size.height / rows
        cells.forEachIndexed { index, cell ->
            val r = index / cols
            val c = index % cols
            val color = when {
                cell.isRevealed && cell.isMine -> Color(0xFFFF5252)
                cell.isRevealed -> Color.White.copy(0.4f)
                cell.isFlagged -> Color(0xFF40C4FF)
                else -> Color.Gray.copy(0.2f)
            }
            drawRect(
                color = color,
                topLeft = Offset(c * cellW, r * cellH),
                size = androidx.compose.ui.geometry.Size(cellW * 0.85f, cellH * 0.85f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedCellView(
    cell: Cell,
    theme: MinesTheme,
    shape: RoundedCornerShape,
    index: Int,
    cols: Int,
    cells: MutableList<Cell>,
    gameState: String,
    settingsManager: SettingsManager,
    vibrationHelper: VibrationHelper,
    isExplodedMine: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope() // Добавлено для работы launch
    val islandColor by animateColorAsState(
        when {
            cell.isRevealed -> Color.Transparent
            cell.isFlagged -> theme.accent.copy(0.35f)
            else -> theme.cellClosed
        }, tween(250)
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                clip = true
                this.shape = shape
            }
            .background(islandColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick() },
                onLongClick = {
                    if (gameState == "playing" && !cell.isRevealed) {
                        // Обновляем ячейку в списке
                        cells[index] = cell.copy(isFlagged = !cell.isFlagged)
                        if (settingsManager.vibrationEnabled) {
                            vibrationHelper.triggerVibration()
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isSticky) InnerCornerFillers(index, cols, cells, theme.cellClosed)
        CellContent(cell, theme, isExplodedMine)
    }
}

@Composable
fun CellContent(cell: Cell, theme: MinesTheme, isExplodedMine: Boolean) {
    val appearance by animateFloatAsState(if (cell.isRevealed) 1f else 0f, tween(500))

    if (cell.isFlagged && !cell.isRevealed) {
        Icon(painterResource(Res.drawable.flag_ic), null, Modifier.size(18.dp), tint = theme.accent)
    }

    if (cell.isRevealed) {
        if (cell.isMine) {
            Box(contentAlignment = Alignment.Center) {
                if (isExplodedMine) {
                    Box(Modifier.size(28.dp).background(Color.Red.copy(0.4f), CircleShape))
                }
                Icon(
                    painterResource(Res.drawable.mine_ic), null,
                    modifier = Modifier.size(22.dp).graphicsLayer {
                        alpha = appearance
                        scaleX = appearance * (if(isExplodedMine) 1.25f else 1f)
                        scaleY = appearance * (if(isExplodedMine) 1.25f else 1f)
                    },
                    tint = if (isExplodedMine) Color.White else Color.Unspecified
                )
            }
        } else if (cell.adjacentMines > 0) {
            Text("${cell.adjacentMines}", color = getNumberColor(cell.adjacentMines),
                fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.graphicsLayer { alpha = appearance })
        }
    }
}

@Composable
fun UIOverlay(
    cells: List<Cell>,
    config: GameConfig,
    theme: MinesTheme,
    timeSeconds: Int,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    isFlagMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            // Исправлено: Явное указание имен параметров решает проблему несоответствия типов
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка назад
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(
                    color = theme.textColor.copy(alpha = 0.05f),
                    shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = theme.textColor
                )
            }

            // Статистика (мины и время)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val remainingMines = max(0, config.minesCount - cells.count { it.isFlagged })
                val overFlagged = cells.count { it.isFlagged } > config.minesCount

                Text(
                    text = if (overFlagged) "ЛИШНИЕ ФЛАГИ!" else "МИНЫ: $remainingMines",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    // Цвет меняется в зависимости от состояния
                    color = when {
                        overFlagged -> Color.Red
                        remainingMines == 0 -> theme.accent // Все мины помечены
                        else -> theme.textColor.copy(alpha = 0.6f)
                    }
                )
                Text(
                    text = String.format("%02d:%02d", timeSeconds / 60, timeSeconds % 60),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.accent
                )
            }

            // Кнопка настроек
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = theme.textColor.copy(alpha = 0.7f)
                )
            }
        }

        // Переключатель режимов внизу
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            ModeToggle(isFlagMode, onModeChange, theme)
        }
    }
}

@Composable
fun ModeToggle(isFlagMode: Boolean, onModeChange: (Boolean) -> Unit, theme: MinesTheme) {
    // Анимация смещения индикатора (кружка)
    val indicatorOffset by animateDpAsState(
        targetValue = if (isFlagMode) 24.dp else (-24.dp),
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(48.dp)
            .clip(CircleShape)
            .background(theme.cellClosed.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Убираем стандартный эффект нажатия для чистоты анимации
            ) { onModeChange(!isFlagMode) },
        contentAlignment = Alignment.Center // Явно указываем выравнивание содержимого
    ) {
        // Фоновый движущийся индикатор (активный кружок)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .size(40.dp) // Чуть уменьшил, чтобы был отступ от краев
                .background(color = theme.accent, shape = CircleShape)
        )

        // Иконки режима
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Иконка мины (обычный режим)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.mine_ic),
                    contentDescription = "Режим копания",
                    modifier = Modifier.size(20.dp),
                    tint = if (!isFlagMode) theme.background else theme.accent
                )
            }

            // Иконка флага (режим пометки)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.flag_ic),
                    contentDescription = "Режим флагов",
                    modifier = Modifier.size(20.dp),
                    tint = if (isFlagMode) theme.background else theme.accent
                )
            }
        }
    }
}
@Composable
fun MovingGradientBackground(theme: MinesTheme) {
    val trans = rememberInfiniteTransition()
    val x1 by trans.animateFloat(0f, 1f, infiniteRepeatable(tween(15000), RepeatMode.Reverse))
    val y1 by trans.animateFloat(0f, 1f, infiniteRepeatable(tween(20000), RepeatMode.Reverse))
    Canvas(Modifier.fillMaxSize()) {
        drawRect(if(theme.isDark) Color(0xFF121212) else Color.White)
        drawCircle(
            brush = Brush.radialGradient(listOf(theme.accent.copy(0.2f), Color.Transparent), Offset(size.width * x1, size.height * y1)),
            radius = size.width
        )
    }
}

@Composable
fun InnerCornerFillers(index: Int, cols: Int, cells: List<Cell>, color: Color) {
    val r = 24f
    val hasT = (index >= cols) && cells[index - cols].isSticky
    val hasB = (index + cols < cells.size) && cells[index + cols].isSticky
    val hasL = (index % cols != 0) && cells[index - 1].isSticky
    val hasR = ((index + 1) % cols != 0) && cells[index + 1].isSticky
    Canvas(Modifier.fillMaxSize()) {
        if (hasT && hasL && cells.getOrNull(index - cols - 1)?.isSticky == false) drawPath(Path().apply { moveTo(0f, r); lineTo(0f, 0f); lineTo(r, 0f); quadraticBezierTo(0f, 0f, 0f, r) }, color)
        if (hasT && hasR && cells.getOrNull(index - cols + 1)?.isSticky == false) drawPath(Path().apply { moveTo(size.width - r, 0f); lineTo(size.width, 0f); lineTo(size.width, r); quadraticBezierTo(size.width, 0f, size.width - r, 0f) }, color)
        if (hasB && hasL && cells.getOrNull(index + cols - 1)?.isSticky == false) drawPath(Path().apply { moveTo(0f, size.height - r); lineTo(0f, size.height); lineTo(r, size.height); quadraticBezierTo(0f, size.height, 0f, size.height - r) }, color)
        if (hasB && hasR && cells.getOrNull(index + cols + 1)?.isSticky == false) drawPath(Path().apply { moveTo(size.width - r, size.height); lineTo(size.width, size.height); lineTo(size.width, size.height - r); quadraticBezierTo(size.width, size.height, size.width - r, size.height) }, color)
    }
}

fun calculateCellStatusShape(index: Int, cols: Int, cells: List<Cell>): RoundedCornerShape {
    val cell = cells.getOrNull(index) ?: return RoundedCornerShape(0.dp)
    if (!cell.isSticky) return RoundedCornerShape(12.dp)
    fun isS(i: Int, off: Int) = ((index % cols) + off) in 0 until cols && cells.getOrNull(i)?.isSticky == true
    val t = isS(index - cols, 0); val b = isS(index + cols, 0); val l = isS(index - 1, -1); val r = isS(index + 1, 1)
    val rd = 12.dp
    return RoundedCornerShape(if(t && l) 0.dp else rd, if(t && r) 0.dp else rd, if(b && r) 0.dp else rd, if(b && l) 0.dp else rd)
}

private fun getNumberColor(n: Int) = when(n) {
    1 -> Color(0xFF2196F3); 2 -> Color(0xFF4CAF50); 3 -> Color(0xFFF44336);
    4 -> Color(0xFF9C27B0); else -> Color.Gray
}

fun Modifier.appleGlass(theme: MinesTheme) = this.clip(RoundedCornerShape(28.dp))
    .background(Brush.verticalGradient(listOf(Color.White.copy(if(theme.isDark) 0.1f else 0.4f), Color.White.copy(0.05f))))
    .border(0.5.dp, Color.White.copy(0.3f), RoundedCornerShape(28.dp))

@Composable
fun GameOverActionButtons(theme: MinesTheme, onBack: () -> Unit, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(bottom = 120.dp), Alignment.BottomCenter) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(theme.cellClosed.copy(0.8f)), shape = RoundedCornerShape(16.dp)) { Text("МЕНЮ", color = theme.textColor) }
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(theme.accent), shape = RoundedCornerShape(16.dp)) { Text("ЗАНОВО", color = theme.background) }
        }
    }
}
