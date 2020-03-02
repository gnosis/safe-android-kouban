package io.gnosis.kouban.ui.filter.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import io.gnosis.kouban.core.ui.base.BaseBottomSheetFragment
import io.gnosis.kouban.databinding.DialogTransactionFilterBinding
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionFilterDialog : BaseBottomSheetFragment<DialogTransactionFilterBinding>() {

    private val viewModel by currentScope.viewModel<TransactionFilterViewModel>(this)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogTransactionFilterBinding =
        DialogTransactionFilterBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewStates.observe(viewLifecycleOwner, Observer {
            when (it) {
                is TokenSymbols -> {
                    binding.title.text = it.possibleValues.joinToString(", ")
                }
            }
        })
    }

}
