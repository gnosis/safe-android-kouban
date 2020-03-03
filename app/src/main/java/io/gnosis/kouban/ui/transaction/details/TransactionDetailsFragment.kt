package io.gnosis.kouban.ui.transaction.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.databinding.FragmentTransactionDetailsBinding

class TransactionDetailsFragment : BaseFragment<FragmentTransactionDetailsBinding>() {

    private val navArgs by navArgs<TransactionDetailsFragmentArgs>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionDetailsBinding =
        FragmentTransactionDetailsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addressText.text = navArgs.txHash
    }
}
