package com.kiu.smoothmines.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class VibrationHelper private constructor() {
    private var vibrator: Vibrator? = null
    private var context: Context? = null

    actual fun initialize(context: Any) {
        if (context is Context) {
            this.context = context
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
    }

    actual fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    actual companion object {
        private var instance: VibrationHelper? = null

        @Composable
        actual fun getInstance(): VibrationHelper {
            val context = LocalContext.current
            return instance ?: synchronized(this) {
                instance ?: VibrationHelper().also {
                    it.initialize(context)
                    instance = it
                }
            }
        }
    }
}
