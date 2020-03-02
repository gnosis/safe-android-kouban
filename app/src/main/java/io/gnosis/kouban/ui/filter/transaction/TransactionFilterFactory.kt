package io.gnosis.kouban.ui.filter.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.databinding.ItemTokenSymolFilterBinding

class TransactionFilterFactory : BaseFactory<BaseTransactionFilterViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionFilterViewHolder<Any> {
        return TokenSymbolFilterViewHolder(viewBinding as ItemTokenSymolFilterBinding) as BaseTransactionFilterViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemTokenSymolFilterBinding.inflate(layoutInflater, parent, false)
    }
}

abstract class BaseTransactionFilterViewHolder<T : Any>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)

class TokenSymbolFilterViewHolder(
    private val viewBinding: ItemTokenSymolFilterBinding
) : BaseTransactionFilterViewHolder<String>(viewBinding) {

    override fun bind(item: String) {
        viewBinding.tokenSymbolCheckbox.text = item
    }
}
