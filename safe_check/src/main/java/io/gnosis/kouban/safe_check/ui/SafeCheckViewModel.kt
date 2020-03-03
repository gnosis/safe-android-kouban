package io.gnosis.kouban.safe_check.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.models.SafeInfo
import io.gnosis.kouban.data.models.SafeInfoDeployment
import io.gnosis.kouban.data.repositories.EnsRepository
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import io.gnosis.kouban.safe_check.R
import kotlinx.coroutines.Dispatchers
import pm.gnosis.model.Solidity

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

        // should have recent contract version
        val contractCheck = when (info.masterCopy) {
            SafeRepository.safeMasterCopy_0_1_0 -> CheckData(CheckResult.YELLOW)
            SafeRepository.safeMasterCopy_1_0_0 -> CheckData(CheckResult.YELLOW)
            SafeRepository.safeMasterCopy_1_1_1 -> CheckData(CheckResult.GREEN)
            else -> CheckData(CheckResult.RED)
        }
        healthCheck[CheckSection.CONTRACT] = contractCheck

        // should have fallback handler
        // fallback handler should be known
        val fallbackHandlerCheck =
            if (info.fallbackHandler != null)
                CheckData(CheckResult.GREEN)
            else
                CheckData(CheckResult.YELLOW)
        healthCheck[CheckSection.FALLBACK_HANDLER] = fallbackHandlerCheck

        val ownersCount = info.owners.size

        // should have more that 1 owner
        val ownersCheck = when (ownersCount) {
            1 -> CheckData(CheckResult.YELLOW)
            else -> CheckData(CheckResult.GREEN)
        }
        healthCheck[CheckSection.OWNERS] = ownersCheck

        // should have multi factor authentication
        // threshold == ownersCount should be avoided => lose of one of the private keys will lead to lock out
        val thresoldCheck = when (info.threshold.toInt()) {
            1 -> CheckData(CheckResult.YELLOW)
            ownersCount -> CheckData(CheckResult.YELLOW)
            else -> CheckData(CheckResult.GREEN)
        }
        healthCheck[CheckSection.THRESHOLD] = thresoldCheck

        // all unaudited modules are potentially unsafe
        val modulesCheck =
            if (info.modules.isNotEmpty())
                CheckData(CheckResult.YELLOW)
            else CheckData(CheckResult.GREEN)
        healthCheck[CheckSection.MODULES] = modulesCheck

        // deployment info should be accessible and it should be possible to decode it
        val deploymentCheck =
            if (deploymentInfo != null)
                CheckData(CheckResult.GREEN)
            else
                CheckData(CheckResult.YELLOW)
        healthCheck[CheckSection.DEPLOYMENT_INFO] = deploymentCheck

        return healthCheck
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
    val hint: String? = null
)

