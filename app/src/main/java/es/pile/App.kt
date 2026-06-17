package es.pile

import android.app.Application
import es.pile.core.activities.di.activitiesModule
import es.pile.core.di.appModule
import es.pile.core.di.dataModule
import es.pile.core.di.databaseModule
import es.pile.core.di.domainModule
import es.pile.features.addDocument.di.addDocumentModule
import es.pile.features.documentDetail.di.documentDetailModule
import es.pile.features.editDocument.di.editDocumentModule
import es.pile.features.externalImport.di.externalImportModule
import es.pile.features.home.di.homeModule
import es.pile.features.onboarding.di.onboardingModule
import es.pile.features.pileDetail.di.pileDetailModule
import es.pile.features.search.di.searchModule
import es.pile.features.settings.di.settingsModule
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
                onboardingModule,
                searchModule,
                homeModule,
                pileDetailModule,
                documentDetailModule,
                editDocumentModule,
                addDocumentModule,
                externalImportModule,
                settingsModule
            )
        }
    }
}