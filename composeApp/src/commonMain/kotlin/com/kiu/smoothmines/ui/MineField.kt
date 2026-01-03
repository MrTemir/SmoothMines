package com.kiu.smoothmines.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.logic.MinesweeperEngine
import com.kiu.smoothmines.models.Cell
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SettingsManager
import com.kiu.smoothmines.utils.VibrationHelper
import com.kiu.smoothmines.models.globalSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import smoothmines.composeapp.generated.resources.Res
import smoothmines.composeapp.generated.resources.flag_ic
import smoothmines.composeapp.generated.resources.mine_ic
import kotlin.math.abs
import kotlin.math.max

// --- ЛОГИКА ВИБРАЦИИ ---
@Composable
private fun triggerVibration(context: Any) {
    // Get the instance of VibrationHelper
    val vibrationHelper = VibrationHelper.getInstance()

    // Initialize with context - the actual implementation will handle the platform-specific part
    vibrationHelper.initialize(context)
}

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
    // --- Анимация входа ---
    var isEntering by remember { mutableStateOf(true) }
    val entranceAlpha by animateFloatAsState(targetValue = if (isEntering) 0f else 1f, animationSpec = tween(800))
    val entranceScale by animateFloatAsState(targetValue = if (isEntering) 0.85f else 1f, animationSpec = tween(800))

    LaunchedEffect(Unit) { isEntering = false }

    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    // --- Состояния игры ---
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

    // --- Анимации камеры (Исправлено!) ---
    val scaleAnim = remember { Animatable(1.2f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val animProgress = remember { mutableStateMapOf<Int, Animatable<Float, *>>() }

    fun animateReveal(index: Int, delayMs: Long) {
        scope.launch {
            delay(delayMs)
            if (index in cells.indices && !cells[index].isRevealed) {
                cells[index] = cells[index].copy(isRevealed = true)
                animProgress[index] = Animatable(0.8f).apply { animateTo(1f, tween(300)) }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            if (currentTheme.isGlass) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027), // Глубокий темный
                        Color(0xFF203A43),
                        Color(0xFF2C5364)  // Светлее к низу
                    )
                )
            } else {
                // Для обычных тем используем сплошной цвет из модели
                androidx.compose.ui.graphics.SolidColor(currentTheme.background)
            }
        )
    ) {

        // 1. Слой игры
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scope.launch {
                            val newScale = (scaleAnim.value * zoom).coerceIn(0.5f, 4f)
                            scaleAnim.snapTo(newScale)
                            offsetAnim.snapTo(offsetAnim.value + pan)
                        }
                    }
                }
                .graphicsLayer(
                    alpha = entranceAlpha,
                    scaleX = scaleAnim.value * entranceScale,
                    scaleY = scaleAnim.value * entranceScale,
                    translationX = offsetAnim.value.x,
                    translationY = offsetAnim.value.y
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .clip(RoundedCornerShape(16.dp))
                    // Блюрим только подложку самой доски
                    .then(if (currentTheme.isGlass) Modifier.blur(15.dp) else Modifier)
                    .background(currentTheme.cellClosed.copy(0.1f))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    modifier = Modifier.width((cols * 44).dp),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(cells, key = { i, _ -> i }) { index, cell ->
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
                                        // ПЛАВНЫЙ ПОДЛЕТ (Исправлено!)
                                        scope.launch {
                                            val targetScale = 2.5f
                                            val cellCenterX = (cell.x - (cols / 2f) + 0.5f) * 44
                                            val cellCenterY = (cell.y - (rows / 2f) + 0.5f) * 44
                                            val targetOffset = Offset(-cellCenterX * targetScale, -cellCenterY * targetScale)

                                            launch { scaleAnim.animateTo(targetScale, tween(600, easing = LinearOutSlowInEasing)) }
                                            launch { offsetAnim.animateTo(targetOffset, tween(600, easing = LinearOutSlowInEasing)) }
                                        }
                                    }
                                    // Логика раскрытия...
                                    if (cell.isMine) {
                                        gameState = "lost"
                                        vibrationHelper.triggerVibration()
                                        cells.forEachIndexed { i, c -> if (c.isMine) animateReveal(i, (abs((i/cols)-(index/cols))*30L)) }
                                    } else {
                                        scope.launch {
                                            engine.revealEmptyCells(cell, cells) { revealed ->
                                                val idx = cells.indexOfFirst { it.x == revealed.x && it.y == revealed.y }
                                                if (idx != -1) animateReveal(idx, 0)
                                            }
                                        }
                                    }
                                    if (cells.none { !it.isRevealed && !it.isMine }) gameState = "won"
                                }
                            },
                            onLongClick = {}
                        )
                    }
                }
            }
        }

        // 2. Слой интерфейса (Статичный, НЕ зумится)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onBack(cells.toList()) },
                    colors = ButtonDefaults.buttonColors(currentTheme.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("МЕНЮ", color = currentTheme.background, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showSettings = true },
                    colors = ButtonDefaults.buttonColors(currentTheme.cellClosed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("НАСТРОЙКИ", color = currentTheme.textColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "ОСТАЛОСЬ: ${max(0, activeConfig.minesCount - cells.count { it.isFlagged })}",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = currentTheme.textColor.copy(0.7f),
                fontWeight = FontWeight.Bold
            )
        }

        // Кнопка переключения режима
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp)) {
            ModeToggle(isFlagMode, { isFlagMode = it }, currentTheme)
        }

        // Экраны конца игры и Диалоги
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false }, theme = currentTheme, settingsManager = settingsManager)
        }

        if (gameState != "playing") {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (gameState == "won") "ПОБЕДА!" else "ВЗРЫВ!",
                        color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            cells.clear(); cells.addAll(engine.generateBoard())
                            animProgress.clear(); isFirstClick = true; gameState = "playing"
                        },
                        colors = ButtonDefaults.buttonColors(currentTheme.accent)
                    ) {
                        Text("ИГРАТЬ СНОВА", color = currentTheme.background)
                    }
                }
            }
        }
    }
}
// --- ЛОГИКА ОРИСОВКИ ЯЧЕЙКИ (Слитное полотно) ---
@Composable
fun AnimatedCellView(
    cell: Cell,
    currentTheme: MinesTheme,
    progress: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val revealAlpha = if (cell.isRevealed) (1f - (progress - 0.8f) * 5f).coerceIn(0f, 1f) else 1f
    val revealScale = if (cell.isRevealed) progress else 1f

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(currentTheme.cellOpened)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        // НИЖНИЙ СЛОЙ
        if (cell.isRevealed) {
            Box(Modifier.graphicsLayer { alpha = (progress - 0.8f) * 5f }) {
                CellContent(cell, currentTheme)
            }
        }

        // ВЕРХНИЙ СЛОЙ (Исправлена вложенность)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = revealAlpha
                    scaleX = revealScale
                    scaleY = revealScale
                }
                .background(if (cell.isFlagged) currentTheme.accent else currentTheme.cellClosed)
        ) {
            if (globalSettings.showBorders) {
                Box(
                    Modifier.fillMaxSize()
                        .border(width = 0.2.dp, color = currentTheme.textColor.copy(alpha = 0.2f))
                )
            }
            if (cell.isFlagged) {
                Icon(
                    painter = painterResource(Res.drawable.flag_ic),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    tint = if (currentTheme.isGlass) Color.White else currentTheme.background
                )
            }
        }
    }
}
// --- ВСПОМОГАТЕЛЬНЫЙ КОМПОНЕНТ ДЛЯ ЦИФР ---
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

// --- ИСПРАВЛЕННЫЙ ModeToggle ---
@Composable
fun ModeToggle(isFlagMode: Boolean, onModeChange: (Boolean) -> Unit, theme: MinesTheme) {
    val indicatorOffset by animateDpAsState(targetValue = if (isFlagMode) 22.dp else (-22).dp)

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(48.dp)
            .background(theme.cellClosed, RoundedCornerShape(24.dp))
            .clickable { onModeChange(!isFlagMode) },
        contentAlignment = Alignment.Center
    ) {
        // Движущийся кружок
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .size(40.dp)
                .background(theme.accent, CircleShape)
        )

        Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.mine_ic),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (!isFlagMode) theme.background else theme.accent.copy(0.6f)
            )
            Icon(
                painter = painterResource(Res.drawable.flag_ic),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isFlagMode) theme.background else theme.accent.copy(0.6f)
            )
        }
    }
}