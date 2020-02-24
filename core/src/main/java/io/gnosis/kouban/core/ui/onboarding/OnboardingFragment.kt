package io.gnosis.kouban.core.ui.onboarding

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
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

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingBinding =
        FragmentOnboardingBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val pagerAdapter = PagerAdapter(
                listOf(
                    R.layout.onboarding_slide1,
                    R.layout.onboarding_slide2,
                    R.layout.onboarding_slide3
                ), this@OnboardingFragment
            )
            pages.adapter =
                pagerAdapter
            TabLayoutMediator(indicator, pages, true) { _, _ -> }.attach()
            navigationButton.setOnClickListener {
                val nextPage = pages.currentItem + 1 % pagerAdapter.itemCount
                if (nextPage == pagerAdapter.itemCount) {
                    findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToAddressCaptureFragment())
                } else {
                    pages.setCurrentItem(nextPage, true)
                }
            }
            pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    navigationButton.text = if (position == 0) {
                        getString(R.string.button_label_get_started)
                    } else {
                        getString(R.string.button_label_next)
                    }
                }
            })
        }
    }

}

class PagerAdapter(private val layouts: List<Int>, fragment: BaseFragment<*>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = layouts.size

    override fun createFragment(position: Int): Fragment = OnboardingSlideFragment.newInstance(layouts[position])
}
