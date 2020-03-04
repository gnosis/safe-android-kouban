package io.gnosis.kouban.safe_check.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.gnosis.kouban.core.ui.base.BaseFragment
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.helper.AddressHelper
import io.gnosis.kouban.core.ui.views.HintTooltip
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.data.repositories.SafeDeploymentInfoNotFound
import io.gnosis.kouban.safe_check.R
import io.gnosis.kouban.safe_check.databinding.FragmentSafeCheckBinding
import io.gnosis.kouban.safe_check.databinding.ItemEthAddressBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.getColorCompat
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.svalinn.common.utils.visible
import pm.gnosis.svalinn.common.utils.withArgs
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import timber.log.Timber

class SafeCheckFragment : BaseFragment<FragmentSafeCheckBinding>() {

    private val viewModel: SafeCheckViewModel by currentScope.viewModel(this)
    private val addressHelper by inject<AddressHelper>()

    private lateinit var address: Solidity.Address

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSafeCheckBinding =
        FragmentSafeCheckBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        address = arguments?.getString(EXTRA_SAFE_ADDRESS)?.asEthereumAddress()!!

        with(binding) {

            safeAddress.text = address.formatEthAddress(context!!)
            safeAddressImage.setAddress(address)
            safeCheckData.visibility = View.GONE

            deploymentParam.setOnClickListener {
                SafeDeploymentDetailsDialog.show(context!!, viewModel.safeDeploymentInfo!!)
            }

            swipeToRefresh.setOnRefreshListener {
                load(address)
            }
        }

        load(address)
    }

    private fun load(address: Solidity.Address) {
        viewModel.loadSafeConfig(address).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> binding.swipeToRefresh.isRefreshing = it.isLoading
                is SafeSettings -> displaySafeSettings(it)
                is Error -> {
                    Timber.e(it.throwable)
                    when (it.throwable) {
                        is SafeDeploymentInfoNotFound -> snackbar(binding.root, getString(R.string.error_load_safe_deployment))
                        else -> snackbar(binding.root, getString(R.string.error_load_safe_info))
                    }
                }
            }
        })
    }

    private fun displaySafeSettings(safeSettings: SafeSettings) {
        binding.safeCheckData.visibility = View.VISIBLE
        binding.contractVersion.text = getString(R.string.safe_mastercopy_version, getString(safeSettings.contractVersionResId))
        binding.masterCopyAddress.text = safeSettings.masterCopy.formatEthAddress(context!!)
        binding.masterCopyImage.setAddress(safeSettings.masterCopy)
        binding.fallbackHandlerAddress.text = safeSettings.fallbackHandler?.formatEthAddress(context!!)
        binding.fallbackHandlerImage.setAddress(safeSettings.fallbackHandler)
        binding.ensName.text = safeSettings.ensName ?: getString(R.string.ens_name_none_set)
        addOwners(safeSettings.owners)
        binding.threshold.text = getString(R.string.required_confirmations_value, safeSettings.threshold, safeSettings.owners.size)
        binding.numTx.text = safeSettings.txCount.toString()
        binding.deploymentParam.isEnabled = safeSettings.deploymentInfoAvailable
        binding.deploymentParam.setText(
            if (safeSettings.deploymentInfoAvailable)
                R.string.click_for_details
            else R.string.deployment_parameters_not_available
        )
        addModules(safeSettings.modules)

        // show health check results
        val healthCheck = safeSettings.checkResults

        healthCheck[CheckSection.CONTRACT]?.let {
            setCheckIndicator(binding.contractCheck, it)
        }

        healthCheck[CheckSection.FALLBACK_HANDLER]?.let {
            setCheckIndicator(binding.fallbackHandlerCheck, it)
        }

        healthCheck[CheckSection.THRESHOLD]?.let {
            setCheckIndicator(binding.thresholdCheck, it)
        }

        healthCheck[CheckSection.OWNERS]?.let {
            setCheckIndicator(binding.ownersCheck, it)
        }

        healthCheck[CheckSection.MODULES]?.let {
            setCheckIndicator(binding.modulesCheck, it)
        }

        healthCheck[CheckSection.DEPLOYMENT_INFO]?.let {
            setCheckIndicator(binding.deploymentParamCheck, it)
        }
    }

    private fun setCheckIndicator(checkCircle: ImageView, check: CheckData) {
        checkCircle.visible(true)
        animateCheckIndicator(checkCircle)
        when (check.result) {
            CheckResult.GREEN -> {
                checkCircle.setImageResource(R.drawable.ic_check_circle_black_24dp)
                checkCircle.setColorFilter(context!!.getColorCompat(R.color.safe_green), PorterDuff.Mode.SRC_IN)
            }
            CheckResult.YELLOW -> {
                checkCircle.setImageResource(R.drawable.ic_info_outline_black_24dp)
                checkCircle.setColorFilter(context!!.getColorCompat(R.color.warning), PorterDuff.Mode.SRC_IN)
            }
            CheckResult.RED -> {
                checkCircle.setImageResource(R.drawable.ic_error_black_24dp)
                checkCircle.setColorFilter(context!!.getColorCompat(R.color.tomato), PorterDuff.Mode.SRC_IN)
            }
        }
        check.hint?. let { hintResId ->
            checkCircle.setOnClickListener {
                HintTooltip(context!!, hintResId).showAsDropDown(it)
            }
        }
    }

    private fun animateCheckIndicator(checkCircle: ImageView) {
        val set = AnimatorSet()
        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            checkCircle,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        scaleUp.duration = 500

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            checkCircle,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f)
        )
        scaleDown.duration = 500

        set.playSequentially(scaleUp, scaleDown)
        set.start()
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

    private fun addModules(modules: List<Solidity.Address>) {
        binding.modulesList.removeAllViews()
        binding.modulesData.reset()
        if (modules.isNotEmpty()) {
            modules.forEach {
                val moduleItem = ItemEthAddressBinding.inflate(layoutInflater)
                moduleItem.ownerAddress.text = it.formatEthAddress(context!!)
                moduleItem.ownerAddressImage.setAddress(it)
                binding.modulesList.addView(moduleItem.root)
            }
            binding.modulesData.displayedChild = 1
        } else {
            binding.modulesData.displayedChild = 0
        }
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

    companion object {

        private const val EXTRA_SAFE_ADDRESS = "safeAddress"

        fun newInstance(safeAddress: Solidity.Address) =
            SafeCheckFragment().withArgs(
                Bundle().apply {
                    putString(EXTRA_SAFE_ADDRESS, safeAddress.asEthereumAddressString())
                }
            )
    }
}




