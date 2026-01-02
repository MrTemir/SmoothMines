package com.kiu.smoothmines.di

import android.content.Context

lateinit var appContext: Context

fun initializeAppContext(context: Context) {
    appContext = context.applicationContext
}
