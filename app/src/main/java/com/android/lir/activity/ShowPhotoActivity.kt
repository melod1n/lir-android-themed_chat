package com.android.lir.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.ViewGroup
import coil.load
import com.android.lir.base.BaseActivity
import com.github.chrisbanes.photoview.PhotoView


class ShowPhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoView = PhotoView(this)
        setContentView(
            photoView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val bitmapImage = intent.getByteArrayExtra("bitmapImage")
        val imageUrl = intent.getStringExtra("image")

        if (bitmapImage == null) {
            photoView.load(imageUrl) { crossfade(100) }
        } else {
            val bitmap = BitmapFactory.decodeByteArray(bitmapImage, 0, bitmapImage.size)
            photoView.setImageBitmap(bitmap)
        }
    }

}