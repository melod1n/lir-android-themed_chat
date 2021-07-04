package com.android.lir.screens.main.subscribe

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.viewModels
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.manager.IClientManagerListener
import com.android.lir.manager.VoxCallManager
import com.android.lir.manager.VoxClientManager
import com.android.lir.utils.AppExtensions.hideKeyboard
import com.voximplant.sdk.Voximplant
import com.voximplant.sdk.call.CallException
import com.voximplant.sdk.call.CallSettings
import com.voximplant.sdk.call.VideoFlags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionsFragment : BaseVMFragment<SubscriptionsViewModel>(R.layout.fragment_subscriptions), IClientManagerListener {

    override val viewModel: SubscriptionsViewModel by viewModels()

    @Inject
    lateinit var mCallManager: VoxCallManager
    @Inject
    lateinit var mClientManager: VoxClientManager
    private var mCallWaitingPermissions: CallDescriptor? = null
    private var mPermissionRequestedMode: Int = PERMISSION_NOT_REQUESTED
    private val mActiveCalls =
        HashMap<String, String>()

    companion object {
        private const val PERMISSION_NOT_REQUESTED = 1
        private const val PERMISSION_REQUESTED_AUDIO = 2
        private const val PERMISSION_REQUESTED_VIDEO = 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioCallButton.setOnClickListener{
            makeCall(callTo.text.toString(), false)
            activity?.hideKeyboard()
        }
        audioCallButton.setOnTouchListener{ _, event ->
            changeButton(audioCallButton, event.action)
            false
        }

        mClientManager.login("Lirchat", "Dfprjgtgrf01)))")
        start()
        val callSettings = CallSettings()
        callSettings.videoFlags = VideoFlags(false, false)

        mClientManager.client?.call("+79788499987", callSettings)?.let {
            try { it.start()
            } catch (callException: CallException) {
                callException.printStackTrace()
            }
        }
    }

    private fun makeCall(user: String?, withVideo: Boolean) {
        if (user == null || user.isEmpty()) {
            notifyInvalidCallUser()
            return
        }
        val callId: String = mCallManager.createCall(user, VideoFlags(withVideo, withVideo))
        if (checkPermissionsGrantedForCall(withVideo)) {
            startCallFragment(callId, withVideo, user, false)
        } else {
            mCallWaitingPermissions = CallDescriptor(
                callId,
                withVideo,
                user,
                false
            )
        }
    }
//
    private fun startCallFragment(callId: String, withVideo: Boolean, user: String, isIncoming: Boolean) {
        mActiveCalls[callId] = user
        enableNewCallControls(false)
    }

    //     public void startCallFragment(String callId, boolean withVideo, String user, boolean isIncoming) {
    //
    // //        addCallToNavigationMenu(callId, user);
    // //        CallFragment callFragment = CallFragment.newInstance(callId, isIncoming, withVideo);
    // //        mFragmentTransactionHelper.addFragment(callFragment, callId, R.id.callsContainer);
    //        enableNewCallControls(false);
    //    }

    private fun notifyInvalidCallUser() {
        //        callTo.error = getString(R.string.error_field_required)
        callTo.requestFocus()
    }

    private fun enableNewCallControls(enable: Boolean) {
        activity?.runOnUiThread(
            Runnable {
                callTo.isEnabled = enable
                audioCallButton.isEnabled = enable
                //            mVideoCallButton.setEnabled(enable)
            }
        )
    }

    private fun changeButton(button: ImageButton, action: Int) {
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                button.setColorFilter(resources.getColor(R.color.white, null))
                button.background = resources.getDrawable(R.drawable.button_image_active, null)
            }
            MotionEvent.ACTION_UP -> {
                button.setColorFilter(resources.getColor(R.color.purple, null))
                button.background = resources.getDrawable(R.drawable.button_image_passive, null)
            }
        }
    }

    private fun start() {
        mClientManager.addListener(this)
    }

    private fun checkPermissionsGrantedForCall(isVideoCall: Boolean): Boolean {
        val missingPermissions =
            Voximplant.getMissingPermissions(
                context,
                isVideoCall
            ) as ArrayList<String>
        return if (missingPermissions.size == 0) {
            true
        } else {
            activity?.parent?.let {
                ActivityCompat.requestPermissions(
                    it,
                    missingPermissions.toTypedArray(),
                    PermissionChecker.PERMISSION_GRANTED
                )
            }
            mPermissionRequestedMode =
                if (isVideoCall) PERMISSION_REQUESTED_VIDEO else PERMISSION_REQUESTED_AUDIO
            false
        }
    }
}

private class CallDescriptor constructor(
    val callId: String,
    val isVideo: Boolean,
    val user: String,
    val isIncoming: Boolean
)
