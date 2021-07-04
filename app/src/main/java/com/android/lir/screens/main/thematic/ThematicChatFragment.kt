package com.android.lir.screens.main.thematic

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.viewModels
import coil.load
import com.android.lir.R
import com.android.lir.common.AppGlobal
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.databinding.FragmentThemedChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThematicChatFragment : BaseVMFragment<ThemedChatVM>(R.layout.fragment_thematic_chat) {

    override val viewModel: ThemedChatVM by viewModels()

    private val binding: FragmentThemedChatBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadThematicChat()
    }

    private fun loadThematicChat() {
//        viewModel.loadThematicChat(requireArguments().getInt("chatId", -1))
    }

    override fun onEvent(event: Event) {
        if (event is LoadThematicChatEvent) {
            fillData(event)
        } else super.onEvent(event)
    }

    private fun fillData(event: LoadThematicChatEvent) {
//        val chat = event.chatInfo

        binding.ivIcon.load(AppGlobal.kittens.random()) {
            placeholder(ColorDrawable(Color.GREEN))
            error(ColorDrawable(Color.RED))
            crossfade(true)
        }

//        binding.tvTitle.text = chat.title
//        binding.tvPhone.text = chat.phone
//        binding.tvAddress.text = chat.address
    }

}