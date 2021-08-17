package com.android.lir.screens.signin.otp

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.screens.main.MainContainer
import com.android.lir.utils.AppExtensions.hideKeyboard
import com.android.lir.utils.AppExtensions.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_otp.*

@AndroidEntryPoint
class OtpFragment : BaseVMFragment<OtpViewModel>(R.layout.fragment_otp) {

    override val viewModel: OtpViewModel by viewModels()

    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumber = arguments?.getString("phoneNumber") ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvNumber.text = "Введите 4 числа, отправленные на \n%s".format(phoneNumber)
        etFirst.requestFocus()
        activity?.showKeyboard()
        setListeners()
    }

    private fun setListeners() {
        etSecond.setOnFocusChangeListener { _, _ ->
            if (etFirst.text.toString().isEmpty()) etFirst.requestFocus()
        }
        etThird.setOnFocusChangeListener { _, _ ->
            when {
                etFirst.text.toString().isEmpty() -> etFirst.requestFocus()
                etSecond.text.toString().isEmpty() -> etSecond.requestFocus()
            }
        }
        etFourth.setOnFocusChangeListener { _, _ ->
            when {
                etFirst.text.toString().isEmpty() -> etFirst.requestFocus()
                etSecond.text.toString().isEmpty() -> etSecond.requestFocus()
                etThird.text.toString().isEmpty() -> etThird.requestFocus()
            }
        }
        etFirst.addTextChangedListener {
            if (it?.length == 1) {
                etFirst.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
                etSecond.requestFocus()
            } else {
                etFirst.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
            }
        }
        etSecond.addTextChangedListener {
            if (it?.length == 1) {
                etSecond.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
                etThird.requestFocus()
            } else {
                etSecond.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
            }
        }
        etThird.addTextChangedListener {
            if (it?.length == 1) {
                etThird.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
                etFourth.requestFocus()
            } else {
                etThird.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
            }
        }
        etFourth.addTextChangedListener {
            if (it?.length == 1) {
                etFourth.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_active_field, null)
                activity?.hideKeyboard()
                etFourth.clearFocus()
                rlEnterOtp.requestFocus()
                viewModel.checkSms(getCode())
            } else {
                etFirst.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
                etSecond.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
                etThird.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
                etFourth.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.shape_empty_field, null)
            }
        }

        etSecond.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                etFirst.requestFocus()
                etFirst.text = null
            }
            false
        }
        etThird.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                etSecond.requestFocus()
                etSecond.text = null
            }
            false
        }
        etFourth.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                etThird.requestFocus()
                etThird.text = null
            }
            false
        }
    }

    private fun getCode() =
        etFirst.text.toString() + etSecond.text.toString() + etThird.text.toString() + etFourth.text.toString()

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is GoToAuth -> goToMain()
        }
    }

    private fun goToMain() {
        activity?.hideKeyboard()

        when (val startId = findNavController().graph.startDestination) {
            R.id.settingsFragment -> findNavController().popBackStack(startId, false)
            else -> (parentFragment?.parentFragment as? MainContainer)?.reselectItems()
        }
    }
}
