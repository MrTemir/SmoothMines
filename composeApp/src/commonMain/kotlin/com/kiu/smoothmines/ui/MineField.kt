package com.kiu.smoothmines.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.logic.MinesweeperEngine
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SettingsManager
import com.kiu.smoothmines.utils.VibrationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    context: Any,
    vibrationHelper: VibrationHelper = VibrationHelper.getInstance(),
    settingsManager: SettingsManager,
    onBack: (List<Cell>) -> Unit
) {
    LaunchedEffect(context) { vibrationHelper.initialize(context) }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val engine = remember(activeConfig) { MinesweeperEngine(rows, cols, activeConfig.minesCount) }
    val cells = remember(activeConfig) {
        mutableStateListOf<Cell>().apply {
            if (!initialCells.isNullOrEmpty()) addAll(initialCells)
            else addAll(engine.generateBoard())
        }
    }

    var isFirstClick by remember { mutableStateOf(true) }
    var isFlagMode by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf("playing") }

    // --- СОСТОЯНИЯ КАМЕРЫ ---
    val scale = remember { Animatable(1.1f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val shakeOffset = remember { Animatable(0f) }

    // ИСПРАВЛЕННЫЙ БЛОК: Сброс камеры при смене уровня
    LaunchedEffect(rows, cols) {
        scale.snapTo(1.1f)
        offset.snapTo(Offset.Zero)
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (!scale.isRunning && !offset.isRunning) {
            val newScale = (scale.value * zoomChange).coerceIn(0.7f, 4f)
            scope.launch { scale.snapTo(newScale) }
            scope.launch { offset.snapTo(offset.value + offsetChange) }
        }
    }

    // --- ФУНКЦИИ АНИМАЦИИ (Размер ячейки теперь зависит от Expert-режима) ---
    fun focusOnCell(index: Int) {
        val rowIndex = index / cols
        val colIndex = index % cols
        val cellSizePx = if (cols >= 21) 34f else 44f

        val centerX = (cols - 1) * cellSizePx / 2f
        val centerY = (rows - 1) * cellSizePx / 2f

        val targetX = (centerX - colIndex * cellSizePx) * 1.5f
        val targetY = (centerY - rowIndex * cellSizePx) * 1.5f

        scope.launch {
            launch { scale.animateTo(1.5f, tween(700, easing = FastOutSlowInEasing)) }
            launch { offset.animateTo(Offset(targetX, targetY), tween(700, easing = FastOutSlowInEasing)) }
        }
    }

    fun zoomOutToOverview() {
        scope.launch {
            launch { scale.animateTo(1.0f, tween(1200, easing = LinearOutSlowInEasing)) }
            launch { offset.animateTo(Offset.Zero, tween(1200, easing = LinearOutSlowInEasing)) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(currentTheme.background)) {
        ThemeDecorations(theme = currentTheme)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .transformable(state = transformState)
        ) {
            // Увеличиваем размер ячейки для расчета, чтобы точно не было наслоения
            val baseCellSize = if (cols >= 21) 36.dp else 46.dp
            val fieldWidth = (cols.dp * baseCellSize.value)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        translationX = offset.value.x + shakeOffset.value,
                        translationY = offset.value.y
                    ),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    modifier = Modifier
                        // Используем widthIn, чтобы поле не сжималось меньше своего размера
                        .width(fieldWidth)
                        .padding(48.dp)
                        .wrapContentHeight(),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(if(settingsManager.showBorders) 1.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(if(settingsManager.showBorders) 1.dp else 0.dp)
                ) {
                    itemsIndexed(
                        items = cells,
                        // КЛЮЧ: Используем индекс и состояние, чтобы Compose не перерисовывал лишнего
                        key = { index, cell -> "${index}_${cell.isRevealed}_${cell.isFlagged}" }
                    ) { index, cell ->
                        // Считаем форму один раз для конкретной ячейки
                        val dynamicShape = remember(cell.isRevealed, cell.isFlagged, index) {
                            calculateCellStatusShape(index, cols, cells)
                        }

                        Box(
                            modifier = Modifier.aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // ЭФФЕКТ ЖИДКОСТИ: Рисуем филлеры только если сетка выключена
                            if (!settingsManager.showBorders) {
                                InnerCornerFillers(index, cols, cells, currentTheme.cellClosed)
                            }

                            AnimatedCellView(
                                cell = cell,
                                theme = currentTheme,
                                shape = dynamicShape,
                                onClick = {
                                    // Твоя логика клика...
                                    if (cell.isRevealed || cell.isFlagged || gameState != "playing") return@AnimatedCellView

                                    if (isFlagMode) {
                                        cells[index] = cell.copy(isFlagged = !cell.isFlagged)
                                    } else {
                                        if (isFirstClick) {
                                            isFirstClick = false
                                            focusOnCell(index)
                                            engine.placeMines(cells, cell, safeRadius = 2)
                                            scope.launch { engine.revealEmptyCells(cells[index], cells) { } }
                                        } else if (cell.isMine) {
                                            // Логика проигрыша...
                                            gameState = "lost"
                                            zoomOutToOverview()
                                            scope.launch {
                                                val power = 20f * settingsManager.shakeIntensity
                                                repeat(4) {
                                                    shakeOffset.animateTo(power, tween(50))
                                                    shakeOffset.animateTo(-power, tween(50))
                                                }
                                                shakeOffset.animateTo(0f, tween(50))
                                            }
                                            scope.launch {
                                                cells.indices.filter { cells[it].isMine && !cells[it].isRevealed }
                                                    .forEach { idx ->
                                                        delay(50L)
                                                        cells[idx] = cells[idx].copy(isRevealed = true)
                                                        if (settingsManager.vibrationEnabled) vibrationHelper.triggerVibration()
                                                    }
                                            }
                                        } else {
                                            scope.launch { engine.revealEmptyCells(cell, cells) { } }
                                            if (cells.none { !it.isMine && !it.isRevealed }) {
                                                gameState = "won"
                                                zoomOutToOverview()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
        UIOverlay(cells, activeConfig, currentTheme, { showSettings = true }, { onBack(cells) }, isFlagMode) { isFlagMode = it }

        if (gameState != "playing") {
            Box(Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.BottomCenter) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { onBack(cells) }, colors = ButtonDefaults.buttonColors(currentTheme.cellClosed)) {
                        Text("МЕНЮ", color = currentTheme.textColor)
                    }
                    Button(onClick = {
                        cells.clear(); cells.addAll(engine.generateBoard())
                        isFirstClick = true; gameState = "playing"
                        scope.launch { scale.snapTo(1.2f); offset.snapTo(Offset.Zero) }
                    }, colors = ButtonDefaults.buttonColors(currentTheme.accent)) {
                        Text("ЗАНОВО", color = currentTheme.background)
                    }
                }
            }
        }
        if (showSettings) SettingsDialog({ showSettings = false }, currentTheme, settingsManager)
    }

@Composable
fun AnimatedCellView(
    cell: Cell,
    theme: MinesTheme,
    shape: RoundedCornerShape, // Принимаем форму извне
    onClick: () -> Unit
) {
    val alphaAnim by animateFloatAsState(if (cell.isRevealed) 0f else 1f, tween(400))
    val scaleAnim by animateFloatAsState(if (cell.isRevealed) 0.8f else 1f, spring(Spring.DampingRatioMediumBouncy))
    val contentAlpha by animateFloatAsState(if (cell.isRevealed) 1f else 0f, tween(300))

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            // Используем переданную "жидкую" форму
            .background(theme.cellClosed.copy(alpha = alphaAnim), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.graphicsLayer { this.alpha = contentAlpha }) {
            CellContent(cell, theme)
        }
    }
}
@Composable
fun GameOverScreen(state: String, theme: MinesTheme, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (state == "won") "ПОБЕДА" else "БУМ!", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(theme.accent)) {
                Text("ЕЩЕ РАЗ", color = theme.background)
            }
        }
    }
}
// --- КОМПОНЕНТ ДЛЯ ЦИФР ---
@Composable
fun CellContent(cell: Cell, theme: MinesTheme) {
    if (cell.isMine) {
        Icon(
            painter = painterResource(Res.drawable.mine_ic),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    } else if (cell.adjacentMines > 0) {
        Text(
            text = "${cell.adjacentMines}",
            color = getNumberColor(cell.adjacentMines),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

// Вынес логику цвета цифр
private fun getNumberColor(number: Int): Color {
    return when (number) {
        1 -> Color(0xFF2196F3); 2 -> Color(0xFF4CAF50); 3 -> Color(0xFFF44336)
        4 -> Color(0xFF3F51B5); 5 -> Color(0xFF795548); else -> Color.Gray
    }
}

// ---  ModeToggle ---
@Composable
fun ModeToggle(isFlagMode: Boolean, onModeChange: (Boolean) -> Unit, theme: MinesTheme) {
    val indicatorOffset by animateDpAsState(if (isFlagMode) 24.dp else (-24.dp))

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(48.dp)
            .clip(CircleShape)
            .background(theme.cellClosed.copy(0.5f))
            .clickable { onModeChange(!isFlagMode) },
        contentAlignment = Alignment.Center
    ) {
        // Бегунок (фон за иконкой)
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .size(42.dp)
                .background(theme.accent, CircleShape)
        )

        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            // Иконка мины
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.mine_ic),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp), // Центрировано внутри веса
                    tint = if (!isFlagMode) theme.background else theme.accent
                )
            }
            // Иконка флажка
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.flag_ic),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp), // Центрировано внутри веса
                    tint = if (isFlagMode) theme.background else theme.accent
                )
            }
        }
    }
}
// Это функция-расширение. Она добавляет твоему проекту новый тип настройки для дизайна.
fun Modifier.appleGlass(theme: MinesTheme): Modifier = this.then(
    Modifier
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.04f)
                )
            )
        )
        .border(
            width = 0.5.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.1f)
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )
)

