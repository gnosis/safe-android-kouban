package io.gnosis.kouban.ui.balances

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import io.gnosis.kouban.R
import io.gnosis.kouban.ui.MainActivity

class BalancesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        // To prevent any ANR timeouts, we perform the update in a service
        context?.startService(Intent(context, UpdateService::class.java))


        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds?.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context?.packageName,
                R.layout.widget_balances
            ).apply {
                setOnClickPendingIntent(R.id.token_item_icon, pendingIntent)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
           appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }

    class UpdateService : Service() {

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onStart(intent: Intent?, startId: Int) {
            super.onStart(intent, startId)
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

    }

}
