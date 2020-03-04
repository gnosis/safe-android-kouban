package io.gnosis.kouban.safe_check.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.models.SafeInfo
import io.gnosis.kouban.data.models.SafeInfoDeployment
import io.gnosis.kouban.data.repositories.EnsRepository
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import io.gnosis.kouban.safe_check.R
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import java.math.BigInteger

class SafeCheckViewModel(
    private val safeRepository: SafeRepository,
    private val tokenRepository: TokenRepository,
    private val ensRepository: EnsRepository
) : ViewModel() {

    var safeDeploymentInfo: SafeInfoDeployment? = null
        private set

    fun loadSafeConfig(address: Solidity.Address): LiveData<ViewState> =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))

            kotlin.runCatching {

                if (safeDeploymentInfo == null)
                    kotlin.runCatching {
                        safeRepository.loadSafeDeploymentParams(address)
                    }.onSuccess {
                        safeDeploymentInfo = it
                    }.onFailure {
                        emit(Error(it))
                    }

                kotlin.runCatching {
                    val safeInfo = safeRepository.loadSafeInfo(address)
                    val ensName = ensRepository.resolve(address)

                    val contractVersionResId = when (safeInfo.masterCopy) {
                        SafeRepository.safeMasterCopy_0_1_0 -> R.string.version_0_1_0
                        SafeRepository.safeMasterCopy_1_0_0 -> R.string.version_1_0_0
                        SafeRepository.safeMasterCopy_1_1_1 -> R.string.version_1_1_1
                        else -> R.string.version_unknown
                    }

                    SafeSettings(
                        contractVersionResId,
                        safeInfo.masterCopy,
                        safeInfo.fallbackHandler,
                        ensName,
                        safeInfo.owners,
                        safeInfo.threshold.toInt(),
                        safeInfo.currentNonce.toInt(),
                        safeInfo.modules,
                        safeDeploymentInfo != null,
                        performHealthCheck(safeInfo, safeDeploymentInfo)
                    )

                }.onSuccess {
                    emit(Loading(false))
                    emit(it)
                }.onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
            }
        }

    private fun performHealthCheck(info: SafeInfo, deploymentInfo: SafeInfoDeployment?): HashMap<CheckSection, CheckData> {
        val healthCheck = HashMap<CheckSection, CheckData>()

        healthCheck[CheckSection.CONTRACT] = checkContract(info)
        healthCheck[CheckSection.FALLBACK_HANDLER] = checkFallbackHandler(info)
        healthCheck[CheckSection.OWNERS] = checkOwners(info)
        healthCheck[CheckSection.THRESHOLD] = checkThreshold(info)
        healthCheck[CheckSection.MODULES] = checkModules(info)
        healthCheck[CheckSection.DEPLOYMENT_INFO] = checkDeployment(deploymentInfo)

        return healthCheck
    }

    private fun checkContract(info: SafeInfo): CheckData {
        // should have recent contract version
        val contractCheck = when (info.masterCopy) {
            SafeRepository.safeMasterCopy_0_1_0 -> CheckData(CheckResult.YELLOW, R.string.check_contract_version_old)
            SafeRepository.safeMasterCopy_1_0_0 -> CheckData(CheckResult.YELLOW, R.string.check_contract_version_old)
            SafeRepository.safeMasterCopy_1_1_1 -> CheckData(CheckResult.GREEN, R.string.check_all_good)
            else -> CheckData(CheckResult.RED, R.string.check_contract_version_unknown)
        }
        return contractCheck
    }

    private fun checkFallbackHandler(info: SafeInfo): CheckData {
        // should have fallback handler
        // fallback handler should be known
        val fallbackHandlerCheck = when {
            info.fallbackHandler == Solidity.Address(BigInteger.ZERO) || info.fallbackHandler == null -> CheckData(
                CheckResult.YELLOW,
                R.string.check_fallback_not_set
            )
            info.fallbackHandler == FALLBACK_HANDLER -> CheckData(CheckResult.GREEN, R.string.check_all_good)
            else -> CheckData(CheckResult.GREEN, R.string.check_all_good)
        }
        return fallbackHandlerCheck
    }

    private fun checkOwners(info: SafeInfo): CheckData {
        val ownersCount = info.owners.size
        // should have more that 1 owner
        val ownersCheck =
            if (ownersCount == 1)
                CheckData(CheckResult.YELLOW, R.string.check_owners_only_one)
            else
                CheckData(CheckResult.GREEN, R.string.check_all_good)
        return ownersCheck
    }

    private fun checkThreshold(info: SafeInfo): CheckData {
        val ownersCount = info.owners.size
        // should have multi factor authentication
        // threshold == ownersCount should be avoided => lose of one of the private keys will lead to lock out
        val thresoldCheck = when (info.threshold.toInt()) {
            1 -> CheckData(CheckResult.YELLOW, R.string.check_threshold_one)
            ownersCount -> CheckData(CheckResult.YELLOW, R.string.check_threshold_all)
            else -> CheckData(CheckResult.GREEN, R.string.check_all_good)
        }
        return thresoldCheck
    }

    private fun checkModules(info: SafeInfo): CheckData {
        // all unaudited modules are potentially unsafe
        //TODO: potentially show checkmark for earch of the modules
        val modulesCheck = when {
            info.modules == listOf(MODULE_ALLOWANCE) -> CheckData(CheckResult.GREEN, R.string.check_all_good)
            info.modules.isNotEmpty() -> CheckData(CheckResult.YELLOW, R.string.check_modules_danger_potential)
            else -> CheckData(CheckResult.GREEN, R.string.check_all_good)
        }
        return modulesCheck
    }

    private fun checkDeployment(deploymentInfo: SafeInfoDeployment?): CheckData {
        // deployment info should be accessible and it should be possible to decode it
        val deploymentCheck =
            if (deploymentInfo != null)
                CheckData(CheckResult.GREEN, R.string.check_all_good)
            else
                CheckData(CheckResult.YELLOW, R.string.check_deployment_info_not_available)
        return deploymentCheck
    }

    companion object {

        private val MODULE_ALLOWANCE = BuildConfig.SAFE_MODULE_ALLOWANCE.asEthereumAddress()!!
        private val FALLBACK_HANDLER = BuildConfig.FALLBACK_HANDLER_DEFAULT.asEthereumAddress()!!
    }
}

data class SafeSettings(
    @StringRes
    val contractVersionResId: Int,
    val masterCopy: Solidity.Address,
    val fallbackHandler: Solidity.Address?,
    val ensName: String?,
    val owners: List<Solidity.Address>,
    val threshold: Int,
    val txCount: Int,
    val modules: List<Solidity.Address>,
    val deploymentInfoAvailable: Boolean,
    val checkResults: HashMap<CheckSection, CheckData>
) : ViewState()


enum class CheckSection {
    CONTRACT,
    FALLBACK_HANDLER,
    OWNERS,
    THRESHOLD,
    MODULES,
    DEPLOYMENT_INFO
}

enum class CheckResult {
    GREEN,
    YELLOW,
    RED
}

data class CheckData(
    val result: CheckResult,
    @StringRes
    val hint: Int? = null
)

