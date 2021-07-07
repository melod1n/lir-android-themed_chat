package com.android.lir.dataclases

import android.graphics.Bitmap
import android.os.Parcelable
import com.android.lir.screens.main.contacts.chatdetail.PrivateChatItem
import com.android.lir.screens.main.map.createchat.ChatType
import com.android.lir.utils.AppExtensions.string
import com.android.lir.utils.Constants
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ChatResponse(
    @SerializedName("author_uid") var authorId: String?,
    @SerializedName("channel_flag") var flag: Int?,
    val messages: List<MessageInChat?>
) : Parcelable

@Parcelize
class MessageInChat(
    @SerializedName("id") var id: Int?,
    @SerializedName("chat_id") var chat_id: Int?,
    @SerializedName("text") var text: String?,
    @SerializedName("photo") var photo: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("created_at") var created_at: String?,
    @SerializedName("updated_at") var updated_at: String?,
    @SerializedName("uid") var uid: String,
    @SerializedName("user_name") var user_name: String?,
    @SerializedName("user_photo") var user_photo: String?
) : Parcelable

@Parcelize
data class Chat(
    @SerializedName("id") var id: Int,
    @SerializedName("coordinates") var coordinates: String?,
    @SerializedName("author_uid") var authorId: String?,
    @SerializedName("channel_flag") var flag: Int?,
    @SerializedName("messages_count") var count: Int?,
    @SerializedName("created_at") var created_at: String?,
) : Parcelable

@Parcelize
data class ChatAdd(
    @SerializedName("chat_id") var chat_id: Int
) : Parcelable {
    fun toChat(type: ChatType, coord: String) = Chat(
        id = chat_id,
        coordinates = coord,
        authorId = if (type == ChatType.CHANNEL) Constants.deviceId else "",
        flag = if (type == ChatType.CHANNEL) 1 else 0,
        count = 0,
        created_at = Date().string()
    )
}

@Parcelize
data class MessageSendResponse(
    @SerializedName("message_id") var message_id: Int?
) : Parcelable

@Parcelize
data class RegUserResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("user_id") var userId: AlwaysString
) : Parcelable

@Parcelize
data class CreatePrivateResponse(
    @SerializedName("chat_id") var chatId: AlwaysString,
    @SerializedName("messages") var messages: List<PrivateMessage>,
    @SerializedName("companion_photo") var photo: String?,
    @SerializedName("companion_name") var name: String?
) : Parcelable

@Parcelize
data class PrivateChatsResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("chats") var chats: List<PrivateChatInfo>
) : Parcelable

@Parcelize
data class PrivateChatInfo(
    @SerializedName("chat_id") var chatId: Int,
    @SerializedName("user_id") var userId: Int,
    @SerializedName("photo") var userPhoto: String?,
    @SerializedName("name") var userName: String?,
    @SerializedName("no_read_count") var noReadCount: Int?,
    @SerializedName("last_message") var lastMessage: String?,
    @SerializedName("created_at") var createdAt: String?,
    @SerializedName("updated_at") var updatedAt: String?,
) : Parcelable

@Parcelize
data class PutPrivateResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("message_id") var messageId: AlwaysString
) : Parcelable

@Parcelize
data class CheckUserResponse(
    @SerializedName("auth_sms_code") var smsCode: AlwaysString,
    @SerializedName("auth_session") var session: AlwaysString,
    @SerializedName("user_id") var userId: AlwaysString,
    @SerializedName("error") var error: AlwaysString
) : Parcelable

@Parcelize
data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("photo") var photo: String,
    @SerializedName("phone") var number: String?,
) : Parcelable

@Parcelize
data class PrivateMessage(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,
    @SerializedName("photo") val photo: String?,
    @SerializedName("user_id") var userId: String,
    @SerializedName("created_at") var createdAt: String,
) : Parcelable {
    fun toModel(myId: String): PrivateChatItem {
        return if (myId == userId) {
            PrivateChatItem.Send(this)
        } else PrivateChatItem.Receiver(this)
    }
}

