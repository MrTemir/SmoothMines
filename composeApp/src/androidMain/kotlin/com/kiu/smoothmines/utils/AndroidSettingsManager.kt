package com.kiu.smoothmines.utils

import android.content.Context
import com.kiu.smoothmines.di.appContext

actual class SettingsManager actual constructor() {
    private val prefs = appContext.getSharedPreferences("mines_settings", Context.MODE_PRIVATE)

    actual fun saveBorders(show: Boolean) {
        prefs.edit().putBoolean("borders", show).apply()
    }

    actual fun getBorders(): Boolean {
        return prefs.getBoolean("borders", true)
    }
}
