package io.gnosis.kouban.core.managers

import android.content.Context
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class SafeAddressManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_SAFE_ADDRESS, Context.MODE_PRIVATE)

    suspend fun storeSafeAddress(safeAddress: Solidity.Address) {
        sharedPreferences.edit {
            putString(KEY_SAFE_ADDRESS, safeAddress.asEthereumAddressString())
        }
    }

    suspend fun getSafeAddress(): Solidity.Address? {
        return sharedPreferences.getString(KEY_SAFE_ADDRESS, "")
            .takeUnless { it?.isEmpty() == true }
            ?.asEthereumAddress()
    }

    private companion object {
        const val KEY_SAFE_ADDRESS = "KEY_SAFE_ADDRESS"
        const val PREFS_SAFE_ADDRESS = "PREFS_SAFE_ADDRESS"
    }
}
