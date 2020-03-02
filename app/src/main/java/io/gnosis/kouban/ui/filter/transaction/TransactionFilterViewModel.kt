package io.gnosis.kouban.ui.filter.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter

class TransactionFilterViewModel(private val searchManager: SearchManager) : ViewModel() {

    val viewStates = MutableLiveData<ViewState>()

    init {
        val filter = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)
        viewStates.postValue(TokenSymbols(filter?.availableValues.orEmpty()))
    }


}

data class TokenSymbols(val possibleValues: List<String>) : ViewState()
