package com.ganadoro.pile.di

import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.DocumentModelRepositoryImpl
import com.ganadoro.pile.repositories.DocumentOrganizationRepository
import com.ganadoro.pile.repositories.DocumentOrganizationRepositoryImpl
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.repositories.PileModelRepositoryImpl
import org.koin.dsl.module

val repositoriesModule = module {
    single<PileModelRepository> {
        PileModelRepositoryImpl(
            databaseQueries = get()
        )
    }

    single<DocumentOrganizationRepository> {
        DocumentOrganizationRepositoryImpl(
            databaseQueries = get()
        )
    }

    single<DocumentModelRepository> {
        DocumentModelRepositoryImpl(
            databaseQueries = get()
        )
    }
}