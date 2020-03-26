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
            putString("$KEY_WIDGET_TOKEN_$appWidgetId", token.asEthereumAddressString())
            putInt("$KEY_WIDGET_ID_$appWidgetId", appWidgetId)
        }
    }

    fun loadTokenForWidget(appWidgetId: Int): Solidity.Address? {
        return prefs.getString("$KEY_WIDGET_TOKEN_$appWidgetId", null)?.asEthereumAddress()
    }

    companion object {

        private val PREFS_WIDGET_BALANCES = "kouban_balances_widget_preferences"

        private val KEY_WIDGET_TOKEN_ = "key_widget_token_"
        private val KEY_WIDGET_ID_ = "key_widget_id_"
    }
}
