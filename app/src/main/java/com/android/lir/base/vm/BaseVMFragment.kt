package com.android.lir.base.vm

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.lir.R
import com.android.lir.utils.AppExtensions.alertDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

abstract class BaseVMFragment<VM : BaseVM>(@LayoutRes layout: Int) : Fragment(layout) {

    protected abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach { onEvent(it) }.collect()
        }
    }

    protected open fun onEvent(event: Event) {
        when (event) {
            is ShowInfoDialogEvent -> showInfoDialog(
                event.title,
                event.message,
                event.positiveBtn,
                event.negativeBtn
            )
        }
    }

    protected fun showInfoDialog(
        title: String?,
        message: String,
        positiveBtnText: String? = null,
        negativeBtn: String? = null
    ) {
        activity?.alertDialog {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveBtnText ?: getString(R.string.ok)) { _, _ -> }
            setNegativeButton(negativeBtn) { _, _ -> }
        }
    }

    protected fun hideKeyboard() {
        activity?.window?.decorView?.let {
            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                it.windowToken,
                0
            )
        }
    }
}