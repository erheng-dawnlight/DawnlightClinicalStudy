package com.example.dawnlightclinicalstudy.presentation

import android.app.Application
import com.didichuxing.doraemonkit.DoraemonKit
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        DoraemonKit.install(this)
    }
}