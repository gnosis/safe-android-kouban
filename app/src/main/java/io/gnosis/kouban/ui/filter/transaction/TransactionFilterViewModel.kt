package io.gnosis.kouban.ui.filter.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.managers.TransactionTimestampFilter
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter

class TransactionFilterViewModel(searchManager: SearchManager) : ViewModel() {

    val viewStates = MutableLiveData<ViewState>()
    private val tokenSymbolFilter: TransactionTokenSymbolFilter? = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)
    private val timestampFilter: TransactionTimestampFilter? = searchManager.getFilterFor(TransactionTimestampFilter::class.java)

    init {
        viewStates.postValue(TokenSymbols(buildList()))
    }

    private fun buildList(): List<Any> {
        return mutableListOf<Any>().apply {
            tokenSymbolFilter?.availableValues?.let(::addAll)
            timestampFilter?.let { add(FilterDates(it.lowerBound, it.upperBound)) }
        }
    }
}

data class TokenSymbols(val possibleValues: List<Any>) : ViewState()
