package com.android.lir

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.lir.data.DataManager
import com.android.lir.network.AuthRepo
import com.android.lir.network.FireBaseService
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TOPIC = "all"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.main_activity) {

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var repo: AuthRepo

    @Inject
    lateinit var appScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FireBaseService.sharedPreferences = getSharedPreferences("sharedPref", MODE_PRIVATE)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FireBaseService.token = it.token
            dataManager.userId.let { id ->
                appScope.launch {
                    repo.sendFireBaseToken(id, it.token)
                }
            }
            Log.d("OkHttp", " TOKEN: ${it.token}")
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
    }
}
