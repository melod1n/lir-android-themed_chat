package com.android.lir.common

import android.app.Application
import android.content.Context
import com.android.lir.R
import com.android.lir.data.DataManager
import com.android.lir.utils.ForegroundCheck
import com.android.lir.utils.NotificationHelper
import com.android.lir.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp()
class AppGlobal : Application() {

    @Inject
    lateinit var dataManager: DataManager

    override fun onCreate() {
        super.onCreate()
        shared = this
        restoreDataManager()
        ForegroundCheck.init(this)
        SharedPreferencesHelper.init(applicationContext)
        NotificationHelper(applicationContext)
    }

    private fun restoreDataManager() {
        val pref = getSharedPreferences("LIR", Context.MODE_PRIVATE) ?: return
        val dataString = pref.getString(getString(R.string.saved_data_manager_key), null)
        if (!dataString.isNullOrEmpty()) {
            try {
                Gson().fromJson(dataString, DataManager::class.java)?.let {
                    dataManager.userId = it.userId
                    dataManager.sessionId = it.sessionId
                    dataManager.token = it.token
                    dataManager.phoneNumber = it.phoneNumber
                }

            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }

    fun saveDataManager() {
        val pref = getSharedPreferences("LIR", Context.MODE_PRIVATE) ?: return
        with(pref.edit()) {
            putString(getString(R.string.saved_data_manager_key), Gson().toJson(dataManager))
            apply()
        }
    }

    companion object {
        lateinit var shared: AppGlobal private set

        val kittens = listOf(
            "https://images.pexels.com/photos/691583/pexels-photo-691583.jpeg?auto=compress&cs=tinysrgb&dpr=3&h=750&w=1260",
            "https://images.pexels.com/photos/2194261/pexels-photo-2194261.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/669015/pexels-photo-669015.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/209037/pexels-photo-209037.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/1317844/pexels-photo-1317844.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/617278/pexels-photo-617278.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/1314550/pexels-photo-1314550.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/69932/tabby-cat-close-up-portrait-69932.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/1183434/pexels-photo-1183434.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/982865/pexels-photo-982865.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/208984/pexels-photo-208984.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/248280/pexels-photo-248280.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/572861/pexels-photo-572861.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/104827/cat-pet-animal-domestic-104827.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/1543793/pexels-photo-1543793.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/3777622/pexels-photo-3777622.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
            "https://images.pexels.com/photos/821736/pexels-photo-821736.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940"
        )

        val thematicChatAvatars = listOf(
            R.drawable.thematic_chat_ball,
            R.drawable.thematic_chat_doggo,
            R.drawable.thematic_chat_donut,
            R.drawable.thematic_chat_flower,
            R.drawable.thematic_chat_guitar,
            R.drawable.thematic_chat_peach
        )

    }
}