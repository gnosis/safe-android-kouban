package io.gnosis.kouban.ui.safe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.databinding.FragmentShareSafeBinding
import pm.gnosis.utils.asEthereumAddress
import androidx.navigation.fragment.navArgs

class ShareSafeFragment : BaseFragment<FragmentShareSafeBinding>() {

    private val navArgs by navArgs<ShareSafeFragmentArgs>()
    private val currentSafe by lazy { navArgs.safeAddress.asEthereumAddress()!! }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentShareSafeBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }
}
