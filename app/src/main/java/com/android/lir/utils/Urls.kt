package com.android.lir.utils

object Urls {
    const val regUser = "reg_user"
    const val checkUser = "auth_check_user"
    const val checkSms = "auth_check_sms_code"
    const val checkToken = "check_token"

    const val updateUser = "update_user"
    const val createPrivateChat = "create_private_chat"
    const val addPrivateMessage = "add_private_message"

    const val deletePhoto = "delete_photo"
    const val getUsers = "get_users"
    const val getRegContacts = "get_contacts_list"
    const val sendToken = "add_mobile_token"
    const val getAllChats = "get_user_chats"

    const val getAllChat = "chats"
    const val getChatById = "chat/{id}"
    const val createChat = "add_chat"
    const val sendMessage = "add_message"

    const val addFavoriteUser = "add_favorite_user"
    const val deleteFavoriteUser = "delete_favorite_user"
    const val getFavoriteUsers = "get_favorite_user"

    const val createThematicChat = "create_thematic_chat"
    const val addThematicMessage = "add_thematic_message"
    const val getThematicChat = "thematicchat"
    const val getThematicChats = "thematicchats"

    const val addPhotoToChat = "add_photo_to_chat"
    const val addPhotoToComment = "add_photo_to_message"
    const val addUserToChat = "add_user_to_chat"
}

object FieldValue {
    const val coordinates = "coordinates"
    const val id = "id"
}