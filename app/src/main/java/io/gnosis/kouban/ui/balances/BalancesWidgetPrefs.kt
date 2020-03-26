package io.gnosis.kouban.ui.balances

import android.content.Context
import android.content.SharedPreferences
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.edit
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

class BalancesWidgetPrefs(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREFS_WIDGET_BALANCES, Context.MODE_PRIVATE)
    }

    fun saveTokenForWidget(token: Solidity.Address, appWidgetId: Int) {
        prefs.edit {
            putString("$KEY_PREFIX_WIDGET_TOKEN$appWidgetId", token.asEthereumAddressString())
        }
    }

    fun loadTokenForWidget(appWidgetId: Int): Solidity.Address? {
        return prefs.getString("$KEY_PREFIX_WIDGET_TOKEN$appWidgetId", null)?.asEthereumAddress()
    }

    companion object {

        private val PREFS_WIDGET_BALANCES = "kouban_balances_widget_preferences"

        private val KEY_PREFIX_WIDGET_TOKEN = "key_widget_token_"
    }
}
