package io.gnosis.kouban.ui.transaction

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.models.DataInfo
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.data.models.TransferInfo
import io.gnosis.kouban.data.repositories.TokenRepository.Companion.ETH_TOKEN_INFO
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemHeaderBinding
import io.gnosis.kouban.databinding.ItemTransactionBinding
import pm.gnosis.utils.asEthereumAddressString

abstract class BaseTransactionViewHolder<T>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)

class TransactionViewHolder(
    private val viewBinding: ItemTransactionBinding,
    private val picasso: Picasso
) : BaseTransactionViewHolder<Transaction>(viewBinding) {


    override fun bind(item: Transaction) {
        with(viewBinding) {
            textAddress.text = item.address.asEthereumAddressString().asMiddleEllipsized(4)
            blockiesAddress.setAddress(item.address)
            textTimestamp.text = item.timestamp.asFormattedDateTime(viewBinding.root.context)
            item.transferInfo?.let { setTransferInfo(it) }
            item.dataInfo?.let { setDataInfo(it) }
            setIconForType(item.type)
        }
    }

    private fun ItemTransactionBinding.setTransferInfo(transferInfo: TransferInfo) {
        textDescription.text = ""
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            transferInfo.amount.shiftedString(transferInfo.decimals, 3),
            transferInfo.tokenSymbol
        )
        imageTokenLogo.setTransactionIcon(picasso, transferInfo.tokenIconUrl)
    }

    private fun ItemTransactionBinding.setDataInfo(dataInfo: DataInfo) {
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            dataInfo.ethValue.shiftedString(ETH_TOKEN_INFO.decimals, 3),
            ETH_TOKEN_INFO.symbol
        )
        textDescription.text = root.context.getString(
            R.string.transaction_description_label,
            dataInfo.methodName.orEmpty(),
            (dataInfo.dataByteLength ?: 0).toString()
        )
        imageTokenLogo.setTransactionIcon(picasso, ETH_TOKEN_INFO.icon)
    }

    private fun ItemTransactionBinding.setIconForType(transactionType: TransactionType) {
        val (drawable, color) = when (transactionType) {
            TransactionType.Incoming -> R.drawable.ic_arrow_right_24dp to R.color.safe_green
            TransactionType.Outgoing -> R.drawable.ic_arrow_left_24dp to R.color.tomato
        }
        iconType.apply {
            setColorFilter(ContextCompat.getColor(root.context, color), android.graphics.PorterDuff.Mode.SRC_IN)
            setImageResource(drawable)
        }
    }
}

data class Header(val label: String)

class HeaderViewHolder(private val viewBinding: ItemHeaderBinding) : BaseTransactionViewHolder<Header>(viewBinding) {

    override fun bind(item: Header) {
        viewBinding.textTo.text = item.label
    }
}
