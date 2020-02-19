package io.gnosis.kouban.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import io.gnosis.kouban.R
import io.gnosis.kouban.core.ui.MainViewModel
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        with(findNavController(R.id.main_container)) {
            binding.bottomNavigationBar.setupWithNavController(this)
            addOnDestinationChangedListener { _, destination, _ ->
                binding.toolbar.title = destination.label
            }
        }

//        binding.addressInputContainer.apply {
//            onAddressChanged = {
//                viewModel.address = it
//                invalidateOptionsMenu()
//            }
//            requestQRScanner = {
//                dynamicFeatureManager.loadModule(
//                    this@MainActivity,
//                    DynamicFeatureManager.QRSCANNER,
//                    DynamicFeatureManager.Request.QRScanner("DESCRIPTION!")
//                ) {
//                    startActivityForResult(it, QR_SCAN_REQUEST_CODE)
//                }
//            }
//        }
        binding.addressInputContainer.setupInputHandler(true)

        viewModel.addListener {
            binding.addressInputContainer.updateAddress(it, false)
        }
//        showOnboarding()
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

//    private fun showOnboarding() {
//        if (shouldShowOnboarding()) {
//            dynamicFeatureManager.loadModule(this, DynamicFeatureManager.ONBOARDING) {
//                startActivity(it)
//            }
//        }
//}

//    private fun shouldShowOnboarding(): Boolean {
//        return getSharedPreferences(DynamicFeatureManager.ONBOARDING, Context.MODE_PRIVATE)
//            ?.getBoolean(SHOULD_SHOW_ONBOARDING, true) ?: true
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == QR_SCAN_REQUEST_CODE) {
//            (dynamicFeatureManager.getResult(DynamicFeatureManager.QRSCANNER, data) as? DynamicFeatureManager.Results.QRScanner)?.code?.let {
//                binding.addressInputContainer.updateAddress(it.asEthereumAddress()!!)
//            }
//        }
//    }

    companion object {
        private const val QR_SCAN_REQUEST_CODE = 0
        const val SHOULD_SHOW_ONBOARDING = "SHOULD_SHOW_ONBOARDING"

    }
}
