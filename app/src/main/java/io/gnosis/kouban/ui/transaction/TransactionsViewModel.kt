package io.gnosis.kouban.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.repositories.EnsRepository
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity
import java.text.SimpleDateFormat

class TransactionsViewModel(
    private val safeRepository: SafeRepository,
    private val searchManager: SearchManager,
    private val ensRepository: EnsRepository,
    private val dateLabelFormatter: SimpleDateFormat
) : ViewModel() {

    fun loadTransactionsOf(address: Solidity.Address) =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))
            runCatching { safeRepository.getTransactions(address) }
                .onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
                .onSuccess {
                    emit(Loading(false))
                    val listItems = mutableListOf<Any>().apply {
                        searchManager.filter(it.pending).takeUnless { it.isEmpty() }?.let { transactions ->
                            add(Header(R.string.pending_label))
                            addAll(withDateLabels(transactions))
                        }

                        searchManager.filter(it.history).takeUnless { it.isEmpty() }?.let { transactions ->
                            add(Header(R.string.history_label))
                            addAll(withDateLabels(transactions))
                        }
                    }
                    emit(ListViewItems(listItems))
                }
        }

    fun loadHeaderInfo(address: Solidity.Address) =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            runCatching {
                emit(Loading(true))
                ensRepository.resolve(address)
            }.onFailure {
                emit(Loading(false))
                emit(Error(it))
            }.onSuccess {
                emit(Loading(false))
                emit(ENSName(it))
            }
        }

    private fun withDateLabels(transactions: List<Transaction>): List<Any> =
        mutableListOf<Any>().apply {
            transactions.groupBy { it.timestamp.asFormattedDateTime(dateLabelFormatter) }
                .entries
                .forEach { (dateLabel, txList) ->
                    add(dateLabel)
                    addAll(txList)
                }
        }
}

data class ListViewItems(val listItems: List<Any>) : ViewState()
data class ENSName(val ensName: String?)
