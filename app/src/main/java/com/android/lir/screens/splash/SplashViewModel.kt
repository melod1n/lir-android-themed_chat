package com.android.lir.screens.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseVM() {

    init {
        Log.d("vmm","created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("vmm","cleared")
    }

    fun startTimer() {
        Log.d("vmm","ыефке ешьук")
        viewModelScope.launch {
            delay(2000)
            tasksEventChannel.send(TimerOver)
        }
    }
}

object TimerOver : Event()