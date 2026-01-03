package com.kiu.smoothmines.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
// ВАЖНО: Импортируй свой класс приложения или используй Static Context
import com.kiu.smoothmines.di.appContext

actual class PlatformSettings actual constructor() {
    actual val settings: Settings = SharedPreferencesSettings(
        appContext.getSharedPreferences("smooth_mines_prefs", Context.MODE_PRIVATE)
    )
}