@Composable
fun InnerCornerFillers(index: Int, cols: Int, cells: List<Cell>, color: Color) {
    val rDp = 16.dp // Радиус, должен совпадать с тем, что в форме

    fun isBusy(idx: Int, colOffset: Int): Boolean {
        val targetCol = (index % cols) + colOffset
        if (targetCol < 0 || targetCol >= cols) return false
        return cells.getOrNull(idx)?.let { !it.isRevealed && !it.isFlagged } ?: false
    }

    Canvas(Modifier.fillMaxSize()) {
        val r = rDp.toPx()

        // ВЕРХ-ЛЕВО
        if (isBusy(index - 1, -1) && isBusy(index - cols, 0) && !isBusy(index - cols - 1, -1)) {
            drawRect(color, size = Size(r, r))
            drawCircle(Color.Transparent, r, Offset(0f, 0f), blendMode = androidx.compose.ui.graphics.BlendMode.Clear)
        }
        // ВЕРХ-ПРАВО
        if (isBusy(index + 1, 1) && isBusy(index - cols, 0) && !isBusy(index - cols + 1, 1)) {
            drawRect(color, topLeft = Offset(size.width - r, 0f), size = Size(r, r))
            drawCircle(Color.Transparent, r, Offset(size.width, 0f), blendMode = androidx.compose.ui.graphics.BlendMode.Clear)
        }
        // НИЗ-ЛЕВО
        if (isBusy(index - 1, -1) && isBusy(index + cols, 0) && !isBusy(index + cols - 1, -1)) {
            drawRect(color, topLeft = Offset(0f, size.height - r), size = Size(r, r))
            drawCircle(Color.Transparent, r, Offset(0f, size.height), blendMode = androidx.compose.ui.graphics.BlendMode.Clear)
        }
        // НИЗ-ПРАВО
        if (isBusy(index + 1, 1) && isBusy(index + cols, 0) && !isBusy(index + cols + 1, 1)) {
            drawRect(color, topLeft = Offset(size.width - r, size.height - r), size = Size(r, r))
            drawCircle(Color.Transparent, r, Offset(size.width, size.height), blendMode = androidx.compose.ui.graphics.BlendMode.Clear)
        }
    }
}
@Composable
fun MovingGradientBackground(theme: MinesTheme) {
    val trans = rememberInfiniteTransition(label = "gradient")
    val color1 = Color(0xFFFFFFFF) // Чистый белый
    val color2 = Color(0xFF00D2FF) // Яркий голубой (он даст "жизнь" под стеклом)
    val color3 = Color(0xFF92FE9D) // Капля мятного для мягкости
    // Анимации перемещения
    val x1 by trans.animateFloat(0f, 1f, infiniteRepeatable(tween(15000), RepeatMode.Reverse), label = "")
    val y1 by trans.animateFloat(0f, 1f, infiniteRepeatable(tween(20000), RepeatMode.Reverse), label = "")
    val x2 by trans.animateFloat(1f, 0f, infiniteRepeatable(tween(18000), RepeatMode.Reverse), label = "")

    Canvas(Modifier.fillMaxSize()) {
        // Основной фон —
        drawRect(color1)

        // Первый круг
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf((color2), Color.Transparent),
                center = Offset(size.width * x1, size.height * y1),
                radius = size.width * 0.9f
            ),
            center = Offset(size.width * x1, size.height * y1),
            radius = size.width * 0.9f
        )

        // Второй круг
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(size.width * x2, size.height * (1f - y1)),
                radius = size.width * 0.8f
            ),
            center = Offset(size.width * x2, size.height * (1f - y1)),
            radius = size.width * 0.8f
        )
    }
}
@Composable
fun UIOverlay(
    cells: List<Cell>,
    config: GameConfig,
    theme: MinesTheme,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    isFlagMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Кнопка ВЫХОДА (используем тот же стиль, что был для смены темы)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                // Добавляем легкий фон-круг, чтобы кнопка была похожа на кнопки из меню
                .background(theme.textColor.copy(alpha = 0.05f), CircleShape)
                .size(40.dp) // Размер всей области кнопки
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Системный знак стрелки
                contentDescription = "Назад",
                tint = theme.textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp) // Размер самой иконки внутри
            )
        }

        // Счетчик (в центре)
        Text(
            text = "ОСТАЛОСЬ: ${max(0, config.minesCount - cells.count { it.isFlagged })}",
            modifier = Modifier.align(Alignment.Center),
            style = androidx.compose.ui.text.TextStyle(
                color = theme.textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )

        // Кнопка настроек (справа)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = theme.textColor.copy(alpha = 0.7f)
            )
        }
    }

    // Таблетка переключения режимов снизу
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        ModeToggle(isFlagMode, onModeChange, theme)
    }
}

