package com.android.lir.screens.main.thematic

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.common.AppGlobal
import com.android.lir.dataclases.ThematicChat
import com.android.lir.dataclases.ThematicComment
import com.android.lir.network.AuthRepo
import com.android.lir.utils.AndroidUtils
import com.android.lir.utils.AppExtensions.compressBitmap
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
                tasksEventChannel.send(MessageSent(it.info))
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

    fun uploadImage(chatId: Int, bitmap: Bitmap) = viewModelScope.launch {
        val image = AndroidUtils.convertBitmapToBase64(bitmap.compressBitmap())

        makeJob({
            repo.addPhotoToChat(
                chatId = chatId,
                image = image,
                token = AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = { tasksEventChannel.send(SuccessAddPhotoToChat) },
            onError = { tasksEventChannel.send(ErrorAddPhotoToChat(it)) })
    }

    fun uploadCommentImage(messageId: Int, bitmap: Bitmap) = viewModelScope.launch {
        val image = AndroidUtils.convertBitmapToBase64(bitmap.compressBitmap())

        makeJob({
            repo.addPhotoToComment(
                messageId = messageId,
                image = image,
                token = AppGlobal.shared.dataManager.token
            )
        })
    }

    fun acceptChatInvite(chatId: Int) = viewModelScope.launch {
        makeJob({
            repo.addUserToChat(
                chatId = chatId,
                token = AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = { tasksEventChannel.send(AddUserToChat) },
            onError = {})
    }


//    fun uploadImage(bitmap: Bitmap) = viewModelScope.launch {
//        val image = AndroidUtils.convertBitmapToBase64(bitmap)
//
//        makeJob({
//            repo.addThematicMessage()
//            repo.addPhotoToChat(
//                chatId = chatId,
//                image = image,
//                token = AppGlobal.shared.dataManager.token
//            )
//        },
//            onAnswer = { tasksEventChannel.send(SuccessAddPhotoToChat) },
//            onError = { tasksEventChannel.send(ErrorAddPhotoToChat(it)) })
//    }


}

object AddUserToChat : Event()

data class MessageSent(val comment: ThematicComment) : Event()
data class MessageError(val error: String) : Event()
data class LoadThematicChatEvent(val response: ThematicChat) : Event()