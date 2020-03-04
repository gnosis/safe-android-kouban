package io.gnosis.kouban.ui.transaction.details

import android.text.Spannable
import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.data.models.ServiceSafeTx

class TransactionDetailsViewModel(private val safeRepository: SafeRepository) : ViewModel() {

    val viewStates = MutableLiveData<ViewState>()

    fun load(txHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                viewStates.postValue(Loading(false))
                safeRepository.loadTransaction(txHash)
            }.onSuccess {
                viewStates.postValue(Loading(false))
                viewStates.postValue(TransactionDetails(it.toDetails()))
            }.onFailure {
                viewStates.postValue(Loading(false))
                viewStates.postValue(Error(it))
            }
        }
    }

    private fun ServiceSafeTx.toDetails(): List<Any> {
        return listOf(
            this.tx.to,
            LabelDescription(R.string.transaction_details_type_label, SpannableString("SOMETHING"))
        )
    }
}

data class TransactionDetails(
    val details: List<Any>
) : ViewState()
