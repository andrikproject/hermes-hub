package com.hermeshub.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "hermes_hub_chat"
    private const val CHANNEL_NAME = "Hermes Chat"
    private const val CHANNEL_DESC = "Notifikasi saat ada respon dari Hermes"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showResponseNotification(context: Context, connectionName: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🤖 $connectionName")
            .setContentText(message.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.take(500)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                connectionName.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun showErrorNotification(context: Context, connectionName: String, error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("❌ Gagal: $connectionName")
            .setContentText(error.take(100))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                "error_${connectionName}".hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
}
