package com.android.lir.screens.signin.otp

import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.base.vm.ShowInfoDialogEvent
import com.android.lir.common.AppGlobal
import com.android.lir.data.DataManager
import com.android.lir.network.AuthRepo
import com.android.lir.network.FireBaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val repo: AuthRepo,
    private val dataManager: DataManager,
    private val appScope: CoroutineScope
) : BaseVM() {
    fun checkSms(smsCode: String) {
        makeJob({ repo.checkSms(dataManager.sessionId, smsCode) }, onAnswer = {
            if (!it.error.notFalse()) {
                dataManager.token = it.token.value

                makeJob({ repo.getUserInfo(AppGlobal.shared.dataManager.token) },
                    onAnswer = { userResponse ->
                        userResponse.user.let { user ->
                            AppGlobal.shared.dataManager.user = user
                            tasksEventChannel.send(GoToAuth)
                        }
                    })

                appScope.launch {
                    repo.sendFireBaseToken(dataManager.userId, FireBaseService.token ?: "")
                }
            } else {
                tasksEventChannel.send(ShowInfoDialogEvent(null, "Неверный код"))
            }
        })
    }
}

object GoToAuth : Event()
