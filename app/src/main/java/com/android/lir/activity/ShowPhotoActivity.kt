package com.android.lir.activity

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.viewbinding.library.activity.viewBinding
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.load
import com.android.lir.R
import com.android.lir.base.BaseActivity
import com.android.lir.databinding.ActivityShowPhotoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ShowPhotoActivity : BaseActivity(R.layout.activity_show_photo) {

    private val binding: ActivityShowPhotoBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ShowPhotoTheme)
        super.onCreate(savedInstanceState)

        val bitmapImage = intent.getByteArrayExtra("bitmapImage")
        val imageUrl = intent.getStringExtra("image")

        if (bitmapImage == null) {
            binding.photoView.load(imageUrl) { crossfade(100) }
        } else {
            val bitmap = BitmapFactory.decodeByteArray(bitmapImage, 0, bitmapImage.size)
            binding.photoView.setImageBitmap(bitmap)
        }

        binding.toolbar.setNavigationIconTint(Color.WHITE)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.download) {
                savePhotoToStorage(binding.photoView.drawable.toBitmap())
                return@setOnMenuItemClickListener true
            }
            false
        }
    }

    private fun savePhotoToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Фотография успешно загружена", Toast.LENGTH_LONG).show()
        }
    }

}