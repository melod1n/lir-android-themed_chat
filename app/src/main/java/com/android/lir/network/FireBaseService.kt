package com.android.lir.network

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import com.android.lir.MainActivity
import com.android.lir.R
import com.android.lir.screens.main.contacts.chatdetail.ChatDetailFragment
import com.android.lir.screens.main.contacts.chatdetail.ChatDetailVM
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


private const val CHANNEL_ID = "my_lir_channel"

class FireBaseService : FirebaseMessagingService() {

    companion object {
        var sharedPreferences: SharedPreferences? = null

        var token: String?
            get() = sharedPreferences?.getString("token", "")
            set(value) {
                sharedPreferences?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val currentChatId = ChatDetailFragment.instance?.chatId ?: 0
        val chatFromPush = message.data["deepLink"]?.substringAfterLast('/', "")?.toIntOrNull() ?: 0
        if (isActivityRunning(MainActivity::class.java) && currentChatId > 0 && currentChatId == chatFromPush) return

        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val newPending = when (chatFromPush > 0) {
            true -> NavDeepLinkBuilder(this)
                .setGraph(R.navigation.contacts)
                .setDestination(R.id.chatDetailFragment)
                .setArguments(bundleOf("chat_id" to chatFromPush))
                .createPendingIntent()
            false -> PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(
            notificationManager
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_message)
            .setAutoCancel(true)
            .setContentIntent(newPending)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelLir"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "LIR"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun isActivityRunning(activityClass: Class<*>): Boolean {
        val activityManager = baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
        for (task in tasks) {
            if (activityClass.canonicalName.equals(
                    task.baseActivity!!.className,
                    ignoreCase = true
                )
            ) return true
        }
        return false
    }
}

