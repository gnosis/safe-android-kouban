package io.gnosis.kouban.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.onboarding.databinding.ActivityOnboardingBinding
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.core.utils.asMiddleEllipsized
import io.gnosis.kouban.qrscanner.QRCodeScanActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.asEthereumAddressString

class OnboardingActivity : BaseActivity() {

    private val binding: ActivityOnboardingBinding by lazy {
        ActivityOnboardingBinding.inflate(layoutInflater)
    }

    private var address: Solidity.Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            addressInput.apply {
                setupInputHandler(true)
                onAddressChanged = { selectedAddress ->
                    address = selectedAddress
                    completeButton.visibility = if (selectedAddress != null) VISIBLE else GONE
                    confirmationText.apply {
                        if (selectedAddress != null) {
                            visibility = VISIBLE
                            text = getString(R.string.label_confirmation, selectedAddress.asEthereumAddressString().asMiddleEllipsized(4))
                        } else {
                            visibility = GONE
                        }
                    }
                }
                requestQRScanner = {
                    startActivityForResult(
                        QRCodeScanActivity.createIntent(this@OnboardingActivity, "Provide the address to observe"),
                        QR_SCAN_REQUEST_CODE
                    )
                }
            }
            completeButton.setOnClickListener {
                completeAndSubmit()
            }
        }
    }

    private fun completeAndSubmit() {
        address?.asEthereumAddressString()?.let { stringAddress ->
            getSharedPreferences(ONBOARDING, Context.MODE_PRIVATE).let { sharedPreferences ->
                sharedPreferences.edit {
                    putString(SAFE_ADDRESS, stringAddress)
                    commit()
                }
            }
            setResult(Activity.RESULT_OK, Intent().putExtra(SAFE_ADDRESS, stringAddress))
        }
        finish()
    }

    companion object {
        const val ONBOARDING = "ONBOARDING"
        const val SAFE_ADDRESS = "SAFE_ADDRESS"
        const val QR_SCAN_REQUEST_CODE = 1
        const val ADDRESS_REQUEST_CODE = 2
        fun createIntent(context: Context?) = Intent(context, OnboardingActivity::class.java)
    }
}
