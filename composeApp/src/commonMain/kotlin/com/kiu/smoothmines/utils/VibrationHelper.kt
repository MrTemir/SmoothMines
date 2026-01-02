package com.kiu.smoothmines.utils

import androidx.compose.runtime.Composable

expect class VibrationHelper {
    fun triggerVibration()
    fun initialize(context: Any)
    
    companion object {
        @Composable
        fun getInstance(): VibrationHelper
    }
}
