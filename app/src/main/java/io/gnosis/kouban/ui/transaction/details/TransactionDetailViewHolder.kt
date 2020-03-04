package io.gnosis.kouban.ui.transaction.details

import android.text.Spannable
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.databinding.ItemDetailsAddressBinding
import io.gnosis.kouban.databinding.ItemDetailsLabelDescriptionBinding
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
