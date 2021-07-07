package com.android.lir.screens.signin.photo

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.utils.AppExtensions.compressBitmap
import com.android.lir.utils.AppExtensions.toBase64
import com.android.lir.utils.AppExtensions.toBitMap
import com.github.florent37.runtimepermission.kotlin.askPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_load_photo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PhotoFragment : BaseVMFragment<PhotoViewModel>(R.layout.fragment_load_photo) {

    override val viewModel: PhotoViewModel by viewModels()

    private lateinit var phone: String
    private lateinit var nickName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener("photo_result") { _, bundle ->
            when (bundle.getInt("type")) {
                1 -> hasCameraPermission()
                2 -> hasStoragePermission()
            }
        }
        phone = arguments?.getString("phoneNumber") ?: ""
        nickName = arguments?.getString("userName") ?: ""
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.image.onEach { onBitMapChanged(it) }.collect()
        }
        tvNext.setOnClickListener {
            lifecycleScope.launch {
                val image = viewModel.image.value?.compressBitmap()?.toBase64()
                if (image == null) {
                    withContext(Dispatchers.Main) { showInfoDialog(null, "Что-то пошло не так") }
                } else {
                    viewModel.regUser(phone, nickName, image)
                }
            }

        }
        ivAddPhoto.setOnClickListener { showLoadImageDialog() }
    }

    private fun showLoadImageDialog() {
        findNavController().navigate(R.id.toLoadDialog)
    }

    private fun cameraTask() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(cameraIntent, 11)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun onBitMapChanged(bm: Bitmap?) {
        if (bm != null) ivAddPhoto.setImageBitmap(bm)
        tvNext.isVisible = bm != null
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        context?.toBitMap(it)?.let { bm ->
            viewModel.saveBitMap(bm)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 11) {
            (data?.extras?.get("data") as? Bitmap)?.let {
                viewModel.saveBitMap(it)
            }
        }
    }


    private fun hasCameraPermission() {
        askPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            cameraTask()
        }.onDeclined { }
    }

    private fun hasStoragePermission() {
        askPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            openGallery()
        }.onDeclined { }
    }

    private fun openGallery() {
        pickImage.launch("image/*")
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is PopToPhone -> popToPhone()
        }
    }

    private fun popToPhone() {
        findNavController().popBackStack(R.id.phoneFragment, false)
    }
}
