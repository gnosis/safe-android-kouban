package io.gnosis.kouban.core.ui.safe.balance

import androidx.lifecycle.*
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.models.Balance
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity

class BalancesViewModel(
    private val safeRepository: SafeRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    fun loadBalancesOf(address: Solidity.Address): LiveData<ViewState> =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))
            kotlin.runCatching { safeRepository.loadTokenBalances(address) }
                .onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
                .onSuccess {
                    emit(Loading(false))
                    emit(Balances(it))
                }
        }
}

data class Balances(val balances: List<Balance>) : ViewState()
