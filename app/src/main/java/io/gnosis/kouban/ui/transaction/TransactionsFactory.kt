package io.gnosis.kouban.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.R
import io.gnosis.kouban.databinding.ItemTransactionBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.ui.adapter.UnsupportedViewType
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.data.utils.shortChecksumString
import io.gnosis.kouban.databinding.ItemHeaderBinding

enum class TransactionViewTypes {
    Header, Transaction
}

class TransactionsFactory : BaseFactory<BaseTransactionViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionViewHolder<Any> {
        return when (TransactionViewTypes.values()[viewType]) {
            TransactionViewTypes.Transaction -> TransactionViewHolder(viewBinding as ItemTransactionBinding)
            TransactionViewTypes.Header -> HeaderViewHolder(viewBinding as ItemHeaderBinding)
        } as BaseTransactionViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (TransactionViewTypes.values()[viewType]) {
            TransactionViewTypes.Transaction -> ItemTransactionBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.Header -> ItemHeaderBinding.inflate(layoutInflater, parent, false)
        }
    }

    override fun <T> viewTypeFor(item: T): Int =
        when (item) {
            is Header -> TransactionViewTypes.Header.ordinal
            is ServiceSafeTx -> TransactionViewTypes.Transaction.ordinal
            else -> throw UnsupportedViewType()
        }

}

abstract class BaseTransactionViewHolder<T>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)

class TransactionViewHolder(
    private val viewBinding: ItemTransactionBinding
) :
    BaseTransactionViewHolder<ServiceSafeTx>(viewBinding) {

    override fun bind(item: ServiceSafeTx) {
        with(viewBinding) {
            val backgroundColor =
                if (item.executed) ContextCompat.getColor(root.context, R.color.safe_green)
                else ContextCompat.getColor(root.context, R.color.tomato)
            root.setBackgroundColor(backgroundColor)

            root.context.let { context ->
                textTxHash.text = context.getString(R.string.hash, item.hash.asMiddleEllipsized(4))
                textTo.text = context.getString(R.string.address_to, item.tx.to.shortChecksumString())
                textAmount.text = item.tx.value.shiftedString(2)
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
