package io.gnosis.kouban.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.gnosis.kouban.R
import com.google.android.material.tabs.TabLayoutMediator
import io.gnosis.kouban.databinding.FragmentOnboardingBinding
import io.gnosis.kouban.core.ui.base.BaseFragment

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

    override fun createFragment(position: Int): Fragment =
        OnboardingSlideFragment.newInstance(layouts[position])
}
