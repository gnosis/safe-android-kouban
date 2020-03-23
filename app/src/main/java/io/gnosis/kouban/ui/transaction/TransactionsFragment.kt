package io.gnosis.kouban.ui.transaction

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
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
import pm.gnosis.utils.asEthereumAddressString

class TransactionsFragment : BaseFragment<FragmentTransactionsBinding>() {

    private val viewModel by currentScope.viewModel<TransactionsViewModel>(this)
    private val adapter by currentScope.inject<BaseAdapter<BaseTransactionViewHolder<Any>>>()
    private val navArgs by navArgs<TransactionsFragmentArgs>()
    private val currentSafe by lazy { navArgs.safeAddress.asEthereumAddress()!! }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionsBinding =
        FragmentTransactionsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            list.layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
            }
            list.adapter = adapter

            swipeToRefresh.setOnRefreshListener {
                load(currentSafe)
            }
        }
        load(currentSafe)
        setupToolbar()
    }

    private fun setupToolbar() {
        with(binding) {
            blockiesHeader.setAddress(currentSafe)
            safeAddress.text = currentSafe.asEthereumAddressString().asMiddleEllipsized(4)
            toolbar.inflateMenu(R.menu.main)
            toolbar.setOnMenuItemClickListener {
                onMenuItemClicked(it.itemId)
                true
            }
        }
        viewModel.loadHeaderInfo(currentSafe)
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Error -> onError(it.throwable)
                    is Loading -> binding.headerProgress.isVisible = it.isLoading
                    is ENSName -> binding.collapingToolbar.title = it.ensName ?: getString(R.string.safe_label)

                }
            })
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
