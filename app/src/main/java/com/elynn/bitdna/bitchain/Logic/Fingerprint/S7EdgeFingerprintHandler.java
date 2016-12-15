package com.elynn.bitdna.bitchain.Logic.Fingerprint;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.LoginActivity;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;
/**
 * Created by Aryo on 15/11/2016.
 */

public class S7EdgeFingerprintHandler implements Handler.Callback  {

    public Callback mCallback;
    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private Context mContext;
    private ArrayAdapter<String> mListAdapter;
    private ArrayList<Integer> designatedFingers = null;
    private ArrayList<Integer> designatedFingersDialog = null;


    private boolean needRetryIdentify = false;
    private boolean onReadyIdentify = false;
    private boolean onReadyEnroll = false;
    private boolean hasRegisteredFinger = false;

    private boolean isFeatureEnabled_fingerprint = false;
    private boolean isFeatureEnabled_index = false;
    private boolean isFeatureEnabled_uniqueId = false;
    private boolean isFeatureEnabled_custom = false;
    private boolean isFeatureEnabled_backupPw = false;

    private Handler mHandler;
    private static final int MSG_AUTH = 1000;
    private static final int MSG_AUTH_UI_WITH_PW = 1001;
    private static final int MSG_AUTH_UI_WITHOUT_PW = 1002;
    private static final int MSG_CANCEL = 1003;
    private static final int MSG_REGISTER = 1004;
    private static final int MSG_GET_NAME = 1005;
    private static final int MSG_GET_UNIQUEID = 1006;
    private static final int MSG_AUTH_INDEX = 1007;
    private static final int MSG_AUTH_UI_INDEX = 1008;
    private static final int MSG_AUTH_UI_CUSTOM_LOGO = 1009;
    private static final int MSG_AUTH_UI_CUSTOM_TRANSPARENCY = 1010;
    private static final int MSG_AUTH_UI_CUSTOM_DISMISS = 1011;
    private static final int MSG_AUTH_UI_CUSTOM_BUTTON_STANDBY = 1012;

