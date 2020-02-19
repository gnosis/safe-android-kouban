package io.gnosis.kouban.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.onboarding.databinding.FragmentHollowBinding

class HollowFragment : BaseFragment<FragmentHollowBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHollowBinding =
        FragmentHollowBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.container.addView(layoutInflater.inflate(arguments!!.getInt(LAYOUT_ID), null))
    }

    companion object {
        private const val LAYOUT_ID = "LAYOUT_ID"

        fun newInstance(@LayoutRes layoutId: Int): HollowFragment =
            HollowFragment().apply {
                arguments = bundleOf(LAYOUT_ID to layoutId)
            }
    }
}
