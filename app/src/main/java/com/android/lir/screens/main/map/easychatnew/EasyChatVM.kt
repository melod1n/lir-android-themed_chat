package com.android.lir.screens.main.map.easychatnew

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.ShowInfoDialogEvent
import com.android.lir.data.DataManager
import com.android.lir.dataclases.Chat
import com.android.lir.dataclases.Message
import com.android.lir.network.AuthRepo
import com.android.lir.screens.main.contacts.chatdetail.ClearEvent
import com.android.lir.screens.main.map.createchat.ChatType
import com.android.lir.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EasyChatVM @Inject constructor(
    private val repo: AuthRepo,
    private val dataManager: DataManager
) : BaseVM() {

    lateinit var info: Triple<Boolean, ChatType?, String?>

    var chat: Chat? = null
    private var chatJob: Job? = null

    private val _messages: MutableLiveData<List<ChatItem>> = MutableLiveData()
    val messages = _messages

    fun startTrackingMessage() {
        chatJob?.cancel()
        chatJob = chatJob(chat?.id ?: -1)
    }

    private fun stopTrackingMessage() {
        chatJob?.cancel()
    }

    private fun chatJob(chatId: Int) = viewModelScope.launch {
        flow {
            while (true) {
                emit(Unit)
                delay(3000)
            }
        }.onEach { getChat(chatId) }.flowOn(Dispatchers.Default).collect()
    }

    private fun getChat(chatId: Int) {
        makeJob(
            { repo.getChatById(chatId) },
            onAnswer = {
                _messages.postValue(
                    it.messages.filterNotNull().map { message ->
                        if (message.uid == Constants.deviceId) {
                            ChatItem.Send(message)
                        } else ChatItem.Receiver(message)
                    }.sortedBy { item -> item.messageInChat.created_at }
                )
            },
            onError = {
                stopTrackingMessage()
                tasksEventChannel.send(ShowInfoDialogEvent(null, it))
            }
        )
    }

    fun sendMessage(message: String) {
        if (info.first) {
            createChat(info.second!!, info.third!!) { publicMessage(message) }
        } else publicMessage(message)
    }

    private fun createChat(type: ChatType, coord: String, onCreated: () -> Unit) {
        val lat = coord.substringBefore("_")
        val long = coord.substringAfter("_")
        makeJob(
            {
                if (type == ChatType.CHANNEL)
                    repo.createAnonimChat("${lat}_$long", Constants.deviceId, 1)
                else
                    repo.createAnonimChat("${lat}_$long")
            },
            onAnswer = {
                chat = it.toChat(type, coord)
                onCreated()
            }
        )
    }

    private fun publicMessage(message: String) {
        makeJob(
            {
                tasksEventChannel.send(ClearEvent)
                repo.sendMessage(Message(chat?.id, message, dataManager.userId, Constants.deviceId))
            },
            onAnswer = {
                startTrackingMessage()
            }
        )
    }
}
