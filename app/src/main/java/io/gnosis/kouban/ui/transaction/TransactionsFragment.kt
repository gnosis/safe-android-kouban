package io.gnosis.kouban.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.data.utils.asMiddleEllipsized
import io.gnosis.kouban.databinding.FragmentTransactionsBinding
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

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
                viewModel.loadTransactionsOf(currentSafe)
            }
        }
        viewModel.loadTransactionsOf(currentSafe)
        setupToolbar()
        bind()
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
        }
    }

    private fun bind() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            with(binding.swipeToRefresh) {
                isRefreshing = false
                when (it) {
                    is Loading -> isRefreshing = it.isLoading
                    is ListViewItems -> adapter.setItemsUnsafe(it.listItems)
                    is Error -> onError(it.throwable)
                }
            }
        })
    }

    private fun onError(throwable: Throwable) {
        Timber.e(throwable)
        snackbar(binding.root, R.string.unknown_error)
    }
}
