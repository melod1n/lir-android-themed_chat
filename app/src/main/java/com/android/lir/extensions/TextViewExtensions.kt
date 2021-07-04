package com.android.lir.extensions

import android.text.InputFilter
import android.widget.TextView

object TextViewExtensions {

    @JvmOverloads
    fun TextView.string(trim: Boolean = true): String {
        var str = text.toString()
        if (trim) str = str.trim()

        return str
    }

    fun TextView.isEmpty() = toString().trim().isEmpty()

    fun TextView.setMaxLength(length: Int) {
        filters = Array<InputFilter>(1) { InputFilter.LengthFilter(length) }
    }

}