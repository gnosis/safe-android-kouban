package io.gnosis.kouban.di

import android.content.SharedPreferences
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.helper.AddressHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { Picasso.get() }
    single { AddressHelper() }
    factory { SafeAddressManager(androidContext()) }
}
