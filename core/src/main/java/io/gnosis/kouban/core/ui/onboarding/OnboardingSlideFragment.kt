package io.gnosis.kouban.core.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import io.gnosis.kouban.core.databinding.FragmentOnboardingSlideBinding
import io.gnosis.kouban.core.ui.base.BaseFragment

class OnboardingSlideFragment : BaseFragment<FragmentOnboardingSlideBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingSlideBinding =
        FragmentOnboardingSlideBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.container.addView(layoutInflater.inflate(arguments!!.getInt(LAYOUT_ID), null))
    }

    companion object {
        private const val LAYOUT_ID = "LAYOUT_ID"

        fun newInstance(@LayoutRes layoutId: Int): OnboardingSlideFragment =
            OnboardingSlideFragment().apply {
                arguments = bundleOf(LAYOUT_ID to layoutId)
            }
    }
}
