package com.ganadoro.pile.di

import androidx.work.WorkManager
import com.ganadoro.pile.data.workers.CleanupWorker
import com.ganadoro.pile.ui.navigation.DocumentOpener
import com.ganadoro.pile.ui.navigation.DocumentOpenerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val appModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<DocumentOpener> { DocumentOpenerImpl(get()) }

    single { WorkManager.getInstance(androidContext()) }
    workerOf(::CleanupWorker)
}