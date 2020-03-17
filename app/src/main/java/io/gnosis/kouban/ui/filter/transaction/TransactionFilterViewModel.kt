package io.gnosis.kouban.ui.filter.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.managers.*

class TransactionFilterViewModel(private val searchManager: SearchManager) : ViewModel() {

    val viewStates = MutableLiveData<ViewState>()
    private val tokenSymbolFilter: TransactionTokenSymbolFilter? = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)
    private val timestampFilter: TransactionTimestampFilter? = searchManager.getFilterFor(TransactionTimestampFilter::class.java)
    private val transactionTypeFilter: TransactionTypeFilter? = searchManager.getFilterFor(TransactionTypeFilter::class.java)

    init {
        viewStates.postValue(FiltersReady(buildList()))
    }

    private fun buildList(): List<Any> {
        return mutableListOf<Any>().apply {
            tokenSymbolFilter?.availableValues?.let {
                add(R.string.transaction_filter_token_symbol_label)
                addAll(it)
            }
            timestampFilter?.let {
                add(R.string.transaction_filter_timestamp_label)
                add(FilterDates(it.lowerBound, it.upperBound))
            }
            transactionTypeFilter?.availableValues?.let {
                add(R.string.transaction_filter_transaction_type_label)
                addAll(it)
            }
        }
    }

    fun clearFilters() {
        searchManager.clearAllFilters()
        viewStates.postValue(FiltersCleared)
    }
}

data class FiltersReady(val filters: List<Any>) : ViewState()
object FiltersCleared : ViewState()
