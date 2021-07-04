package com.android.lir

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExitDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.exit_app))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> activity?.finish() }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> findNavController().popBackStack() }.create()
}