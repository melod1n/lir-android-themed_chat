package com.android.lir.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View.OnKeyListener
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import com.android.lir.R
import com.android.lir.extensions.TextViewExtensions.setMaxLength
import com.google.android.material.textfield.TextInputEditText

class PhoneNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var isRemovingNumber: Boolean = false

    private var customOnKeyListener: OnKeyListener? = null

    private val onKeyListener = OnKeyListener { v, keyCode, event ->
        isRemovingNumber = keyCode == KeyEvent.KEYCODE_DEL
        customOnKeyListener?.onKey(v, keyCode, event)
        false
    }

    init {
        setMaxLength(18)

        imeOptions = EditorInfo.IME_ACTION_GO

        setOnKeyListener(onKeyListener)

        addTextChangedListener {
            with(it.toString().trim()) {
                if (isEmpty() || length == 1 || substring(0, 2) != "+7") {
                    setText("+7")
                    setSelection(2)
                } else {
                    if (isRemovingNumber) return@with

                    val text = text.toString()
                    val newText = when (length) {
                        3 -> text.substring(0, 2) + " (" + text.substring(2, 3)
                        7 -> "$text) "
                        12, 15 -> "$text-"
                        else -> ""
                    }

                    if (newText.isEmpty()) return@with

                    setText(newText)
                    setSelection(newText.length)
                }
            }
        }
    }

    override fun setOnKeyListener(l: OnKeyListener?) {
        if (l != onKeyListener) {
            customOnKeyListener = l
            return
        }
        super.setOnKeyListener(l)
    }

}