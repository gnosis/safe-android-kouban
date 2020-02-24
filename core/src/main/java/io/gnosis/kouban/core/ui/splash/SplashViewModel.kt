package io.gnosis.kouban.core.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.ViewState
import kotlinx.coroutines.delay
import pm.gnosis.model.Solidity

class SplashViewModel(private val safeAddressManager: SafeAddressManager) : ViewModel() {

    fun loadAddress() =
        liveData(viewModelScope.coroutineContext) {
            delay(500)
            safeAddressManager.getSafeAddress()?.let { emit(SafeAddressAvailable(it)) }
                ?: emit(SafeAddressUnavailable)

        }
}

data class SafeAddressAvailable(val safeAddress: Solidity.Address) : ViewState()

object SafeAddressUnavailable : ViewState()
