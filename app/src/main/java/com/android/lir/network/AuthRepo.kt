package com.android.lir.network

import com.android.lir.dataclases.*
import com.android.lir.utils.Answer


interface AuthRepo {
    //registration
    suspend fun regUser(phone: String, name: String, photo: String): Answer<RegUserResponse>
    suspend fun checkUser(phone: String): Answer<CheckUserResponse>
    suspend fun checkSms(sessionId: String, smsCode: String): Answer<SmsResponse>
    suspend fun checkToken(token: String): Answer<TokenResponse>

    suspend fun updateUser(
        token: String,
        name: String,
        birthday: String,
        photo: String
    ): Answer<RegUserResponse>

    suspend fun deletePhoto(token: String): Answer<RegUserResponse>

    //auth
    suspend fun getUsers(): Answer<List<User>>

    suspend fun createChat(userFrom: Int, userTo: Int): Answer<CreatePrivateResponse>
    suspend fun addPrivateMessage(
        chatId: Int,
        text: String,
        photo: String?,
        geocode: String?,
        token: String
    ): Answer<PutPrivateResponse>

    suspend fun getAllChats(): Answer<List<Chat?>>
    suspend fun getChatById(page: Int): Answer<ChatResponse>
    suspend fun createAnonimChat(
        coordinates: String,
        uid: String = "",
        channelFlag: Int = 0
    ): Answer<ChatAdd>

    suspend fun sendMessage(message: Message): Answer<MessageSendResponse>

    suspend fun getRegContacts(numbers: String): Answer<RegContactResponse>
    suspend fun sendFireBaseToken(userId: Int, token: String): Answer<Boolean>
    suspend fun getPrivateAllChats(userId: Int, token: String): Answer<PrivateChatsResponse>

    suspend fun addFavoriteUser(favoriteUserId: Int, token: String): Answer<FavoriteUserAddResponse>
    suspend fun deleteFavoriteUser(
        favoriteUserId: Int,
        token: String
    ): Answer<FavoriteUserDeleteResponse>

    suspend fun getFavoriteUsers(token: String): Answer<FavoriteUsersResponse>

    suspend fun createThematicChat(
        title: String,
        description: String,
        phone: String,
        address: String,
        coordinates: String,
        avatarNumber: Int,
        usersCount: Int,
        token: String,
    ): Answer<CreateThematicChatResponse>

    suspend fun addThematicMessage(
        chatId: Int,
        text: String,
        token: String
    ): Answer<AddThematicMessageResponse>

    suspend fun getThematicChat(
        chatId: Int,
        token: String
    ): Answer<GetThematicChatResponse>

    suspend fun getAllThematicChats(): Answer<GetThematicChatsResponse>

    suspend fun addPhotoToChat(
        chatId: Int,
        image: String,
        token: String
    ): Answer<AddPhotoToChatResponse>

    suspend fun addPhotoToComment(
        messageId: Int,
        image: String,
        token: String
    ): Answer<AddPhotoToCommentResponse>

    suspend fun addUserToChat(
        chatId: Int,
        token: String
    ): Answer<AddUserToChatResponse>

    suspend fun getUserInfo(
        token: String
    ): Answer<GetUserInfoResponse>

    suspend fun addAttach(
        chatId: Int,
        ext: String,
        chatType: Int,
        fileBase64: String,
        token: String
    ): Answer<AddAttachResponse>
}