package com.kiu.smoothmines.platform

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class PlatformContext(context: Context) {
    val androidContext: Context = context
}

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext(LocalContext.current)

@Composable
actual fun androidContext(): Any = LocalContext.current