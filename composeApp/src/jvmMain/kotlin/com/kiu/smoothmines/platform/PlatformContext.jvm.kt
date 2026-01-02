package com.kiu.smoothmines.platform

import androidx.compose.runtime.Composable

actual class PlatformContext actual constructor()

actual fun getPlatformContext(): PlatformContext = PlatformContext()

@Composable
actual fun androidContext(): Any {
    TODO("Not yet implemented")
}