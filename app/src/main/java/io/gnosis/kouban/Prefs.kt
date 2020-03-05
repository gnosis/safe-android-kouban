package io.gnosis.kouban

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class Prefs(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREFS_APP_FILE, Context.MODE_PRIVATE)
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
            val editor = prefs.edit()
            editor.putString(KEY_CLIENT_ID, value)
            editor.apply()
        }

    var pushesRegisteredDevice: Boolean
        get() = prefs.getBoolean(KEY_PUSH_REGISTERED_DEVICE, false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(KEY_PUSH_REGISTERED_DEVICE, value)
            editor.apply()
        }

    var pushesRegisteredSafe: Boolean
        get() = prefs.getBoolean(KEY_PUSH_REGISTERED_SAFE, false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(KEY_PUSH_REGISTERED_SAFE, value)
            editor.apply()
        }


    companion object {

        val PREFS_APP_FILE = "kouban_app_prefs"

        val KEY_CLIENT_ID = "key_client_id"
        val KEY_PUSH_REGISTERED_DEVICE = "key_push_registered_device"
        val KEY_PUSH_REGISTERED_SAFE = "key_push_registered_safe"
    }
}
