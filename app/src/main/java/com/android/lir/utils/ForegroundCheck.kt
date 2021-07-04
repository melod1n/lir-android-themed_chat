package com.android.lir.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class ForegroundCheck : ActivityLifecycleCallbacks {
    var isForeground = false
        private set

    override fun onActivityResumed(activity: Activity) {
        isForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isForeground = false
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private var instance: ForegroundCheck? = null
        fun init(application: Application): ForegroundCheck? {
            if (instance == null) {
                instance = ForegroundCheck()
                application.registerActivityLifecycleCallbacks(instance)
            }
            return instance
        }

        fun get(): ForegroundCheck? {
            checkNotNull(instance) { "ForegroundCheck is not initialized" }
            return instance
        }
    }
}
