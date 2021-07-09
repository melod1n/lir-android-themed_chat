package com.android.lir.screens.main.thematic

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.android.lir.R
import com.android.lir.activity.ShowPhotoActivity
import com.android.lir.base.BaseFullScreenDialog
import com.android.lir.base.adapter.BaseAdapter
import com.android.lir.base.adapter.BindingHolder
import com.android.lir.common.AppGlobal
import com.android.lir.databinding.FragmentThematicChatBinding
import com.android.lir.databinding.ItemCommentBinding
import com.android.lir.dataclases.ThematicChat
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.dataclases.ThematicComment
import com.android.lir.utils.AndroidUtils
import com.android.lir.utils.AppExtensions.compressBitmap
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.random.Random


@AndroidEntryPoint
class ThematicChatCommentsDialog : BaseFullScreenDialog(R.layout.fragment_thematic_chat) {

    private val viewModel: ThematicChatCommentsVM by viewModels()
    private val binding: FragmentThematicChatBinding by viewBinding()

    private var info: ThematicChatInfo? = null
    private var chat: ThematicChat? = null

    private lateinit var photosAdapter: ThematicChatPicturesAdapter
    private lateinit var adapter: CommentsAdapter

    private lateinit var placeholder: Drawable

    private var pickType: PickType? = null
    private var lastPhoto: Bitmap? = null

    private val memberState = MutableLiveData<MemberState>()

    enum class PickType { COMMENT, CHAT }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d("PICK_IMAGE", "RESULT: $uri")

            val imageStream = uri?.let { requireActivity().contentResolver.openInputStream(it) }
                ?: return@registerForActivityResult
            val drawable = BitmapDrawable.createFromStream(imageStream, "")

            lifecycleScope.launch { compressImage(drawable) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.ic_user_placeholder)!!

        info = requireArguments().getParcelable("info") as? ThematicChatInfo

        var coordinates = requireArguments().getString("coordinates")
        if (coordinates.isNullOrBlank()) coordinates = info?.coordinates ?: ""

        binding.toolbar.menu.findItem(R.id.edit).isVisible =
            info?.creatorId == AppGlobal.shared.dataManager.userId

