package com.android.lir.screens.main.contacts.chatdetail

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.databinding.ChatDetailFragmentBinding
import com.android.lir.utils.AppExtensions.compressBitmap
import com.android.lir.utils.AppExtensions.toBitMap
import com.bumptech.glide.Glide
import com.github.florent37.runtimepermission.kotlin.askPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_detail_fragment.*
import kotlinx.coroutines.delay
import java.io.File
import java.net.URI
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class ChatDetailFragment : BaseVMFragment<ChatDetailVM>(R.layout.chat_detail_fragment) {

    @Inject
    lateinit var adapter: ChatDetailAdapterNew

    override val viewModel: ChatDetailVM by viewModels()

    private val binding: ChatDetailFragmentBinding by viewBinding()

    private var pickPhoto = false

    var chatId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = arguments?.getInt("chat_id") ?: 0
    }

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) return@registerForActivityResult
        lifecycleScope.launchWhenStarted {

            if (pickPhoto) {
                requireContext().toBitMap(it)?.compressBitmap()?.let(viewModel::sendMessage)
            } else {
//                compressFile(it)
//                Log.d("PICK_FILE", "uri: $it: ")
            }
        }
    }

    private fun compressFile(uri: Uri) {
        val newUri = URI(uri.path).path
        val file = File(URI(uri.path))

        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
        if (!file.exists()) file.createNewFile()

        val zip = ZipOutputStream(CheckedOutputStream(file.outputStream(), CRC32()))

        zip.setLevel(9)
        zip.putNextEntry(ZipEntry(file.name))
        zip.write(file.readBytes())

        zip.flush()
        zip.close()
    }

    private val cameraPicker =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            if (it == null) return@registerForActivityResult
            lifecycleScope.launchWhenStarted {
                it.compressBitmap().let(viewModel::sendMessage)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.partnerId ?: run { viewModel.setPartnerId(arguments?.getInt("chat_id") ?: 0) }

        binding.attach.setOnClickListener { showAttachmentDialog() }

        btnSend.setOnClickListener { viewModel.sendMessage() }

        back.setOnClickListener { findNavController().popBackStack() }

        (rvMessages.layoutManager as LinearLayoutManager).reverseLayout = true
        rvMessages.adapter = adapter

        etMessage.doAfterTextChanged { viewModel.onMessageChanged(it?.toString()) }

        viewModel.messages.observe(viewLifecycleOwner) {
            val oldSize = adapter.itemCount

            adapter.submitList(it)

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                delay(50)

                if (oldSize != adapter.itemCount)
                    (rvMessages.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
            }
        }

        viewModel.currentMessage.observe(viewLifecycleOwner) {
            btnSend.isEnabled = !it.isNullOrEmpty()
        }

        setFragmentResultListener("pickType") { _, bundle ->
            val pickType = bundle.getInt("type", -1)
            if (pickType == -1) return@setFragmentResultListener

            if (pickType == 0) {
                askPermission(Manifest.permission.CAMERA) {
                    cameraPicker.launch()
                }.onDeclined { e ->
                    if (e.hasDenied()) {
                        AlertDialog.Builder(requireContext())
                            .setMessage(getString(R.string.permission_need))
                            .setPositiveButton("Ок") { _, _ -> e.askAgain() }
                            .show()
                    }

                    if (e.hasForeverDenied()) {
                        e.goToSettings()
                    }
                    return@onDeclined
                }

            } else if (pickType == 1) {
                pickPhoto = true
                picker.launch("image/*")
            } else {
                pickPhoto = false
                picker.launch("*/*")
            }

        }
    }

    private fun showAttachmentDialog() {
        findNavController().navigate(R.id.toAttachDialog)
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is ClearEvent -> etMessage.setText(null)
            is LogOutEvent -> {
                showInfoDialog(null, "Авторизуйтесь для отправки приватных сообщений")

                findNavController().popBackStack()
            }
            is PutPartnerInfoEvent -> {
                Glide.with(requireContext()).load(event.url).circleCrop().into(partnerPhoto)
                partnerName.text = event.name
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        instance = this
    }

    override fun onStop() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        instance = null
        super.onStop()
    }

    companion object {
        var instance: ChatDetailFragment? = null
    }
}
