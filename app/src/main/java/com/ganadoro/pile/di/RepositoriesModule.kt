package com.ganadoro.pile.di

import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.repositories.PileModelRepositoryImpl
import org.koin.dsl.module

val repositoriesModule = module {
    single<PileModelRepository> {
        PileModelRepositoryImpl(
            databaseQueries = get()
        )
    }
}