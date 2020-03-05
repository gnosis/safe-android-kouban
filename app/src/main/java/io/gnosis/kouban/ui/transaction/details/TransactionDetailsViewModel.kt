package io.gnosis.kouban.ui.transaction.details

import android.text.Spannable
import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.repositories.SafeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.data.models.SafeTx
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.models.TransactionType
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asDecimalString

class TransactionDetailsViewModel(
    private val transaction: Transaction,
    private val safeRepository: SafeRepository,
    private val addressManager: SafeAddressManager
) : ViewModel() {

    fun load() =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            runCatching {
                emit(Loading(true))
                addressManager.getSafeAddress() to safeRepository.loadTransaction(transaction.txHash!!)
            }.onSuccess {
                emit(Loading(false))
                emit(TransactionDetails(it.toDetails()))
            }.onFailure {
                emit(Loading(false))
                emit(Error(it))
            }
        }

    private fun Pair<Solidity.Address?, ServiceSafeTx>.toDetails(): List<Any> {
        return listOf(
            first!!,
            second.tx.buildTransactionTypeView(first),
            second.tx.to,
            LabelDescription(R.string.transaction_details_network_fees_label, SpannableString(second.execInfo.fees.asDecimalString())),
            LabelDate(R.string.transaction_details_timestamp_label, dateInSecs = transaction.timestamp),
            Link(transaction.executionHash!!, R.string.view_transaction_on),
            LabelDescription(R.string.transaction_details_raw_data_label, SpannableString(second.tx.data))
        )
    }

    private fun SafeTx.buildTransactionTypeView(currentSafe: Solidity.Address?): TransactionTypeView =
        TransactionTypeView(
            currentSafe!!,
            to,
            transaction.type,
            transaction.dataInfo,
            transaction.transferInfo
        )


}

data class TransactionDetails(
    val details: List<Any>
) : ViewState()
