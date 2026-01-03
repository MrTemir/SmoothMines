package com.kiu.smoothmines.platform

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class PlatformContext actual constructor(context: Any) {
    val androidContext: Context = context as Context
}

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext(LocalContext.current)

@Composable
actual fun androidContext(): Any = LocalContext.current