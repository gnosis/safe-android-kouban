package io.gnosis.kouban.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.gnosis.kouban.core.R
import io.gnosis.kouban.databinding.ItemTransactionBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.data.models.ServiceSafeTx
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.data.utils.shortChecksumString

class TransactionsFactory : BaseFactory<ItemTransactionBinding, BaseTransactionViewHolder>() {

    override fun newViewHolder(viewBinding: ItemTransactionBinding, viewType: Int): BaseTransactionViewHolder {
        return BaseTransactionViewHolder(viewBinding)
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemTransactionBinding {
        return ItemTransactionBinding.inflate(layoutInflater, parent, false)
    }
}

class BaseTransactionViewHolder(
    private val viewBinding: ItemTransactionBinding
) :
    BaseViewHolder<ServiceSafeTx, ItemTransactionBinding>(viewBinding) {

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
