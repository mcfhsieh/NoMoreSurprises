package com.mhsieh.myapplication

import android.app.Application
import timber.log.Timber

class NoMoreSurprisesApp: Application(){
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

    }
}