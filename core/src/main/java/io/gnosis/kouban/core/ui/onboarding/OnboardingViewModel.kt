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

class OnboardingViewModel : ViewModel() {

    val safeAddressEvents = MutableLiveData<ViewState>()
}
