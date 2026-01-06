package com.kiu.smoothmines.utils

expect class VibrationHelper() {
    fun triggerVibration()
    fun initialize(context: Any)

    companion object {
        fun getInstance(): VibrationHelper
    }
}