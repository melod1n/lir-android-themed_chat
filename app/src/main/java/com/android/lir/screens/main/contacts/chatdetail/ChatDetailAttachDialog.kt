package com.android.lir.screens.main.contacts.chatdetail

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.adapter.BaseAdapter
import com.android.lir.base.adapter.BindingHolder
import com.android.lir.databinding.AttachDialogBinding
import com.android.lir.databinding.ItemAttachBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChatDetailAttachDialog : BottomSheetDialogFragment() {

    private val binding: AttachDialogBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.attach_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancel.setOnClickListener { dismiss() }

        val items = arrayListOf(
            AttachItem(
                "camera",
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_photo_camera_24
                )!!,
                "Камера"
            ),
            AttachItem(
                "gallery",
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_baseline_image_24
                )!!,
                "Галерея"
            ),
            AttachItem(
                "file", ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_insert_drive_file_24
                )!!, "Файл"
            ),
            AttachItem(
                "geolocation", ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_location_on_24
                )!!, "Геопозиция"
            )
        )

        val adapter = AttachAdapter(requireContext(), items).also { adapter ->
            adapter.itemClickListener = { position ->
                findNavController().navigateUp()
                dismiss()

                setFragmentResult("pickType", bundleOf("type" to adapter[position].key))
            }
        }

        binding.recyclerView.adapter = adapter

    }

}

data class AttachItem(val key: String, val icon: Drawable, val text: String)

class AttachAdapter(
    context: Context,
    values: ArrayList<AttachItem>
) : BaseAdapter<AttachItem, AttachAdapter.Holder>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemAttachBinding.inflate(inflater, parent, false))

    inner class Holder(binding: ItemAttachBinding) : BindingHolder<ItemAttachBinding>(binding) {

        init {
            binding.iconBorder.setImageDrawable(ColorDrawable(Color.parseColor("#EADFFD")))
        }

        override fun bind(position: Int) {
            super.bind(position)

            val item = getItem(position)

            binding.icon.setImageDrawable(item.icon)
            binding.text.text = item.text
        }

    }


}