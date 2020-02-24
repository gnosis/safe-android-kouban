package io.gnosis.kouban.settings.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.core.utils.asMiddleEllipsized
import io.gnosis.kouban.settings.R
import io.gnosis.kouban.settings.databinding.ActivitySettingsBinding
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.transaction
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class SettingsActivity : BaseActivity() {

    private lateinit var safeAddress: Solidity.Address

    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent?.extras?.getString(EXTRA_SAFE_ADDRESS)?.let {
            safeAddress = it.asEthereumAddress()!!
        } ?: finish()

        binding.toolbar.subtitle = safeAddress.asEthereumAddressString().asMiddleEllipsized(4)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            loadFragment(SettingsFragment.newInstance(safeAddress))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.transaction {
            replace(R.id.main_container, fragment)
        }
    }

    companion object {

        private const val EXTRA_SAFE_ADDRESS = "extra.strings.safe_address"

        fun createIntent(context: Context?, safeAddress: Solidity.Address) =
            Intent(context, SettingsActivity::class.java).putExtra(EXTRA_SAFE_ADDRESS, safeAddress.asEthereumAddressString())
    }
}
