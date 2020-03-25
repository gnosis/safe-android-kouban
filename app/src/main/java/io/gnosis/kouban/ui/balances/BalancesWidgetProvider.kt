package io.gnosis.kouban.ui.balances

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import com.squareup.picasso.Picasso
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.ui.SplashActivity
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class BalancesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        // To prevent any ANR timeouts, we perform the update in a service
        context?.startService(
            Intent(context, UpdateService::class.java)
                .putExtra(UpdateService.EXTRA_WIDGET_IDS, appWidgetIds)
        )
    }

    class UpdateService : Service() {

        private val addressManager: SafeAddressManager by inject()
        private val prefs: BalancesWidgetPrefs by inject()
        private val safeRepository: SafeRepository by inject()
        private val picasso: Picasso by inject()

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val appWidgetIds = intent?.getSerializableExtra(EXTRA_WIDGET_IDS) as IntArray

            runBlocking {

                val safe = addressManager.getSafeAddress()!!
                val balances = safeRepository.loadTokenBalances(safe)

                // Create an Intent to launch App
                val pendingIntent: PendingIntent = Intent(this@UpdateService, SplashActivity::class.java)
                    .let { intent ->
                        PendingIntent.getActivity(this@UpdateService, 0, intent, 0)
                    }

                appWidgetIds?.forEach { appWidgetId ->
                    // Perform this loop procedure for each App Widget that belongs to this provider

                    val tokenAddress = prefs.loadTokenForWidget(appWidgetId)
                    val token = balances.find { it.tokenInfo.address == tokenAddress }

                    token?.let { token ->
                        // Get the layout for the App Widget and attach an on-click listener
                        // to the button
                        val views: RemoteViews = RemoteViews(
                            packageName,
                            R.layout.widget_balances
                        ).apply {
                            setOnClickPendingIntent(R.id.token_item_icon, pendingIntent)
                            setTextViewText(R.id.token_item_symbol, token.tokenInfo.symbol)
                            setTextViewText(R.id.safe_balance, token.balance.shiftedString(token.tokenInfo.decimals, 5))
                            setTransactionIcon(R.id.token_item_icon, picasso, token.tokenInfo.icon, appWidgetIds)
                        }
                        // Tell the AppWidgetManager to perform an update on the current app widget
                        appWidgetManager?.updateAppWidget(appWidgetId, views)
                    }
                }
            }

            // if the process is shut down, the Intent will be redelivered
            // so that widget balance update can be resumed
            return START_REDELIVER_INTENT
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        companion object {

            const val EXTRA_WIDGET_IDS = "extra.serializable.widget_ids"
        }

    }

}
