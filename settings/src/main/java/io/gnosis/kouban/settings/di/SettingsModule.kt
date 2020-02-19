package io.gnosis.kouban.settings.di

import io.gnosis.instantsafe.core.ui.adapter.BaseAdapter
import io.gnosis.instantsafe.settings.ui.SafeOwnerFactory
import io.gnosis.instantsafe.settings.ui.SettingsFragment
import io.gnosis.instantsafe.settings.ui.SettingsViewModel
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
