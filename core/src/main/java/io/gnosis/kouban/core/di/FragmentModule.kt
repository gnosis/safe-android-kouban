package io.gnosis.kouban.core.di

import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.safe.balance.BalanceFactory
import io.gnosis.kouban.core.ui.safe.balance.BalancesFragment
import io.gnosis.kouban.core.ui.safe.balance.BalancesViewModel
import io.gnosis.kouban.core.ui.safe.transaction.TransactionsFactory
import io.gnosis.kouban.core.ui.safe.transaction.TransactionsFragment
import io.gnosis.kouban.core.ui.safe.transaction.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val fragmentModule = module {
    scope(named<BalancesFragment>()) {
        viewModel {
            BalancesViewModel(
                get(),
                get()
            )
        }
        factory { BaseAdapter(BalanceFactory(get())) }
    }

    scope(named<TransactionsFragment>()) {
        viewModel { TransactionsViewModel(get()) }
        factory { BaseAdapter(TransactionsFactory()) }
    }

    viewModel { MainViewModel() }
}
