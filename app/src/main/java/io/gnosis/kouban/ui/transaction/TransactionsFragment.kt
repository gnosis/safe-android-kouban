package io.gnosis.kouban.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.databinding.FragmentTransactionsBinding
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.Error
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import timber.log.Timber
import io.gnosis.kouban.R

class TransactionsFragment : BaseFragment<FragmentTransactionsBinding>() {

    private val viewModel by currentScope.viewModel<TransactionsViewModel>(this)
    private val adapter by currentScope.inject<BaseAdapter<BaseTransactionViewHolder<Any>>>()
    private val navArgs by navArgs<TransactionsFragmentArgs>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionsBinding =
        FragmentTransactionsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            list.layoutManager = LinearLayoutManager(context)
            list.adapter = adapter
            list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

            swipeToRefresh.setOnRefreshListener {
                load(navArgs.safeAddress.asEthereumAddress()!!)
            }

            toolbar.inflateMenu(R.menu.main)
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.safe_check) {
                    findNavController().navigate(
                        TransactionsFragmentDirections.actionTransactionsFragmentToSafeCheckFragment(navArgs.safeAddress)
                    )
                }
                true
            }
        }
        load(navArgs.safeAddress.asEthereumAddress()!!)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadTransactionsOf(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> binding.swipeToRefresh.isRefreshing = it.isLoading
                is Transactions -> adapter.setItemsUnsafe(it.transactions.flatMap { transactions ->
                    mutableListOf<Any>(
                        Header(transactions.key.name)
                    ).apply {
                        addAll(transactions.value)
                    }
                })
                is ListViewItems -> adapter.setItemsUnsafe(it.listItems)
                is Error -> {
                    it.throwable.printStackTrace()
                    Timber.e(it.throwable)
                    snackbar(binding.root, R.string.unknown_error)
                }
            }
        })
    }
}
