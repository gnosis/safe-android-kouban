package io.gnosis.kouban.safe_check.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import io.gnosis.kouban.core.ui.base.BaseBottomSheetDialogFragment
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.core.utils.setupLink
import io.gnosis.kouban.data.models.SafeInfoDeployment
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.safe_check.R
import io.gnosis.kouban.safe_check.databinding.DialogSafeDeploymentDetailsBinding
import io.gnosis.kouban.safe_check.databinding.ItemEthAddressBinding
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.withArgs

class SafeDeploymentDetailsDialog : BaseBottomSheetDialogFragment<DialogSafeDeploymentDetailsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogSafeDeploymentDetailsBinding =
        DialogSafeDeploymentDetailsBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deploymentInfo = arguments!![EXTRA_DEPLOYMENT_DETAILS] as SafeInfoDeployment

        binding.contractVersion.text = getString(
            R.string.safe_mastercopy_version,
            getString(
                when (deploymentInfo.masterCopy) {
                    SafeRepository.safeMasterCopy_0_1_0 -> R.string.version_0_1_0
                    SafeRepository.safeMasterCopy_1_0_0 -> R.string.version_1_0_0
                    SafeRepository.safeMasterCopy_1_1_1 -> R.string.version_1_1_1
                    else -> R.string.version_unknown
                }
            )
        )

        binding.masterCopyAddress.text = deploymentInfo.masterCopy.formatEthAddress(context!!)
        binding.masterCopyImage.setAddress(deploymentInfo.masterCopy)
        binding.fallbackHandlerAddress.text = deploymentInfo.fallbackHandler?.formatEthAddress(context!!)
        binding.fallbackHandlerImage.setAddress(deploymentInfo.fallbackHandler)
        addOwners(deploymentInfo.owners)
        binding.threshold.text = getString(R.string.required_confirmations_value, deploymentInfo.threshold, deploymentInfo.owners.size)
        binding.etherscanLink.setupLink(getString(R.string.etherscan_transaction_url, deploymentInfo.txHash), getString(R.string.view_transaction_on))
    }

    private fun addOwners(owners: List<Solidity.Address>) {
        binding.ownersList.removeAllViews()
        owners.forEach {
            val ownerItem = ItemEthAddressBinding.inflate(layoutInflater)
            ownerItem.ownerAddress.text = it.formatEthAddress(context!!)
            ownerItem.ownerAddressImage.setAddress(it)
            binding.ownersList.addView(ownerItem.root)
        }
    }

    companion object {

        private val TAG = SafeDeploymentDetailsDialog::class.java.simpleName

        private const val EXTRA_DEPLOYMENT_DETAILS = "extra.serializable.deployment_details"

        fun show(context: Context, deploymentInfo: SafeInfoDeployment) {
            val dialog = SafeDeploymentDetailsDialog().withArgs(
                Bundle().apply {
                    putParcelable(EXTRA_DEPLOYMENT_DETAILS, deploymentInfo)
                }
            ) as SafeDeploymentDetailsDialog

            dialog.show((context as FragmentActivity).supportFragmentManager, TAG)
        }
    }
}
