package com.android.lir.screens.main.contacts.chatdetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.lir.R
import com.android.lir.data.DataManager
import com.android.lir.dataclases.PrivateMessage
import com.android.lir.utils.AppExtensions.check1Day
import com.android.lir.utils.AppExtensions.decodeFromBase64
import com.android.lir.utils.AppExtensions.formatDate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_chat_message_received.view.*
import kotlinx.android.synthetic.main.item_chat_message_sent.view.*
import kotlinx.android.synthetic.main.row_chat_item.view.*
import javax.inject.Inject


class ChatDetailAdapter @Inject constructor(
    private val dataManager: DataManager
) : ListAdapter<PrivateMessage, RecyclerView.ViewHolder>(MESSAGES_COMPARATOR) {

    private var listener: ((Int) -> Unit)? = null

    fun setOnMessageListener(block: (Int) -> Unit) {
        listener = block
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SentViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false))
            else -> ReceivedViewHolder(inflater.inflate(R.layout.item_chat_message_received, parent, false))
        }
    }

    override fun getItemViewType(position: Int) = when(currentList.getOrNull(position)?.userId == dataManager.userId) {
        true -> 0
        false -> 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        currentList.getOrNull(position)?.let { current ->
            val next = currentList.getOrNull(position+1)
            val title = if(check1Day(current.createdAt, next?.createdAt)) current.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "dd\nMMMM") else null
            when (holder) {
                is SentViewHolder -> holder.bind(current, title)
                is ReceivedViewHolder -> holder.bind(current, title)
            }
        }

    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatItem: PrivateMessage, title: String?) = with(itemView) {
            textMessageBodySent.text = chatItem.text
            sender_message_date.isVisible = title != null
            sender_message_date.text = title
            sPhoto.isVisible = chatItem.photo != null
            Glide.with(context).load(chatItem.photo).into(sPhoto)
            tvTimeSend.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatItem: PrivateMessage, title: String?) = with(itemView) {
            textMessageBody.text = chatItem.text
            reciever_message_date.isVisible = title != null
            reciever_message_date.text = title
            rPhoto.isVisible = chatItem.photo != null
            Glide.with(context).load(chatItem.photo).into(rPhoto)
            tvTime.text = chatItem.createdAt.formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
        }
    }

    companion object {
        val MESSAGES_COMPARATOR = object : DiffUtil.ItemCallback<PrivateMessage>() {
            override fun areItemsTheSame(oldItem: PrivateMessage, newItem: PrivateMessage) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PrivateMessage,
                newItem: PrivateMessage
            ): Boolean = oldItem.text == newItem.text
        }
    }
}