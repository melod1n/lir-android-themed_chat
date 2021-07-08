package com.android.lir.base.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.lir.utils.Answer
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class BaseVM : ViewModel() {

    protected val tasksEventChannel = Channel<Event>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    protected fun <T> makeJob(
        job: suspend () -> Answer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (String) -> Unit)? = null
    ) = viewModelScope.launch {
        onStart?.invoke()
        when (val response = job()) {
            is Answer.Success -> onAnswer(response.data)
            is Answer.Error -> onError?.invoke(response.errorString) ?: tasksEventChannel.send(
                ShowInfoDialogEvent(null, response.errorString)
            )
        }
    }.also { it.invokeOnCompletion { viewModelScope.launch { onEnd?.invoke() } } }

}

open class Event

data class ShowInfoDialogEvent(
    val title: String?,
    val message: String,
    val positiveBtn: String? = null,
    val negativeBtn: String? = null
) : Event()
