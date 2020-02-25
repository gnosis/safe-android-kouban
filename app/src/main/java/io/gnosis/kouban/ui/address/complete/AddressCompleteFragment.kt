package io.gnosis.kouban.ui.address.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.R
import io.gnosis.kouban.databinding.FragmentAddressCompleteBinding
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddressString

class AddressCompleteFragment : BaseFragment<FragmentAddressCompleteBinding>() {

    private val viewModel by currentScope.viewModel<AddressCompleteViewModel>(this)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAddressCompleteBinding =
        FragmentAddressCompleteBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            setupCompleteBtn.setOnClickListener {
                viewModel.setupComplete()
            }
        }
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is SetupComplete -> findNavController().navigate(
                    AddressCompleteFragmentDirections.actionAddressCompleteFragmentToTransactionsFragment(
                        viewState.safeAddress.asEthereumAddressString()
                    )
                )
                is Error -> handleError(viewState.throwable)
            }
        })
    }

    private fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is NoAddressAvailable -> R.string.error_address_not_set
            else -> R.string.error_unknown
        }
        view?.let { snackbar(it, errorMessage) }
    }
}
