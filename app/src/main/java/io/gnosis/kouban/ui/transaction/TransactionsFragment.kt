package io.gnosis.kouban.ui.transaction

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.ui.filter.transaction.TransactionFilterDialog
import org.koin.android.ext.android.bind
import pm.gnosis.utils.asEthereumAddressString

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

            swipeToRefresh.setOnRefreshListener {
                load(navArgs.safeAddress.asEthereumAddress()!!)
            }
        }
        load(navArgs.safeAddress.asEthereumAddress()!!)
        setupToolbar()
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.inflateMenu(R.menu.main)
            toolbar.setOnMenuItemClickListener {
                onMenuItemClicked(it.itemId)
                true
            }
        }
        viewModel.loadHeaderInfo().observe(viewLifecycleOwner, Observer {
            when (it) {
                is Error -> onError(it.throwable)
                is Loading -> binding.headerProgress.isVisible = it.isLoading
                is ToolbarSetup -> setToolbarUi(it.safeAddress, it.ensName)
            }
        })
    }

    private fun setToolbarUi(address: Solidity.Address, ensName: String?) {
        with(binding) {
            blockiesHeader.setAddress(address)
            collapingToolbar.title = ensName ?: "Safe"
            safeAddress.text = address.asEthereumAddressString().asMiddleEllipsized(4)
        }
    }

    private fun onMenuItemClicked(itemId: Int) {
        when (itemId) {
            R.id.safe_check -> findNavController().navigate(
                TransactionsFragmentDirections.actionTransactionsFragmentToSafeCheckFragment(navArgs.safeAddress)
            )
            R.id.transaction_filter -> {
                TransactionFilterDialog().apply {
                    onDismissCallback = { load(navArgs.safeAddress.asEthereumAddress()!!) }
                }.show(childFragmentManager, TransactionFilterDialog::class.java.simpleName)
            }
        }
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadTransactionsOf(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> binding.swipeToRefresh.isRefreshing = it.isLoading
                is ListViewItems -> adapter.setItemsUnsafe(it.listItems)
                is Error -> onError(it.throwable)
            }
        })
    }

    private fun onError(throwable: Throwable) {
        Timber.e(throwable)
        snackbar(binding.root, R.string.unknown_error)
    }
}
