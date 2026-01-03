package com.kiu.smoothmines.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kiu.smoothmines.platform.PlatformSettings

// This object will be initialized in the App's initialization
object GameSettings {
    // These will be initialized by the SettingsManager
    var showBorders by mutableStateOf(true)
    var animationSpeed by mutableStateOf(300L)
    var vibrationEnabled by mutableStateOf(true)
}

// Global settings instance
val globalSettings = GameSettings

// Initialize settings manager
fun initSettings(platformSettings: PlatformSettings): SettingsManager {
    return SettingsManager(platformSettings.settings)
}