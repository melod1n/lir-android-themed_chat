package com.android.lir.common

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.lir.R
import kotlinx.android.synthetic.main.dialog_exit_confirm.view.*

class ExitConfirmDialog : DialogFragment() {
    private var mListener: OnDialogDenyResultListener? = null

    interface OnDialogDenyResultListener {
        fun onResultDialog(statusCode: Int)
    }

    fun setOnResultListener(listener: OnDialogDenyResultListener) {
        mListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_exit_confirm, null)
        val builder =
            AlertDialog.Builder(requireActivity())
        builder.setView(view)
        view.btn_dialog_exit_confirm_accept.setOnClickListener {
            dismiss()
            if (mListener != null) {
                mListener!!.onResultDialog(Activity.RESULT_OK)
            }
        }
        view.btn_dialog_exit_confirm_deny.setOnClickListener { dismiss() }
        return builder.create()
    }

    companion object {
        const val TAG = "ExitConfirmDialog"
        fun newInstance(): ExitConfirmDialog {
            return ExitConfirmDialog()
        }
    }
}
