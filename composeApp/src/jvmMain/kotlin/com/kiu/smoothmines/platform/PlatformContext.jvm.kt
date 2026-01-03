package com.kiu.smoothmines.platform

import androidx.compose.runtime.Composable

actual class PlatformContext actual constructor(context: Any)

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext(Unit)

@Composable
actual fun androidContext(): Any {
    return Unit
}