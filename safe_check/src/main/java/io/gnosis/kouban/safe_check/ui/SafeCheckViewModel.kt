package io.gnosis.kouban.safe_check.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity

class SettingsViewModel(
    private val safeRepository: SafeRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    fun loadOwners(address: Solidity.Address): LiveData<ViewState> =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))
            kotlin.runCatching { safeRepository.loadSafeInfo(address) }
                .onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
                .onSuccess {
                    emit(Loading(false))
                    emit(SafeSettings(it.owners, it.threshold.toInt()))
                }
        }

}

data class SafeSettings(
    val owners: List<Solidity.Address>,
    val threshold: Int) : ViewState()
