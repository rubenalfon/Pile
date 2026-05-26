package com.ganadoro.pile

import android.app.Application
import com.ganadoro.pile.core.activities.di.activitiesModule
import com.ganadoro.pile.core.di.appModule
import com.ganadoro.pile.core.di.dataModule
import com.ganadoro.pile.core.di.databaseModule
import com.ganadoro.pile.core.di.domainModule
import com.ganadoro.pile.features.addDocument.di.addDocumentModule
import com.ganadoro.pile.features.documentDetail.di.documentDetailModule
import com.ganadoro.pile.features.editDocument.di.editDocumentModule
import com.ganadoro.pile.features.home.di.homeModule
import com.ganadoro.pile.features.pileDetail.di.pileDetailModule
import com.ganadoro.pile.features.search.di.searchBarModule
import com.ganadoro.pile.features.settings.di.settingsModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
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
            workManagerFactory()
            modules(
                appModule,
                databaseModule,
                dataModule,
                domainModule,
                activitiesModule,
                homeModule,
                searchBarModule,
                pileDetailModule,
                documentDetailModule,
                editDocumentModule,
                addDocumentModule,
                settingsModule
            )
        }
    }
}