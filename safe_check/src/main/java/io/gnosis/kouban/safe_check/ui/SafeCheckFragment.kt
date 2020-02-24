package io.gnosis.kouban.safe_check.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.safe_check.databinding.FragmentSettingsBinding
import org.koin.android.ext.android.inject
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.withArgs
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class SafeCheckFragment : BaseFragment<FragmentSettingsBinding>() {


//    private val viewModel: SettingsViewModel by currentScope.viewModel(this)
    //private val adapter by currentScope.inject<BaseAdapter<Solidity.Address, ItemSafeOwnerBinding, SafeOwnerViewHolder>>()
    private val addressHelper by inject<AddressHelper>()

    private lateinit var safeAddress: Solidity.Address

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        safeAddress = arguments?.getString(EXTRA_SAFE_ADDRESS)?.asEthereumAddress()!!

        with(binding) {



//            swipeToRefresh.setOnRefreshListener {
//                load(safeAddress)
//            }
        }

        //load(safeAddress)
    }

    private fun load(address: Solidity.Address) {
//        viewModel.loadOwners(address).observe(viewLifecycleOwner, Observer {
//            when (it) {
//                is Loading -> {
//                    //binding.swipeToRefresh.isRefreshing = it.isLoading
//                }
//                is SafeSettings -> {
//                    //adapter.setItemsUnsafe(it.owners)
//                    binding.threshold.text = getString(R.string.threshold, it.threshold)
//                }
//                is Error -> {
//                    Timber.e(it.throwable)
//                    snackbar(binding.root, "SOME ERRROR")
//                }
//            }
//        })
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



