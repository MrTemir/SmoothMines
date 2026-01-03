package com.kiu.smoothmines.platform

import androidx.compose.runtime.Composable

expect class PlatformContext(context: Any)

@Composable
expect fun getPlatformContext(): PlatformContext

@Composable
expect fun androidContext(): Any
