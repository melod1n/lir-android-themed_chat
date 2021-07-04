package com.android.lir.screens.main.contacts.contactscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.lir.R
import com.android.lir.dataclases.Contact
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.android.synthetic.main.row_contact_item.view.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@ActivityRetainedScoped
class ContactsAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : ListAdapter<Contact, ContactsAdapter.ViewHolder>(CONTACTS_COMPARATOR) {

    private var listener: ((Int) -> Unit)? = null
    private var longListener: ((Contact) -> Unit)? = null

    fun setListener(block: (Int) -> Unit) {
        listener = block
    }

    fun setLongListener(block: (Contact) -> Unit) {
        longListener = block
    }

    private var connectListener: ((Contact) -> Unit)? = null

    fun setConnectListener(block: (Contact) -> Unit) {
        connectListener = block
    }

    private val colorPairs = listOf(
        R.color.small_pink to R.color.small_pink_alpha,
        R.color.small_green to R.color.small_green_alpha,
        R.color.small_red to R.color.small_red_alpha,
        R.color.small_blue to R.color.small_blue_alpha,
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            colorPairs.random(),
            LayoutInflater.from(parent.context).inflate(R.layout.row_contact_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentList.getOrNull(position)?.let {
            val isNotFirst = currentList.getOrNull(position - 1)?.name?.first() == it.name?.first()
            holder.bind(it, isNotFirst)
        }
    }

    inner class ViewHolder(private val colorScheme: Pair<Int, Int>, val view: View) :
        RecyclerView.ViewHolder(view) {

        private lateinit var user: Contact

        init {
            view.setOnClickListener {
                user.serverId?.toIntOrNull()?.let { listener?.invoke(it) }
            }

            view.setOnLongClickListener {
                user.serverId?.toIntOrNull()?.let { longListener?.invoke(user) }
                true
            }
        }

        fun bind(item: Contact, isNotFirst: Boolean) {
            user = item
            if (item.serverPhoto != null) {
                Glide.with(view.context).load(item.serverPhoto).circleCrop().into(view.imageView)
            } else {
                with(view.imageView) {
                    setBackgroundResource(R.drawable.circle)
                    backgroundTintList =
                        ContextCompat.getColorStateList(context, colorScheme.second)
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_base_person))
                    setColorFilter(context.getColor(colorScheme.first))
                }
            }
            view.letter.text = if (!isNotFirst) item.name?.first()?.toString() else ""
            view.connect.isVisible = !item.isRegister
//            view.call.isVisible = item.isRegister
            if (!item.isRegister) {
                view.connect.isVisible = true
                if (item.isSendRequest) {
                    view.connect.setBackgroundColor(ContextCompat.getColor(context, R.color.greey))
                    view.connect.text = "✓ Отправлено"
                    view.connect.setTextColor(ContextCompat.getColor(context, R.color.phiolet))
                } else {
                    view.connect.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))
                    view.connect.text = "+ Пригласить"
                    view.connect.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                if (!item.isSendRequest) view.connect.setOnClickListener {
                    with(view.connect) {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.greey))
                        text = "✓ Отправлено"
                        setTextColor(ContextCompat.getColor(context, R.color.phiolet))
                        val l = currentList.toMutableList()
                        val index = l.indexOf(item)
                        l.remove(item)
                        l.add(index, item.copy(isSendRequest = true))
                        submitList(l)
                        setOnClickListener(null)
                        connectListener?.invoke(item)
                    }
                } else view.connect.setOnClickListener(null)
            }
            view.name.text = item.name ?: item.numbers.firstOrNull { it.isNotBlank() }
        }
    }

    companion object {
        val CONTACTS_COMPARATOR = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
                oldItem.name == newItem.name
        }
    }
}