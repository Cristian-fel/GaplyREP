package com.gaply.app

import android.app.Application
import com.gaply.app.di.AppContainer

class GaplyApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
