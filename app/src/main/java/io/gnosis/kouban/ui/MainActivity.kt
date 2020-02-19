package io.gnosis.kouban.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.databinding.ActivityMainBinding
import io.gnosis.kouban.onboarding.OnboardingActivity
import io.gnosis.kouban.onboarding.OnboardingActivity.Companion.ADDRESS_REQUEST_CODE
import io.gnosis.kouban.onboarding.OnboardingActivity.Companion.ONBOARDING
import io.gnosis.kouban.onboarding.OnboardingActivity.Companion.QR_SCAN_REQUEST_CODE
import io.gnosis.kouban.onboarding.OnboardingActivity.Companion.SAFE_ADDRESS
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.utils.asEthereumAddress

class MainActivity : BaseActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        with(findNavController(R.id.main_container)) {
//            binding.bottomNavigationBar.setupWithNavController(this)
//            addOnDestinationChangedListener { _, destination, _ ->
//                binding.toolbar.title = destination.label
//            }
//        }
        binding.toolbar.title = getString(R.string.app_name)
        loadAddress()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.settings).isVisible = viewModel.address != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadAddress() {
        if (isAddressDefined()) {
            getSharedPreferences(ONBOARDING, Context.MODE_PRIVATE)
                .getString(SAFE_ADDRESS, "")?.asEthereumAddress()?.let { address ->
                    viewModel.address = address
                } ?: error("Address must not be null")
        } else {
            startActivityForResult(OnboardingActivity.createIntent(this), ADDRESS_REQUEST_CODE)
        }
    }

    private fun isAddressDefined(): Boolean {
        return !getSharedPreferences(ONBOARDING, Context.MODE_PRIVATE)
            ?.getString(SAFE_ADDRESS, "").isNullOrEmpty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(SAFE_ADDRESS)?.let { viewModel.address = it.asEthereumAddress() }
                ?: snackbar(binding.root, "Error capturing the address")
        }
    }


}
