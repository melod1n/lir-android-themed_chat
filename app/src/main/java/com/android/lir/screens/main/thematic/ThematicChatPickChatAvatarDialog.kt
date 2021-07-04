package com.android.lir.screens.main.thematic

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import coil.load
import com.android.lir.R
import com.android.lir.base.BaseFullScreenDialog
import com.android.lir.base.adapter.BaseAdapter
import com.android.lir.base.adapter.BindingHolder
import com.android.lir.common.AppGlobal
import com.android.lir.databinding.DialogThematicPickAvatarBinding
import com.android.lir.databinding.ItemThematicChatPickAvatarBinding

class ThematicChatPickChatAvatarDialog :
    BaseFullScreenDialog(R.layout.dialog_thematic_pick_avatar) {

    private val binding: DialogThematicPickAvatarBinding by viewBinding()

    private var selectedIndex = -1
    private lateinit var adapter: PickAvatarAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val index = requireArguments().getInt("index", -1)

        val items = arrayListOf<SelectableItem>()
        AppGlobal.thematicChatAvatars.forEach {
            items.add(
                SelectableItem(
                    ContextCompat.getDrawable(
                        requireContext(),
                        it
                    )
                )
            )
        }

        adapter = PickAvatarAdapter(requireContext(), items)

        binding.recyclerView.adapter = adapter.also { _ ->
            adapter.itemClickListener = { selectItem(it) }
        }

        selectItem(index)

        binding.done.setOnClickListener { dismiss() }
    }

    private fun selectItem(index: Int) {
        if (selectedIndex != index) {
            selectedIndex = index

            val values = ArrayList(adapter.values.value ?: listOf())

            for (i in values.indices) {
                values[i].isSelected = index == i
            }

            adapter.updateValues(values)
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        findNavController().navigateUp()
        setFragmentResult("image_index", bundleOf("index" to selectedIndex))
        super.onDismiss(dialog)
    }
}

class PickAvatarAdapter(
    context: Context,
    values: ArrayList<SelectableItem>
) : BaseAdapter<SelectableItem, PickAvatarAdapter.Holder>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemThematicChatPickAvatarBinding.inflate(inflater, parent, false))
    }

    inner class Holder(binding: ItemThematicChatPickAvatarBinding) :
        BindingHolder<ItemThematicChatPickAvatarBinding>(binding) {

        override fun bind(position: Int) {
            val item = getItem(position)

            binding.icon.load(item.icon) {
                size(200, 200)
                crossfade(100)
            }

            binding.selected.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (item.isSelected) R.color.green else R.color.grey
                )
            )
        }
    }


}

data class SelectableItem(
    val icon: Drawable?,
    var isSelected: Boolean = false
)