package com.android.lir.screens.main.chats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.data.DataManager
import com.android.lir.dataclases.PrivateChatInfo
import com.android.lir.network.AuthRepo
import com.android.lir.screens.main.contacts.contactscreen.StartProgress
import com.android.lir.screens.main.contacts.contactscreen.StopProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: AuthRepo,
    private val dataManager: DataManager
) : BaseVM() {

    private val allChats: MutableLiveData<List<PrivateChatInfo>> = MutableLiveData()

    private val query = MutableLiveData("")
    val chats = query.switchMap { q ->
        if (q.isNullOrBlank()) allChats else liveData {
            emit(allChats.value?.filter {
                it.lastMessage?.contains(
                    q,
                    true
                ) == true || it.userName?.contains(q, true) == true
            })
        }
    }

    fun getChats() = viewModelScope.launch {
        makeJob(
            { repo.getPrivateAllChats(dataManager.userId, dataManager.token) },
            onAnswer = {
                if (!it.error.notFalse()) allChats.postValue(it.chats)
            },
            onStart = { tasksEventChannel.send(StartProgress) },
            onEnd = { tasksEventChannel.send(StopProgress) },
            onError = {}
        )
    }

    fun setQuery(str: String) {
        query.postValue(str)
    }
}
