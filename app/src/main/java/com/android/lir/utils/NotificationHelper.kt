package com.android.lir.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.lir.MainActivity
import com.android.lir.R

class NotificationHelper() {
    private var instance: NotificationHelper? = null
    private var mNotificationManager: NotificationManager? = null
    private val notificationId = 0

    constructor(context: Context) : this() {
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "description"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            mNotificationManager!!.createNotificationChannel(channel)
        }
    }

    fun init(context: Context) {
        if (instance == null) {
            instance = NotificationHelper(context)
        }
    }

    public fun get(): NotificationHelper {
        checkNotNull(instance) { "NotificationHelper is not initialized" }
        return instance as NotificationHelper
    }

    fun buildCallNotification(
        text: String?,
        context: Context?
    ): Notification? {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        return NotificationCompat.Builder(
            context!!,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("Voximplant")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_vox_notification)
            .build()
    }

    fun cancelNotification(notificationId: Int) {
        if (notificationId > -1) {
            mNotificationManager!!.cancel(notificationId)
        }
    }
}
