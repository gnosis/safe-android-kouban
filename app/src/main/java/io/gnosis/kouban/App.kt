package io.gnosis.kouban

import android.app.Application
import android.content.Context
import io.gnosis.kouban.di.appModule
import io.gnosis.kouban.core.di.fragmentModule
import io.gnosis.kouban.core.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // some of your own operations before content provider will launch
        startKoin {
            // Android context
            androidContext(this@App)
            modules(listOf(appModule, networkModule, fragmentModule))
        }
    }
}
