package com.kiu.smoothmines.models

/**
 * Manages application settings with persistence
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsManager(private val settings: Settings) {
    companion object {
        private const val KEY_SHOW_BORDERS = "show_borders"
        private const val KEY_ANIMATION_SPEED = "animation_speed"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SAVED_THEME_INDEX = "saved_theme_index"
    }

    // Используем 'by mutableStateOf' — это свяжет данные с интерфейсом Compose
    var showBorders by mutableStateOf(settings.getBoolean(KEY_SHOW_BORDERS, true))
        private set

    var animationSpeed by mutableLongStateOf(settings.getLong(KEY_ANIMATION_SPEED, 300L))
        private set

    var vibrationEnabled by mutableStateOf(settings.getBoolean(KEY_VIBRATION_ENABLED, true))
        private set

    var savedThemeIndex by mutableIntStateOf(settings.getInt(KEY_SAVED_THEME_INDEX, 0))
        private set

    // Функции для обновления (вызываются из UI)
    fun updateShowBorders(value: Boolean) {
        showBorders = value
        settings[KEY_SHOW_BORDERS] = value
        globalSettings.showBorders = value
    }

    fun updateAnimationSpeed(value: Long) {
        animationSpeed = value
        settings[KEY_ANIMATION_SPEED] = value
        globalSettings.animationSpeed = value
    }

    fun updateVibrationEnabled(value: Boolean) {
        vibrationEnabled = value
        settings[KEY_VIBRATION_ENABLED] = value
        globalSettings.vibrationEnabled = value
    }

    fun updateThemeIndex(value: Int) {
        savedThemeIndex = value
        settings[KEY_SAVED_THEME_INDEX] = value
    }
}