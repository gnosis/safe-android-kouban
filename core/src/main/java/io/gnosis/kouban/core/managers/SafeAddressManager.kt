package io.gnosis.kouban.core.managers

import android.content.Context
import android.content.SharedPreferences
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class SafeAddressManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(SAFE_ADDRESS_PREFS, Context.MODE_PRIVATE)

    fun storeSafeAddress(safeAddress: Solidity.Address) {
        sharedPreferences.edit {
            putString(SAFE_ADDRESS_KEY, safeAddress.asEthereumAddressString())
        }
    }

    fun getSafeAddress(): Solidity.Address? {
        return sharedPreferences.getString(SAFE_ADDRESS_KEY, "")
            .takeUnless { it?.isEmpty() == true }
            ?.asEthereumAddress()
    }

    private companion object {
        const val SAFE_ADDRESS_KEY = "SAFE_ADDRESS_KEY"
        const val SAFE_ADDRESS_PREFS = "SAFE_ADDRESS_PREFS"
    }
}
