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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
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

    private var isCommercial = false

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

        isCommercial = requireArguments().getBoolean("isCommercial", false)
        isCreating = requireArguments().getBoolean("isCreate", true)

        if (isCommercial) binding.toolbar.title = "БИЗНЕС"

        chat = requireArguments().getParcelable("chat") as? ThematicChat
        info = requireArguments().getParcelable("info") as? ThematicChatInfo

        var coordinates = requireArguments().getString("coordinates")
        if (coordinates.isNullOrBlank()) coordinates = info?.coordinates ?: ""

        isEditing = info?.creatorId == AppGlobal.shared.dataManager.userId && !isCreating

        binding.address.setText(viewModel.loadAddress(requireContext(), coordinates))

        binding.usersCount.isVisible = !isCommercial
        binding.usersCountTitle.isVisible = !isCommercial
        binding.restrictComments.isVisible = isCommercial

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
            if (index == -1) return@setFragmentResultListener
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

        binding.create.setOnClickListener { createThematicChat(imageIndex, coordinates) }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach {
                when (it) {
                    is ErrorAddPhotoToChat -> deleteLastPicture(it.error)
                    is SuccessAddPhotoToChat -> showSuccessUploadToast()
                    is ChatCreatedEvent -> loadPictures(it.info)
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

    private fun createThematicChat(imageIndex: Int, coordinates: String) {
        val title = binding.title.text.toString().trim()
        val description = binding.description.text.toString().trim()
        val phone = binding.phone.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val usersCountString = binding.usersCount.text.toString().trim()

        if (!checkRequiredFields(
                imageIndex,
                title,
                description,
                phone,
                address,
                usersCountString
            )
        ) {
            Snackbar.make(
                requireView(),
                "Заполните обязательные поля" + if (imageIndex > -1) "" else " и выберите аватарку чата",
                5000
            ).show()
            return
        }

        viewModel.createThematicChat(
            title,
            description,
            phone,
            address,
            imageIndex,
            coordinates,
            usersCountString.toIntOrNull() ?: -1
        )
    }

    private fun checkRequiredFields(
        imageIndex: Int,
        title: String,
        description: String,
        phone: String,
        address: String,
        usersCountString: String
    ): Boolean {
        val errorString = "Обязательное поле"
        var isOK = true

        if (imageIndex == -1) {
            isOK = false
        }

        if (title.isEmpty()) {
            isOK = false
            binding.title.error = errorString
        }

        if (description.isEmpty()) {
            isOK = false
            binding.description.error = errorString
        }

        if (phone.isEmpty()) {
            isOK = false
            binding.phone.error = errorString
        }

        if (address.isEmpty()) {
            isOK = false
            binding.address.error = errorString
        }

        if (!isCommercial && usersCountString.isEmpty()) {
            isOK = false
            binding.usersCount.error = errorString
        }

        return isOK
    }

    private suspend fun loadPictures(info: ThematicChatInfo) {
        val values = adapter.values.value ?: listOf()

        remainPictures = values.count()

        if (remainPictures == 0) {
            dismiss()
            findNavController().navigate(
                R.id.toThematicChatCommentsDialog,
                bundleOf(
                    "info" to info,
                    "coordinates" to info.coordinates
                )
            )
            return
        }

        Toast.makeText(requireContext(), "Идёт обработка фотографий", Toast.LENGTH_LONG).show()

        withContext(Dispatchers.Default) {
            values.forEach {
                it.first?.let { drawable ->
                    val bitmap = AndroidUtils.compressDrawable(drawable)
                    viewModel.uploadImage(info.chatId, bitmap)
                }
            }
        }
    }

    private fun deleteLastPicture(error: String) {
        adapter.values.value?.last()?.let {
            adapter.remove(it)
            adapter.notifyDataSetChanged()

            Toast.makeText(requireContext(), "Произошла ошибка", Toast.LENGTH_LONG).show()
        }

    }

    private suspend fun showSuccessUploadToast() {
        remainPictures--

        if (remainPictures == 0) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Фотографии загружены", Toast.LENGTH_LONG).show()

                with(findNavController()) {
                    navigateUp()
                    navigate(R.id.toThematicChatCommentsDialog, requireArguments())
                }
            }
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
                binding.usersCount.setText(it.usersCount.toString())

                binding.picture.setImageResource(AppGlobal.thematicChatAvatars[it.imageIndex])
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
        findNavController().navigateUp()
        setFragmentResult("update", bundleOf())
    }


}