        binding.toolbar.menu.forEach { it.icon?.setTint(Color.DKGRAY) }

        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.edit) {
                findNavController().navigate(
                    R.id.toThematicChatCreateDialog,
                    bundleOf(
                        "coordinates" to coordinates,
                        "isCreate" to false,
                        "type" to requireArguments().getString("type"),
                        "chat" to chat,
                        "info" to info
                    )
                )
                return@setOnMenuItemClickListener true
            }

            false
        }

        info?.let {
            binding.title.text = it.title
            binding.address.text = it.address
            binding.address.isSelected = true
            binding.phone.text = it.phone
            binding.description.text = it.description
        }

        val rating = Random.nextDouble(0.0, 5.0).toFloat().coerceIn(0.0f, 5.0f)
        binding.rating.rating = rating
        binding.ratingText.text = rating.toString().substring(0, 4)

        info?.let { fillMembers(it.usersCount, it.membersCount) }

        memberState.observe({ lifecycleRegistry }) { s ->
            when (s) {
                MemberState.FULL -> {
                    binding.accept.backgroundTintList = ColorStateList.valueOf(0xffEFEFF4.toInt())
                    binding.accept.text = "Нет мест"
                    binding.accept.isClickable = false
                    binding.accept.setTextColor(0xff8E8E93.toInt())

                    info?.let { fillMembers(it.usersCount, it.membersCount) }
                }
                MemberState.NOT_IN -> {
                    binding.accept.backgroundTintList = ColorStateList.valueOf(0xff4CD964.toInt())
                    binding.accept.text = "Принять участие"

                    info?.let { fillMembers(it.usersCount, it.membersCount) }
                }
                else -> {
                    binding.accept.backgroundTintList = ColorStateList.valueOf(0xff4CD964.toInt())
                    binding.accept.text = "Ты участник"
                    binding.accept.isClickable = false

                    info?.let { fillMembers(it.usersCount, it.membersCount) }
                }
            }
        }

        checkMembers()

        binding.accept.setOnClickListener {
            if (AppGlobal.shared.dataManager.userId == info?.creatorId) {
                Snackbar.make(
                    requireView(),
                    "Создатель чата не может выполнить это действие",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (memberState.value == MemberState.IN ||
                memberState.value == MemberState.FULL
            ) return@setOnClickListener

            viewModel.acceptChatInvite(info?.chatId ?: -1)
        }

        binding.userIcon.load(AppGlobal.kittens.random()) {
            crossfade(100)
            size(200, 200)
            error(placeholder)
            placeholder(placeholder)
        }

        binding.send.setOnClickListener {
            val message = binding.commentMessage.text.toString().trim()
            if (message.isEmpty()) return@setOnClickListener

            binding.commentMessage.text = null

            adapter.add(
                0,
                ThematicComment(
                    messageId = -1,
                    chatId = info?.chatId ?: -1,
                    text = message,
                    userId = AppGlobal.shared.dataManager.userId,
                )
            )
            adapter.notifyDataSetChanged()

            viewModel.sendMessage(info?.chatId ?: -1, message)
        }

        adapter = CommentsAdapter(requireContext(), arrayListOf())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach {
                when (it) {
                    is MessageSent -> addMessage(it.comment, lastPhoto)
                    is MessageError -> removeFirstMessage()
                    is LoadThematicChatEvent -> fillInfo(it.response)
                    is SuccessAddPhotoToChat -> addPhoto()
                    is ErrorAddPhotoToChat -> lastPhoto = null
                    is AddUserToChat -> {
                        info?.let { info ->
                            info.membersCount++
                            info.isMember = 1
                        }
                        checkMembers()
                    }
                }
            }.collect()
        }

        photosAdapter = ThematicChatPicturesAdapter(requireContext(), arrayListOf())
        binding.photos.adapter = photosAdapter

        val ad1 = ContextCompat.getDrawable(requireContext(), R.drawable.ad1)
        val ad2 = ContextCompat.getDrawable(requireContext(), R.drawable.ad2)

        binding.ad1.load(ad1) { size(350, 200) }

        binding.ad2.load(ad2) { size(350, 200) }

        binding.icon1.load(AppGlobal.kittens.random()) {
            error(ColorDrawable(Color.RED))
            crossfade(100)
            transformations(CircleCropTransformation())
        }

        binding.icon2.load(AppGlobal.kittens.random()) {
            error(ColorDrawable(Color.RED))
            crossfade(100)
            transformations(CircleCropTransformation())
        }

        binding.icon3.load(AppGlobal.kittens.random()) {
            error(ColorDrawable(Color.RED))
            crossfade(100)
            transformations(CircleCropTransformation())
        }

        binding.attachPhoto.setOnClickListener {
            pickType = PickType.COMMENT
            pickPhoto()
        }

        binding.addPhotos.setOnClickListener {
            pickType = PickType.CHAT
            pickPhoto()
        }

        loadChat()
    }

    private fun checkMembers() {
        memberState.value = when {
            info?.membersCount == info?.usersCount -> MemberState.FULL
            info?.isMember == 1 -> MemberState.IN
            else -> MemberState.NOT_IN
        }
    }

    private fun fillMembers(usersCount: Int, membersCount: Int) {
        binding.progress.max = usersCount
        binding.progress.progress = membersCount

        binding.left.text = "%d/%d мест".format(usersCount - membersCount, usersCount)
    }

    enum class MemberState { IN, NOT_IN, FULL }

    private fun addPhoto() {
        lastPhoto?.let {
            photosAdapter.add(it.toDrawable(resources) as Drawable to null)
            photosAdapter.notifyDataSetChanged()
        }
    }

    private fun pickPhoto() {
        getContent.launch("image/*")
    }

    private fun fillInfo(chat: ThematicChat) {
        this.chat = chat
        this.info = chat.info

        info?.let { info ->
            binding.title.text = info.title
            binding.address.text = info.address
            binding.address.isSelected = true
            binding.phone.text = info.phone

            val icon = ContextCompat.getDrawable(
                requireContext(),
                AppGlobal.thematicChatAvatars[info.imageIndex]
            )

            binding.icon.setImageDrawable(icon)

            binding.creatorPhoto.load(info.creatorPhoto ?: "") {
                crossfade(100)
                size(200, 200)
                error(placeholder)
                placeholder(placeholder)
            }

            binding.creatorName.text = info.creatorName

            fillMembers(info.usersCount, info.membersCount)
            checkMembers()
        }

        val newItems = arrayListOf<Pair<Drawable?, String?>>()
        chat.images.forEach { newItems.add(null to it) }
        photosAdapter.updateValues(newItems)
        photosAdapter.notifyDataSetChanged()

        val comments = chat.comments

        adapter.addAll(comments)
        adapter.notifyDataSetChanged()
    }

    private fun loadChat() {
        viewModel.loadThematicChat(info?.chatId ?: -1)
    }

    private fun removeFirstMessage() {
        adapter.removeAt(0)
        adapter.notifyDataSetChanged()
    }

    private fun addMessage(comment: ThematicComment, image: Bitmap? = null) {
        if (image != null) {
            pickType = null
            image.let { viewModel.uploadCommentImage(id, it) }
            return
        }

        adapter[0] = comment
        adapter.notifyDataSetChanged()
    }

    private suspend fun compressImage(image: Drawable) {
        Snackbar.make(requireView(), "Обработка", Snackbar.LENGTH_SHORT).show()

        withContext(Dispatchers.Default) {
            val result = AndroidUtils.scaleBitmap(image.toBitmap().compressBitmap())
            lastPhoto = result

            withContext(Dispatchers.Main) {
                Snackbar.make(requireView(), "Готово", Snackbar.LENGTH_SHORT).show()

                if (pickType == PickType.COMMENT) {
                    val text = binding.commentMessage.text.toString().trim()
                    binding.commentMessage.text = null

                    adapter.add(
                        0,
                        ThematicComment(
                            -1, -1, text, -1, "", "", "", "", result
                        )
                    )
                    adapter.notifyDataSetChanged()

                    viewModel.sendMessage(chat?.info?.chatId ?: -1, text)
                } else {
                    viewModel.uploadImage(chat?.info?.chatId ?: -1, result)
                }

                pickType = null
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        findNavController().navigateUp()
        super.onDismiss(dialog)
    }
}

class CommentsAdapter(
    context: Context,
    values: ArrayList<ThematicComment>
) : BaseAdapter<ThematicComment, CommentsAdapter.Holder>(context, values) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCommentBinding.inflate(inflater, parent, false))

    inner class Holder(binding: ItemCommentBinding) : BindingHolder<ItemCommentBinding>(binding) {

        private val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_user_placeholder)

        override fun bind(position: Int) {
            val item = getItem(position)

            binding.message.text = item.text
            binding.name.text = item.userName
            binding.date.text = item.createdAt

            with(binding.attachmentPhoto) {
                isVisible = item.images.isNotEmpty() || item.photo != null
                if (isVisible && (item.photo != null || item.images.isNotEmpty())) {
                    if (item.photo == null) {
                        load(item.images[0]) { crossfade(100) }
                    } else {
                        setImageBitmap(item.photo)
                    }
                } else setImageDrawable(null)

                setOnClickListener {
                    var bitmapImage: ByteArray? = null
                    if (item.images.isEmpty() && item.photo != null) {
                        val baos = ByteArrayOutputStream()
                        drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos)
                        bitmapImage = baos.toByteArray()
                    }

                    val intent = Intent(context, ShowPhotoActivity::class.java)
                    if (item.images.isNotEmpty()) intent.putExtra("image", item.images[0])
                    intent.putExtra("bitmapImage", bitmapImage)

                    context.startActivity(intent)
                }
            }

            val photo = item.userPhoto ?: AppGlobal.kittens.random()

            binding.avatar.load(photo) {
                crossfade(100)
                size(200, 200)
                error(placeholder)
                placeholder(placeholder)
            }
        }

    }
}