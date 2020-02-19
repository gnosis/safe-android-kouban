package io.gnosis.kouban.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.gnosis.kouban.onboarding.databinding.ActivityOnboardingBinding
import io.gnosis.kouban.core.ui.base.BaseActivity

class OnboardingActivity : BaseActivity() {

    private val binding: ActivityOnboardingBinding by lazy {
        ActivityOnboardingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            val pagerAdapter = PagerAdapter(
                listOf(R.layout.onboarding_page1, R.layout.onboarding_page2, R.layout.onboarding_page3),
                this@OnboardingActivity
            )
            pages.adapter = pagerAdapter
            TabLayoutMediator(indicator, pages, true) { _, _ -> }.attach()
            pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    navigationButton.text = if (position == pagerAdapter.itemCount - 1) {
                        getString(R.string.button_label_get_started)
                    } else {
                        getString(R.string.button_label_next)
                    }
                }
            })
            navigationButton.setOnClickListener {
                val nextPage = pages.currentItem + 1 % pagerAdapter.itemCount
                if (nextPage == pagerAdapter.itemCount) {
                    setShouldShow()
                    finish()
                } else {
                    pages.setCurrentItem(nextPage, true)
                }
            }
        }
    }

    override fun onBackPressed() {
        setShouldShow()
        super.onBackPressed()
    }

    private fun setShouldShow() {
//        getSharedPreferences(DynamicFeatureManager.ONBOARDING, Context.MODE_PRIVATE).let { sharedPreferences ->
//            sharedPreferences.edit {
//                putBoolean(MainActivity.SHOULD_SHOW_ONBOARDING, false)
//                commit()
//            }
//        }
//        dynamicFeatureManager.uninstallModule(DynamicFeatureManager.ONBOARDING)
    }

    companion object {
        fun createIntent(context: Context?) = Intent(context, OnboardingActivity::class.java)
    }
}


class PagerAdapter(private val layouts: List<Int>, activity: OnboardingActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = layouts.size

    override fun createFragment(position: Int): Fragment = HollowFragment.newInstance(layouts[position])
}
