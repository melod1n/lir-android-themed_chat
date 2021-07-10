package com.android.lir.screens.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.common.AppGlobal
import com.android.lir.network.LirApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    var repo: LirApi
) : BaseVM() {

    init {
        Log.d("vmm", "created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("vmm", "cleared")
    }

    private fun startTimer() {
        Log.d("vmm", "ыефке ешьук")
        viewModelScope.launch {
            delay(2000)
            tasksEventChannel.send(TimerOver)
        }
    }

    fun loadCurrentUser() = viewModelScope.launch {
        val token = AppGlobal.shared.dataManager.token
        if (token.isBlank()) {
            startTimer()
            return@launch
        }

        makeJob({ repo.getUserInfo(AppGlobal.shared.dataManager.token) },
            onAnswer = {
                it.user.let { user ->
                    AppGlobal.shared.dataManager.user = user
                    tasksEventChannel.send(TimerOver)
                }
            })
    }
}

object TimerOver : Event()