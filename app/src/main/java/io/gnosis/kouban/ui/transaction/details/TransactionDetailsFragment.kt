package io.gnosis.kouban.ui.transaction.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.databinding.FragmentTransactionDetailsBinding
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pm.gnosis.svalinn.common.utils.snackbar
import timber.log.Timber

class TransactionDetailsFragment : BaseFragment<FragmentTransactionDetailsBinding>() {

    private val navArgs by navArgs<TransactionDetailsFragmentArgs>()
    private val viewModel by currentScope.viewModel<TransactionDetailsViewModel>(this) { parametersOf(navArgs.transactionHash) }
    private val adapter by currentScope.inject<BaseAdapter<BaseDetailViewHolder<Any>>>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionDetailsBinding =
        FragmentTransactionDetailsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            txDetails.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            txDetails.adapter = this@TransactionDetailsFragment.adapter
            swipeToRefresh.setOnRefreshListener {
                load()
            }
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        }
        load()
    }

    private fun load() {
        viewModel.load().observe(viewLifecycleOwner, Observer {
            when (it) {
                is TransactionDetails -> adapter.setItemsUnsafe(it.details)
                is Loading -> binding.swipeToRefresh.isRefreshing = it.isLoading
                is Error -> {
                    Timber.e(it.throwable)
                    snackbar(view!!, R.string.error_unknown)
                }
            }
        })
    }
}
