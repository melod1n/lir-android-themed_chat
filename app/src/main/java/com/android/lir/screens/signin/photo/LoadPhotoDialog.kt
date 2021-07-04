package com.android.lir.screens.signin.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_load_photo.*

@AndroidEntryPoint
class LoadPhotoDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.view_load_photo, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), R.style.Theme_Design_BottomSheetDialog)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivCancel.setOnClickListener { dialog?.cancel() }
        ivCamera.setOnClickListener {
            setFragmentResult("photo_result", bundleOf("type" to 1))
            findNavController().navigateUp()
        }
        tvCamera.setOnClickListener {
            setFragmentResult("photo_result", bundleOf("type" to 1))
            findNavController().navigateUp()
        }
        ivGallery.setOnClickListener {
            setFragmentResult("photo_result", bundleOf("type" to 2))
            findNavController().navigateUp()
        }
        tvGallery.setOnClickListener {
            setFragmentResult("photo_result", bundleOf("type" to 2))
            findNavController().navigateUp()
        }
    }
}
