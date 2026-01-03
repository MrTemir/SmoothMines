package com.kiu.smoothmines.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SettingsDialog(onDismiss: () -> Unit, theme: MinesTheme, settingsManager: SettingsManager) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.background, // ИСПРАВЛЕНО: теперь фон непрозрачный
        modifier = Modifier.clip(RoundedCornerShape(28.dp)),
        title = { Text("Настройки", color = theme.textColor, fontWeight = FontWeight.Bold) },
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

                // Вибрация
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вибрация", modifier = Modifier.weight(1f), color = theme.textColor)
                    Switch(
                        checked = settingsManager.vibrationEnabled,
                        onCheckedChange = { settingsManager.updateVibrationEnabled(it) } // Исправлено
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

                // В SettingsDialog.kt
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Сила тряски: ${(settingsManager.shakeIntensity * 100).toInt()}%",
                    color = theme.textColor,
                    fontSize = 16.sp
                )
                // Слайдер для регулировки
                androidx.compose.material3.Slider(
                    value = settingsManager.shakeIntensity,
                    onValueChange = { settingsManager.updateShakeIntensity(it) },
                    valueRange = 0f..1f,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = theme.accent,
                        activeTrackColor = theme.accent,
                        inactiveTrackColor = theme.accent.copy(alpha = 0.2f)
                    )
                )
                Text("Лимит FPS: ${if(settingsManager.fpsLimit == 0f) "Макс" else settingsManager.fpsLimit.toInt()}", color = theme.textColor)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60f, 90f, 120f, 0f).forEach { limit ->
                        Button(
                            onClick = { settingsManager.updateFpsLimit(limit) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(settingsManager.fpsLimit == limit) theme.accent else theme.cellClosed
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if(limit == 0f) "∞" else limit.toInt().toString(), fontSize = 10.sp)
                        }
                    }
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

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Обновленные размеры согласно твоей просьбе (9x12, 16x18, 21x24)
                MenuTextButton("Новичок (9x12)", currentTheme) {
                    onStartGame(GameConfig(12, 9, 10, "Новичок"))
                }
                MenuTextButton("Любитель (16x18)", currentTheme) {
                    onStartGame(GameConfig(18, 16, 40, "Любитель"))
                }
                MenuTextButton("Эксперт (21x24)", currentTheme) {
                    onStartGame(GameConfig(24, 21, 99, "Эксперт"))
                }

                MenuTextButton("Своя игра", currentTheme) { showCustomDialog = true }
                MenuTextButton("Настройки", currentTheme) { showSettingsDialog = true }
            }

            ThemeSwitcher(currentTheme, onPrevTheme, onNextTheme)
        }

        if (showCustomDialog) {
            CustomGameDialog(
                theme = currentTheme,
                onDismiss = { showCustomDialog = false },
                onConfirm = { r, c, m ->
                    onStartGame(GameConfig(r, c, m, "Кастом"))
                    showCustomDialog = false
                }
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
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    // Начальные значения для кастомного уровня
    var r by remember { mutableStateOf(15f) }
    var c by remember { mutableStateOf(15f) }
    var m by remember { mutableStateOf(20f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .appleGlass(theme) // Эффект стекла
                .padding(24.dp)
                .clickable(enabled = false) { }, // Чтобы клик внутри не закрывал диалог
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "НАСТРОЙКА ПОЛЯ",
                color = theme.textColor,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            CustomSlider(
                label = "Строки",
                value = r,
                current = r.toInt(),
                min = 5f, max = 40f,
                theme = theme
            ) { r = it }

            CustomSlider(
                label = "Колонки",
                value = c,
                current = c.toInt(),
                min = 5f, max = 30f,
                theme = theme
            ) { c = it }

            // Ограничиваем кол-во мин, чтобы оно не превышало кол-во ячеек
            val maxMines = (r.toInt() * c.toInt() * 0.8f)
            if (m > maxMines) m = maxMines

            CustomSlider(
                label = "Мины",
                value = m,
                current = m.toInt(),
                min = 1f, max = maxMines,
                theme = theme
            ) { m = it }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { onConfirm(r.toInt(), c.toInt(), m.toInt()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accent,
                    contentColor = theme.background
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("ПОЕХАЛИ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CustomSlider(
    label: String,
    value: Float,
    current: Int,
    min: Float,
    max: Float,
    theme: MinesTheme,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = theme.textColor.copy(0.6f), fontSize = 12.sp)
            Text(current.toString(), color = theme.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = theme.accent,
                activeTrackColor = theme.accent,
                inactiveTrackColor = theme.textColor.copy(0.1f)
            )
        )
    }
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
fun MenuTextButton(text: String, currentTheme: MinesTheme, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(56.dp) // Фиксированная высота для стабильности
            .clip(RoundedCornerShape(16.dp))
            .background(currentTheme.cellClosed) // Пастельный фон ячейки
            .clickable { onClick() },
        contentAlignment = Alignment.Center // Центрируем текст строго посередине
    ) {
        Text(text.uppercase(), color = currentTheme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}