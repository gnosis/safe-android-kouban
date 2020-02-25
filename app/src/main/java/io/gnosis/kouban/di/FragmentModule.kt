package io.gnosis.kouban.di

import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.ui.address.capture.AddressCaptureFragment
import io.gnosis.kouban.ui.address.capture.AddressCaptureViewModel
import io.gnosis.kouban.ui.address.complete.AddressCompleteFragment
import io.gnosis.kouban.ui.address.complete.AddressCompleteViewModel
import io.gnosis.kouban.ui.splash.SplashFragment
import io.gnosis.kouban.ui.splash.SplashViewModel
import io.gnosis.kouban.ui.transaction.TransactionsFactory
import io.gnosis.kouban.ui.transaction.TransactionsFragment
import io.gnosis.kouban.ui.transaction.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val fragmentModule = module {

    scope(named<io.gnosis.kouban.ui.transaction.TransactionsFragment>()) {
        viewModel { io.gnosis.kouban.ui.transaction.TransactionsViewModel(get()) }
        factory { BaseAdapter(io.gnosis.kouban.ui.transaction.TransactionsFactory()) }
    }

    scope(named<io.gnosis.kouban.ui.splash.SplashFragment>()) {
        viewModel { io.gnosis.kouban.ui.splash.SplashViewModel(get()) }
    }

    scope(named<io.gnosis.kouban.ui.address.capture.AddressCaptureFragment>()) {
        viewModel { io.gnosis.kouban.ui.address.capture.AddressCaptureViewModel(get()) }
    }

    scope(named<io.gnosis.kouban.ui.address.complete.AddressCompleteFragment>()) {
        viewModel { io.gnosis.kouban.ui.address.complete.AddressCompleteViewModel(get()) }
    }
}
