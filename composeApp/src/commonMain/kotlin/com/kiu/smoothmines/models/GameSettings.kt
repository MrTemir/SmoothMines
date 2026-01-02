package com.kiu.smoothmines.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// В файле, где объявлен globalSettings
class GameSettings {
    var showBorders by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(true)
    var animationSpeed by mutableStateOf(300L) // скорость анимации в мс
}

var globalSettings = GameSettings()
// Глобальное состояние настроек в App.kt
