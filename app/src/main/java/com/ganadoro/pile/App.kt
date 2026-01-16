package com.ganadoro.pile

import android.app.Application
import com.ganadoro.pile.di.appModule
import com.ganadoro.pile.di.databaseModule
import com.ganadoro.pile.di.domainModule
import com.ganadoro.pile.di.repositoriesModule
import com.ganadoro.pile.di.viewmodelModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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
                appModule,
                databaseModule,
                repositoriesModule,
                domainModule,
                viewmodelModule
            )
        }
    }
}