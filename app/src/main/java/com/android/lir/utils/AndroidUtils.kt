package com.android.lir.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.android.lir.io.BytesOutputStream
import java.io.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AndroidUtils {

    suspend fun compressDrawable(source: Drawable, maxSize: Int = 1024): Bitmap {
        val bitmap = source.toBitmap()

        Log.d("BITMAP_COMPRESS", "beforeSize: ${bitmap.byteCount / 1024}KB")

        val destWidth = 600

        val origWidth = bitmap.width
        val origHeight = bitmap.height

        val destHeight: Int = origHeight / (origWidth / destWidth)
        val result = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false)

        Log.d("BITMAP_COMPRESS", "afterSize: ${result.byteCount / 1024}KB")


        return BitmapFactory.decodeStream(
            ByteArrayInputStream(
                compressBitmap(
                    result, KilobyteSize(maxSize)
                )
            )
        )
    }

    suspend fun scaleBitmap(source: Bitmap, maxSize: KilobyteSize = KilobyteSize(1024)) =
        suspendCoroutine<Bitmap> {
            var bitmap = source
            val origWidth = source.width
            val origHeight = source.height

            var byteCount = bitmap.byteCount

            val destWidth = 600

            while (bitmap.byteCount / 1024 > maxSize.value) {
                val destHeight = origHeight / (origWidth / destWidth)
                bitmap = Bitmap.createScaledBitmap(source, destWidth, destHeight, false)
            }

            byteCount = bitmap.byteCount

            it.resume(bitmap)
        }

    // TODO: 7/4/2021 learn
    suspend fun compressBitmap(image: Bitmap, maxSize: KilobyteSize) = suspendCoroutine<ByteArray> {
        Log.d("BITMAP_COMPRESS", "beforeSize: ${image.byteCount / 1024 / 8}KB")

        val baos = ByteArrayOutputStream()
        var options = 100

        image.compress(Bitmap.CompressFormat.JPEG, options, baos)

        while (baos.size() / 1024 > maxSize.value) {
            baos.reset()
            options -= 1
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
        }
        if (!image.isRecycled) {
            try {
                image.recycle()
            } catch (e: Exception) {
            }
        }

        Log.d("BITMAP_COMPRESS", "afterSize: ${baos.size() / 1024 / 8}KB")

        it.resume(baos.toByteArray())
    }

    suspend fun convertBitmapToBase64(bitmap: Bitmap) = suspendCoroutine<String> {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        val imageBytes = byteArrayOutputStream.toByteArray()

        it.resume(Base64.encodeToString(imageBytes, Base64.DEFAULT))
    }

    fun serialize(source: Any?): ByteArray? {
        try {
            val bos = BytesOutputStream()
            val out = ObjectOutputStream(bos)
            out.writeObject(source)
            out.close()
            return bos.byteArray
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deserialize(source: ByteArray?): Any? {
        if (source == null || source.isEmpty()) {
            return null
        }

        try {
            val bis = ByteArrayInputStream(source)
            val `in` = ObjectInputStream(bis)
            val o = `in`.readObject()
            `in`.close()
            return o
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}