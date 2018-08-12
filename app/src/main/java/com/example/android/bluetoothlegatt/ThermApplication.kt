package com.example.android.bluetoothlegatt

import android.app.Application

import timber.log.Timber

class ThermApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree());
        app = this
    }

    companion object {
        var app: ThermApplication? = null
    }
}
