package com.android.lir.screens.main.thematic

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import coil.load
import com.android.lir.base.adapter.BaseAdapter
import com.android.lir.base.adapter.BindingHolder
import com.android.lir.databinding.ItemThematicChatPhotoBinding

class ThematicChatPicturesAdapter(context: Context, images: ArrayList<Pair<Drawable?, String?>>) :
    BaseAdapter<Pair<Drawable?, String?>, ThematicChatPicturesAdapter.Holder>(context, images) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemThematicChatPhotoBinding.inflate(inflater, parent, false)
        return Holder(binding)
    }

    inner class Holder(binding: ItemThematicChatPhotoBinding) :
        BindingHolder<ItemThematicChatPhotoBinding>(binding) {

        init {
            val model = binding.picture.shapeAppearanceModel.withCornerSize { 12f }
            binding.picture.shapeAppearanceModel = model
        }

        override fun bind(position: Int) {
            val item = getItem(position)

            item.first?.let { binding.picture.setImageDrawable(it) }

            item.second?.let {
                binding.picture.load(it) {
                    crossfade(100)
                    error(ColorDrawable(Color.RED))
                }
            }


        }

    }

}