package io.gnosis.kouban.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.models.TransactionState
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity

class TransactionsViewModel(
    private val safeRepository: SafeRepository
) : ViewModel() {

    fun loadTransactionsOf(address: Solidity.Address) = loadFakeTransactionsOf(address)

    private fun loadFakeTransactionsOf(safe: Solidity.Address) =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))
            kotlin.runCatching { safeRepository.fakeGeTransactions(safe) }
                .onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
                .onSuccess {
                    emit(Loading(false))
                    val listItems = mutableListOf<Any>().apply {
                        add(Header(R.string.pending_label))
                        addAll(it.pending)
                        add(Header(R.string.history_label))
                        addAll(it.history)
                    }
                    emit(ListViewItems(listItems))
                }
        }


}

data class ListViewItems(val listItems: List<Any>) : ViewState()
