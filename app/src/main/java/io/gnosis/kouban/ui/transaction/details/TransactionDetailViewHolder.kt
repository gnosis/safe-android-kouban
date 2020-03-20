package io.gnosis.kouban.ui.transaction.details

import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.core.utils.parseEthereumAddress
import io.gnosis.kouban.core.utils.setupEtherscanTransactionUrl
import io.gnosis.kouban.data.models.DataInfo
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.data.models.TransferInfo
import io.gnosis.kouban.data.repositories.TokenRepository.Companion.ETH_TOKEN_INFO
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemDetailsAddressBinding
import io.gnosis.kouban.databinding.ItemDetailsLabelDescriptionBinding
import io.gnosis.kouban.databinding.ItemDetailsTransactionTypeBinding
import io.gnosis.kouban.databinding.ItemTransactionDetailsLinkBinding
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.copyToClipboard
import pm.gnosis.svalinn.common.utils.snackbar

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

data class LabelDate(
    @StringRes val label: Int,
    val date: Spannable? = null,
    val dateInSecs: Long? = null
)

class LabelDateViewHolder(
    private val viewBinding: ItemDetailsLabelDescriptionBinding
) : BaseDetailViewHolder<LabelDate>(viewBinding) {

    override fun bind(item: LabelDate) {
        with(viewBinding) {
            label.setText(item.label)
            item.date?.let { description.text = it }
            item.dateInSecs?.let { description.text = it.asFormattedDateTime(root.context) }
        }
    }
}

data class TransactionTypeView(
    val safe: Solidity.Address,
    val to: Solidity.Address,
    val type: TransactionType,
    val dataInfo: DataInfo?,
    val transferInfo: TransferInfo?
)

class TransactionTypeViewHolder(
    private val viewBinding: ItemDetailsTransactionTypeBinding
) : BaseDetailViewHolder<TransactionTypeView>(viewBinding) {

    override fun bind(item: TransactionTypeView) {
        val (drawable, color) = when (item.type) {
            TransactionType.Incoming -> R.drawable.ic_arrow_upward_black_24dp to R.color.safe_green
            TransactionType.Outgoing -> R.drawable.ic_arrow_downward_black_24dp to R.color.tomato
        }
        viewBinding.directionArrowImage.apply {
            setColorFilter(ContextCompat.getColor(context, color), android.graphics.PorterDuff.Mode.SRC_IN)
            setImageResource(drawable)
        }
        item.transferInfo?.let { viewBinding.setTransferInfo(it) }
        item.dataInfo?.let { viewBinding.setDataInfo(it) }
    }

    private fun ItemDetailsTransactionTypeBinding.setTransferInfo(transferInfo: TransferInfo) {
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            transferInfo.amount.shiftedString(transferInfo.decimals, 3),
            transferInfo.tokenSymbol
        )
        textDescription.isVisible = false
    }

    private fun ItemDetailsTransactionTypeBinding.setDataInfo(dataInfo: DataInfo) {
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            dataInfo.ethValue.shiftedString(ETH_TOKEN_INFO.decimals, 3),
            ETH_TOKEN_INFO.symbol
        )
        textDescription.isVisible = true
        textDescription.text = dataInfo.methodName ?: root.context.getString(R.string.transaction_byte_label, dataInfo.dataByteLength ?: 0)
    }
}

class AddressDetailsViewHolder(
    private val viewBinding: ItemDetailsAddressBinding
) : BaseDetailViewHolder<Solidity.Address>(viewBinding) {

    override fun bind(item: Solidity.Address) {
        with(viewBinding) {
            viewBinding.root.setOnLongClickListener { view ->
                view.context.copyToClipboard(view.context.getString(R.string.share_address), item.toString()) {
                    snackbar(view, R.string.address_clipboard_success)
                }
                true
            }
            addressImage.setAddress(item)
            address.text = item.formatEthAddress(root.context)
        }
    }
}

data class Link(
    val entityId: String,
    @StringRes val displayableText: Int
)

class LinkViewHolder(
    private val viewBinding: ItemTransactionDetailsLinkBinding
) : BaseDetailViewHolder<Link>(viewBinding) {

    override fun bind(item: Link) {
        viewBinding.layoutTransactionStatusEtherscanLink.setupEtherscanTransactionUrl(item.entityId, item.displayableText)
    }
}
