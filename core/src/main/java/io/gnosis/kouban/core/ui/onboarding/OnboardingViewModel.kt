package io.gnosis.kouban.core.ui.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pm.gnosis.model.Solidity

class OnboardingViewModel(private val safeAddressManager: SafeAddressManager) : ViewModel() {

    val safeAddressEvents = MutableLiveData<ViewState>()

    fun handleSafeAddress(safeAddress: Solidity.Address) {
        viewModelScope.launch(Dispatchers.IO) {
            safeAddressManager.storeSafeAddress(safeAddress)
            safeAddressEvents.postValue(SafeAddressUpdated(safeAddress))
        }
    }

    fun submitAddress() {
        viewModelScope.launch(Dispatchers.IO) {
            safeAddressManager.getSafeAddress()?.run { safeAddressEvents.postValue(SafeAddressStored(this)) }
                ?: safeAddressEvents.postValue(Error(AddressNotSet()))
        }
    }
}

data class SafeAddressStored(val safeAddress: Solidity.Address) : ViewState()
data class SafeAddressUpdated(val safeAddress: Solidity.Address) : ViewState()
class AddressNotSet : Throwable()
