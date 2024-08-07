package com.android.lir.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

abstract class BaseLifecycleDialogFragment : DialogFragment, LifecycleOwner {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    protected lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onStart() {
        super.onStart()

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

}