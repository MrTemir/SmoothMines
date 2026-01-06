package com.kiu.smoothmines.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

actual class SettingsManager actual constructor() {
    private val settings: Settings = Settings()

    actual var showBorders by mutableStateOf(settings.getBoolean("show_borders", false))
    actual var vibrationEnabled by mutableStateOf(settings.getBoolean("vibration_enabled", true))
    actual var longPressDelay by mutableLongStateOf(settings.getLong("long_press_delay", 400L))
    actual var savedThemeIndex by mutableIntStateOf(settings.getInt("saved_theme_index", 0))
    actual var animationSpeed by mutableLongStateOf(settings.getLong("animation_speed", 300L))
    actual var shakeIntensity by mutableFloatStateOf(settings.getFloat("shake_intensity", 0.5f))

    actual fun updateShowBorders(value: Boolean) {
        showBorders = value
        settings["show_borders"] = value
    }

    actual fun updateVibrationEnabled(value: Boolean) {
        vibrationEnabled = value
        settings["vibration_enabled"] = value
    }

    actual fun updateLongPressDelay(value: Long) {
        longPressDelay = value
        settings["long_press_delay"] = value
    }

    actual fun updateThemeIndex(value: Int) {
        savedThemeIndex = value
        settings["saved_theme_index"] = value
    }

    actual fun updateAnimationSpeed(value: Long) {
        animationSpeed = value
        settings["animation_speed"] = value
    }

    actual fun updateShakeIntensity(value: Float) {
        shakeIntensity = value
        settings["shake_intensity"] = value
    }
}