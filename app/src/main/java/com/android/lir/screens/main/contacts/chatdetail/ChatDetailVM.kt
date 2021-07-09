package com.android.lir.screens.main.contacts.chatdetail

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.base.vm.ShowInfoDialogEvent
import com.android.lir.data.DataManager
import com.android.lir.network.AuthRepo
import com.android.lir.utils.Answer
import com.android.lir.utils.AppExtensions.compressBitmap
import com.android.lir.utils.AppExtensions.toBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailVM @Inject constructor(
    private val repo: AuthRepo,
    private val dataManager: DataManager
) : BaseVM() {

    private var chatId: Int? = null
    var partnerId: Int? = null

    private val _messages: MutableLiveData<List<PrivateChatItem>> = MutableLiveData()
    val messages = _messages

    private val _currentMessage: MutableLiveData<String> = MutableLiveData()
    val currentMessage = _currentMessage

    private var chatJob: Job? = null

    fun onMessageChanged(str: String?) {
        _currentMessage.postValue(str)
    }

    fun setPartnerId(id: Int) {
        partnerId = id
        startChat()
    }

    private fun startChat() {
        chatJob?.cancel()
        chatJob = chatJob()
    }

    private fun chatJob() = viewModelScope.launch {
        flow {
            while (true) {
                emit(Unit)
                delay(3000)
            }
        }.onEach { getChat() }.flowOn(Dispatchers.Default).collect()
    }

    private fun getChat() {
        makeJob(
            { repo.createChat(dataManager.userId, partnerId ?: 0) },
            onAnswer = {
                if (it.chatId.notFalse()) {
                    tasksEventChannel.send(
                        PutPartnerInfoEvent(
                            it.photo ?: "",
                            it.name ?: "Неизвестный"
                        )
                    )
                    chatId = it.chatId.value.toIntOrNull() ?: 0
                    _messages.postValue(it.messages.map { message -> message.toModel(dataManager.userId) })
                }
            }
        )
    }

    fun sendMessage(bitmap: Bitmap? = null) {
        if (chatId == null || (currentMessage.value.isNullOrEmpty() && bitmap == null)) return
        makeJob(
            {
                val isTokenCorrect =
                    (repo.checkToken(dataManager.token) as? Answer.Success)?.data?.success?.value?.equals(
                        "true"
                    ) ?: false
                if (!isTokenCorrect) return@makeJob Answer.Error("")
                val message = currentMessage.value
                tasksEventChannel.send(ClearEvent)
                repo.addPrivateMessage(
                    chatId!!, message.orEmpty(),
                    bitmap?.compressBitmap()?.toBase64(), dataManager.token
                )
            },
            onAnswer = {
                if (!it.error.notFalse()) {
                    startChat()
                }
            },
            onError = {
                if (it.isEmpty()) {
                    dataManager.clear()
                    tasksEventChannel.send(LogOutEvent)
                } else tasksEventChannel.send(ShowInfoDialogEvent(null, it))
            }
        )
    }
}

enum class EnabledType { ENABLE, CHAT_DISABLE, NO_AUTH_DISABLE }

object LogOutEvent : Event()
object ClearEvent : Event()
data class EnableEditEvent(val enabledType: EnabledType) : Event()
data class PutPartnerInfoEvent(val url: String, val name: String) : Event()
