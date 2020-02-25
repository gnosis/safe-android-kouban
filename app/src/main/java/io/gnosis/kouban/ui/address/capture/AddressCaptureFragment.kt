package io.gnosis.kouban.ui.address.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.R
import io.gnosis.kouban.databinding.FragmentAddressCaptureBinding
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.ui.onboarding.OnboardingFragmentDirections
import io.gnosis.kouban.qrscanner.QRCodeScanActivity
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class AddressCaptureFragment : BaseFragment<FragmentAddressCaptureBinding>() {

    private val viewModel by currentScope.viewModel<AddressCaptureViewModel>(this)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAddressCaptureBinding =
        FragmentAddressCaptureBinding.inflate(inflater, container, false)

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
        binding.connectSafeSubmitBtn.setOnClickListener { viewModel.submitAddress() }
        binding.connectSafeBackBtn.setOnClickListener { findNavController().navigateUp() }
        consumeViewStates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCAN_REQUEST_CODE && requestCode == Activity.RESULT_OK) {
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
                        AddressCaptureFragmentDirections
                            .actionAddressCaptureFragmentToAddressCompleteFragment()
                    )
                is SafeAddressUpdated -> {
                    with(binding) {
                        connectSafeAddressStatusIcon.isVisible = viewState.safeAddress != null
                        connectSafeAddressStatusText.isVisible = viewState.safeAddress != null
                    }
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

    private companion object {
        const val QR_SCAN_REQUEST_CODE = 0
    }
}
