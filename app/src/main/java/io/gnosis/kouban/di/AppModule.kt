package io.gnosis.kouban.di

import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.ui.helper.AddressHelper
import org.koin.dsl.module

val appModule = module {
    single { Picasso.get() }
    single { AddressHelper() }
}
