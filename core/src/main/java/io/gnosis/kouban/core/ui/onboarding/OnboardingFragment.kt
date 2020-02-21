package io.gnosis.kouban.core.ui.onboarding

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.databinding.FragmentOnboardingBinding
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.qrscanner.QRCodeScanActivity
import kotlinx.android.synthetic.main.address_input.*
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import io.gnosis.kouban.core.ui.base.Error

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>() {

    private val viewModel by currentScope.viewModel<OnboardingViewModel>(this)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingBinding =
        FragmentOnboardingBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.addressInput) {
            setupInputHandler(true)
            onAddressChanged = { safeAddress ->
                if (safeAddress != null) {
                    viewModel.handleSafeAddress(safeAddress)
                }
            }
            requestQRScanner = {
                startActivityForResult(QRCodeScanActivity.createIntent(context), QR_SCAN_REQUEST_CODE)
            }
        }
        binding.navigationButton.setOnClickListener { viewModel.submitAddress() }
        consumeViewStates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCAN_REQUEST_CODE && requestCode == RESULT_OK) {
            QRCodeScanActivity.handleResult(QR_SCAN_REQUEST_CODE, resultCode, data) { safeAddressString ->
                viewModel.handleSafeAddress(safeAddressString.asEthereumAddress()!!)
            }
        }
    }

    private fun consumeViewStates() {
        viewModel.safeAddressEvents.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is SafeAddressStored ->
                    findNavController().navigate(
                        OnboardingFragmentDirections
                            .actionOnboardingFragmentToTransactionsFragment(
                                viewState.safeAddress.asEthereumAddressString()
                            )
                    )
                is SafeAddressUpdated -> {
                    //update UI
                }
                is Error -> {
                    val stringId = when (viewState.throwable) {
                        is AddressNotSet -> R.string.error_address_not_set
                        else -> R.string.error_unknown
                    }
                    view?.let { view -> snackbar(view, stringId) }
                }
            }
        })
    }

    private fun getAction() {
    }

    private companion object {
        const val QR_SCAN_REQUEST_CODE = 0
    }
}
