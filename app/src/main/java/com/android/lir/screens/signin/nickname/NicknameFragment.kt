package com.android.lir.screens.signin.nickname

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.utils.AppExtensions.hideKeyboard
import com.android.lir.utils.AppExtensions.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_nikname.*

@AndroidEntryPoint
class NicknameFragment : Fragment(R.layout.fragment_create_nikname) {

    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phone = arguments?.getString("phoneNumber").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
    }

    private fun setListeners() {
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etName.hint = ""
                rlNameEnter.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
            }
        }
        etName.addTextChangedListener { text ->
            if (text?.length!! > 2) {
                ivNext.visibility = View.VISIBLE
                rlNameEnter.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
            } else {
                ivNext.visibility = View.GONE
            }
            etName.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    openPhoto()
                    true
                } else false
            }
            ivNext.setOnClickListener {
                openPhoto()
            }
        }
    }

    private fun initView() {
        etName.requestFocus()
        activity?.showKeyboard()
    }

    private fun openPhoto() {
        activity?.hideKeyboard()
        etName.clearFocus()
        rlNameEnter.requestFocus()
        goToPhoto()
    }

    private fun goToPhoto() {
        findNavController().navigate(R.id.toPhoto, bundleOf(
            "phoneNumber" to  phone,
            "userName" to etName.text.toString()))
    }
}
