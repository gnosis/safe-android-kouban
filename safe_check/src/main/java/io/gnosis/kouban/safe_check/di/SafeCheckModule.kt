package io.gnosis.kouban.safe_check.di

import io.gnosis.kouban.safe_check.ui.SafeCheckFragment
import io.gnosis.kouban.safe_check.ui.SafeCheckViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {



    scope(named<SafeCheckFragment>()) {
        viewModel {
            SafeCheckViewModel(get(), get(), get())
        }
    }
}
