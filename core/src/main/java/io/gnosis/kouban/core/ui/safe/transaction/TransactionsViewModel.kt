package io.gnosis.kouban.core.ui.safe.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity
import timber.log.Timber
import java.math.BigInteger

class TransactionsViewModel(
    private val safeRepository: SafeRepository
) : ViewModel() {

    fun loadTransactionsOf(address: Solidity.Address) =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))
            kotlin.runCatching {
                val safeNonce = kotlin.runCatching { safeRepository.loadSafeNonce(address) }
                    .onFailure { Timber.e(it) }
                    .getOrDefault(BigInteger.ZERO)
                val txs = safeRepository.loadPendingTransactions(address)
                Transactions(txs.groupBy { transactionToType(it, safeNonce) })
            }.onSuccess {
                emit(Loading(false))
                emit(it)
            }.onFailure {
                emit(Loading(false))
                emit(Error(it))
            }
        }

    private fun transactionToType(
        transaction: ServiceSafeTx,
        safeNonce: BigInteger,
        deviceId: Solidity.Address? = null //TODO add loadDeviceId to SafeRepository
    ): TransactionState =
        when {
            transaction.executed -> TransactionState.Executed
            transaction.execInfo.nonce < safeNonce -> TransactionState.Cancelled
            transaction.confirmations.find { (address, _) -> address == deviceId } != null -> TransactionState.Confirmed
            else -> TransactionState.Pending
        }
}

enum class TransactionState {
    Executed, Cancelled, Confirmed, Pending
}

data class Transactions(val transactions: Map<TransactionState, List<ServiceSafeTx>>) : ViewState()
