package io.gnosis.kouban.ui.transaction

import androidx.lifecycle.*
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTimestampFilter
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter
import io.gnosis.kouban.data.managers.TransactionTypeFilter
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.repositories.EnsRepository
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pm.gnosis.model.Solidity
import java.text.SimpleDateFormat
import java.util.*

class TransactionsViewModel(
    private val safeRepository: SafeRepository,
    private val searchManager: SearchManager,
    private val ensRepository: EnsRepository,
    private val dateLabelFormatter: SimpleDateFormat
) : ViewModel() {

    val viewState = MutableLiveData<ViewState>()

    fun loadTransactionsOf(address: Solidity.Address) {
        viewModelScope.launch(Dispatchers.IO) {
            viewState.postValue(Loading(true))
            runCatching { safeRepository.getTransactions(address) }
                .onFailure {
                    viewState.postValue(Error(it))
                }
                .onSuccess {
                    val listItems = mutableListOf<Any>().apply {
                        tokenSymbolFilters(address)
                        transactionTypeFilters(address)
                        dateFilters(address)

                        searchManager.filter(it.pending).takeUnless { it.isEmpty() }?.let { transactions ->
                            add(Header(R.string.pending_label))
                            addAll(withDateLabels(transactions))
                        }

                        searchManager.filter(it.history).takeUnless { it.isEmpty() }?.let { transactions ->
                            add(Header(R.string.history_label))
                            addAll(withDateLabels(transactions))
                        }
                    }
                    viewState.postValue(ListViewItems(listItems))
                }
        }
    }

    private fun MutableList<Any>.transactionTypeFilters(address: Solidity.Address) {
        searchManager.getFilterFor(TransactionTypeFilter::class.java)?.let { typeFilter ->
            typeFilter.availableValues.forEach { availableValue ->
                add(CheckFilterView(availableValue, availableValue.name, R.drawable.ic_check_black_24dp) {
                    if (typeFilter.selectedValue.contains(it)) {
                        typeFilter.selectedValue.remove(it)
                        false
                    } else {
                        typeFilter.selectedValue.add(it)
                        true
                    }.also { applyFilters(address) }
                })
            }
        }
    }

    private fun MutableList<Any>.tokenSymbolFilters(address: Solidity.Address) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.let { tokenFilter ->
            tokenFilter.availableValues.forEach { availableValue ->
                add(CheckFilterView(availableValue, availableValue, R.drawable.ic_check_black_24dp) {
                    if (tokenFilter.selectedValue.contains(it)) {
                        tokenFilter.selectedValue.remove(it)
                        false
                    } else {
                        tokenFilter.selectedValue.add(it)
                        true
                    }.also { applyFilters(address) }
                })
            }
        }
    }

    private fun MutableList<Any>.dateFilters(address: Solidity.Address) {
//        searchManager.getFilterFor()
        add(DateFilterView(R.string.transaction_filter_date_from_hint, {

        }) { fromDate ->
            searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.lowerBound = fromDate
            applyFilters(address)
        })
        add(DateFilterView(R.string.transaction_filter_date_to_hint, {}) { toDate ->
            searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.upperBound = toDate
            applyFilters(address)
        })
    }

    private fun applyFilters(address: Solidity.Address) {
        loadTransactionsOf(address)
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
        transactions.fold(mutableListOf(), { withLabels, tx ->
            withLabels.apply {
                tx.timestamp.asFormattedDateTime(dateLabelFormatter)
                    .takeUnless { contains(it) }?.let { add(it) }
                add(tx)
            }
        })
}

data class ListViewItems(val listItems: List<Any>) : ViewState()
data class ENSName(val ensName: String?)
