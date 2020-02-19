package io.gnosis.kouban.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.runBlocking
import pm.gnosis.model.Solidity

typealias OnAddressChangedListener = (Solidity.Address?) -> Unit

class MainViewModel : ViewModel() {

    var address: Solidity.Address? = null
        set(value) {
            runBlocking {
                listeners.forEach { it(value) }
                field = value
            }
        }

    private val listeners = mutableListOf<OnAddressChangedListener>()

    fun addListener(callback: OnAddressChangedListener) = listeners.add(callback)

    fun removeListener(callback: OnAddressChangedListener) = listeners.remove(callback)
}
