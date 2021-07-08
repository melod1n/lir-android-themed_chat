package com.android.lir.screens.main.thematic

import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.common.AppGlobal
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.network.AuthRepo
import com.android.lir.utils.AndroidUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ThemedChatCreateVM @Inject constructor(
    private val repo: AuthRepo
) : BaseVM() {

    fun createThematicChat(
        title: String,
        description: String,
        phone: String,
        address: String,
        avatarNumber: Int,
        coordinates: String,
        usersCount: Int
    ) = viewModelScope.launch {
        makeJob({
            repo.createThematicChat(
                title,
                description,
                phone,
                address,
                coordinates,
                avatarNumber,
                usersCount,
                AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = {
                tasksEventChannel.send(ChatCreatedEvent(it.info))
            })
    }

    fun loadAddress(context: Context, coordinates: String = ""): String {
        val lat = coordinates.substringBefore("_").toDouble()
        val lon = coordinates.substringAfter("_").toDouble()

        val geocoder = Geocoder(context, Locale.getDefault())

        val addresses = geocoder.getFromLocation(lat, lon, 1)

        val address = addresses[0].getAddressLine(0)
        return address
    }

    fun uploadImage(chatId: Int, bitmap: Bitmap) = viewModelScope.launch {
        val image = AndroidUtils.convertBitmapToBase64(bitmap)

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

}

object SuccessAddPhotoToChat : Event()
data class ErrorAddPhotoToChat(val error: String) : Event()
data class ChatCreatedEvent(val info: ThematicChatInfo) : Event()