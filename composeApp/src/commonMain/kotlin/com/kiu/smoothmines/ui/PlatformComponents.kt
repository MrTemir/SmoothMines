// Файл: jvmMain/.../ui/PlatformComponents.kt
package com.kiu.smoothmines.ui

import androidx.compose.runtime.Composable

@Composable
expect fun MineBackHandler(onBack: () -> Unit)
    // Ничего не делаем, на ПК это не нужно
