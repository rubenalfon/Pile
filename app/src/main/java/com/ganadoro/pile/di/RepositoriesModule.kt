package com.ganadoro.pile.di

import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentImageRepositoryImpl
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.DocumentModelRepositoryImpl
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.repositories.PileModelRepositoryImpl
import org.koin.dsl.module

val repositoriesModule = module {
    single<PileModelRepository> {
        PileModelRepositoryImpl(
            databaseQueries = get()
        )
    }

    single<DocumentModelRepository> {
        DocumentModelRepositoryImpl(
            databaseQueries = get()
        )
    }

    single<DocumentImageRepository> {
        DocumentImageRepositoryImpl(
            databaseQueries = get()
        )
    }

    single<BitmapCacheRepository> {
        BitmapCacheRepository(
            appContext = get()
        )
    }
}