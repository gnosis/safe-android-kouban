package io.gnosis.kouban.ui.filter.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter
import io.gnosis.kouban.databinding.ItemTokenSymolFilterBinding

class TransactionFilterFactory(private val searchManager: SearchManager) : BaseFactory<BaseTransactionFilterViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionFilterViewHolder<Any> {
        return TokenSymbolFilterViewHolder(
            searchManager,
            viewBinding as ItemTokenSymolFilterBinding
        ) as BaseTransactionFilterViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemTokenSymolFilterBinding.inflate(layoutInflater, parent, false)
    }
}

abstract class BaseTransactionFilterViewHolder<T : Any>(
    viewBinding: ViewBinding
) : BaseViewHolder<T>(viewBinding)

class TokenSymbolFilterViewHolder(
    private val searchManager: SearchManager,
    private val viewBinding: ItemTokenSymolFilterBinding
) : BaseTransactionFilterViewHolder<String>(viewBinding) {

    override fun bind(item: String) {
        with(viewBinding.tokenSymbolCheckbox) {
            text = item
            isChecked = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.contains(item) == true
            setOnClickListener {
                if (isChecked) uncheck(item) else check(item)
            }
        }
    }

    private fun uncheck(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        viewBinding.tokenSymbolCheckbox.isChecked = false
        viewBinding.tokenSymbolCheckbox.invalidate()
    }

    private fun check(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.add(item)
        viewBinding.tokenSymbolCheckbox.isChecked = true
        viewBinding.tokenSymbolCheckbox.invalidate()
    }
}
