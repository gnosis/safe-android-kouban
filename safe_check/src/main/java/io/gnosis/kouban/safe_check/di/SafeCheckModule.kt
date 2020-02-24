package io.gnosis.kouban.safe_check.di

import io.gnosis.kouban.safe_check.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {

//    scope(named<SettingsFragment>()) {
//        factory { BaseAdapter(SafeOwnerFactory(get())) }
//    }

    viewModel {
        SettingsViewModel(get(), get())
    }
}
