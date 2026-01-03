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

    /**
     * Инициализация сервиса вибрации.
     * Теперь код активен и готов к работе.
     */
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

    /**
     * Метод для запуска короткого вибро-отклика.
     * Используем 40мс — это идеальное время для тактильного "щелчка" при открытии мины.
     */
    actual fun triggerVibration() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Создаем короткий "выстрел"
                v.vibrate(VibrationEffect.createOneShot(40L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(40L)
            }
        }
    }

    /**
     * Праздничная вибрация для победы (двойной короткий удар)
     */
    fun triggerSuccessVibration() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 50, 50) // пауза, вибро, пауза, вибро
                val amplitudes = intArrayOf(0, 255, 0, 255)
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 50, 50, 50), -1)
            }
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