package es.pile.features.externalImport.di

import es.pile.features.externalImport.ui.ImportViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val externalImportModule = module {
    viewModelOf(::ImportViewModel)
}
