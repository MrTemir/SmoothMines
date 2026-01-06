package com.kiu.smoothmines.utils

expect class SettingsManager() {
    var showBorders: Boolean
    var vibrationEnabled: Boolean
    var longPressDelay: Long
    var savedThemeIndex: Int
    var animationSpeed: Long
    var shakeIntensity: Float

    fun updateShowBorders(value: Boolean)
    fun updateVibrationEnabled(value: Boolean)
    fun updateLongPressDelay(value: Long)
    fun updateThemeIndex(value: Int)
    fun updateAnimationSpeed(value: Long)
    fun updateShakeIntensity(value: Float)
}