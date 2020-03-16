package io.gnosis.kouban.ui.transaction.details

import android.text.SpannableString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.models.DataInfo
import io.gnosis.kouban.data.models.SafeTx
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository.Companion.ETH_TOKEN_INFO
import io.gnosis.kouban.data.utils.shiftedString
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asDecimalString
import pm.gnosis.utils.hexStringToByteArray
import java.math.BigInteger

class TransactionDetailsViewModel(
    private val transactionHash: String,
    private val safeRepository: SafeRepository,
    private val addressManager: SafeAddressManager
) : ViewModel() {

    fun load() =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            runCatching {
                emit(Loading(true))
                (addressManager.getSafeAddress() ?: throw IllegalStateException("Safe Address must not be null")) to safeRepository.loadTransaction(
                    transactionHash
                )
            }.onSuccess {
                emit(Loading(false))
                emit(TransactionDetails(it.toDetails()))
            }.onFailure {
                emit(Loading(false))
                emit(Error(it))
            }
        }

    private fun Pair<Solidity.Address, ServiceSafeTx>.toDetails(): List<Any> {
        val decodedData = decode(second.tx)
        return mutableListOf<Any>().apply {
            add(first)
            add(second.buildTransactionTypeView(first, decodedData))
            add(second.tx.to)
            add(
                LabelDescription(
                    R.string.transaction_details_network_fees_label,
                    SpannableString(second.execInfo.fees.shiftedString(ETH_TOKEN_INFO.decimals, decimalsToDisplay = ETH_TOKEN_INFO.decimals))
                )
            )
            add(LabelDescription(R.string.transaction_details_operation_label, SpannableString(second.tx.operation.name)))
            add(LabelDescription(R.string.transaction_details_value_label, SpannableString(second.tx.value.asDecimalString())))
            second.txHash?.let { add(Link(it, R.string.view_transaction_on)) }
            add(LabelDescription(R.string.transaction_details_raw_data_label, SpannableString(second.tx.data)))
        }
    }

    private fun ServiceSafeTx.buildTransactionTypeView(currentSafe: Solidity.Address?, decodedData: DecodedData): TransactionTypeView =
        TransactionTypeView(
            currentSafe!!,
            tx.to,
            TransactionType.Outgoing, // FIXME the only one that the backend has info for at them moment
            decodedData.toDataInfo(tx),
            null // FIXME Transfers
        )

    private fun DecodedData.toDataInfo(safeTx: SafeTx): DataInfo {
        return DataInfo(safeTx.value, dataLength?.toInt(), null)
    }

    private fun decode(safeTx: SafeTx): DecodedData {
        val data = safeTx.data.hexStringToByteArray()

        val dataLength = Solidity.UInt256(data.size.toBigInteger()).encodePacked()
        val dataEncoded = Solidity.Bytes(data).encodePacked()

        return DecodedData(safeTx.value, dataLength, dataEncoded)
    }

    private data class DecodedData(
        val value: BigInteger,
        val dataLength: String?,
        val dataEncoded: String
    )
}

data class TransactionDetails(
    val details: List<Any>
) : ViewState()
