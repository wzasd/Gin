package com.founicy.demo.dlgin.app

import android.app.Application
import timber.log.Timber

class DLGinDemoApp:Application() {

    override fun onCreate() {
        super.onCreate()
        dlGinDemoApp = this

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object{
        @JvmStatic
        lateinit var dlGinDemoApp : Application
            private set
    }
}