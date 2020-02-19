package io.gnosis.kouban.settings.di

import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.settings.ui.SafeOwnerFactory
import io.gnosis.kouban.settings.ui.SettingsFragment
import io.gnosis.kouban.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {

    scope(named<SettingsFragment>()) {
        factory { BaseAdapter(SafeOwnerFactory(get())) }
    }

    viewModel {
        SettingsViewModel(get(), get())
    }
}
