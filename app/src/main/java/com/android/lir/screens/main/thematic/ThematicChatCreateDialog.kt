package com.android.lir.screens.main.thematic

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.android.lir.R
import com.android.lir.base.BaseFullScreenDialog
import com.android.lir.common.AppGlobal
import com.android.lir.databinding.FragmentThematicChatCreateBinding
import com.android.lir.dataclases.ThematicChat
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.utils.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.util.*


@AndroidEntryPoint
class ThematicChatCreateDialog : BaseFullScreenDialog(R.layout.fragment_thematic_chat_create) {

    private val viewModel: ThemedChatCreateVM by viewModels()
    private val binding: FragmentThematicChatCreateBinding by viewBinding()

    private var isCreating = true
    private var isEditing = false

    private var chat: ThematicChat? = null
    private var info: ThematicChatInfo? = null

    private lateinit var adapter: ThematicChatPicturesAdapter

    private var remainPictures = -1

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d("PICK_IMAGE", "RESULT: $uri")

            val imageStream = uri?.let { requireActivity().contentResolver.openInputStream(it) }
                ?: return@registerForActivityResult
            val drawable = BitmapDrawable.createFromStream(imageStream, "")

            with(adapter) {
                add(drawable to null)
                notifyDataSetChanged()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCreating = requireArguments().getBoolean("isCreate", true)

        chat = requireArguments().getParcelable("chat") as? ThematicChat
        info = requireArguments().getParcelable("info") as? ThematicChatInfo

        var coordinates = requireArguments().getString("coordinates")
        if (coordinates.isNullOrBlank()) coordinates = info?.coordinates ?: ""

        isEditing =
            info?.creatorId?.toString() == AppGlobal.shared.dataManager.userId && !isCreating

        binding.address.setText(viewModel.loadAddress(requireContext(), coordinates))

        binding.toolbar.setNavigationOnClickListener { dismiss() }

//        var imageResId = AppGlobal.thematicChatAvatars.random()
        var imageIndex = -1


        binding.picture.setOnClickListener {
//            val imageIndex = AppGlobal.thematicChatAvatars.indexOf(imageResId)

            findNavController().navigate(
                R.id.toThematicChatAvatarDialog,
                bundleOf("index" to imageIndex)
            )
        }

        setFragmentResultListener("image_index") { _, bundle ->
            val index = bundle["index"] as Int
//            imageResId = AppGlobal.thematicChatAvatars[index]
            imageIndex = index
            loadPicture(AppGlobal.thematicChatAvatars[index])
        }

        preparePictures()

        binding.chatUsers.forEach {
            with((it as ImageView)) {
                it.scaleType = ImageView.ScaleType.CENTER_CROP

                load(AppGlobal.kittens.random()) {
                    placeholder(ColorDrawable(Color.GREEN))
                    error(ColorDrawable(Color.RED))
                    crossfade(100)
                    size(200, 200)
                }
            }
        }

        binding.create.setOnClickListener {
            val title = binding.title.text.toString().trim()
            val description = binding.description.text.toString().trim()
            val phone = binding.phone.text.toString().trim()
            val address = binding.address.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || phone.isEmpty() || address.isEmpty() || imageIndex == -1)
                return@setOnClickListener

            viewModel.createThematicChat(
                title,
                description,
                phone,
                address,
                imageIndex,
                coordinates
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach {
                when (it) {
                    is ErrorAddPhotoToChat -> deleteLastPicture(it.error)
                    is SuccessAddPhotoToChat -> showSuccessUploadToast()
                    is ChatCreatedEvent -> loadPictures(it.chatId)
                }
            }.collect()
        }

        binding.addPhoto.setOnClickListener { pickImage() }

        setAccess()

        if (isEditing) {
            val newItems = arrayListOf<Pair<Drawable?, String?>>()
            chat?.images?.forEach { newItems.add(null to it) }
            adapter.updateValues(newItems)
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadPictures(chatId: Int) {
        val values = adapter.values.value ?: listOf()

        remainPictures = values.count()

        if (remainPictures == 0) {
            dismiss()
            return
        }
//
//        binding.addPhoto.isEnabled = false
//        binding.create.isEnabled = false

        Toast.makeText(requireContext(), "Идёт обработка фотографий", Toast.LENGTH_LONG).show()
        Thread {
            values.forEach {
                it.first?.let { drawable ->
                    val bitmap = AndroidUtils.compressDrawable(drawable)
                    viewModel.uploadImage(chatId, bitmap)
                }
            }
        }.start()

    }

    private fun deleteLastPicture(error: String) {
        adapter.remove(adapter.values.value?.last()!!)
        adapter.notifyDataSetChanged()

        Toast.makeText(requireContext(), "Произошла ошибка", Toast.LENGTH_LONG).show()
    }

    private fun showSuccessUploadToast() {
//        Toast.makeText(
//            requireContext(),
//            "Фотография загружена",
//            Toast.LENGTH_LONG
//        ).show()
        remainPictures--

        if (remainPictures == 0) {
            lifecycleScope.launchWhenResumed {
                Toast.makeText(requireContext(), "Фотографии загружены", Toast.LENGTH_LONG).show()
            }

            dismiss()
        }
    }

    private fun loadPicture(resId: Int) {
        val image = ContextCompat.getDrawable(
            requireContext(), resId
        )
        binding.picture.load(image) {
            size(200, 200)
            crossfade(100)
        }
    }

    private fun setAccess() {
        val enabled = isCreating || isEditing
        binding.title.isEnabled = enabled
        binding.description.isEnabled = enabled
        binding.phone.isEnabled = enabled
        binding.address.isEnabled = enabled
        binding.photos.isEnabled = enabled
        binding.addPhoto.isVisible = enabled
        binding.create.isVisible = enabled

        if (binding.create.isVisible)
            binding.create.text = if (isCreating) "Создать" else "Сохранить"

        if (!isCreating) {
            info?.let {
                binding.title.setText(it.title)
                binding.description.setText(it.description)
                binding.phone.setText(it.phone)
                binding.address.setText(it.address)
            }

        }
    }

    private fun preparePictures() {
        adapter = ThematicChatPicturesAdapter(
            requireContext(),
            arrayListOf()
        )

        binding.photos.adapter = adapter
    }

    private fun pickImage() {
        getContent.launch("image/*")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult("update", bundleOf())
    }


}