package io.gnosis.kouban.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
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
import io.gnosis.kouban.databinding.ItemLabelBinding

enum class TransactionViewTypes {
    Header, Transaction, Label
}

class TransactionsFactory(private val picasso: Picasso) : BaseFactory<BaseTransactionViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionViewHolder<Any> {
        return when (viewType) {
            TransactionViewTypes.Transaction.ordinal -> TransactionViewHolder(viewBinding as ItemTransactionBinding, picasso)
            TransactionViewTypes.Header.ordinal -> HeaderViewHolder(viewBinding as ItemHeaderBinding)
            TransactionViewTypes.Label.ordinal -> LabelViewHolder(viewBinding as ItemLabelBinding)
            else -> throw UnsupportedViewType("TransactionsFactory $viewType")
        } as BaseTransactionViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            TransactionViewTypes.Transaction.ordinal -> ItemTransactionBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.Header.ordinal -> ItemHeaderBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.Label.ordinal -> ItemLabelBinding.inflate(layoutInflater, parent, false)
            else -> throw UnsupportedViewType("TransactionsFactory $viewType")
        }
    }

    override fun <T> viewTypeFor(item: T): Int =
        when (item) {
            is Header -> TransactionViewTypes.Header.ordinal
            is Transaction -> TransactionViewTypes.Transaction.ordinal
            is String -> TransactionViewTypes.Label.ordinal
            else -> throw UnsupportedViewType(item.toString())
        }

}
