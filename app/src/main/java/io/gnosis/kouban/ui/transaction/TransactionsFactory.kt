package io.gnosis.kouban.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.databinding.ItemTransactionBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.UnsupportedViewType
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.databinding.ItemFilterBinding
import io.gnosis.kouban.databinding.ItemHeaderBinding
import io.gnosis.kouban.databinding.ItemLabelBinding

enum class TransactionViewTypes {
    Header, Transaction, Label, CheckFilter, DateFilter
}

class TransactionsFactory(private val picasso: Picasso, private val searchManager: SearchManager) : BaseFactory<BaseTransactionViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionViewHolder<Any> {
        return when (viewType) {
            TransactionViewTypes.Transaction.ordinal -> TransactionViewHolder(viewBinding as ItemTransactionBinding, picasso)
            TransactionViewTypes.Header.ordinal -> HeaderViewHolder(viewBinding as ItemHeaderBinding)
            TransactionViewTypes.Label.ordinal -> LabelViewHolder(viewBinding as ItemLabelBinding)
            TransactionViewTypes.CheckFilter.ordinal -> CheckFilterViewHolder(viewBinding as ItemFilterBinding)
            TransactionViewTypes.DateFilter.ordinal -> DateFilterViewHolder(viewBinding as ItemFilterBinding, searchManager)
            else -> throw UnsupportedViewType("TransactionsFactory $viewType")
        } as BaseTransactionViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            TransactionViewTypes.Transaction.ordinal -> ItemTransactionBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.Header.ordinal -> ItemHeaderBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.Label.ordinal -> ItemLabelBinding.inflate(layoutInflater, parent, false)
            TransactionViewTypes.CheckFilter.ordinal, TransactionViewTypes.DateFilter.ordinal -> ItemFilterBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            else -> throw UnsupportedViewType("TransactionsFactory $viewType")
        }
    }

    override fun <T> viewTypeFor(item: T): Int =
        when (item) {
            is Header -> TransactionViewTypes.Header.ordinal
            is Transaction -> TransactionViewTypes.Transaction.ordinal
            is String -> TransactionViewTypes.Label.ordinal
            is CheckFilterView<*> -> TransactionViewTypes.CheckFilter.ordinal
            is DateFilterView -> TransactionViewTypes.DateFilter.ordinal
            else -> throw UnsupportedViewType(item.toString())
        }

}
