package io.gnosis.kouban.safe_check.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.safe_check.databinding.FragmentSettingsBinding
import io.gnosis.kouban.safe_check.databinding.ItemSafeOwnerBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.svalinn.common.utils.withArgs
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

class SafeCheckFragment : BaseFragment<FragmentSettingsBinding>() {


    private val viewModel: SafeCheckViewModel by currentScope.viewModel(this)
    //private val adapter by currentScope.inject<BaseAdapter<Solidity.Address, ItemSafeOwnerBinding, SafeOwnerViewHolder>>()
    private val addressHelper by inject<AddressHelper>()

    private lateinit var address: Solidity.Address

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //safeAddress = SafeAddressManager(context).getSafeAddress()!!//arguments?.getString(EXTRA_SAFE_ADDRESS)?.asEthereumAddress()!!
        address = "0x83eC7B0506556a7749306D69681aDbDbd08f0769".asEthereumAddress()!!
        with(binding) {

            safeAddress.text = address.asEthereumAddressString()
            safeAddressImage.setAddress(address)

            swipeToRefresh.setOnRefreshListener {
                load(address)
            }
        }

        load(address)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadOwners(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    binding.swipeToRefresh.isRefreshing = it.isLoading
                }
                is SafeSettings -> {
                    //adapter.setItemsUnsafe(it.owners)
                    addOwners(it.owners)
                    binding.threshold.text = it.threshold.toString()
                }
                is Error -> {
                    Timber.e(it.throwable)
                    snackbar(binding.root, "SOME ERRROR")
                }
            }
        })
    }

    private fun addOwners(owners: List<Solidity.Address>) {
        binding.ownersList.removeAllViews()
        owners.forEach {
            val ownerItem = ItemSafeOwnerBinding.inflate(layoutInflater)
            ownerItem.ownerAddress.text = it.asEthereumAddressString()
            ownerItem.ownerAddressImage.setAddress(it)
            binding.ownersList.addView(ownerItem.root)
        }
    }

    companion object {

        private const val EXTRA_SAFE_ADDRESS = "extra.string.safe_address"

        fun newInstance(safeAddress: Solidity.Address) =
            SafeCheckFragment().withArgs(
                Bundle().apply {
                    putString(EXTRA_SAFE_ADDRESS, safeAddress.asEthereumAddressString())
                }
            )
    }
}



