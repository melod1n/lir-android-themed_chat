package com.android.lir.utils

object Urls {
    const val regUser: String = "reg_user"
    const val checkUser: String = "auth_check_user"
    const val checkSms: String = "auth_check_sms_code"
    const val checkToken: String = "check_token"

    const val updateUser: String = "update_user"
    const val createPrivateChat: String = "create_private_chat"
    const val addPrivateMessage: String = "add_private_message"

    const val deletePhoto: String = "delete_photo"
    const val getUsers: String = "get_users"
    const val getRegContacts: String = "get_contacts_list"
    const val sendToken: String = "add_mobile_token"
    const val getAllChats: String = "get_user_chats"

    const val getAllChat: String = "chats"
    const val getChatById: String = "chat/{id}"
    const val createChat: String = "add_chat"
    const val sendMessage: String = "add_message"

    const val addFavoriteUser: String = "add_favorite_user"
    const val deleteFavoriteUser: String = "delete_favorite_user"
    const val getFavoriteUsers: String = "get_favorite_user"

    const val createThematicChat: String = "create_thematic_chat"
    const val addThematicMessage: String = "add_thematic_message"
    const val getThematicChat: String = "thematicchat"
    const val getThematicChats: String = "thematicchats"
    const val addPhotoToChat: String = "add_photo_to_chat"
}

object FieldValue {
    const val coordinates: String = "coordinates"
    const val id: String = "id"
}