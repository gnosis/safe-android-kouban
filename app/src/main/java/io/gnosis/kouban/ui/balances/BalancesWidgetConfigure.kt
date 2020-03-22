package io.gnosis.kouban.ui.balances

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import io.gnosis.kouban.R

class BalancesWidgetConfigure : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.widget_balances_configure)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Set the Activity result to RESULT_CANCELED, along with EXTRA_APPWIDGET_ID.
        // This way, if the user backs-out of the Activity before reaching the end,
        // the App Widget host is notified that the configuration was cancelled and
        // the App Widget will not be added.
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        })


//        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
//
//
//        RemoteViews(context.packageName, R.layout.widget_balances).also { views ->
//            appWidgetManager.updateAppWidget(appWidgetId, views)
//        }


        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
       // finish()
    }
}