fun calculateCellStatusShape(index: Int, cols: Int, cells: List<Cell>): RoundedCornerShape {
    val cell = cells.getOrNull(index) ?: return RoundedCornerShape(0.dp)

    // Если ячейка открыта или с флагом — она "отлипает", скругляем ей углы
    if (cell.isRevealed || cell.isFlagged) return RoundedCornerShape(12.dp)

    fun isSolid(idx: Int, colOffset: Int): Boolean {
        val targetCol = (index % cols) + colOffset
        if (targetCol < 0 || targetCol >= cols) return false
        val neighbor = cells.getOrNull(idx) ?: return false
        // Сосед "липкий", только если он закрыт и БЕЗ флага
        return !neighbor.isRevealed && !neighbor.isFlagged
    }

    val t = isSolid(index - cols, 0)
    val b = isSolid(index + cols, 0)
    val l = isSolid(index - 1, -1)
    val r = isSolid(index + 1, 1)

    // Если со всех сторон соседи — углы 0 (острые), если край — 12dp
    return RoundedCornerShape(
        topStart = if (t && l) 0.dp else 12.dp,
        topEnd = if (t && r) 0.dp else 12.dp,
        bottomStart = if (b && l) 0.dp else 12.dp,
        bottomEnd = if (b && r) 0.dp else 12.dp
    )
}