@Parcelize
data class SmsResponse(
    @SerializedName("auth_token") var token: AlwaysString,
    @SerializedName("error") var error: AlwaysString,
) : Parcelable

@Parcelize
data class TokenResponse(
    @SerializedName("success") var success: AlwaysString
) : Parcelable

@Parcelize
data class AlwaysString(val value: String) : Parcelable {
    fun notFalse(): Boolean = value != "false"
}

@Parcelize
data class Message(
    @SerializedName("chat_id") var chat_id: Int?,
    @SerializedName("text") var text: String?,
    @SerializedName("user_id") var user_id: String?,
    @SerializedName("uid") var uid: String?
) : Parcelable

@Parcelize
data class Contact(
    val id: String?,
    val name: String?,
    val numbers: List<String>,
    val photoUri: String?,
    var serverId: String? = null,
    var serverName: String? = null,
    var serverPhoto: String? = null,
    var serverPhone: String? = null,
    var isRegister: Boolean = false,
    var isSendRequest: Boolean = false
) : Parcelable

@Parcelize
data class RegContactResponse(
    @SerializedName("users") var users: List<RegContact>?,
) : Parcelable

@Parcelize
data class RegContact(
    @SerializedName("id") var contact_id: Int?,
    @SerializedName("name") var name: String?,
    @SerializedName("photo") var photo: String?,
    @SerializedName("phone") var phone: String?,
) : Parcelable

@Parcelize
data class FavoriteUserAddResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("success") var success: AlwaysString
) : Parcelable

@Parcelize
data class FavoriteUserDeleteResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("success") var success: AlwaysString
) : Parcelable

@Parcelize
data class FavoriteUsersResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("favorite_users") var favoriteUsers: List<User>?
) : Parcelable

@Parcelize
data class CreateThematicChatResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("thematicchat_id") var chatId: Int
) : Parcelable

@Parcelize
data class AddThematicMessageResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("thematicchatmessage_id") var messageId: Int
) : Parcelable

@Parcelize
data class GetThematicChatResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("thematicchat") var chat: ThematicChat
) : Parcelable

@Parcelize
data class GetThematicChatsResponse(
    @SerializedName("thematicchats") var chats: List<ThematicChatInfo>
) : Parcelable

@Parcelize
data class ThematicChat(
    @SerializedName("chat_info") var info: ThematicChatInfo,
    @SerializedName("chat_messages") var comments: List<ThematicComment> = listOf(),
    @SerializedName("images") var images: List<String> = listOf()
) : Parcelable

@Parcelize
data class ThematicChatInfo(
    @SerializedName("id") var chatId: Int,
    @SerializedName("user_id") var creatorId: Int,
    @SerializedName("title") var title: String,
    @SerializedName("description") var description: String,
    @SerializedName("address") var address: String,
    @SerializedName("phone") var phone: String = "",
    @SerializedName("coordinates") var coordinates: String?,
    @SerializedName("avatar_num") var avatarNum: Int,
    @SerializedName("user_count") var usersCount: Int
) : Parcelable

@Parcelize
data class ThematicComment(
    @SerializedName("id") var messageId: Int = -1,
    @SerializedName("thematicchat_id") var chatId: Int = -1,
    @SerializedName("text") var text: String = "",
    @SerializedName("user_id") var userId: Int = -1,
    @SerializedName("created_at") var createdAt: String = "",
    @SerializedName("updated_at") var updatedAt: String = "",
    @SerializedName("user_photo") var userPhoto: String? = "",
    @SerializedName("user_name") var userName: String? = "",
    var photo: Bitmap? = null,
    @SerializedName("images") var images: List<String> = listOf()
) : Parcelable

@Parcelize
data class AddPhotoToChatResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("photo_id") var id: Int
) : Parcelable

@Parcelize
data class AddPhotoToCommentResponse(
    @SerializedName("error") var error: AlwaysString,
    @SerializedName("photo_id") var photoId: Int
) : Parcelable

@Parcelize
data class AddUserToChatResponse(
    @SerializedName("error") var error: AlwaysString
) : Parcelable