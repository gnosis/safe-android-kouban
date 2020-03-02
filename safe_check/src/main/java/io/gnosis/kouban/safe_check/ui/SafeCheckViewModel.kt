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
            kotlin.runCatching {

                safeRepository.loadSafeDeploymentParams(address)
                val safeInfo = safeRepository.loadSafeInfo(address)
                val ensName = ensRepository.resolve(address)

                val contractVersionResId = when(safeInfo.masterCopy) {
                    SafeRepository.safeMasterCopy_0_1_0 -> R.string.version_0_1_0
                    SafeRepository.safeMasterCopy_1_0_0 -> R.string.version_1_0_0
                    SafeRepository.safeMasterCopy_1_1_1 -> R.string.version_1_1_1
                    else -> R.string.version_unknown

                }
                SafeSettings(contractVersionResId, ensName, safeInfo.owners, safeInfo.threshold.toInt(), safeInfo.currentNonce.toInt(), safeInfo.modules)
            }
                .onFailure {
                    emit(Loading(false))
                    emit(Error(it))
                }
                .onSuccess {
                    emit(Loading(false))
                    emit(it)
                }
        }

}

data class SafeSettings(
    @StringRes
    val contractVersionResId: Int,
    val ensName: String?,
    val owners: List<Solidity.Address>,
    val threshold: Int,
    val txCount: Int,
    val modules: List<Solidity.Address>
) : ViewState()

