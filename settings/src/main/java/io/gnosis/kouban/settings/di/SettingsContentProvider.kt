package io.gnosis.kouban.settings.di

import io.gnosis.instantsafe.core.di.CoreContentProvider
import org.koin.core.context.loadKoinModules

class SettingsContentProvider : CoreContentProvider() {

    override fun onCreate(): Boolean {
        loadKoinModules(settingsModule)
        return true
    }
}
