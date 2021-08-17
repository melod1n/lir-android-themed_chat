package com.android.lir.screens.main.contacts.chatdetail

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
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
import com.android.lir.utils.AppExtensions.toBitmap
import com.android.lir.utils.FileUtils
import com.bumptech.glide.Glide
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.chat_detail_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@AndroidEntryPoint
class ChatDetailFragment :
    BaseVMFragment<ChatDetailVM>(com.android.lir.R.layout.chat_detail_fragment) {

    lateinit var adapter: ChatDetailAdapterNew

    override val viewModel: ChatDetailVM by viewModels()

    private val binding: ChatDetailFragmentBinding by viewBinding()

    private var progressDialog: AlertDialog? = null

    var chatId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = arguments?.getInt("chat_id") ?: 0
    }

    private val picker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (it == null || it.isEmpty()) return@registerForActivityResult
        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.Default) {
                if (it.size == 1) {
                    listOf(
                        requireContext().toBitmap(it[0])?.compressBitmap() ?: return@withContext
                    ).let(viewModel::sendMessage)
                } else {
                    val images = mutableListOf<Bitmap>()
                    it.forEach { uri ->
                        requireContext().toBitmap(uri)?.compressBitmap()?.let(images::add)
                    }

                    images.let(viewModel::sendMessage)
                }
            }

        }
    }

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) return@registerForActivityResult

        Log.d("PICK_FILE", "uri: $it: ")
        getFileFromUri(it)
    }

    private fun getFileFromUri(uri: Uri) = lifecycleScope.launch {
        try {
            val path = FileUtils.getFilePathFromUri(uri, requireContext()).path

            if (path == null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Ошибка")
                    .setMessage("Что-то пошло не так")
                    .setPositiveButton("ОК", null)
                    .show()
                return@launch
            }

            val file = File(path)

            Log.d(
                "PICK_FILE",
                "file { name: ${file.name}; ext: ${file.extension}; size: ${file.length()} } "
            )

            if (file.length() / 1024 / 1024 > 41) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Внимание")
                    .setMessage("Выберите для загрузки файл весом не более 40 МБайт")
                    .setPositiveButton("ОК", null)
                    .show()
                return@launch
            }

            viewModel.sendMessage(file = file)
//            val list = adapter.currentList
//            list.add(PrivateChatItem(PrivateMessage(-1, "", null, AppGlobal.shared.dataManager.userId, )))
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Ошибка")
                    .setMessage(Log.getStackTraceString(e))
                    .setPositiveButton("ОК", null)
                    .show()
            }
        }
    }

    private val cameraPicker =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            if (it == null) return@registerForActivityResult
            lifecycleScope.launchWhenStarted {
                listOf(it.compressBitmap()).let(viewModel::sendMessage)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.partnerId ?: run { viewModel.setPartnerId(arguments?.getInt("chat_id") ?: 0) }

        binding.attach.setOnClickListener { showAttachmentDialog() }

        btnSend.setOnClickListener { viewModel.sendMessage() }

        back.setOnClickListener { findNavController().popBackStack() }

        adapter = ChatDetailAdapterNew(this)
        (rvMessages.layoutManager as LinearLayoutManager).reverseLayout = true
        rvMessages.adapter = adapter

        etMessage.doAfterTextChanged { viewModel.onMessageChanged(it?.toString()) }

        viewModel.messages.observe(viewLifecycleOwner) {
            val oldSize = adapter.itemCount

            adapter.submitList(it)
            adapter.notifyDataSetChanged()

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                delay(50)

                if (oldSize != adapter.itemCount)
                    (rvMessages.layoutManager as? LinearLayoutManager)?.scrollToPosition(0)
            }
        }

        viewModel.currentMessage.observe(viewLifecycleOwner) {
            btnSend.isEnabled = !it.isNullOrEmpty()
        }

        setFragmentResultListener("pickLocation") { _, bundle ->
            val coordinates = bundle.getString("coordinates") ?: return@setFragmentResultListener
            viewModel.sendMessage(geocode = coordinates)
        }

        setFragmentResultListener("pickType") { _, bundle ->
            val pickType = bundle.getString("type", "")
            if (pickType.isEmpty()) return@setFragmentResultListener

            when (pickType) {
                "camera" -> {
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

                }
                "gallery" -> {
                    picker.launch("image/*")
                }
                "file" -> {
                    if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                        return@setFragmentResultListener
                    }

                    askPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                        filePicker.launch("*/*")
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
                }
                "geolocation" -> {
                    findNavController().navigate(
                        R.id.toPickLocationFragment,
                        bundleOf()
                    )
                }
            }

        }
    }

    fun showLocation(coordinates: String) {
        findNavController().navigate(
            R.id.toPickLocationFragment, bundleOf(
                "alreadyPicked" to true,
                "coordinates" to coordinates
            )
        )
    }

    fun downloadFile(fileUrl: String) {
        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            Toast.makeText(requireContext(), "Загрузка началась...", Toast.LENGTH_SHORT).show()
            val split = fileUrl.split('.')
            val filename = "${System.currentTimeMillis()}.${split.last()}"

            val destination = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val newFile = File(destination, filename)

            val request = DownloadManager.Request(Uri.parse(fileUrl))
            request.setDescription("Загрузка файла")
            request.setTitle("Имя файла: $filename")
            request.setDestinationUri(Uri.fromFile(newFile))

            val manager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            requireContext().registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        Log.d("DOWNLOADER", "onReceive: complete")

                        Toast.makeText(
                            requireContext(),
                            "Файл успешно сохранён в /Android/data/com.android.lir",
                            Toast.LENGTH_LONG
                        ).show()

                        requireContext().unregisterReceiver(this)
                    }
                },
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
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
    }

    private fun showAttachmentDialog() {
        findNavController().navigate(R.id.toAttachDialog)
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(requireContext())

        val progressIndicator = LinearProgressIndicator(requireContext())
        progressIndicator.isIndeterminate = true

        builder.setCancelable(false)
        builder.setTitle("Подождите")
        builder.setMessage("Обработка файлов...")
        builder.setView(progressIndicator)

        progressDialog = builder.show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is ShowProgressDialog -> showProgressDialog()
            is DismissProgressDialog -> dismissProgressDialog()

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
