package com.kiu.smoothmines

import android.app.Application
import com.kiu.smoothmines.di.initializeAppContext

class SmoothMinesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAppContext(this)
    }
}
