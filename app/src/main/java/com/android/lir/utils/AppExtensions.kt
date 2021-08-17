package com.android.lir.utils

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Px
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4
import com.android.lir.dataclases.Chat
import com.android.lir.dataclases.Contact
import com.android.lir.dataclases.RegContact
import com.android.lir.dataclases.ThematicChatInfo
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object AppExtensions {

    fun check1Day(first: String?, second: String?, inputFormat: String = "yyyy-MM-dd"): Boolean {
        val str1 = first?.split(' ')?.first()
        val str2 = second?.split(' ')?.first()
        val simpleDateFormat = SimpleDateFormat(inputFormat)
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = simpleDateFormat.parse(str1 ?: "") ?: Date()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return calendar.time.time >= simpleDateFormat.parse(str2 ?: "")?.time ?: 0
        } catch (e: Exception) {
            false
        }
    }

    fun String?.formatDate(inputFormat: String, outputString: String): String? {
        return this?.let {
            val simpleDateFormat = SimpleDateFormat(inputFormat)
            val resultDateFormat = SimpleDateFormat(outputString)
            try {
                simpleDateFormat.parse(this)?.let { resultDateFormat.format(it) }
            } catch (e: Exception) {
                null
            }
        } ?: ""
    }

    fun Date.string(outputString: String = "yyyy-MM-dd HH:mm:ss"): String? {
        return this.let {
            val resultDateFormat = SimpleDateFormat(outputString)
            try {
                resultDateFormat.format(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun Context.alertDialog(builder: AlertDialog.Builder.() -> Unit = {}) = with(
        AlertDialog.Builder(
            this
        )
    ) {
        builder.invoke(this)
        create().show()
    }

    fun String.decodeFromBase64(): Bitmap? = try {
        val decodedString = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: Exception) {
        null
    }

    fun String.ifBlankMakeNull(): String? = if (this.isBlank()) null else this

    suspend fun Bitmap.compressBitmap() = suspendCoroutine<Bitmap> {
        val baos = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        if (baos.toByteArray().size / 1024 > 800) {
            baos.reset() //baosbaos
            this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }

        var isBm = ByteArrayInputStream(
            baos.toByteArray()
        )
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeStream(isBm, null, newOpts)
        newOpts.inJustDecodeBounds = false
        val w = newOpts.outWidth
        val h = newOpts.outHeight
        val hh = 1512
        val ww = 900
        var be = 1
        if (w > h && w > ww) {
            be = (newOpts.outWidth / ww)
        } else if (w < h && h > hh) {
            be = (newOpts.outHeight / hh)
        }
        if (be <= 0) be = 1
        newOpts.inSampleSize = be
        isBm = ByteArrayInputStream(baos.toByteArray())

        it.resume(BitmapFactory.decodeStream(isBm, null, newOpts)!!)
    }

    fun Context.toBitmap(uri: Uri?): Bitmap? = when {
        uri == null -> null
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                contentResolver,
                uri
            )
        )
        else -> MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }

    fun Bitmap.toBase64(): String? = try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        null
    }

    fun Context.getContactList(): List<Contact> {
        var myContacts = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null)
            ?.let {
                while (it.moveToNext()) {
                    val id: String? = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String? =
                        it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val photoUri: String? =
                        it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val numberList = mutableListOf<String?>()
                    if (it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val numberProjection =
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val pCur: Cursor? = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            numberProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        while (pCur?.moveToNext() == true)
                            numberList.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                        pCur?.close()
                    }
                    myContacts.add(Contact(id, name, numberList.filterNotNull(), photoUri))
                }
                it
            }.also { cursor ->
                cursor?.close()
                return myContacts.filter {
                    (it.name ?: it.numbers.firstOrNull { num -> num.isNotBlank() }) != null
                }.toMutableList()
            }
    }

    fun List<String>.onlyDigits(): List<String> = map { it.filter { char -> char.isDigit() } }

    fun List<Contact>?.unite(response: List<RegContact>?): List<Contact>? {
        val remoteNumbers =
            response?.mapNotNull { it.phone?.filter { char -> char.isDigit() } } ?: listOf()
        return this?.map {
            val intersect = it.numbers.onlyDigits().intersect(remoteNumbers)
            if (intersect.isNotEmpty()) {
                val currentUser = response?.firstOrNull { user ->
                    val phone =
                        if (user.phone?.first() == '8') "7${user.phone?.substring(1)}" else user.phone
                    phone == intersect.firstOrNull()
                } ?: return@map it
                it.serverId = currentUser.contact_id.toString()
                it.serverName = currentUser.name
                it.serverPhone = currentUser.phone
                it.serverPhoto = currentUser.photo
                it.isRegister = true
                it
            } else it
        }
    }

    fun Activity.hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun Activity.showKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.SHOW_FORCED,
            0
        )
    }

    fun String.chatId(): Int? = substringAfter("_").toIntOrNull()

    fun String.toChat(): Chat? = try {
        Gson().fromJson(this, Chat::class.java)
    } catch (e: Exception) {
//        e.printStackTrace()
        null
    }

    fun String.toThematicChat(): ThematicChatInfo? = try {
        Gson().fromJson(this, ThematicChatInfo::class.java)
    } catch (e: Exception) {
//        e.printStackTrace()
        null
    }
}

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}

fun Drawable.toBitmapWithBadge(
    @Px width: Int = intrinsicWidth,
    @Px height: Int = intrinsicHeight,
    count: Int
): Bitmap {
    val (oldLeft, oldTop, oldRight, oldBottom) = bounds
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(15, 15, width - 15, height - 15)
    val canvas = Canvas(bitmap)
    draw(canvas)
    val badge = when {
        count in 1..99 -> count.toString()
        count > 99 -> "99+"
        else -> null
    }
    if (badge != null) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val fm = Paint.FontMetrics()
        paint.getFontMetrics(fm)
        val r = RectF(
            width * 7f / 10 - paint.measureText(badge) / 2 - 15,
            height / 3.5f + fm.top - 10,
            width * 7f / 10 + paint.measureText(badge) / 2 + 15,
            height / 3.5f + fm.bottom + 10
        )
        canvas.drawRoundRect(r, 20f, 20f, paint)
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText(
            badge.toString(),
            width * 7f / 10 - paint.measureText(badge) / 2,
            height / 3.5f,
            paint
        )
    }
    setBounds(oldLeft - 15, oldTop - 15, oldRight + 15, oldBottom + 15)
    return bitmap
}