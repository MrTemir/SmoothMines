package com.kiu.smoothmines.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiu.smoothmines.models.Difficulties
import com.kiu.smoothmines.models.GameConfig
import com.kiu.smoothmines.models.SaveData
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kiu.smoothmines.models.globalSettings

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
fun SettingsDialog(onDismiss: () -> Unit, theme: MinesTheme) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cellOpened,
        title = { Text("Настройки", color = theme.textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Переключатель границ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Линии сетки", modifier = Modifier.weight(1f), color = theme.textColor)
                    Switch(
                        checked = globalSettings.showBorders,
                        onCheckedChange = { globalSettings.showBorders = it }
                    )
                }

                // Скорость анимации
                Column {
                    Text("Скорость анимации: ${globalSettings.animationSpeed}мс", color = theme.textColor)
                    Slider(
                        value = globalSettings.animationSpeed.toFloat(),
                        onValueChange = { globalSettings.animationSpeed = it.toLong() },
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(thumbColor = theme.accent)
                    )
                }

                // Вибрация
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", modifier = Modifier.weight(1f), color = theme.textColor)
                    Switch(
                        checked = globalSettings.vibrationEnabled,
                        onCheckedChange = { globalSettings.vibrationEnabled = it }
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
    savedGames: List<SaveData>
)  {
    var showCustomDialog by remember { mutableStateOf(false) }

    // Состояния для полей ввода
    var rowsInput by remember { mutableStateOf("20") }
    var colsInput by remember { mutableStateOf("20") }
    var minesInput by remember { mutableStateOf("40") }
    @Composable
    fun CustomInputField(label: String, value: String, theme: MinesTheme, onValueChange: (String) -> Unit) {
        OutlinedTextField(
            value = value,
            onValueChange = { inputString ->
                // Оставляем только цифры
                val filtered = inputString.filter { char -> char.isDigit() }
                onValueChange(filtered)
            },
            label = { Text(label, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accent,
                unfocusedBorderColor = theme.textColor.copy(0.3f),
                focusedLabelColor = theme.accent,
                cursorColor = theme.accent,
                focusedTextColor = theme.textColor,
                unfocusedTextColor = theme.textColor
            )
        )
    }
    Surface(modifier = Modifier.fillMaxSize(), color = currentTheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Это обеспечит центрирование
        ) {
            Text(
                "SmoothMines",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = currentTheme.textColor
            )

            // Блок сохранений (появляется и сдвигает остальное)
            AnimatedVisibility(
                visible = savedGames.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("ПРОДОЛЖИТЬ", fontSize = 12.sp, color = currentTheme.textColor.copy(0.5f))

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

            // Кнопки новой игры
            Difficulties.forEach { config ->
                Button(
                    onClick = { onStartGame(config) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accent)
                ) {
                    Text(config.difficultyName, fontWeight = FontWeight.Bold)
                }
            }
                OutlinedButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, currentTheme.accent)
                ) {
                    Text("СВОЯ ИГРА", color = currentTheme.accent, fontWeight = FontWeight.Bold)
                }

            // Кнопка смены темы (внизу)
            TextButton(onClick = onNextTheme, modifier = Modifier.padding(top = 16.dp)) {
                Text("СМЕНИТЬ ТЕМУ", color = currentTheme.textColor.copy(0.6f))
            }
        }
        // ДИАЛОГОВОЕ ОКНО
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                containerColor = currentTheme.cellOpened,
                title = { Text("Настройки поля", color = currentTheme.textColor) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomInputField("Строк (max 50)", rowsInput, currentTheme) { rowsInput = it.filter { c -> c.isDigit() } }
                        CustomInputField("Столбцов (max 50)", colsInput, currentTheme) { colsInput = it.filter { c -> c.isDigit() } }
                        CustomInputField("Мин", minesInput, currentTheme) { minesInput = it.filter { c -> c.isDigit() } }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val r = rowsInput.toIntOrNull()?.coerceIn(5, 50) ?: 10
                            val c = colsInput.toIntOrNull()?.coerceIn(5, 50) ?: 10
                            val m = minesInput.toIntOrNull()?.coerceIn(1, (r * c) - 1) ?: 10

                            onStartGame(GameConfig(r, c, m, "Кастом"))
                            showCustomDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accent)
                    ) {
                        Text("Создать", color = currentTheme.cellOpened)
                    }
                }
            )
        }
    }
}
