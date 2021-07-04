package com.android.lir.screens.main.map.createchat

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.view_create_chat.*

@AndroidEntryPoint
class CreateChatDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.view_create_chat, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), R.style.Theme_Design_BottomSheetDialog)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llCreateThemedChat.setOnClickListener { goBack(ChatType.THEMED) }
        llCreateEasyChat.setOnClickListener { goBack(ChatType.EASY) }
        llCreateCommercialChat.setOnClickListener { goBack(ChatType.COMMERCIAL) }
        llCreateChatChannel.setOnClickListener { goBack(ChatType.CHANNEL) }
    }

    private fun goBack(type: ChatType) {
        setFragmentResult(
            "create_chat",
            bundleOf(
                "chat_type" to type,
                "coordinates" to (arguments?.getString("coordinates") ?: "")
            )
        )
        findNavController().navigateUp()
    }
}

@Parcelize
enum class ChatType : Parcelable { THEMED, EASY, CHANNEL, COMMERCIAL }
