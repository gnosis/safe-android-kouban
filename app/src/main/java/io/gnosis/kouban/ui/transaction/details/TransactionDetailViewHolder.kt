package io.gnosis.kouban.ui.transaction.details

import android.text.Spannable
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.databinding.ItemDetailsAddressBinding
import io.gnosis.kouban.databinding.ItemDetailsLabelDescriptionBinding
import io.gnosis.kouban.databinding.ItemDetailsTransactionTypeBinding
import pm.gnosis.model.Solidity

abstract class BaseDetailViewHolder<T>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)

data class LabelDescription(
    @StringRes val label: Int,
    val description: Spannable
)

class LabelDescriptionViewHolder(
    private val viewBinding: ItemDetailsLabelDescriptionBinding
) : BaseDetailViewHolder<LabelDescription>(viewBinding) {

    override fun bind(item: LabelDescription) {
        with(viewBinding) {
            label.setText(item.label)
            description.text = item.description
        }
    }
}

data class TransactionTypeView(
    val from: Solidity.Address,
    val to: Solidity.Address,
    val type: TransactionType
)

class TransactionTypeViewHolder(
    private val viewBinding: ItemDetailsTransactionTypeBinding
) : BaseDetailViewHolder<TransactionTypeView>(viewBinding) {

    override fun bind(item: TransactionTypeView) {
        with(viewBinding.directionArrowImage) {
            if (item.type == TransactionType.Outgoing) {
                setImageResource(R.drawable.ic_arrow_upward_black_24dp)
            } else {
                setImageResource(R.drawable.ic_arrow_downward_black_24dp)
            }
        }
    }
}

class AddressDetailsViewHolder(
    private val viewBinding: ItemDetailsAddressBinding
) : BaseDetailViewHolder<Solidity.Address>(viewBinding) {

    override fun bind(item: Solidity.Address) {
        with(viewBinding) {
            addressImage.setAddress(item)
            address.text = item.formatEthAddress(root.context)
        }
    }
}
