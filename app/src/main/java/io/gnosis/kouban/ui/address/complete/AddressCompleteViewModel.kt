package io.gnosis.kouban.ui.address.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pm.gnosis.model.Solidity

class AddressCompleteViewModel(private val safeAddressManager: SafeAddressManager) : ViewModel() {

    val viewState = MutableLiveData<ViewState>()

    fun setupComplete() {
        viewModelScope.launch(Dispatchers.IO) {
            safeAddressManager.getSafeAddress()?.let { viewState.postValue(SetupComplete(it)) }
                ?: viewState.postValue(Error(NoAddressAvailable()))
        }
    }
}

class NoAddressAvailable : Throwable()

data class SetupComplete(val safeAddress: Solidity.Address) : ViewState()
