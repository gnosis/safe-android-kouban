package io.gnosis.kouban.ui.transaction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.asFormattedDateTime
import io.gnosis.kouban.core.utils.openUrl
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTimestampFilter
import io.gnosis.kouban.data.models.DataInfo
import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.utils.showDatePickerDialog
import io.gnosis.kouban.data.models.TransferInfo
import io.gnosis.kouban.data.repositories.TokenRepository.Companion.ETH_TOKEN_INFO
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemFilterBinding
import io.gnosis.kouban.databinding.ItemHeaderBinding
import io.gnosis.kouban.databinding.ItemLabelBinding
import io.gnosis.kouban.databinding.ItemTransactionBinding
import pm.gnosis.utils.asEthereumAddressString
import java.text.SimpleDateFormat
import java.util.*

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
            root.setOnClickListener { view ->
                with(item) {
                    txHash?.let {
                        root.findNavController()
                            .navigate(TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetailsFragment(it))
                    } ?: executionHash?.let { executionHash ->
                        with(view.context) {
                            openUrl(BuildConfig.BLOCK_EXPLORER_TX.format(executionHash))
                        }
                    }
                }
            }
        }
    }

    private fun ItemTransactionBinding.setTransferInfo(transferInfo: TransferInfo) {
        textDescription.isVisible = false
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            transferInfo.amount.shiftedString(transferInfo.decimals, DECIMALS),
            transferInfo.tokenSymbol
        )
        imageTokenLogo.setTransactionIcon(picasso, transferInfo.tokenIconUrl)
    }

    private fun ItemTransactionBinding.setDataInfo(dataInfo: DataInfo) {
        textTokenAmount.text = root.context.getString(
            R.string.transaction_token_amount_label,
            dataInfo.ethValue.shiftedString(ETH_TOKEN_INFO.decimals, DECIMALS),
            ETH_TOKEN_INFO.symbol
        )
        textDescription.isVisible = true
        textDescription.text = dataInfo.methodName ?: root.context.getString(R.string.transaction_byte_label, dataInfo.dataByteLength ?: 0)

        imageTokenLogo.setTransactionIcon(picasso, ETH_TOKEN_INFO.icon)
    }

    private fun ItemTransactionBinding.setIconForType(transactionType: TransactionType) {
        val (drawable, color) = when (transactionType) {
            TransactionType.Incoming -> R.drawable.ic_arrow_left_24dp to R.color.safe_green
            TransactionType.Outgoing -> R.drawable.ic_arrow_right_24dp to R.color.tomato
        }
        iconType.apply {
            setColorFilter(ContextCompat.getColor(root.context, color), android.graphics.PorterDuff.Mode.SRC_IN)
            setImageResource(drawable)
        }
    }

    companion object {
        const val DECIMALS = 5
    }
}

data class Header(@StringRes val label: Int)

class HeaderViewHolder(private val viewBinding: ItemHeaderBinding) : BaseTransactionViewHolder<Header>(viewBinding) {

    override fun bind(item: Header) {
        viewBinding.textTo.setText(item.label)
    }
}

class LabelViewHolder(private val viewBinding: ItemLabelBinding) : BaseTransactionViewHolder<String>(viewBinding) {

    override fun bind(item: String) {
        viewBinding.label.text = item
    }
}

data class CheckFilterView<T>(
    val value: T,
    val label: String,
    @DrawableRes val drawableId: Int,
    val toggleValue: (T) -> Boolean
)

class CheckFilterViewHolder(
    private val viewBinding: ItemFilterBinding
) : BaseTransactionViewHolder<CheckFilterView<Any>>(viewBinding) {

    override fun bind(item: CheckFilterView<Any>) {
        with(viewBinding.chip) {
            isCheckable = true
            isCloseIconVisible = false
            text = item.label
            chipIcon = ResourcesCompat.getDrawable(resources, item.drawableId, context.theme)
            setOnClickListener {
                isChecked = item.toggleValue(item.value)
                invalidate()
            }
            setOnCloseIconClickListener(null)
        }
    }
}

data class DateFilterView(
    @StringRes val label: Int,
    val initialValue: Date?,
    val onCleared: () -> Unit,
    val onDateSelect: (Date) -> Unit
)

class DateFilterViewHolder(
    private val viewBinding: ItemFilterBinding,
    private val dateFormat: SimpleDateFormat
) : BaseTransactionViewHolder<DateFilterView>(viewBinding) {

    override fun bind(item: DateFilterView) {
        with(viewBinding.chip) {
            isCheckable = false
            isCloseIconVisible = true
            text = context.getString(item.label, item.initialValue?.let(dateFormat::format).orEmpty())
            chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_date_range_black_24dp, context.theme)
            setOnClickListener {
                showDatePickerDialog(context, item.initialValue) { pickedDate ->
                    item.onDateSelect(pickedDate)
                }
            }
            setOnCloseIconClickListener {
                item.onCleared()
            }
        }
    }
}


