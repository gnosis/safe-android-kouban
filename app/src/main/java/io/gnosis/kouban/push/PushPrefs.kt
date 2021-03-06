package io.gnosis.kouban.push

import android.content.Context
import android.content.SharedPreferences
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import java.util.*

class PushPrefs(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREFS_PUSH_NOTIFICATIONS, Context.MODE_PRIVATE)
    }

    var clientId: String
        get() {
            var value = prefs.getString(KEY_CLIENT_ID, null)
            if (value == null) {
                value = UUID.randomUUID().toString()
                clientId = value
            }
            return value
        }
        set(value) {
            prefs.edit {
                putString(KEY_CLIENT_ID, value)
            }
        }

    val isDeviceRegistered
        get() = token != null

    val isSafeRegistered
        get() = safe != null

    var token: String?
        get() = prefs.getString(KEY_PUSHES_TOKEN, null)
        set(value) {
            prefs.edit {
                putString(KEY_PUSHES_TOKEN, value)
            }
        }

    var safe: Solidity.Address?
        get() = prefs.getString(KEY_PUSHES_SAFE, null)?.asEthereumAddress()
        set(value) {
            prefs.edit {
                putString(KEY_PUSHES_SAFE, value?.asEthereumAddressString())
            }
        }

    companion object {

        private val PREFS_PUSH_NOTIFICATIONS = "kouban_push_preferences"

        private val KEY_CLIENT_ID = "key_client_id"
        private val KEY_PUSHES_TOKEN = "key_pushes_token"
        private val KEY_PUSHES_SAFE = "key_pushes_safe"
    }
}
