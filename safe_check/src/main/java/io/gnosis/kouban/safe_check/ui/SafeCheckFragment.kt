package io.gnosis.kouban.safe_check.ui

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.safe_check.R
import io.gnosis.kouban.safe_check.databinding.FragmentSettingsBinding
import io.gnosis.kouban.safe_check.databinding.ItemSafeOwnerBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.getColorCompat
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

            safeAddress.text = formatEthAddress(address)
            safeAddressImage.setAddress(address)

            swipeToRefresh.setOnRefreshListener {
                load(address)
            }
        }
        binding.safeCheckData.visibility = View.GONE

        load(address)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadOwners(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    binding.swipeToRefresh.isRefreshing = it.isLoading
                }
                is SafeSettings -> {
                    binding.safeCheckData.visibility = View.VISIBLE
                    //adapter.setItemsUnsafe(it.owners)
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
            val ownerItem = ItemSafeOwnerBinding.inflate(layoutInflater)
            ownerItem.ownerAddress.text = formatEthAddress(it)
            ownerItem.ownerAddressImage.setAddress(it)
            binding.ownersList.addView(ownerItem.root)
        }
    }

    private fun addModules(modules: List<Solidity.Address>) {
        binding.modulesList.removeAllViews()
        binding.modulesData.reset()
        if (modules.isNotEmpty()) {
            modules.forEach {
                val moduleItem = ItemSafeOwnerBinding.inflate(layoutInflater)
                moduleItem.ownerAddress.text = formatEthAddress(it)
                moduleItem.ownerAddressImage.setAddress(it)
                binding.modulesList.addView(moduleItem.root)
            }
            binding.modulesData.displayedChild = 1
        } else {
            binding.modulesData.displayedChild = 0
        }
    }

    fun formatEthAddress(address: Solidity.Address): Spannable {
        //make first & last 4 characters black
        val addressString = SpannableStringBuilder(address.asEthereumAddressString()).insert(21, "\n")
        addressString.setSpan(ForegroundColorSpan(context!!.getColorCompat(R.color.address_boundaries)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        addressString.setSpan(ForegroundColorSpan(context!!.getColorCompat(R.color.address_boundaries)), addressString.length - 4, addressString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return addressString
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




