package com.android.lir.screens.main.map.easychatnew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.lir.R
import com.android.lir.dataclases.MessageInChat
import com.android.lir.utils.AppExtensions.formatDate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_chat_message_received.view.*
import kotlinx.android.synthetic.main.item_chat_message_sent.view.*
import javax.inject.Inject

class EasyChatAdapterNew @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatListItem: MutableList<ChatItem> = mutableListOf()

    fun setData(list: List<ChatItem>) {
        chatListItem.clear()
        chatListItem.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SentViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false))
            else -> ReceivedViewHolder(
                inflater.inflate(
                    R.layout.item_chat_message_received,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int) = when (chatListItem.getOrNull(position)) {
        is ChatItem.Send -> 0
        is ChatItem.Receiver -> 1
        else -> -1
    }

    override fun getItemCount(): Int {
        return chatListItem.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(chatListItem[position].messageInChat)
            is ReceivedViewHolder -> holder.bind(chatListItem[position].messageInChat)
        }
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatItem: MessageInChat) {
            itemView.sender_photo.isVisible = chatItem.photo != null
            itemView.tvSenderName.isVisible = chatItem.name != null
            if (chatItem.photo != null)
                Glide.with(itemView.context).load(chatItem.photo).circleCrop()
                    .into(itemView.sender_photo)
            if (chatItem.name != null)
                itemView.tvSenderName.text = chatItem.name
            itemView.textMessageBodySent.text = chatItem.text
            itemView.tvTimeSend.text =
                chatItem.created_at.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatItem: MessageInChat) {
            itemView.reciever_photo.isVisible = chatItem.user_photo != null
            itemView.tvRecieverName.isVisible = chatItem.user_name != null
            if (chatItem.photo != null)
                Glide.with(itemView.context).load(chatItem.user_photo).circleCrop()
                    .into(itemView.reciever_photo)
            if (chatItem.user_name != null)
                itemView.tvRecieverName.text = chatItem.user_name
            itemView.textMessageBody.text = chatItem.text
            itemView.tvTime.text = chatItem.created_at.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
        }
    }
}

sealed class ChatItem(open val messageInChat: MessageInChat) {
    data class Send(override val messageInChat: MessageInChat) : ChatItem(messageInChat)
    data class Receiver(override val messageInChat: MessageInChat) : ChatItem(messageInChat)
}
