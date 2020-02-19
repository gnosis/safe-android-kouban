package io.gnosis.kouban.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.settings.R
import io.gnosis.kouban.settings.databinding.FragmentSettingsBinding
import io.gnosis.kouban.settings.databinding.ItemSafeOwnerBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.svalinn.common.utils.withArgs
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by currentScope.viewModel(this)
    private val mainViewModel: MainViewModel by sharedViewModel()
    private val adapter by currentScope.inject<BaseAdapter<Solidity.Address, ItemSafeOwnerBinding, SafeOwnerViewHolder>>()
    private val addressHelper by inject<AddressHelper>()

    private lateinit var safeAddress: Solidity.Address

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        safeAddress = mainViewModel.address!!

        with(binding) {

            ownerList.layoutManager = LinearLayoutManager(context)
            ownerList.adapter = adapter
            ownerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

            swipeToRefresh.setOnRefreshListener {
                load(safeAddress)
            }
        }

        load(safeAddress)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadOwners(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    binding.swipeToRefresh.isRefreshing = it.isLoading
                }
                is SafeSettings -> {
                    adapter.setItemsUnsafe(it.owners)
                    binding.threshold.text = getString(R.string.threshold, it.threshold)
                }
                is Error -> {
                    Timber.e(it.throwable)
                    snackbar(binding.root, "SOME ERRROR")
                }
            }
        })
    }

    companion object {

        private const val EXTRA_SAFE_ADDRESS = "extra.string.safe_address"

        fun newInstance(safeAddress: Solidity.Address) =
            SettingsFragment().withArgs(
                Bundle().apply {
                    putString(EXTRA_SAFE_ADDRESS, safeAddress.asEthereumAddressString())
                }
            )
    }
}



