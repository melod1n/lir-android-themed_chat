package com.android.lir.network

import com.android.lir.dataclases.Message
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val api: LirApi
) : AuthRepo {
    override suspend fun regUser(phone: String, name: String, photo: String) =
        api.regUser(phone, name, photo)

    override suspend fun checkUser(phone: String) = api.checkUser(phone)
    override suspend fun checkSms(sessionId: String, smsCode: String) =
        api.checkSms(sessionId, smsCode)

    override suspend fun checkToken(token: String) = api.checkToken(token)
    override suspend fun updateUser(token: String, name: String, birthday: String, photo: String) =
        api.updateUser(token, name, birthday, photo)

    override suspend fun deletePhoto(token: String) = api.deletePhoto(token)
    override suspend fun getUsers() = api.getUsers()
    override suspend fun createChat(userFrom: Int, userTo: Int) =
        api.createPrivateChat(userFrom, userTo)

    override suspend fun addPrivateMessage(
        chatId: Int,
        text: String,
        photo: String?,
        token: String
    ) = api.addPrivateMessage(chatId, text, photo, token)

    override suspend fun getAllChats() = api.getAllChats()
    override suspend fun getChatById(page: Int) = api.getChatById(page)
    override suspend fun createAnonimChat(coordinates: String, uid: String, channelFlag: Int) =
        api.createChat(coordinates, uid, channelFlag)

    override suspend fun sendMessage(message: Message) = api.sendMessage(message)
    override suspend fun getRegContacts(numbers: String) = api.getRegContacts(numbers)
    override suspend fun sendFireBaseToken(userId: Int, token: String) =
        api.sendToken(userId, token)

    override suspend fun getPrivateAllChats(userId: Int, token: String) =
        api.getAllChats(userId, token)

    override suspend fun addFavoriteUser(
        favoriteUserId: Int,
        token: String
    ) = api.addFavoritesUser(favoriteUserId, token)

    override suspend fun deleteFavoriteUser(
        favoriteUserId: Int,
        token: String
    ) = api.deleteFavoriteUser(favoriteUserId, token)

    override suspend fun getFavoriteUsers(token: String) = api.getFavoriteUsers(token)

    override suspend fun createThematicChat(
        title: String,
        description: String,
        phone: String,
        address: String,
        coordinates: String,
        avatarNumber: Int,
        usersCount: Int,
        token: String
    ) = api.createThematicChat(
        title,
        description,
        phone,
        address,
        coordinates,
        avatarNumber,
        usersCount,
        token
    )

    override suspend fun addThematicMessage(
        chatId: Int,
        text: String,
        token: String
    ) = api.addThematicMessage(chatId, text, token)

    override suspend fun getThematicChat(
        chatId: Int,
        token: String
    ) = api.getThematicChat(chatId, token)

    override suspend fun getAllThematicChats() = api.getAllThematicChats()
    override suspend fun addPhotoToChat(
        chatId: Int,
        image: String,
        token: String
    ) = api.addPhotoToChat(chatId, image, token)

    override suspend fun addPhotoToComment(
        messageId: Int,
        image: String,
        token: String
    ) = api.addPhotoToComment(messageId, image, token)

    override suspend fun addUserToChat(chatId: Int, token: String) =
        api.addUserToChat(chatId, token)

    override suspend fun getUserInfo(token: String) = api.getUserInfo(token)
}