    private BroadcastReceiver mPassReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SpassFingerprint.ACTION_FINGERPRINT_RESET.equals(action)) {
                Toast.makeText(mContext, "all fingerprints are removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_REMOVED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(mContext, fingerIndex + " fingerprints is removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_ADDED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(mContext, fingerIndex + " fingerprints is added", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_RESET);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_REMOVED);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_ADDED);
        mContext.registerReceiver(mPassReceiver, filter);
    };

    public void registerBroadcastReceiver(Context ctx) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_RESET);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_REMOVED);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_ADDED);
        ctx.registerReceiver(mPassReceiver, filter);
    };

    private void unregisterBroadcastReceiver() {
        try {
            if (mContext != null) {
                mContext.unregisterReceiver(mPassReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initSpass(Activity act){
        mHandler=new Handler();
        Spass mSpass = new Spass();

        try {
            mSpass.initialize(act);
        } catch (SsdkUnsupportedException e) {
            Log.d("debug", "Exception SsdkUnsupportedException"  + e);
        } catch (UnsupportedOperationException e) {
            Log.d("debug", "UnsupportedOperationException"  + e);
        }

        mSpassFingerprint = new SpassFingerprint(act);
    }

    private void resetAll() {
        designatedFingers = null;
        needRetryIdentify = false;
        onReadyIdentify = false;
        onReadyEnroll = false;
        hasRegisteredFinger = false;
    }

    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            log("identify finished : reason =" + getEventStatusName(eventStatus));
            int FingerprintIndex = 0;
            String FingerprintGuideText = null;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                log("IllegalStateException" + ise.getMessage());
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
                mCallback.onSuccess();
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                log("onFinished() : Password authentification Success");
                mCallback.onSuccess();
            } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
                log("onFinished() : Authentification is blocked because of fingerprint service internally.");
                mCallback.onFailed();
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
                log("onFinished() : User cancel this identify.");
                mCallback.onCancel();
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                log("onFinished() : The time for identify is finished.");
                mCallback.onTimeOut();
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                log("onFinished() : Authentification Fail for identify.");
                needRetryIdentify = true;
                //FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
                //Toast.makeText(mContext, "Authentification Fail for identify", Toast.LENGTH_SHORT).show();
                mCallback.onFailed();
            } else {
                log("onFinished() : Authentification Fail for identify");
                needRetryIdentify = true;
                mCallback.onFailed();
            }
            if (!needRetryIdentify) {
                resetIdentifyIndex();
            }
        }

        @Override
        public void onReady() {
            log("identify state is ready");
        }

        @Override
        public void onStarted() {
            log("User touched fingerprint sensor");
        }

        @Override
        public void onCompleted() {
            log("the identify is completed 1");
            onReadyIdentify = false;
            if (needRetryIdentify) {
                needRetryIdentify = false;
                //startIdentify();
                //mHandler.sendEmptyMessageDelayed(MSG_AUTH, 100);
            }
        }
    };

    private SpassFingerprint.IdentifyListener mIdentifyListenerDialog = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            log("identify finished : reason =" + getEventStatusName(eventStatus));
            int FingerprintIndex = 0;
            boolean isFailedIdentify = false;
            onReadyIdentify = false;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
                mCallback.onSuccess();
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                log("onFinished() : Password authentification Success");
                mCallback.onSuccess();
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED
                    || eventStatus == SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE) {
                log("onFinished() : User cancel this identify.");
                mCallback.onCancel();
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                log("onFinished() : The time for identify is finished.");
                mCallback.onTimeOut();
            } else if (!mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_AVAILABLE_PASSWORD)) {
                if (eventStatus == SpassFingerprint.STATUS_BUTTON_PRESSED) {
                    log("onFinished() : User pressed the own button");
                    Toast.makeText(mContext, "Please connect own Backup Menu", Toast.LENGTH_SHORT).show();
                    mCallback.onCancel();
                }
            } else {
                log("onFinished() : Authentification Fail for identify");
                isFailedIdentify = true;
                mCallback.onFailed();
            }
            if (!isFailedIdentify) {
                resetIdentifyIndexDialog();
            }
        }

        @Override
        public void onReady() {
            log("identify state is ready");
        }

        @Override
        public void onStarted() {
            log("User touched fingerprint sensor");
        }

        @Override
        public void onCompleted() {
            log("the identify is completed 2");
        }
    };
    private SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener() {
        @Override
        public void onFinished() {
            onReadyEnroll = false;
            log("RegisterListener.onFinished()");
        }
    };

    private static String getEventStatusName(int eventStatus) {
        switch (eventStatus) {
            case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                return "STATUS_AUTHENTIFICATION_SUCCESS";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                return "STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS";
            case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                return "STATUS_TIMEOUT";
            case SpassFingerprint.STATUS_SENSOR_FAILED:
                return "STATUS_SENSOR_ERROR";
            case SpassFingerprint.STATUS_USER_CANCELLED:
                return "STATUS_USER_CANCELLED";
            case SpassFingerprint.STATUS_QUALITY_FAILED:
                return "STATUS_QUALITY_FAILED";
            case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                return "STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE";
            case SpassFingerprint.STATUS_BUTTON_PRESSED:
                return "STATUS_BUTTON_PRESSED";
            case SpassFingerprint.STATUS_OPERATION_DENIED:
                return "STATUS_OPERATION_DENIED";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
            default:
                return "STATUS_AUTHENTIFICATION_FAILED";
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        log(msg.toString());
        switch (msg.what) {
            case MSG_AUTH:
                startIdentify();
                break;
            case MSG_AUTH_UI_WITH_PW:
                startIdentifyDialog(true);
                break;
            case MSG_AUTH_UI_WITHOUT_PW:
                startIdentifyDialog(false);
                break;
            case MSG_CANCEL:
                cancelIdentify();
                break;
            case MSG_REGISTER:
                registerFingerprint();
                break;
            case MSG_GET_NAME:
                getFingerprintName();
                break;
            case MSG_GET_UNIQUEID:
                getFingerprintUniqueID();
                break;
            case MSG_AUTH_INDEX:
                makeIdentifyIndex(1);
                startIdentify();
                break;
            case MSG_AUTH_UI_INDEX:
                makeIdentifyIndexDialog(2);
                makeIdentifyIndexDialog(3);
                startIdentifyDialog(false);
                break;
            case MSG_AUTH_UI_CUSTOM_LOGO:
                setDialogTitleAndLogo();
                startIdentifyDialog(false);
                break;
            case MSG_AUTH_UI_CUSTOM_TRANSPARENCY:
                setDialogTitleAndTransparency();
                startIdentifyDialog(false);
                break;
            case MSG_AUTH_UI_CUSTOM_DISMISS:
                setDialogTitleAndDismiss();
                startIdentifyDialog(false);
                break;
            case MSG_AUTH_UI_CUSTOM_BUTTON_STANDBY:
                setDialogButtonAndStandbyText();
                startIdentifyDialog(false);
                break;
        }
        return true;
    }

    public void startIdentify() {
        Log.d("debug", "onReadyIdentify value : " + onReadyIdentify );
        if (onReadyIdentify == false) {
            try {
                onReadyIdentify = true;
                if (mSpassFingerprint != null) {
                    Log.d("debug", "mSpassFingerprint value : " + mSpassFingerprint );
                    setIdentifyIndex();
                    mSpassFingerprint.startIdentify(mIdentifyListener);
                }
                if (designatedFingers != null) {
                    log("Please identify finger to verify you with " + designatedFingers.toString() + " finger");
                } else {
                    log("Please identify finger to verify you");
                }
            } catch (SpassInvalidStateException ise) {
                onReadyIdentify = false;
                resetIdentifyIndex();
                if (ise.getType() == SpassInvalidStateException.STATUS_OPERATION_DENIED) {
                    log("Exception SpassInvalidStateException  " +ise.getType()+ ": " + ise.getMessage());
                    mCallback.onAttempt(ise.getType(),ise.getMessage());
                }
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                resetIdentifyIndex();
                log("Exception IllegalStateException: " + e);
            }
        } else {
            log("The previous request is remained. Please finished or cancel first");
        }
    }

    private void startIdentifyDialog(boolean backup) {
        if (onReadyIdentify == false) {
            onReadyIdentify = true;
            try {
                if (mSpassFingerprint != null) {
                    setIdentifyIndexDialog();
                    //mSpassFingerprint.startIdentifyWithDialog(SampleActivity.this, mIdentifyListenerDialog, backup);
                }
                if (designatedFingersDialog != null) {
                    log("Please identify finger to verify you with " + designatedFingersDialog.toString() + " finger");
                } else {
                    log("Please identify finger to verify you");
                }
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                resetIdentifyIndexDialog();
                log("Exception IllegalStateException2: " + e);
            }
        } else {
            log("The previous request is remained. Please finished or cancel first");
        }
    }

    public void cancelIdentify() {
        if (onReadyIdentify == true) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.cancelIdentify();
                }
                log("cancelIdentify is called");
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
            onReadyIdentify = false;
            needRetryIdentify = false;
        } else {
            log("Please request Identify first");
        }
    }

    private void registerFingerprint() {
        if (onReadyIdentify == false) {
            if (onReadyEnroll == false) {
                onReadyEnroll = true;
                if (mSpassFingerprint != null) {
                    //mSpassFingerprint.registerFinger(SampleActivity.this, mRegisterListener);
                }
                log("Jump to the Enroll screen");
            } else {
                log("Please wait and try to register again");
            }
        } else {
            log("Please cancel Identify first");
        }
    }

    private void getFingerprintName() {
        SparseArray<String> mList = null;
        log("=Fingerprint Name=");
        if (mSpassFingerprint != null) {
            mList = mSpassFingerprint.getRegisteredFingerprintName();
        }
        if (mList == null) {
            log("Registered fingerprint is not existed.");
        } else {
            for (int i = 0; i < mList.size(); i++) {
                int index = mList.keyAt(i);
                String name = mList.get(index);
                log("index " + index + ", Name is " + name);
            }
        }
    }

    private void getFingerprintUniqueID() {
        SparseArray<String> mList = null;
        try {
            log("=Fingerprint Unique ID=");
            if (mSpassFingerprint != null) {
                mList = mSpassFingerprint.getRegisteredFingerprintUniqueID();
            }
            if (mList == null) {
                log("Registered fingerprint is not existed.");
            } else {
                for (int i = 0; i < mList.size(); i++) {
                    int index = mList.keyAt(i);
                    String ID = mList.get(index);
                    log("index " + index + ", Unique ID is " + ID);
                }
            }
        } catch (IllegalStateException ise) {
            log(ise.getMessage());
        }
    }

    private void setIdentifyIndex() {
        if (isFeatureEnabled_index) {
            if (mSpassFingerprint != null && designatedFingers != null) {
                mSpassFingerprint.setIntendedFingerprintIndex(designatedFingers);
            }
        }
    }

    private void makeIdentifyIndex(int i) {
        if (designatedFingers == null) {
            designatedFingers = new ArrayList<Integer>();
        }
        for(int j = 0; j< designatedFingers.size(); j++){
            if(i == designatedFingers.get(j)){
                return;
            }
        }
        designatedFingers.add(i);
    }

    private void resetIdentifyIndex() {
        designatedFingers = null;
    }
    private void setIdentifyIndexDialog() {
        if (isFeatureEnabled_index) {
            if (mSpassFingerprint != null && designatedFingersDialog != null) {
                mSpassFingerprint.setIntendedFingerprintIndex(designatedFingersDialog);
            }
        }
    }

    private void makeIdentifyIndexDialog(int i) {
        if (designatedFingersDialog == null) {
            designatedFingersDialog = new ArrayList<Integer>();
        }
        for(int j = 0; j< designatedFingersDialog.size(); j++){
            if(i == designatedFingersDialog.get(j)){
                return;
            }
        }
        designatedFingersDialog.add(i);
    }

    private void resetIdentifyIndexDialog() {
        designatedFingersDialog = null;
    }
    private void setDialogTitleAndLogo() {
        if (isFeatureEnabled_custom) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.setDialogTitle("Customized Dialog With Logo", 0x000000);
                    mSpassFingerprint.setDialogIcon("logo_image");
                }
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
        }
    }

    private void setDialogTitleAndTransparency() {
        if (isFeatureEnabled_custom) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.setDialogTitle("Customized Dialog With Transparency", 0x000000);
                    mSpassFingerprint.setDialogBgTransparency(0);
                }
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
        }
    }

    private void setDialogTitleAndDismiss() {
        if (isFeatureEnabled_custom) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.setDialogTitle("Customized Dialog With Setting Dialog dismiss", 0x000000);
                    mSpassFingerprint.setCanceledOnTouchOutside(true);
                }
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
        }
    }

    private void setDialogButtonAndStandbyText() {
        if (!isFeatureEnabled_backupPw) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.setDialogButton("OWN BUTTON");
                    mSpassFingerprint.changeStandbyString("Touch your fingerprint or press the button for launching own menu");
                }
            } catch (IllegalStateException ise) {
                log(ise.getMessage());
            }
        }
    }

    private void log(String text) {
        Log.d("debug",text);
    }

    public interface Callback {

        void onSuccess();

        void onCancel();

        void onTimeOut();

        void onFailed();

        void onAttempt(int code,String msg);
    }
}
