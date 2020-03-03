package io.gnosis.kouban.ui.filter.transaction

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.BaseBottomSheetFragment
import io.gnosis.kouban.databinding.DialogTransactionFilterBinding
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionFilterDialog : BaseBottomSheetFragment<DialogTransactionFilterBinding>() {

    private val viewModel by currentScope.viewModel<TransactionFilterViewModel>(this)
    private val adapter by currentScope.inject<BaseAdapter<BaseTransactionFilterViewHolder<Any>>>()
    var onDismissCallback: (() -> Unit)? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogTransactionFilterBinding =
        DialogTransactionFilterBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.filters) {
            this.adapter = this@TransactionFilterDialog.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            setHasFixedSize(true)
        }
        viewModel.viewStates.observe(viewLifecycleOwner, Observer {
            when (it) {
                is TokenSymbols -> {
                    adapter.setItemsUnsafe(it.possibleValues)
                }
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissCallback?.invoke()
        super.onDismiss(dialog)
    }
}
