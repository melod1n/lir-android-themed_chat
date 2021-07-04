package com.android.lir.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.annotation.UiThread
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object AndroidUtils {

    fun compressDrawable(source: Drawable): Bitmap {
        return BitmapFactory.decodeStream(
            ByteArrayInputStream(
                compressBitmap(
                    source.toBitmap(),
                    128
                )
            )
        )
    }

    // TODO: 7/4/2021 learn
    fun compressBitmap(image: Bitmap, maxSize: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        var options = 100
        image.compress(Bitmap.CompressFormat.JPEG, options, baos)

        while (baos.size() / 1024 > maxSize) {
            baos.reset()
            options -= 10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
        }
        if (!image.isRecycled) {
            try {
                image.recycle()
            } catch (e: Exception) {
            }
        }
        return baos.toByteArray()
    }

}