package com.android.lir.screens.signin.phone

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.utils.AppExtensions.hideKeyboard
import com.android.lir.utils.AppExtensions.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in.*
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

@AndroidEntryPoint
class PhoneFragment : BaseVMFragment<PhoneViewModel>(R.layout.fragment_sign_in) {

    override val viewModel: PhoneViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor?.let {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        setListener()
        initView()
    }

    private fun initView() {
        etPhone.requestFocus()
        activity?.showKeyboard()
    }

    private fun setListener() {
        val formatWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER) // маска для серии и номера
        )
        formatWatcher.installOn(etPhone)
        etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etPhone.hint = ""
                if (etPhone.text.isNullOrEmpty()) {
                    etPhone.setText("+7 (")
                }
                rlPhoneEnter.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
            }
        }
        etPhone.addTextChangedListener { text ->
            if (text?.length == 18) {
                ivNext.visibility = View.VISIBLE
                etPhone.clearFocus()
                rlPhoneEnter.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_fill_field, null)
            } else {
                ivNext.visibility = View.GONE
                rlPhoneEnter.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
            }
            etPhone.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && etPhone.length() == 18) {
                    openOtp()
                    true
                } else false
            }
            ivNext.setOnClickListener {
                openOtp()
            }
        }
    }

    private fun openOtp() {
        activity?.hideKeyboard()
        etPhone.clearFocus()
        tvPhone.requestFocus()
        viewModel.checkUser(etPhone.text.toString().filter { it.isDigit() })
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when(event) {
            GoToCode -> goToCodeScreen()
            GoToRegistration -> goToRegistration()
        }
    }

    private fun goToRegistration() {
        findNavController().navigate(R.id.toNickName, bundleOf(
            "phoneNumber" to etPhone.text.toString().filter { it.isDigit() }
        ))
    }

    private fun goToCodeScreen() {
        findNavController().navigate(R.id.toOtp, bundleOf(
            "phoneNumber" to etPhone.text.toString().filter { it.isDigit() }
        ))
    }
}
