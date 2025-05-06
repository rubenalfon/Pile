package com.ganadoro.pile

import android.app.Application
import android.util.Log
import com.ganadoro.pile.di.viewmodelModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("APP", "BuildConfig.DEBUG ${BuildConfig.DEBUG}")
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                viewmodelModule
            )
        }
    }
}