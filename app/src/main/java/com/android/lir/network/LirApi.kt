package com.android.lir.network

import com.android.lir.dataclases.*
import com.android.lir.utils.Answer
import com.android.lir.utils.FieldValue
import com.android.lir.utils.Urls
import retrofit2.http.*


interface LirApi {
    @FormUrlEncoded
    @POST(Urls.regUser)
    suspend fun regUser(
        @Field("phone") phone: String,
        @Field("name") name: String,
        //@Field("birthdate") birthdate: String,
        @Field("photo") photo: String,
    ): Answer<RegUserResponse>

    @FormUrlEncoded
    @POST(Urls.checkUser)
    suspend fun checkUser(@Field("phone") phone: String): Answer<CheckUserResponse>

    @FormUrlEncoded
    @POST(Urls.checkSms)
    suspend fun checkSms(
        @Field("auth_session") sessionId: String,
        @Field("auth_sms_code") smsCode: String,
    ): Answer<SmsResponse>

    @GET(Urls.checkToken)
    suspend fun checkToken(@Query("token") token: String): Answer<TokenResponse>

    @FormUrlEncoded
    @POST(Urls.updateUser)
    suspend fun updateUser(
        @Field("token") token: String,
        @Field("name") name: String,
        @Field("birthdate") birthday: String,
        @Field("photo") photo: String
    ): Answer<RegUserResponse>

    @FormUrlEncoded
    @POST(Urls.deletePhoto)
    suspend fun deletePhoto(@Field("token") token: String): Answer<RegUserResponse>

    @GET(Urls.getUsers)
    suspend fun getUsers(): Answer<List<User>>

    @FormUrlEncoded
    @POST(Urls.createPrivateChat)
    suspend fun createPrivateChat(
        @Field("user_from") userFrom: Int,
        @Field("user_to") userTo: Int
    ): Answer<CreatePrivateResponse>

    @FormUrlEncoded
    @POST(Urls.addPrivateMessage)
    suspend fun addPrivateMessage(
        @Field("chat_id") chatId: Int,
        @Field("text") text: String,
        @Field("photo") photo: String?,
        @Field("token") token: String
    ): Answer<PutPrivateResponse>

    @GET(Urls.getAllChat)
    suspend fun getAllChats(): Answer<List<Chat?>>

    @GET(Urls.getChatById)
    suspend fun getChatById(@Path(FieldValue.id) page: Int): Answer<ChatResponse>

    @POST(Urls.createChat)
    suspend fun createChat(
        @Query(FieldValue.coordinates) coordinates: String,
        @Query("author_uid") uid: String,
        @Query("channel_flag") flag: Int
    ): Answer<ChatAdd>

    @POST(Urls.sendMessage)
    suspend fun sendMessage(@Body message: Message): Answer<MessageSendResponse>

    @FormUrlEncoded
    @POST(Urls.getRegContacts)
    suspend fun getRegContacts(@Field("contacts") numbers: String): Answer<RegContactResponse>


    @FormUrlEncoded
    @POST(Urls.sendToken)
    suspend fun sendToken(
        @Field("user_id") userId: Int,
        @Field("token") token: String
    ): Answer<Boolean>

    @FormUrlEncoded
    @POST(Urls.getAllChats)
    suspend fun getAllChats(
        @Field("user_id") chatId: Int,
        @Field("token") token: String
    ): Answer<PrivateChatsResponse>

    @FormUrlEncoded
    @POST(Urls.addFavoriteUser)
    suspend fun addFavoritesUser(
        @Field("favorite_user_id") favoriteUserId: Int,
        @Field("token") token: String
    ): Answer<FavoriteUserAddResponse>

    @FormUrlEncoded
    @POST(Urls.deleteFavoriteUser)
    suspend fun deleteFavoriteUser(
        @Field("favorite_user_id") favoriteUserId: Int,
        @Field("token") token: String
    ): Answer<FavoriteUserDeleteResponse>

    @FormUrlEncoded
    @POST(Urls.getFavoriteUsers)
    suspend fun getFavoriteUsers(
        @Field("token") token: String
    ): Answer<FavoriteUsersResponse>


    @FormUrlEncoded
    @POST(Urls.createThematicChat)
    suspend fun createThematicChat(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("coordinates") coordinates: String,
        @Field("avatar_num") avatarNumber: Int,
        @Field("user_count") usersCount: Int,
        @Field("token") token: String
    ): Answer<CreateThematicChatResponse>

    @FormUrlEncoded
    @POST(Urls.addThematicMessage)
    suspend fun addThematicMessage(
        @Field("chat_id") chatId: Int,
        @Field("text") text: String,
        @Field("token") token: String
    ): Answer<AddThematicMessageResponse>

    @GET(Urls.getThematicChat)
    suspend fun getThematicChat(
        @Query("thematicchat_id") chatId: Int,
        @Query("token") token: String
    ): Answer<GetThematicChatResponse>

    @GET(Urls.getThematicChats)
    suspend fun getAllThematicChats(): Answer<GetThematicChatsResponse>

    @FormUrlEncoded
    @POST(Urls.addPhotoToChat)
    suspend fun addPhotoToChat(
        @Field("chat_id") chatId: Int,
        @Field("image") image: String,
        @Field("token") token: String
    ): Answer<AddPhotoToChatResponse>

    @FormUrlEncoded
    @POST(Urls.addPhotoToComment)
    suspend fun addPhotoToComment(
        @Field("thematicchatmessage_id") messageId: Int,
        @Field("image") image: String,
        @Field("token") token: String
    ): Answer<AddPhotoToCommentResponse>

    @FormUrlEncoded
    @POST(Urls.addUserToChat)
    suspend fun addUserToChat(
        @Field("chat_id") chatId: Int,
        @Field("token") token: String
    ): Answer<AddUserToChatResponse>
}