package com.kiu.smoothmines.platform

import androidx.compose.runtime.Composable

actual class BackHandler actual constructor(
    private val enabled: Boolean,
    private val onBack: () -> Unit
) {
    fun onBackPressed(): Boolean {
        return if (enabled) {
            onBack()
            true
        } else {
            false
        }
    }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // On desktop, we don't handle back button by default
    // You can add keyboard shortcut handling here if needed
}
