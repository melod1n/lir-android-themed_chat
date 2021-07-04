package com.android.lir.screens.main.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.lir.R
import com.android.lir.dataclases.PrivateChatInfo
import com.android.lir.utils.AppExtensions.formatDate
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_chat_item.view.*
import javax.inject.Inject

class ChatsAdapter @Inject constructor(
): ListAdapter<PrivateChatInfo, ChatsAdapter.ViewHolder>(CONTACTS_COMPARATOR) {

    private var listener: ((Int) -> Unit)? = null

    fun setListener(block: (Int) -> Unit) {
        listener = block
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_chat_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentList.getOrNull(position)?.let(holder::bind)
    }

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {

        fun bind(item: PrivateChatInfo) {
            view.setOnClickListener { listener?.invoke(item.userId) }
            if (item.userPhoto != null) {
                Glide.with(view.context).load(item.userPhoto).circleCrop().into(view.imageView)
            } else {
                with(view.imageView) {
                    setBackgroundResource(R.drawable.circle)
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.small_blue_alpha)
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_base_person))
                    setColorFilter(context.getColor(R.color.small_blue))
                }
            }
            view.name.text = item.userName
            view.messageText.text = item.lastMessage
            view.time.text = item.updatedAt?.replace("T"," ").formatDate("yyyy-MM-dd HH:mm:ss", "HH:mm")
        }
    }
    companion object {
        val CONTACTS_COMPARATOR = object : DiffUtil.ItemCallback<PrivateChatInfo>() {
            override fun areItemsTheSame(oldItem: PrivateChatInfo, newItem: PrivateChatInfo) = oldItem == newItem
            override fun areContentsTheSame(oldItem: PrivateChatInfo, newItem: PrivateChatInfo): Boolean = oldItem.chatId == newItem.chatId
        }
    }
}