package com.android.lir.screens.main.map.easychatnew

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.lir.R
import com.android.lir.data.DataManager
import com.android.lir.dataclases.Chat
import com.android.lir.screens.main.contacts.chatdetail.ClearEvent
import com.android.lir.screens.main.contacts.chatdetail.EnableEditEvent
import com.android.lir.screens.main.contacts.chatdetail.EnabledType
import com.android.lir.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class EasyChatDialog : BottomSheetDialogFragment() {

    private val viewModel: EasyChatVM by viewModels()

    @Inject
    lateinit var adapter: EasyChatAdapterNew

    @Inject
    lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.chat_view, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetDialogTheme).apply {
            setOnShowListener {
                val bottomSheet: FrameLayout? = (it as? BottomSheetDialog)?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { layout ->
                    BottomSheetBehavior.from(layout).state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.from(layout).skipCollapsed = true
                    BottomSheetBehavior.from(layout).isHideable = true
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.info = Triple(
            arguments?.getBoolean("isCreate") ?: false,
            arguments?.getParcelable("type"),
            arguments?.getString("coordinates")
        )
        ic_close.setOnClickListener { dismiss() }
        etMessage.requestFocus()
        chatRecyclerView.setHasFixedSize(true)
        chatRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        chatRecyclerView.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach {
                when (it) {
                    is ClearEvent -> etMessage.setText("")
                    is EnableEditEvent -> changeSendingEnable(it.enabledType)
                }
            }.collect()
        }
        viewModel.messages.observe(viewLifecycleOwner) {
            if (it != null) {
                val oldSize = adapter.itemCount
                adapter.setData(it)
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    delay(50)
                    if (oldSize != adapter.itemCount)
                        (chatRecyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
        if (viewModel.chat == null) {
            viewModel.chat = arguments?.getParcelable("chat")
            viewModel.chat?.also {
                changeSendingEnable(getEnabledChat(it, dataManager.token.isNotBlank()))
                viewModel.startTrackingMessage()
            } ?: run {
                changeSendingEnable(getEnabledChat(null, dataManager.token.isNotBlank()))
            }
        }
        btnSend.setOnClickListener {
            if (etMessage.text.isNotEmpty()) {
                viewModel.sendMessage(etMessage.text.toString())
            }
        }
    }

    private fun getEnabledChat(chat: Chat?, isAuth: Boolean): EnabledType = when {
        !isAuth -> EnabledType.NO_AUTH_DISABLE
        chat?.flag == 1 && chat.authorId != Constants.deviceId -> EnabledType.CHAT_DISABLE
        else -> EnabledType.ENABLE
    }

    private fun changeSendingEnable(enableType: EnabledType) {
        val (inputType, hint) = when (enableType) {
            EnabledType.ENABLE -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS or InputType.TYPE_TEXT_FLAG_MULTI_LINE to getString(R.string.chat_sent_message_hint)
            EnabledType.CHAT_DISABLE -> 0 to "Это канал, писать здесь нельзя!"
            EnabledType.NO_AUTH_DISABLE -> 0 to "Авторизуйтесь чтоб написать сообщение"
        }
        etMessage.inputType = inputType
        etMessage.hint = hint
        btnSend.isVisible = enableType == EnabledType.ENABLE
//        image_button_chat_menu.isVisible = enableType == EnabledType.ENABLE
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult("update", bundleOf())
    }
}
