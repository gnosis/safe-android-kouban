package io.gnosis.kouban.core.di

import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.onboarding.OnboardingFragment
import io.gnosis.kouban.core.ui.onboarding.OnboardingViewModel
import io.gnosis.kouban.core.ui.splash.SplashFragment
import io.gnosis.kouban.core.ui.splash.SplashViewModel
import io.gnosis.kouban.core.ui.transaction.TransactionsFactory
import io.gnosis.kouban.core.ui.transaction.TransactionsFragment
import io.gnosis.kouban.core.ui.transaction.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val fragmentModule = module {

    scope(named<TransactionsFragment>()) {
        viewModel { TransactionsViewModel(get()) }
        factory { BaseAdapter(TransactionsFactory()) }
    }

    scope(named<SplashFragment>()) {
        viewModel { SplashViewModel(get()) }
    }

    scope(named<OnboardingFragment>()) {
        viewModel { OnboardingViewModel(get()) }
    }
}
