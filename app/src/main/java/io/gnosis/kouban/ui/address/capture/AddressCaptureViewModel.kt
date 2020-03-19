package io.gnosis.kouban.ui.address.capture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.push.PushServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pm.gnosis.model.Solidity

class AddressCaptureViewModel(
    private val safeRepository: SafeRepository,
    private val safeAddressManager: SafeAddressManager,
    private val pushRepo: PushServiceRepository
) : ViewModel() {

    val safeAddressEvents = MutableLiveData<ViewState>()

    fun handleSafeAddress(safeAddress: Solidity.Address) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { safeRepository.checkSafe(safeAddress) }
                .onSuccess { (masterCopyAddress, isValid) ->
                    if (masterCopyAddress != null && isValid && SafeRepository.isSupported(masterCopyAddress)) {
                        safeAddressManager.storeSafeAddress(safeAddress)
                        pushRepo.checkRegistration()
                        safeAddressEvents.postValue(SafeAddressUpdated(safeAddress))
                    } else {
                        safeAddressEvents.postValue(Error(InvalidSafeAddress()))
                    }
                }.onFailure {
                    safeAddressEvents.postValue(Error(it))
                }
        }
    }

    fun submitAddress() {
        viewModelScope.launch(Dispatchers.IO) {
            safeAddressManager.getSafeAddress()?.run {
                safeAddressEvents.postValue(SafeAddressStored(this))
            } ?: safeAddressEvents.postValue(Error(AddressNotSet()))
        }
    }
}

data class SafeAddressStored(val safeAddress: Solidity.Address) : ViewState()
data class SafeAddressUpdated(val safeAddress: Solidity.Address) : ViewState()
class AddressNotSet : Throwable()
class InvalidSafeAddress : Throwable()
