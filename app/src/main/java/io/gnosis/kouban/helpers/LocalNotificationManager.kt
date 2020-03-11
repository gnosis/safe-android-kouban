package io.gnosis.kouban.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import io.gnosis.kouban.R

class LocalNotificationManager(
    context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel(context, CHANNEL_ID, context.getString(R.string.channel_tx_notifications_description))
    }

    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        description: String,
        importance: Int = NotificationManager.IMPORTANCE_HIGH
    ) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val name = context.getString(R.string.channel_tx_notifications_name)
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description

        channel.enableLights(true)
        channel.lightColor = LIGHT_COLOR

        channel.enableVibration(true)
        channel.vibrationPattern = VIBRATE_PATTERN

        notificationManager?.createNotificationChannel(channel)
    }

    fun hide(id: Int) {
        notificationManager?.cancel(id)
    }

    private fun builder(
        context: Context,
        title: String,
        message: String,
        intent: PendingIntent,
        channelId: String,
        category: String? = null,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ) =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_transactions)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setVibrate(VIBRATE_PATTERN)
            .setLights(LIGHT_COLOR, 100, 100)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(category)
            .setPriority(priority)
            .setContentIntent(intent)!!


    fun show(context: Context, id: Int, title: String, message: String, intent: Intent, channelId: String? = null) =
        show(context, id, title, message, PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT), channelId)

    fun show(context: Context, id: Int, title: String, message: String, intent: PendingIntent, channelId: String?) =
        builder(context, title, message, intent, channelId ?: CHANNEL_ID).let {
            show(id, it.build())
        }

    fun show(id: Int, notification: Notification) {
        notificationManager?.notify(id, notification)
    }

    @Deprecated("remove when there is enough data to show tx details screen")
    private fun builder(
        context: Context,
        title: String,
        message: String,
        channelId: String,
        category: String? = null,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ) =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_transactions)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setVibrate(VIBRATE_PATTERN)
            .setLights(LIGHT_COLOR, 100, 100)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(category)
            .setPriority(priority)

    @Deprecated("remove when there is enough data to show tx details screen")
    fun show(context: Context, title: String, message: String) =
        builder(context, title, message, CHANNEL_ID).let {
            show(0, it.build())
        }

    companion object {
        private val VIBRATE_PATTERN = longArrayOf(0, 100, 50, 100)
        private const val LIGHT_COLOR = Color.MAGENTA
        private const val CHANNEL_ID = "channel_tx_notifications"
    }
}
