package com.ganadoro.pile.di

import com.ganadoro.pile.ui.navigation.DocumentOpener
import com.ganadoro.pile.ui.navigation.DocumentOpenerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val appModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<DocumentOpener> { DocumentOpenerImpl(get()) }
}