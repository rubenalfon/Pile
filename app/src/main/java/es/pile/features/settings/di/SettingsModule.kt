package es.pile.features.settings.di

import es.pile.features.settings.ui.overview.SettingsOverviewViewModel
import es.pile.features.settings.ui.resolution.SettingsResolutionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {

    viewModelOf(::SettingsOverviewViewModel)
    viewModelOf(::SettingsResolutionViewModel)
}