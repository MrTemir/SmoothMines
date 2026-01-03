package com.kiu.smoothmines.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.models.Difficulties
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SaveData
import com.kiu.smoothmines.models.SettingsManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SaveSlotCard(save: SaveData, theme: MinesTheme, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 100.dp, height = 76.dp), // Чуть увеличил высоту для текста
        color = theme.cellClosed,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // ИСПРАВЛЕНО: вместо gravity
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = save.config.difficultyName,
                fontSize = 10.sp,
                color = theme.textColor.copy(alpha = 0.7f)
            )
            Text(
                text = save.date,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accent
            )
        }
    }
}
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    theme: MinesTheme,
    settingsManager: SettingsManager
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cellOpened,
        title = { Text("Настройки", color = theme.textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Линии сетки
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Линии сетки", modifier = Modifier.weight(1f), color = theme.textColor)
                    Switch(
                        checked = settingsManager.showBorders,
                        onCheckedChange = { settingsManager.updateShowBorders(it) } // Исправлено
                    )
                }

                // Скорость анимации
                Column {
                    Text("Скорость анимации: ${settingsManager.animationSpeed}мс", color = theme.textColor)
                    Slider(
                        value = settingsManager.animationSpeed.toFloat(),
                        onValueChange = { settingsManager.updateAnimationSpeed(it.toLong()) }, // Исправлено
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accent,
                            activeTrackColor = theme.accent.copy(0.5f)
                        )
                    )
                }

                // Вибрация
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", modifier = Modifier.weight(1f), color = theme.textColor)
                    Switch(
                        checked = settingsManager.vibrationEnabled,
                        onCheckedChange = { settingsManager.updateVibrationEnabled(it) } // Исправлено
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = theme.accent) }
        }
    )
}
@Composable
fun MenuScreen(
    onStartGame: (GameConfig) -> Unit,
    onContinueGame: (SaveData) -> Unit,
    currentTheme: MinesTheme,
    onNextTheme: () -> Unit,
    onPrevTheme: () -> Unit,
    savedGames: List<SaveData>,
    settingsManager: SettingsManager
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var rowsInput by remember { mutableStateOf("20") }
    var colsInput by remember { mutableStateOf("20") }
    var minesInput by remember { mutableStateOf("40") }

    // Используем прозрачный Surface, так как фон (градиент) теперь в App.kt
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "SmoothMines",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = currentTheme.textColor
            )

            // Блок сохранений
            AnimatedVisibility(visible = savedGames.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("ПРОДОЛЖИТЬ", fontSize = 10.sp, color = currentTheme.textColor.copy(0.5f))
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        savedGames.forEach { save ->
                            SaveSlotCard(save, currentTheme) { onContinueGame(save) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ВСЕ КНОПКИ ТЕПЕРЬ ОДИНАКОВЫЕ
            Difficulties.forEach { config ->
                MenuTextButton(config.difficultyName, currentTheme) { onStartGame(config) }
            }

            MenuTextButton("Своя игра", currentTheme) { showCustomDialog = true }

            Spacer(modifier = Modifier.height(20.dp))

            MenuTextButton("Настройки", currentTheme) { showSettingsDialog = true }

            // Переключатель темы
            ThemeSwitcher(currentTheme, onPrevTheme, onNextTheme)
        }

        // Диалоги
        if (showCustomDialog) {
            CustomGameDialog(
                currentTheme,
                rowsInput, colsInput, minesInput,
                onRowsChange = { rowsInput = it },
                onColsChange = { colsInput = it },
                onMinesChange = { minesInput = it },
                onDismiss = { showCustomDialog = false },
                onConfirm = { r, c, m -> onStartGame(GameConfig(r, c, m, "Кастом")) }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(onDismiss = { showSettingsDialog = false }, theme = currentTheme, settingsManager = settingsManager)
        }
    }
}
@Composable
fun CustomGameDialog(
    theme: MinesTheme,
    rows: String,
    cols: String,
    mines: String,
    onRowsChange: (String) -> Unit,
    onColsChange: (String) -> Unit,
    onMinesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cellOpened,
        title = { Text("Своя игра", color = theme.textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomInputField("Строк (5-50)", rows, theme, onRowsChange)
                CustomInputField("Столбцов (5-50)", cols, theme, onColsChange)
                CustomInputField("Мин", mines, theme, onMinesChange)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val r = rows.toIntOrNull()?.coerceIn(5, 50) ?: 10
                    val c = cols.toIntOrNull()?.coerceIn(5, 50) ?: 10
                    val m = mines.toIntOrNull()?.coerceIn(1, (r * c) - 1) ?: 10
                    onConfirm(r, c, m)
                    onDismiss()
                }
            ) {
                Text("СОЗДАТЬ", color = theme.accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ОТМЕНА", color = theme.textColor.copy(0.6f))
            }
        }
    )
}

// Вспомогательный компонент для переключателя тем
@Composable
fun ThemeSwitcher(theme: MinesTheme, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.padding(top = 32.dp).width(250.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.KeyboardArrowLeft, null, tint = theme.textColor.copy(0.6f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ТЕМАТИКА", fontSize = 9.sp, color = theme.textColor.copy(0.4f))
            Text(theme.name.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textColor)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowRight, null, tint = theme.textColor.copy(0.6f))
        }
    }
}
// Вынес CustomInputField отдельно для чистоты кода
@Composable
fun CustomInputField(label: String, value: String, theme: MinesTheme, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }) },
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = theme.textColor
        )
    )
}
@Composable
fun MenuTextButton(
    text: String,
    currentTheme: MinesTheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(200.dp) // Единая ширина для всех кнопок
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (currentTheme.isGlass) {
            // Эффект стекла (только фон блюрится)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(0.08f))
                    .blur(12.dp)
                    .border(0.5.dp, Color.White.copy(0.15f), RoundedCornerShape(12.dp))
            )
        } else {
            // Обычная тема
            Box(Modifier.fillMaxSize().background(currentTheme.cellClosed.copy(0.3f)))
        }

        Text(
            text = text.uppercase(),
            color = if (currentTheme.isGlass) Color.White else currentTheme.textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}