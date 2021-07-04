package com.android.lir.screens.main.contacts.chatdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.lir.R
import com.android.lir.dataclases.PrivateMessage
import com.android.lir.utils.AppExtensions
import com.android.lir.utils.AppExtensions.formatDate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_chat_message_received.view.*
import kotlinx.android.synthetic.main.item_chat_message_sent.view.*
import javax.inject.Inject

class ChatDetailAdapterNew @Inject constructor() : ListAdapter<PrivateChatItem, RecyclerView.ViewHolder>(MESSAGES_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SentViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false))
            else -> ReceivedViewHolder(inflater.inflate(R.layout.item_chat_message_received, parent, false))
        }
    }

    override fun getItemViewType(position: Int) = when(currentList.getOrNull(position)) {
        is PrivateChatItem.Send -> 0
        is PrivateChatItem.Receiver -> 1
        else -> -1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList.getOrNull(position)?.let { current ->
            val next = currentList.getOrNull(position+1)
            val title = if(AppExtensions.check1Day(current.messageInChat.createdAt, next?.messageInChat?.createdAt)) current.messageInChat.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "dd\nMMMM") else null
            when (holder) {
                is SentViewHolder -> holder.bind(current.messageInChat, title)
                is ReceivedViewHolder -> holder.bind(current.messageInChat, title)
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
            sPhoto.isVisible = chatItem.photo != null
            Glide.with(context).load(chatItem.photo).into(sPhoto)
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatItem: PrivateMessage, title: String?) = with(itemView){
            textMessageBody.text = chatItem.text
            tvTime.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
            reciever_message_date.isVisible = title != null
            reciever_message_date.text = title
            rPhoto.isVisible = chatItem.photo != null
            Glide.with(context).load(chatItem.photo).into(rPhoto)

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
    data class Send(override val messageInChat:PrivateMessage): PrivateChatItem(messageInChat)
    data class Receiver(override val messageInChat:PrivateMessage): PrivateChatItem(messageInChat)
}
