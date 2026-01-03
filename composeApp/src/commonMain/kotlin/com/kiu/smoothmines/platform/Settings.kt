package com.kiu.smoothmines.platform

import com.russhwolf.settings.Settings

// Убедись, что тут НЕТ конструктора с параметрами
expect class PlatformSettings() {
    val settings: Settings
}