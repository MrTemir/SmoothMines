// Файл: androidMain/.../ui/PlatformComponents.kt
package com.kiu.smoothmines.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun MineBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack) // Используем родной Android BackHandler
}