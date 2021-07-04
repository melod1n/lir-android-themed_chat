package com.android.lir.screens.main.contacts.chatdetail

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.utils.AppExtensions.compressBitmap
import com.android.lir.utils.AppExtensions.toBitMap
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_detail_fragment.*
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class ChatDetailFragment : BaseVMFragment<ChatDetailVM>(R.layout.chat_detail_fragment) {

    @Inject
    lateinit var adapter: ChatDetailAdapterNew

    override val viewModel: ChatDetailVM by viewModels()

    var chatId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = arguments?.getInt("chat_id") ?: 0
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        context?.toBitMap(it)?.compressBitmap()?.let(viewModel::sendMessage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.partnerId ?: run { viewModel.setPartnerId(arguments?.getInt("chat_id") ?: 0) }
        btnSend.setOnClickListener { viewModel.sendMessage() }
        btnAdd.setOnClickListener { pickImage.launch("image/*") }
        back.setOnClickListener { findNavController().popBackStack() }
        rvMessages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        rvMessages.adapter = adapter
        etMessage.doAfterTextChanged { viewModel.onMessageChanged(it?.toString()) }
        viewModel.messages.observe(viewLifecycleOwner) {
            val oldSize = adapter.itemCount
            adapter.submitList(it)
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                delay(50)
                if (oldSize != adapter.itemCount)
                    (rvMessages.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
            }
        }
        viewModel.currentMessage.observe(viewLifecycleOwner) {
            btnSend.isEnabled = !it.isNullOrEmpty()
        }
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is ClearEvent -> etMessage.setText(null)
            is LogOutEvent -> {
                showInfoDialog(null, "Авторизуйтесь для отправки приватных сообщений")
                findNavController().popBackStack()
            }
            is PutPartnerInfoEvent -> {
                Glide.with(requireContext()).load(event.url).circleCrop().into(partnerPhoto)
                partnerName.text = event.name
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        instance = this
    }

    override fun onStop() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        instance = null
        super.onStop()
    }

    companion object {
        var instance: ChatDetailFragment? = null
    }
}
