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
        binding.navigationButton.setOnClickListener {
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToAddressCaptureFragment())
        }
//        consumeViewStates()
    }

//    private fun consumeViewStates() {
//        viewModel.safeAddressEvents.observe(viewLifecycleOwner, Observer { viewState ->
//            when (viewState) {
//            }
//        })
//    }

}
