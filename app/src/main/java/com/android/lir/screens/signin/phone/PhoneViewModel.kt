package com.android.lir.screens.signin.phone

import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.data.DataManager
import com.android.lir.network.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val repo: AuthRepo,
    private val dataManager: DataManager
) : BaseVM() {
    fun checkUser(phone: String) {
        makeJob({ repo.checkUser(phone) }, onAnswer = {
            if (!it.error.notFalse() && it.session.notFalse()) {
                dataManager.userId = it.userId
                dataManager.sessionId = it.session.value
                tasksEventChannel.send(GoToCode)
            } else tasksEventChannel.send(GoToRegistration)
        })
    }
}

object GoToCode : Event()
object GoToRegistration : Event()