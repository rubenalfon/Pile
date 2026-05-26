package com.pile.features.settings.di

import com.pile.features.settings.ui.overview.SettingsOverviewViewModel
import com.pile.features.settings.ui.resolution.SettingsResolutionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {

    viewModelOf(::SettingsOverviewViewModel)
    viewModelOf(::SettingsResolutionViewModel)
}