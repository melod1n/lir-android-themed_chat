package com.android.lir.screens.main.thematic

import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.common.AppGlobal
import com.android.lir.dataclases.ThematicChat
import com.android.lir.network.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThematicChatCommentsVM @Inject constructor(
    private val repo: AuthRepo
) : BaseVM() {

    fun sendMessage(chatId: Int, message: String) = viewModelScope.launch {
        makeJob({ repo.addThematicMessage(chatId, message, AppGlobal.shared.dataManager.token) },
            onAnswer = {
                tasksEventChannel.send(MessageSent(it.messageId))
            },
            onError = {
                tasksEventChannel.send(MessageError(it))
            })
    }

    fun loadThematicChat(chatId: Int) = viewModelScope.launch {
        makeJob({ repo.getThematicChat(chatId, AppGlobal.shared.dataManager.token) },
            onAnswer = {
                tasksEventChannel.send(LoadThematicChatEvent(it.chat))
            }
        )
    }


}

data class MessageSent(val id: Int) : Event()
data class MessageError(val error: String) : Event()
data class LoadThematicChatEvent(val response: ThematicChat) : Event()