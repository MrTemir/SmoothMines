package com.kiu.smoothmines.utils

actual class SettingsManager actual constructor() {
    private var borders: Boolean = true

    actual fun saveBorders(show: Boolean) {
        borders = show
    }

    actual fun getBorders(): Boolean {
        return borders
    }
}