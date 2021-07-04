package com.android.lir.screens.main.thematic

import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.common.AppGlobal
import com.android.lir.network.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
        coordinates: String
    ) = viewModelScope.launch {
        makeJob({
            repo.createThematicChat(
                title,
                description,
                phone,
                address,
                coordinates,
                avatarNumber,
                AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = {
                tasksEventChannel.send(ChatCreatedEvent(it.chatId))
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

    fun uploadImage(chatId: Int, image: Bitmap) = viewModelScope.launch {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        val imageBytes = byteArrayOutputStream.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        makeJob({
            repo.addPhotoToChat(
                chatId = chatId,
                image = imageString,
                token = AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = { tasksEventChannel.send(SuccessAddPhotoToChat) },
            onError = { tasksEventChannel.send(ErrorAddPhotoToChat(it)) })
    }

}

object SuccessAddPhotoToChat : Event()
data class ErrorAddPhotoToChat(val error: String) : Event()
data class ChatCreatedEvent(val chatId: Int) : Event()