package com.kiu.smoothmines.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class VibrationHelper actual constructor() { // Должен быть actual constructor
    private var vibrator: Vibrator? = null

    actual fun initialize(context: Any) {
        if (context is Context) {
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
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(40L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(40L)
            }
        }
    }

    // Обычные методы (не actual) можно добавлять свободно
    fun triggerSuccessVibration() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 100, 50)
                v.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 50, 100, 50), -1)
            }
        }
    }

    actual companion object {
        private var instance: VibrationHelper? = null

        actual fun getInstance(): VibrationHelper {
            return instance ?: synchronized(this) {
                instance ?: VibrationHelper().also { instance = it }
            }
        }
    }
}