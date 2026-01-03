package com.kiu.smoothmines.models

/**
 * Manages application settings with persistence
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
        private const val KEY_SHAKE_INTENSITY = "shake_intensity"
        private const val KEY_FPS_LIMIT = "fps_limit" // Новый ключ
    }

    // Приводим к одному имени, которое используется в UI
    var showBorders by mutableStateOf(settings.getBoolean(KEY_SHOW_BORDERS, false))
        private set

    // Добавляем сохранение FPS
    var fpsLimit by mutableFloatStateOf(settings.getFloat(KEY_FPS_LIMIT, 60f))
        private set

    var animationSpeed by mutableLongStateOf(settings.getLong(KEY_ANIMATION_SPEED, 300L))
        private set

    var vibrationEnabled by mutableStateOf(settings.getBoolean(KEY_VIBRATION_ENABLED, true))
        private set

    var savedThemeIndex by mutableIntStateOf(settings.getInt(KEY_SAVED_THEME_INDEX, 0))
        private set

    var shakeIntensity by mutableFloatStateOf(settings.getFloat(KEY_SHAKE_INTENSITY, 0.5f))
        private set

    // Методы обновления
    fun updateShowBorders(value: Boolean) {
        showBorders = value
        settings[KEY_SHOW_BORDERS] = value
    }

    fun updateFpsLimit(limit: Float) {
        fpsLimit = limit
        settings[KEY_FPS_LIMIT] = limit
    }

    fun updateAnimationSpeed(value: Long) {
        animationSpeed = value
        settings[KEY_ANIMATION_SPEED] = value
    }

    fun updateVibrationEnabled(value: Boolean) {
        vibrationEnabled = value
        settings[KEY_VIBRATION_ENABLED] = value
    }

    fun updateThemeIndex(value: Int) {
        savedThemeIndex = value
        settings[KEY_SAVED_THEME_INDEX] = value
    }

    fun updateShakeIntensity(newValue: Float) {
        shakeIntensity = newValue
        settings[KEY_SHAKE_INTENSITY] = newValue
    }
}