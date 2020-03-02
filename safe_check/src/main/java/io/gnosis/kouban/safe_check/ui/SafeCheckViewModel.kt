package io.gnosis.kouban.safe_check.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.data.repositories.EnsRepository
import io.gnosis.kouban.data.repositories.SafeDeploymentInfoNotFound
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

    fun loadSafeConfig(address: Solidity.Address): LiveData<ViewState> =
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Loading(true))

            try {
                val safeInfo = safeRepository.loadSafeInfo(address)
                val ensName = ensRepository.resolve(address)

                val contractVersionResId = when (safeInfo.masterCopy) {
                    SafeRepository.safeMasterCopy_0_1_0 -> R.string.version_0_1_0
                    SafeRepository.safeMasterCopy_1_0_0 -> R.string.version_1_0_0
                    SafeRepository.safeMasterCopy_1_1_1 -> R.string.version_1_1_1
                    else -> R.string.version_unknown
                }

                emit(Loading(false))
                emit(
                    SafeSettings(
                        contractVersionResId,
                        safeInfo.fallbackHandler,
                        ensName,
                        safeInfo.owners,
                        safeInfo.threshold.toInt(),
                        safeInfo.currentNonce.toInt(),
                        safeInfo.modules
                    )
                )

            } catch (e: Exception) {
                emit(Loading(false))
                emit(Error(e))
            }

            try {
                val deploymentInfo = safeRepository.loadSafeDeploymentParams(address)
                val deploymentContractVersionResId = when (deploymentInfo.masterCopy) {
                    SafeRepository.safeMasterCopy_0_1_0 -> R.string.version_0_1_0
                    SafeRepository.safeMasterCopy_1_0_0 -> R.string.version_1_0_0
                    SafeRepository.safeMasterCopy_1_1_1 -> R.string.version_1_1_1
                    else -> R.string.version_unknown
                }
                emit(
                    SafeDeploymentSettings(
                        deploymentContractVersionResId,
                        deploymentInfo.fallbackHandler,
                        deploymentInfo.owners,
                        deploymentInfo.threshold.toInt()
                    )
                )
            } catch (e: SafeDeploymentInfoNotFound) {
                emit(SafeDeploymentInfoNotFoundError(e))
            }
        }

}

data class SafeSettings(
    @StringRes
    val contractVersionResId: Int,
    val fallbackHandler: Solidity.Address,
    val ensName: String?,
    val owners: List<Solidity.Address>,
    val threshold: Int,
    val txCount: Int,
    val modules: List<Solidity.Address>
) : ViewState()

data class SafeDeploymentSettings(
    @StringRes
    val contractVersionResId: Int,
    val fallbackHandler: Solidity.Address,
    val owners: List<Solidity.Address>,
    val threshold: Int
) : ViewState()

data class SafeDeploymentInfoNotFoundError(val throwable: Throwable) : ViewState()

