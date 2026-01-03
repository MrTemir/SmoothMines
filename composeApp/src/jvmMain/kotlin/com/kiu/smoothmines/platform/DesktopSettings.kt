package com.kiu.smoothmines.platform

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings

actual class PlatformSettings actual constructor() {
    // MapSettings точно будет работать на всех ПК без доп. настроек
    actual val settings: Settings = MapSettings()
}