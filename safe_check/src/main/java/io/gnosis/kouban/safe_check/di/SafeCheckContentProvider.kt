package io.gnosis.kouban.safe_check.di

import io.gnosis.kouban.core.di.CoreContentProvider
import org.koin.core.context.loadKoinModules

class SafeCheckContentProvider : CoreContentProvider() {

    override fun onCreate(): Boolean {
        loadKoinModules(settingsModule)
        return true
    }
}
