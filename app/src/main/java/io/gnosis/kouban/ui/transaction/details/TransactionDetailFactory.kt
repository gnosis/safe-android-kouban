package io.gnosis.kouban.ui.transaction.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.UnsupportedViewType
import io.gnosis.kouban.databinding.*
import pm.gnosis.model.Solidity

class TransactionDetailFactory : BaseFactory<BaseDetailViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseDetailViewHolder<Any> {
        return when (viewType) {
            ViewType.Address.ordinal -> AddressDetailsViewHolder(viewBinding as ItemDetailsAddressBinding)
            ViewType.LabelDate.ordinal -> LabelDateViewHolder(viewBinding as ItemDetailsLabelDescriptionBinding)
            ViewType.LabelDescription.ordinal -> LabelDescriptionViewHolder(viewBinding as ItemDetailsLabelDescriptionBinding)
            ViewType.Link.ordinal -> LinkViewHolder(viewBinding as ItemTransactionDetailsLinkBinding)
            ViewType.TransactionType.ordinal -> TransactionTypeViewHolder(viewBinding as ItemDetailsTransactionTypeBinding)
            ViewType.Hash.ordinal -> LabelHashViewHolder(viewBinding as ItemDetailsHashBinding)
            else -> throw UnsupportedViewType()
        } as BaseDetailViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            ViewType.Address.ordinal -> ItemDetailsAddressBinding.inflate(layoutInflater, parent, false)
            ViewType.LabelDate.ordinal, ViewType.LabelDescription.ordinal ->
                ItemDetailsLabelDescriptionBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            ViewType.Hash.ordinal -> ItemDetailsHashBinding.inflate(layoutInflater, parent, false)
            ViewType.Link.ordinal -> ItemTransactionDetailsLinkBinding.inflate(layoutInflater, parent, false)
            ViewType.TransactionType.ordinal -> ItemDetailsTransactionTypeBinding.inflate(layoutInflater, parent, false)
            else -> throw UnsupportedViewType()
        }
    }

    override fun <T> viewTypeFor(item: T): Int {
        return when (item) {
            is Solidity.Address -> ViewType.Address.ordinal
            is LabelDate -> ViewType.LabelDate.ordinal
            is LabelDescription -> ViewType.LabelDescription.ordinal
            is Link -> ViewType.Link.ordinal
            is TransactionTypeView -> ViewType.TransactionType.ordinal
            is LabelHash -> ViewType.Hash.ordinal
            else -> throw UnsupportedViewType(item.toString())
        }
    }

    private enum class ViewType {
        Address,
        LabelDate,
        LabelDescription,
        Link,
        TransactionType,
        Hash
    }
}
