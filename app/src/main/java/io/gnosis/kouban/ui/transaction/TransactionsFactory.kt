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
import io.gnosis.kouban.data.models.Transaction
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
            is Transaction -> TransactionViewTypes.Transaction.ordinal
            else -> throw UnsupportedViewType()
        }

}
