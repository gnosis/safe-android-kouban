package io.gnosis.kouban.safe_check.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.safe_check.databinding.FragmentSafeCheckBinding
import io.gnosis.kouban.safe_check.databinding.ItemEthAddressBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.svalinn.common.utils.withArgs
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

class SafeCheckFragment : BaseFragment<FragmentSafeCheckBinding>() {

    private val viewModel: SafeCheckViewModel by currentScope.viewModel(this)
    private val addressHelper by inject<AddressHelper>()

    private lateinit var address: Solidity.Address

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSafeCheckBinding =
        FragmentSafeCheckBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address = arguments?.getString(EXTRA_SAFE_ADDRESS)?.asEthereumAddress()!!

        with(binding) {

            safeAddress.text = context!!.formatEthAddress(address)
            safeAddressImage.setAddress(address)
            safeCheckData.visibility = View.GONE

            swipeToRefresh.setOnRefreshListener {
                load(address)
            }
        }

        load(address)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadSafeConfig(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    binding.swipeToRefresh.isRefreshing = it.isLoading
                }
                is SafeSettings -> {
                    binding.safeCheckData.visibility = View.VISIBLE
                    addOwners(it.owners)
                    binding.threshold.text = it.threshold.toString()
                    binding.numTx.text = it.txCount.toString()
                    addModules(it.modules)
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
            val ownerItem = ItemEthAddressBinding.inflate(layoutInflater)
            ownerItem.ownerAddress.text = context!!.formatEthAddress(it)
            ownerItem.ownerAddressImage.setAddress(it)
            binding.ownersList.addView(ownerItem.root)
        }
    }

    private fun addModules(modules: List<Solidity.Address>) {
        binding.modulesList.removeAllViews()
        binding.modulesData.reset()
        if (modules.isNotEmpty()) {
            modules.forEach {
                val moduleItem = ItemEthAddressBinding.inflate(layoutInflater)
                moduleItem.ownerAddress.text = context!!.formatEthAddress(it)
                moduleItem.ownerAddressImage.setAddress(it)
                binding.modulesList.addView(moduleItem.root)
            }
            binding.modulesData.displayedChild = 1
        } else {
            binding.modulesData.displayedChild = 0
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




