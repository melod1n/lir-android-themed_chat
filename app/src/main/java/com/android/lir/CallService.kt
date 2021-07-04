package com.android.lir

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.android.lir.utils.Constants
import com.android.lir.utils.NotificationHelper


class CallService : Service() {
    override fun onCreate() {}
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != null) {
            if (action == Constants.ACTION_FOREGROUND_SERVICE_START) {
                var numberOfCalls = 0
                if (intent.extras != null) {
                    numberOfCalls =
                        intent.extras!!.getInt(Constants.SERVICE_NOTIFICATION_DETAILS)
                }
                val notification: Notification? = NotificationHelper().get()
                    .buildCallNotification(
                        numberOfCalls.toString() + " " + getString(R.string.call_service_notification_text),
                        applicationContext
                    )
                startForeground(1, notification)
            } else if (action == Constants.ACTION_FOREGROUND_SERVICE_STOP) {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {}
}
