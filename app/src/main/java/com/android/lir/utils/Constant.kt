package com.android.lir.utils

import android.os.Build
import android.provider.Settings
import com.android.lir.common.AppGlobal

object Constants {

    const val APP_TAG = "Voximplant"
    const val USERNAME = "username"
    const val REFRESH_TIME = "refreshTime"
    const val LOGIN_ACCESS_TOKEN = "accessToken"
    const val LOGIN_ACCESS_EXPIRE = "accessExpire"
    const val LOGIN_REFRESH_TOKEN = "refreshToken"
    const val LOGIN_REFRESH_EXPIRE = "refreshExpire"
    const val MILLISECONDS_IN_SECOND = 1000
    const val INCOMING_CALL = "incoming_call"
    const val DISPLAY_NAME = "display_name"
    const val CALL_ID = "call_id"
    const val WITH_VIDEO = "with_video"
    const val NEW_CALL_FRAGMENT_ID = 123456
    const val KEY_PREF_PUSH_ENABLE = "push_enable"
    const val INCOMING_CALL_RESULT = "incoming_call_result"
    const val INTENT_PROCESSED = "processed"
    const val CALL_ANSWERED = 1
    const val ACTION_FOREGROUND_SERVICE_START = "com.voximplant.sdkdemo.service_start"
    const val ACTION_FOREGROUND_SERVICE_STOP = "com.voximplant.sdkdemo.service_stop"
    const val NOTIFICATION_CHANNEL_ID = "VoximplantChannel"
    const val SERVICE_NOTIFICATION_DETAILS = "service_notification_details"

    val deviceId: String = Settings.Secure.getString(
        AppGlobal.shared.contentResolver,
        Settings.Secure.ANDROID_ID
    ) + Build.MODEL
}


