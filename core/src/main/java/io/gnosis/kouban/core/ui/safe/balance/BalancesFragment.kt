package io.gnosis.kouban.core.ui.safe.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.gnosis.kouban.core.databinding.FragmentBalancesBinding
import io.gnosis.kouban.core.databinding.ItemTokenBalanceBinding
import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.core.ui.OnAddressChangedListener
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.data.models.Balance
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import timber.log.Timber

class BalancesFragment : BaseFragment<FragmentBalancesBinding>() {

    private val viewModel: BalancesViewModel by currentScope.viewModel(this)
    private val mainViewModel by sharedViewModel<MainViewModel>()
    private val adapter by currentScope.inject<BaseAdapter<Balance, ItemTokenBalanceBinding, BalanceViewHolder>>()
    private val addressChangeListener: OnAddressChangedListener = { it?.let { address -> load(address) } }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBalancesBinding {
        return FragmentBalancesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            list.layoutManager = LinearLayoutManager(context)
            list.adapter = adapter
            list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

            swipeToRefresh.setOnRefreshListener {
                mainViewModel.address.let { address ->
                    if (address != null) {
                        load(address)
                    } else {
                        swipeToRefresh.isRefreshing = false
                    }
                }
            }

        }

        if (arguments?.containsKey("safeAddress") == true) {
            mainViewModel.address = arguments!!.getString("safeAddress", "").asEthereumAddress()!!
            mainViewModel.address?.let { load(it) }
        } else {
            mainViewModel.address?.let { load(it) }
        }
        mainViewModel.addListener(addressChangeListener)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadBalancesOf(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> binding.swipeToRefresh.isRefreshing = it.isLoading
                is Balances -> adapter.setItemsUnsafe(it.balances)
                is Error -> {
                    Timber.e(it.throwable)
                    snackbar(binding.root, "SOME ERRROR")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.removeListener(addressChangeListener)
    }

    companion object {
        fun newInstance(): BalancesFragment = BalancesFragment()
    }
}
