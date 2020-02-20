package io.gnosis.kouban.core.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.ViewState
import pm.gnosis.model.Solidity

class OnboardingViewModel(private val safeAddressManager: SafeAddressManager) : ViewModel() {

    fun handleSafeAddress(safeAddress: Solidity.Address) {
        safeAddressManager.storeSafeAddress(safeAddress)
    }

    fun submitAddress(): Solidity.Address =
        safeAddressManager.getSafeAddress() ?: throw AddressNotSet()
}

class AddressNotSet() : Throwable()
