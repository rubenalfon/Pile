package com.ganadoro.pile

import android.app.Application
import com.ganadoro.pile.di.viewmodelModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.BuildConfig
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

//        if (BuildConfig.DEBUG) {
        Napier.base(DebugAntilog())
//        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                viewmodelModule
            )
        }
    }
}