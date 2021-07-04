package com.android.lir.screens.main.thematic

import androidx.lifecycle.viewModelScope
import com.android.lir.common.AppGlobal
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.network.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemedChatVM @Inject constructor(
    private val repo: AuthRepo
) : BaseVM() {


}
