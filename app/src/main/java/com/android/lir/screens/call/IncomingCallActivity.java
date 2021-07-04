package com.android.lir.screens.call;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.lir.MainActivity;
import com.android.lir.R;
import com.android.lir.manager.VoxCallManager;
import com.android.lir.utils.SharedPreferencesHelper;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.RejectMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.android.lir.utils.Constants.APP_TAG;
import static com.android.lir.utils.Constants.CALL_ANSWERED;
import static com.android.lir.utils.Constants.CALL_ID;
import static com.android.lir.utils.Constants.DISPLAY_NAME;
import static com.android.lir.utils.Constants.INCOMING_CALL_RESULT;
import static com.android.lir.utils.Constants.WITH_VIDEO;

@AndroidEntryPoint
public class IncomingCallActivity extends AppCompatActivity implements ICallListener {
    private boolean mIsAudioPermissionsGranted;
    private boolean mIsVideoPermissionsGranted;

    private WeakReference<ICall> mCall;
    private HashMap<String, String> mHeaders = null;

    @Inject
    VoxCallManager callManager;

    public void create(String callId) {
        if (callId != null && callManager != null) {
            ICall call = callManager.getCallById(callId);
            if (call != null) {
                mCall = new WeakReference<>(call);
                call.addCallListener(this);
            }
        } else {
            Log.e(APP_TAG, "IncomingCallPresenter: failed to get call by id");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_incoming_call);

        if (SharedPreferencesHelper.get().getBooleanFromPrefs(getString(R.string.pref_call_vibrate_enable_key))) {
            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(500);
            }
        }

        String callId = null;
        boolean isVideo = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            TextView callFrom = findViewById(R.id.incoming_call_from);
            callFrom.setText(extras.getString(DISPLAY_NAME));
            callId = extras.getString(CALL_ID);
            isVideo = extras.getBoolean(WITH_VIDEO);
        }

        create(callId);

        ImageButton answerWithAudio = findViewById(R.id.answer_call_button);
        answerWithAudio.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(answerWithAudio, event, false);
            return false;
        });
        answerWithAudio.setOnClickListener(view -> {
            if (permissionsGrantedForCall(false)) {
                answerCall(false);
            }
        });

        ImageButton answerWithVideo = findViewById(R.id.answer_with_video_button);
        answerWithVideo.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        answerWithVideo.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(answerWithVideo, event, false);
            return false;
        });
        answerWithVideo.setOnClickListener(view -> {
            if (permissionsGrantedForCall(true)) {
                answerCall(true);
            }
        });

        ImageButton reject = findViewById(R.id.decline_call_button);
        reject.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(reject, event, true);
            return false;
        });
        reject.setOnClickListener(view -> {
            rejectCall();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVideoCall() && mIsAudioPermissionsGranted && mIsVideoPermissionsGranted) {
            answerCall(true);
            finish();
        } else if (!isVideoCall() && mIsAudioPermissionsGranted) {
            answerCall(false);
            finish();
        }
    }

    private void changeButton(ImageButton button, MotionEvent event, boolean isRed) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(getResources().getColor(R.color.white));
                button.setBackground(isRed ? getResources().getDrawable(R.drawable.button_image_red_active) : getResources().getDrawable(R.drawable.button_image_active));
                break;
            case MotionEvent.ACTION_UP:
                button.setColorFilter(isRed ? getResources().getColor(R.color.colorRed) : getResources().getColor(R.color.purple));
                button.setBackground(isRed ? getResources().getDrawable(R.drawable.button_image_red_passive) : getResources().getDrawable(R.drawable.button_image_passive));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        rejectCall();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) && grantResults[i] == PERMISSION_GRANTED) {
                    mIsAudioPermissionsGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PERMISSION_GRANTED) {
                    mIsVideoPermissionsGranted = true;
                }
            }
        }
    }

    private void answerCall(boolean withVideo) {
        answerCall();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(INCOMING_CALL_RESULT, CALL_ANSWERED);
        intent.putExtra(CALL_ID, getCallId());
        intent.putExtra(WITH_VIDEO, withVideo);

        startMainActivity(intent);
    }

    private void startMainActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean permissionsGrantedForCall(boolean isVideoCall) {
        ArrayList<String> missingPermissions = (ArrayList<String>) Voximplant.getMissingPermissions(getApplicationContext(), isVideoCall);
        if (missingPermissions.size() == 0) {
            mIsAudioPermissionsGranted = true;
            mIsVideoPermissionsGranted = true;
            return true;
        } else {
            mIsAudioPermissionsGranted = !missingPermissions.contains(Manifest.permission.RECORD_AUDIO);
            mIsVideoPermissionsGranted = !missingPermissions.contains(Manifest.permission.CAMERA);
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSION_GRANTED);
            return false;
        }
    }

    public void onCallEnded(String callId) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(CALL_ID, callId);
        startMainActivity(intent);
    }

    private void stop() {
        ICall call = mCall.get();
        if (call != null) {
            call.removeCallListener(this);
            onCallEnded(call.getCallId());
        }
    }

    public boolean isVideoCall() {
        return mCall.get() != null && mCall.get().isVideoEnabled();
    }


    public String getCallId() {
        return mCall != null ? mCall.get().getCallId() : null;
    }


    public void answerCall() {
        ICall call = mCall.get();
        if (call != null) {
            call.removeCallListener(this);
        }
    }


    public void rejectCall() {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "IncomingCallPresenter: rejectCall: invalid call");
            return;
        }
        try {
            call.reject(RejectMode.DECLINE, mHeaders);
        } catch (CallException e) {
            Log.e(APP_TAG, "IncomingCallPresenter: reject call exception: " + e.getMessage());
            stop();
        }
    }

    @Override
    public void onCallFailed(ICall call, int code, String description, Map<String, String> headers) {
        stop();
    }

    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        stop();
    }
}
