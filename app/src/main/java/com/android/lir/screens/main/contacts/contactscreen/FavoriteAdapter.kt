package com.android.lir.screens.main.contacts.contactscreen

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.lir.R
import com.android.lir.common.AppGlobal
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.android.synthetic.main.favorites_item.view.*
import javax.inject.Inject

data class FavInfo(
    val id: Int,
    val imageUrl: String,
    val text: String
)

@ActivityRetainedScoped
class FavoriteAdapter @Inject constructor() : RecyclerView.Adapter<FavoriteAdapter.VH>() {

    private var addListener: (() -> Unit)? = null
    private var startChatListener: ((Int) -> Unit)? = null

    private var longListener: ((FavInfo) -> Unit)? = null

    fun setLongListener(block: ((FavInfo) -> Unit)) {
        longListener = block
    }

    internal val list: MutableList<FavInfo> = mutableListOf()

    fun submitList(l: List<FavInfo>) {
        list.clear()
        list.addAll(l)
    }

    fun setActions(onAdd: () -> Unit, onStartChat: (Int) -> Unit) {
        addListener = onAdd
        startChatListener = onStartChat
    }

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {

        fun setHeader() {
            view.setOnClickListener { addListener?.invoke() }
            with(view.favImage) {
//                setBackgroundResource(R.drawable.circle)
//                setPadding(view.context.resources.displayMetrics.density.toInt() * 16)
//                backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.end_color_gradient)
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_only_plus))
                setColorFilter(context.getColor(R.color.black))
            }
            with(view.favText) {
                text = "Добавить"
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(view.context.getColor(R.color.end_color_gradient))
            }
        }

        fun bind(info: FavInfo) {
            view.setOnClickListener { startChatListener?.invoke(info.id) }

            view.setOnLongClickListener {
                longListener?.invoke(info)
                true
            }

//            with(view.favImage) {
//                setBackgroundResource(R.drawable.circle)
//                setPadding(view.context.resources.displayMetrics.density.toInt() * 16)
//                backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.end_color_gradient)
//            }

//            Glide.with(view.context).load(kittens.random()).centerCrop().into(view.favImage)


//            Glide.with(view.context).load(info.imageUrl).circleCrop().into(view.favImage)

            val photo = if (info.imageUrl.trim().isEmpty()) null else info.imageUrl

            Log.d("AVATAR", photo ?: "no avatar")

            view.favImage.load(photo ?: AppGlobal.kittens.random()) {
                placeholder(ColorDrawable(Color.GREEN))
                error(ColorDrawable(Color.RED))
                crossfade(true)
            }
            view.favText.text = info.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.favorites_item, parent, false))

    override fun onBindViewHolder(holder: FavoriteAdapter.VH, position: Int) {
        if (list.isEmpty()) {
            holder.setHeader()
            return
        }

        list.getOrNull(position)?.let(holder::bind)
    }

//    override fun getItemCount() = list.count() + 1

    override fun getItemCount() = list.count()

    fun containsId(id: Int): Boolean {
        for (fav in list) {
            if (fav.id == id) return true
        }
        return false
    }
}