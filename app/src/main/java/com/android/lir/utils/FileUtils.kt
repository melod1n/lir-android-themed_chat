package com.android.lir.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    fun getFilePathFromUri(uri: Uri, context: Context): Uri {
        val fileName = getFileName(uri, context)
        val file = File(context.externalCacheDir, fileName)
        file.createNewFile()
        FileOutputStream(file).use { outputStream ->
            context.contentResolver?.openInputStream(uri).use { inputStream ->
                copyFile(inputStream, outputStream)
                outputStream.flush()
            }
        }
        return Uri.fromFile(file)
    }

    private fun copyFile(`in`: InputStream?, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int?
        while (`in`?.read(buffer).also { read = it!! } != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }

    fun getFileName(uri: Uri, context: Context): String {
        var fileName = getFileNameFromCursor(uri, context)
        if (fileName == null) {
            val fileExtension = getFileExtension(uri, context)
            fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
        } else if (!fileName.contains(".")) {
            val fileExtension = getFileExtension(uri, context)
            fileName = "$fileName.$fileExtension"
        }
        return fileName
    }

    fun getFileExtension(uri: Uri, context: Context): String? {
        val fileType = context.contentResolver?.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    fun getFileNameFromCursor(uri: Uri, context: Context): String? {
        val fileCursor = context.contentResolver?.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        var fileName: String? = null
        if (fileCursor != null && fileCursor.moveToFirst()) {
            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex)
            }
        }

        fileCursor?.close()
        return fileName
    }

}