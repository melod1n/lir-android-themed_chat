package com.android.lir.data

import com.android.lir.common.AppGlobal
import com.android.lir.dataclases.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor() {
    var user: User? = null

    fun clear() {
        phoneNumber = ""
        token = ""
        sessionId = ""
        userId = -1
    }

    var phoneNumber: String = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) AppGlobal.shared.saveDataManager()
        }
    var token: String = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) AppGlobal.shared.saveDataManager()
        }
    var sessionId: String = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) AppGlobal.shared.saveDataManager()
        }
    var userId: Int = -1
        set(value) {
            field = value
            AppGlobal.shared.saveDataManager()
        }
}