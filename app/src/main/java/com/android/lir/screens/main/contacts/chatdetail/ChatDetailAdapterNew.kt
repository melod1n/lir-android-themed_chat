package com.android.lir.screens.main.contacts.chatdetail

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.lir.R
import com.android.lir.activity.ShowPhotoActivity
import com.android.lir.base.adapter.BindingHolder
import com.android.lir.databinding.ItemChatMessageAttachReceivedBinding
import com.android.lir.databinding.ItemChatMessageAttachSentBinding
import com.android.lir.dataclases.PrivateMessage
import com.android.lir.utils.AppExtensions
import com.android.lir.utils.AppExtensions.formatDate
import kotlinx.android.synthetic.main.item_chat_message_received.view.*
import kotlinx.android.synthetic.main.item_chat_message_sent.view.*

class ChatDetailAdapterNew constructor(
    private var fragment: ChatDetailFragment
) :
    ListAdapter<PrivateChatItem, RecyclerView.ViewHolder>(MESSAGES_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SentViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false))
            1 -> ReceivedViewHolder(
                inflater.inflate(
                    R.layout.item_chat_message_received,
                    parent,
                    false
                )
            )
            2 -> AttachSentHolder(ItemChatMessageAttachSentBinding.inflate(inflater, parent, false))
            else -> AttachReceivedHolder(
                ItemChatMessageAttachReceivedBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int) = when (currentList.getOrNull(position)) {
        is PrivateChatItem.Send -> 0
        is PrivateChatItem.Receiver -> 1
        is PrivateChatItem.AttachSend -> 2
        is PrivateChatItem.AttachReceiver -> 3
        else -> -1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList.getOrNull(position)?.let { current ->
            val next = currentList.getOrNull(position + 1)
            val title = if (AppExtensions.check1Day(
                    current.messageInChat.createdAt,
                    next?.messageInChat?.createdAt
                )
            ) current.messageInChat.createdAt.formatDate(
                "yyyy-MM-dd HH:mm:ss",
                "dd\nMMMM"
            ) else null
            when (holder) {
                is SentViewHolder -> holder.bind(current.messageInChat, title)
                is ReceivedViewHolder -> holder.bind(current.messageInChat, title)
                is AttachSentHolder -> holder.bind(current.messageInChat, title)
                is AttachReceivedHolder -> holder.bind(current.messageInChat, title)
                else -> Unit
            }
        }
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatItem: PrivateMessage, title: String?) = with(itemView) {
            itemView.textMessageBodySent.text = chatItem.text
            itemView.tvTimeSend.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
            sender_message_date.isVisible = title != null
            sender_message_date.text = title

            with(sPhoto) {
                isVisible = chatItem.photo != null

                if (!isVisible) {
                    setImageDrawable(null)
                    setOnClickListener(null)
                    return
                }

                if (isVisible && chatItem.files.isEmpty()) {
                    load(chatItem.photo) {
                        crossfade(100)
                        error(ColorDrawable(Color.RED))
                    }
                }

                setOnClickListener {
                    context.startActivity(
                        Intent(
                            context,
                            ShowPhotoActivity::class.java
                        ).putExtra("image", chatItem.photo)
                    )
                }

            }
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatItem: PrivateMessage, title: String?) = with(itemView) {
            textMessageBody.text = chatItem.text
            tvTime.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
            reciever_message_date.isVisible = title != null
            reciever_message_date.text = title

            with(rPhoto) {
                isVisible = chatItem.photo != null

                if (!isVisible) {
                    setImageDrawable(null)
                    setOnClickListener(null)
                    return
                }

                if (isVisible && chatItem.files.isEmpty()) {
                    load(chatItem.photo) {
                        crossfade(100)
                        error(ColorDrawable(Color.RED))
                    }
                }

                setOnClickListener {
                    context.startActivity(
                        Intent(
                            context,
                            ShowPhotoActivity::class.java
                        ).putExtra("image", chatItem.photo)
                    )
                }
            }
        }
    }

    inner class AttachSentHolder(binding: ItemChatMessageAttachSentBinding) :
        BindingHolder<ItemChatMessageAttachSentBinding>(binding) {

        fun bind(chatItem: PrivateMessage, dateTitle: String?) = with(binding) {
            attachPicture.setImageResource(if (chatItem.location == null) R.drawable.ic_baseline_insert_drive_file_24 else R.drawable.ic_baseline_location_on_24)
            title.text = if (chatItem.location == null) "Filename" else "Geolocation"

            time.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")

            date.isVisible = dateTitle != null
            date.text = dateTitle

            binding.root.setOnClickListener {
                chatItem.location?.let { fragment.showLocation(it) }
                chatItem.files.firstOrNull()?.let { fragment.downloadFile(it) }
            }
        }
    }

    inner class AttachReceivedHolder(binding: ItemChatMessageAttachReceivedBinding) :
        BindingHolder<ItemChatMessageAttachReceivedBinding>(binding) {

        fun bind(chatItem: PrivateMessage, dateTitle: String?) = with(binding) {
            attachPicture.setImageResource(if (chatItem.location == null) R.drawable.ic_baseline_insert_drive_file_24 else R.drawable.ic_baseline_location_on_24)
            title.text = if (chatItem.location == null) "Filename" else "Geolocation"

            time.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")

            date.isVisible = dateTitle != null
            date.text = dateTitle

            binding.root.setOnClickListener {
                chatItem.location?.let { fragment.showLocation(it) }
                chatItem.files.firstOrNull()?.let { fragment.downloadFile(it) }
            }
        }

    }

    companion object {
        val MESSAGES_COMPARATOR = object : DiffUtil.ItemCallback<PrivateChatItem>() {
            override fun areItemsTheSame(oldItem: PrivateChatItem, newItem: PrivateChatItem) =
                oldItem.messageInChat.id == newItem.messageInChat.id

            override fun areContentsTheSame(
                oldItem: PrivateChatItem,
                newItem: PrivateChatItem
            ): Boolean = oldItem.messageInChat.text == newItem.messageInChat.text
        }
    }
}

sealed class PrivateChatItem(open val messageInChat: PrivateMessage) {
    data class Send(override val messageInChat: PrivateMessage) : PrivateChatItem(messageInChat)
    data class Receiver(override val messageInChat: PrivateMessage) : PrivateChatItem(messageInChat)

    data class AttachSend(override val messageInChat: PrivateMessage) :
        PrivateChatItem(messageInChat)

    data class AttachReceiver(override val messageInChat: PrivateMessage) :
        PrivateChatItem(messageInChat)
}
