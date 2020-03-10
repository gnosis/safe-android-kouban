package io.gnosis.kouban.di

import androidx.core.os.ConfigurationCompat
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.utils.DateFormats
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.ui.address.capture.AddressCaptureFragment
import io.gnosis.kouban.ui.address.capture.AddressCaptureViewModel
import io.gnosis.kouban.ui.address.complete.AddressCompleteFragment
import io.gnosis.kouban.ui.address.complete.AddressCompleteViewModel
import io.gnosis.kouban.ui.filter.transaction.TokenSymbolFilterViewHolder
import io.gnosis.kouban.ui.filter.transaction.TransactionFilterDialog
import io.gnosis.kouban.ui.filter.transaction.TransactionFilterFactory
import io.gnosis.kouban.ui.filter.transaction.TransactionFilterViewModel
import io.gnosis.kouban.ui.splash.SplashFragment
import io.gnosis.kouban.ui.splash.SplashViewModel
import io.gnosis.kouban.ui.transaction.TransactionsFactory
import io.gnosis.kouban.ui.transaction.TransactionsFragment
import io.gnosis.kouban.ui.transaction.TransactionsViewModel
import io.gnosis.kouban.ui.transaction.details.TransactionDetailFactory
import io.gnosis.kouban.ui.transaction.details.TransactionDetailsFragment
import io.gnosis.kouban.ui.transaction.details.TransactionDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.text.DateFormat
import java.text.SimpleDateFormat

val fragmentModule = module {

    scope(named<TransactionsFragment>()) {
        viewModel {
            TransactionsViewModel(
                get(),
                get(),
                get(),
                SimpleDateFormat(
                    DateFormats.monthYear,
                    ConfigurationCompat.getLocales(androidContext().resources.configuration)[0]
                )
            )
        }
        factory { BaseAdapter(TransactionsFactory(get())) }
    }

    scope(named<SplashFragment>()) {
        viewModel { SplashViewModel(get()) }
    }

    scope(named<AddressCaptureFragment>()) {
        viewModel { AddressCaptureViewModel(get()) }
    }

    scope(named<AddressCompleteFragment>()) {
        viewModel { AddressCompleteViewModel(get()) }
    }

    scope(named<TransactionFilterDialog>()) {
        viewModel { TransactionFilterViewModel(get()) }
        factory { BaseAdapter(TransactionFilterFactory(get())) }
    }

    scope(named<TransactionDetailsFragment>()) {
        viewModel { (transaction: Transaction) -> TransactionDetailsViewModel(transaction, get(), get()) }
        factory { BaseAdapter(TransactionDetailFactory()) }
    }
}
