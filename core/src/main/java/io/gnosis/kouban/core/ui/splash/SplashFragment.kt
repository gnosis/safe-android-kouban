package io.gnosis.kouban.core.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.databinding.FragmentSplashBinding
import io.gnosis.kouban.core.ui.base.BaseFragment
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.utils.asEthereumAddressString

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private val viewModel by currentScope.viewModel<SplashViewModel>(this)

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSplashBinding =
        FragmentSplashBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAddress().observe(viewLifecycleOwner, Observer {
            with(findNavController()) {
                when (it) {
                    is SafeAddressUnavailable -> navigate(R.id.action_splashFragment_to_onboardingFragment)
                    is SafeAddressAvailable -> navigate(
                        SplashFragmentDirections.actionSplashFragmentToTransactionsFragment(
                            it.safeAddress.asEthereumAddressString()
                        )
                    )
                    else -> {
                    }
                }
            }
        })
    }

}
