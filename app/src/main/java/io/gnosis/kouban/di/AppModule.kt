package io.gnosis.kouban.di

import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.core.ui.helper.AddressHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { Picasso.get() }
    single { AddressHelper() }
    single { SearchManager() }
    factory { SafeAddressManager(androidContext()) }
}
