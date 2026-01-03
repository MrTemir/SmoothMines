package com.kiu.smoothmines.ui

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import kotlin.math.abs
import kotlin.math.max

// --- ОСНОВНОЕ ИГРОВОЕ ПОЛЕ ---
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

    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

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

    val scaleAnim = remember { Animatable(1.2f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val animProgress = remember { mutableStateMapOf<Int, Animatable<Float, *>>() }

    fun animateReveal(index: Int, delayMs: Long) {
        scope.launch {
            delay(delayMs)
            if (index in cells.indices && !cells[index].isRevealed) {
                cells[index] = cells[index].copy(isRevealed = true)
                animProgress[index] = Animatable(0.7f).apply {
                    animateTo(1f, tween(400, easing = LinearOutSlowInEasing))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(currentTheme.background)) {

        // СЛОЙ 1: Сетка карты (тонкие линии)
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = (cols * 44).dp, height = (rows * 44).dp)
        ) {
            val strokeWidth = 0.5.dp.toPx()
            val color = currentTheme.textColor.copy(alpha = 0.05f)

            // Вертикальные линии
            for (i in 0..cols) {
                val x = i * 44.dp.toPx()
                drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth)
            }
            // Горизонтальные линии
            for (i in 0..rows) {
                val y = i * 44.dp.toPx()
                drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
            }
        }

        // СЛОЙ 2: Игровое поле
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // ПОДЛОЖКА для ровных краев — берет цвет закрытой ячейки из темы
            Box(
                modifier = Modifier
                    .size(width = (cols * 44).dp, height = (rows * 44).dp)
                    .background(currentTheme.cellClosed, RoundedCornerShape(16.dp))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    itemsIndexed(cells) { index, cell ->
                        AnimatedCellView(
                            index = index,
                            cells = cells,
                            cols = cols,
                            rows = rows, // Передай rows!
                            currentTheme = currentTheme,
                            progress = animProgress[index]?.value ?: 1f,
                            settingsManager = settingsManager,
                            onClick = {
                                if (gameState != "playing" || cell.isRevealed) return@AnimatedCellView

                                if (isFlagMode) {
                                    cells[index] = cell.copy(isFlagged = !cell.isFlagged)
                                    if (settingsManager.vibrationEnabled) vibrationHelper.triggerVibration()
                                } else if (!cell.isFlagged) {
                                    if (isFirstClick) {
                                        isFirstClick = false
                                        engine.placeMines(cells, cell)
                                        cells.forEachIndexed { i, c ->
                                            if (abs(c.x - cell.x) <= 1 && abs(c.y - cell.y) <= 1) {
                                                animateReveal(
                                                    i,
                                                    (abs(c.x - cell.x) + abs(c.y - cell.y)) * 30L
                                                )
                                            }
                                        }
                                    }

                                    val currentCell = cells[index]
                                    if (currentCell.isMine) {
                                        gameState = "lost"
                                        if (settingsManager.vibrationEnabled) vibrationHelper.triggerVibration()
                                        cells.forEachIndexed { i, c ->
                                            if (c.isMine) animateReveal(
                                                i,
                                                (abs(c.x - currentCell.x) + abs(c.y - currentCell.y)) * 40L
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            engine.revealEmptyCells(
                                                currentCell,
                                                cells
                                            ) { revealed ->
                                                val idx =
                                                    cells.indexOfFirst { it.x == revealed.x && it.y == revealed.y }
                                                if (idx != -1) animateReveal(
                                                    idx,
                                                    (abs(revealed.x - currentCell.x) + abs(revealed.y - currentCell.y)) * 30L
                                                )
                                            }
                                        }
                                    }
                                    if (cells.none { !it.isRevealed && !it.isMine }) gameState =
                                        "won"
                                }
                            }
                        )
                    }
                }
            }
        }

        // ИНТЕРФЕЙС
        UIOverlay(
            cells = cells,
            config = activeConfig,
            theme = currentTheme,
            onSettingsClick = { showSettings = true },
            onBackClick = { onBack(cells) }, // Вызываем callback выхода, передавая текущее состояние
            isFlagMode = isFlagMode,
            onModeChange = { isFlagMode = it }
        )

        if (showSettings) SettingsDialog(onDismiss = { showSettings = false }, theme = currentTheme, settingsManager = settingsManager)
        if (gameState != "playing") GameOverScreen(gameState, currentTheme) {
            cells.clear(); cells.addAll(engine.generateBoard()); animProgress.clear(); isFirstClick = true; gameState = "playing"
        }
    }
}
@Composable
fun AnimatedCellView(
    index: Int,
    cells: List<Cell>,
    cols: Int,
    rows: Int,
    currentTheme: MinesTheme,
    progress: Float,
    settingsManager: SettingsManager,
    onClick: () -> Unit
) {
    val cell = cells[index]
    val dynamicShape = remember(cell.isRevealed, cell.isFlagged) {
        calculateCellStatusShape(index, cols, rows, cells) // Теперь передаем и rows
    }

    val scale by animateFloatAsState(
        targetValue = if (cell.isRevealed || cell.isFlagged) 0.88f else 1.0f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                // Используй значения напрямую из стейта анимации
                scaleX = scale * progress
                scaleY = scale * progress
                // Это говорит системе не пересчитывать слои, если изменился только масштаб
                clip = true
                shape = dynamicShape
                compositingStrategy = CompositingStrategy.Offscreen
            },
        contentAlignment = Alignment.Center
    ) {
        // Заплатки для скругления внутреннего пространства острова
        if (!cell.isRevealed && !cell.isFlagged) {
            InnerCornerFillers(index, cols, cells, currentTheme.cellClosed)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(dynamicShape)
                .background(
                    if (cell.isRevealed) currentTheme.cellOpened// Проявляем сетку под ячейкой
                    else currentTheme.cellClosed
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (cell.isRevealed) {
                CellContent(cell, currentTheme)
            } else if (cell.isFlagged) {
                Icon(
                    painter = painterResource(Res.drawable.flag_ic),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = currentTheme.accent // Используем спокойный акцент из темы
                )
            }
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
    val indicatorOffset by animateDpAsState(
        targetValue = if (isFlagMode) 20.dp else (-20).dp, // Чуть меньше ход
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .width(90.dp) // Уменьшил ширину таблетки
            .height(48.dp)
            .clip(CircleShape)
            .background(theme.cellClosed.copy(0.3f))
            .clickable { onModeChange(!isFlagMode) },
        contentAlignment = Alignment.Center
    ) {
        // Индикатор
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .size(38.dp)
                .background(theme.accent, CircleShape)
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), // Меньше отступ = иконки ближе к центру
            horizontalArrangement = Arrangement.SpaceEvenly, // Сближаем иконки
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.mine_ic),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (!isFlagMode) theme.background else theme.accent.copy(0.6f)
            )
            Icon(
                painter = painterResource(Res.drawable.flag_ic),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isFlagMode) theme.background else theme.accent.copy(0.6f)
            )
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
fun calculateCellStatusShape(index: Int, cols: Int, rows: Int, cells: List<Cell>): RoundedCornerShape {
    val cell = cells.getOrNull(index) ?: return RoundedCornerShape(0.dp)
    if (cell.isRevealed || cell.isFlagged) return RoundedCornerShape(16.dp)

    fun isSolid(idx: Int, colOffset: Int): Boolean {
        val targetCol = (index % cols) + colOffset
        if (targetCol < 0 || targetCol >= cols) return false
        val neighbor = cells.getOrNull(idx) ?: return false
        return !neighbor.isRevealed && !neighbor.isFlagged
    }

    val t = isSolid(index - cols, 0)
    val b = isSolid(index + cols, 0)
    val l = isSolid(index - 1, -1)
    val r = isSolid(index + 1, 1)

    return RoundedCornerShape(
        topStart = if (t && l) 0.dp else 16.dp,
        topEnd = if (t && r) 0.dp else 16.dp,
        bottomStart = if (b && l) 0.dp else 16.dp,
        bottomEnd = if (b && r) 0.dp else 16.dp
    )
}