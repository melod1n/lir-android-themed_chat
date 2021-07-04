package com.android.lir.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    var errorSnackbar: Snackbar? = null

}