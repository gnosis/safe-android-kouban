package io.gnosis.kouban.ui.transaction

import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.data.utils.shortChecksumString
import io.gnosis.kouban.databinding.ItemHeaderBinding
import io.gnosis.kouban.databinding.ItemTransactionBinding
import pm.gnosis.utils.asEthereumAddressString

abstract class BaseTransactionViewHolder<T>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)

class TransactionViewHolder(
    private val viewBinding: ItemTransactionBinding
) :
    BaseTransactionViewHolder<Transaction>(viewBinding) {

    override fun bind(item: Transaction) {
        with(viewBinding) {
            root.context.let { context ->
                textTxHash.text = context.getString(R.string.hash, item.address.asEthereumAddressString().asMiddleEllipsized(4))
                textAmount.text = item.transferInfo?.amount?.shiftedString(2)
            }
        }
    }
}

data class Header(val label: String)

class HeaderViewHolder(private val viewBinding: ItemHeaderBinding) : BaseTransactionViewHolder<Header>(viewBinding) {

    override fun bind(item: Header) {
        viewBinding.textTo.text = item.label
    }
}
