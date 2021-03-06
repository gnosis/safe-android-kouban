package io.gnosis.kouban.ui.address.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.R
import io.gnosis.kouban.databinding.FragmentAddressCaptureBinding
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.ui.onboarding.OnboardingFragmentDirections
import io.gnosis.kouban.qrscanner.QRCodeScanActivity
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

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
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            QRCodeScanActivity.handleResult(QR_SCAN_REQUEST_CODE, resultCode, data) { safeAddressString ->
                safeAddressString.asEthereumAddress()?.let { safeAddress ->
                    binding.addressInput.updateAddress(safeAddress)
                    viewModel.handleSafeAddress(safeAddress)
                }
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
                    setSafeAddressStatus(R.drawable.ic_check_circle_green_24dp, R.string.valid_safe_address_label)
                }
                is Error -> {
                    if (viewState.throwable is InvalidSafeAddress) {
                        setSafeAddressStatus(R.drawable.ic_info_red_24dp, R.string.error_invalid_safe)
                    } else {
                        val stringId = when (viewState.throwable) {
                            is AddressNotSet -> R.string.error_address_not_set
                            else -> R.string.error_unknown
                        }
                        Timber.e(viewState.throwable)
                        view?.let { view -> snackbar(view, stringId) }
                    }
                }
            }
        })
    }

    private fun setSafeAddressStatus(
        @DrawableRes iconId: Int,
        @StringRes messageId: Int
    ) {
        with(binding.connectSafeAddressStatusIcon) {
            setImageResource(iconId)
            isVisible = true
        }
        with(binding.connectSafeAddressStatusText) {
            setText(messageId)
            isVisible = true
        }
    }

    private companion object {
        const val QR_SCAN_REQUEST_CODE = 0
    }
}
