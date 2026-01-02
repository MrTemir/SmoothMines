package com.kiu.smoothmines.utils

expect class SettingsManager() {
    fun saveBorders(show: Boolean)
    fun getBorders(): Boolean
}