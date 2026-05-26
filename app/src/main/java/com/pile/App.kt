package com.pile

import android.app.Application
import com.pile.core.activities.di.activitiesModule
import com.pile.core.di.appModule
import com.pile.core.di.dataModule
import com.pile.core.di.databaseModule
import com.pile.core.di.domainModule
import com.pile.features.addDocument.di.addDocumentModule
import com.pile.features.documentDetail.di.documentDetailModule
import com.pile.features.editDocument.di.editDocumentModule
import com.pile.features.home.di.homeModule
import com.pile.features.pileDetail.di.pileDetailModule
import com.pile.features.search.di.searchBarModule
import com.pile.features.settings.di.settingsModule
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