package com.android.lir.screens.main.contacts.contactscreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.android.lir.common.AppGlobal
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.data.ContactHolder
import com.android.lir.dataclases.Contact
import com.android.lir.dataclases.User
import com.android.lir.network.AuthRepo
import com.android.lir.utils.AppExtensions.getContactList
import com.android.lir.utils.AppExtensions.onlyDigits
import com.android.lir.utils.AppExtensions.unite
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private var contactHolder: ContactHolder,
    private val repo: AuthRepo,
) : BaseVM() {

     val ourUsers: MutableLiveData<List<Contact>> = MutableLiveData()

    private val query = MutableLiveData("")
    val users = query.switchMap { q ->
        if (q.isNullOrBlank()) ourUsers else liveData {
            emit(ourUsers.value?.filter { it.name?.contains(q, true) == true })
        }
    }

    fun getUsers(context: Context?) = viewModelScope.launch {
        tasksEventChannel.send(StartProgress)
        withContext(Dispatchers.Default) {
            if (contactHolder.contacts == null) contactHolder.contacts = context?.getContactList()
        }
        makeJob(
            {
                val numbersList = contactHolder.contacts?.map { it.numbers }?.flatten()?.map {
                    var num = it
                    if (num.firstOrNull() == '8') num = num.replaceFirst('8', '7')
                    num
                }?.onlyDigits()
                Log.d("OkHttp", Gson().toJson(numbersList))
                repo.getRegContacts(Gson().toJson(numbersList))
            },
            onAnswer = { answer ->
                val myContacts = contactHolder.contacts.unite(answer.users)
                    ?.sortedWith(compareBy<Contact> { !it.isRegister }.thenBy { it.name })
                ourUsers.postValue(myContacts)
            },
            onEnd = {
                tasksEventChannel.send(StopProgress)
            }
        )
    }

    fun goToChat(id: Int) = viewModelScope.launch {
        tasksEventChannel.send(GoToChatEvent(id))
    }

    fun setSearchStr(q: String) {
        query.postValue(q)
    }

    //TODO: использовать инъекцию для DataManager
    fun addToFavorites(contact: Contact) = viewModelScope.launch {
        makeJob({
            repo.addFavoriteUser(
                contact.serverId?.toIntOrNull() ?: -1,
                AppGlobal.shared.dataManager.token
            )
        },
            onAnswer = {
                if (!it.error.notFalse()) tasksEventChannel.send(AddToFavoritesEvent(contact))
            },
            onStart = {
                tasksEventChannel.send(StartProgress)
            },
            onEnd = {
                tasksEventChannel.send(StopProgress)
            },
            onError = {})
    }

    //TODO: использовать инъекцию для DataManager
    fun deleteFromFavorites(userId: Int) = viewModelScope.launch {
        makeJob({ repo.deleteFavoriteUser(userId, AppGlobal.shared.dataManager.token) },
            onAnswer = {
                if (!it.error.notFalse()) tasksEventChannel.send(DeleteFromFavoritesEvent(userId))
            },
            onStart = {
                tasksEventChannel.send(StartProgress)
            },
            onEnd = {
                tasksEventChannel.send(StopProgress)
            },
            onError = {})
    }

    //TODO: использовать инъекцию для DataManager
    fun loadFavorites() = viewModelScope.launch {
        makeJob({ repo.getFavoriteUsers(AppGlobal.shared.dataManager.token) },
            onAnswer = {
                if (!it.error.notFalse()) it.favoriteUsers?.let { favorites ->
                    tasksEventChannel.send(GetFavoritesEvent(favorites))
                }
            },
            onStart = {
                tasksEventChannel.send(StartProgress)
            },
            onEnd = {
                tasksEventChannel.send(StopProgress)
            },
            onError = {})
    }
}

data class GoToChatEvent(val id: Int) : Event()
data class AddToFavoritesEvent(val contact: Contact) : Event()
data class DeleteFromFavoritesEvent(val id: Int) : Event()
data class GetFavoritesEvent(val favoriteUsers: List<User>) : Event()

object StartProgress : Event()
object StopProgress : Event()
