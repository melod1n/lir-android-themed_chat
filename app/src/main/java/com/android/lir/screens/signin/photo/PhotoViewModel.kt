package com.android.lir.screens.signin.photo

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.base.vm.ShowInfoDialogEvent
import com.android.lir.common.AppGlobal
import com.android.lir.network.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val repo: AuthRepo,
) : BaseVM() {

    private val bitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val image = bitmap.asStateFlow()

    fun regUser(phone: String, nickName: String, photo: String) {
        makeJob({ repo.regUser(phone, nickName, photo) },
            onAnswer = {
                if (!it.error.notFalse() && it.userId.notFalse()) {
                    tasksEventChannel.send(PopToPhone)
                } else {
                    tasksEventChannel.send(
                        ShowInfoDialogEvent(
                            null,
                            "Что-то пошло не так, попробуйте позже"
                        )
                    )
                    delay(1000)
                    tasksEventChannel.send(PopToPhone)
                }
            },
            onError = {
                tasksEventChannel.send(
                    ShowInfoDialogEvent(
                        null,
                        "Что-то пошло не так, попробуйте позже"
                    )
                )
                delay(1000)
                tasksEventChannel.send(PopToPhone)
            })
    }

    fun saveBitMap(bm: Bitmap) = viewModelScope.launch {
        bitmap.emit(bm)
    }
}

object PopToPhone : Event()
