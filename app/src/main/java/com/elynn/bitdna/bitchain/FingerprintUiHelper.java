/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.elynn.bitdna.bitchain;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.elynn.bitdna.bitchain.Model.StatisGlobal;

import javax.inject.Inject;

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    @VisibleForTesting static final long ERROR_TIMEOUT_MILLIS = 1600;
    @VisibleForTesting static final long SUCCESS_DELAY_MILLIS = 1300;

    private final FingerprintManager mFingerprintManager;
    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    @VisibleForTesting boolean mSelfCancelled;

    /**
     * Builder class for {@link FingerprintUiHelper} in which injected fields from Dagger
     * holds its fields and takes other arguments in the {@link #build} method.
     */
    public static class FingerprintUiHelperBuilder {
        private final FingerprintManager mFingerPrintManager;

        @Inject
        public FingerprintUiHelperBuilder(FingerprintManager fingerprintManager) {
            Log.d("debug", "FingerprintUiHelper FingerprintUiHelperBuilder");
            mFingerPrintManager = fingerprintManager;
        }

        public FingerprintUiHelper build(ImageView icon, TextView errorTextView, Callback callback) {
            Log.d("debug", "FingerprintUiHelper build");
            return new FingerprintUiHelper(mFingerPrintManager, icon, errorTextView,
                    callback);
        }
    }

    /**
     * Constructor for {@link FingerprintUiHelper}. This method is expected to be called from
     * only the {@link FingerprintUiHelperBuilder} class.
     */
    private FingerprintUiHelper(FingerprintManager fingerprintManager,
                                ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        Log.d("debug", "isFingerprintAuthAvailable ");
        if (mFingerprintManager.isHardwareDetected()==true) {
            Log.d("debug", "isHardwareDetected = true");
        }else{
            Log.d("debug", "isHardwareDetected = false");
        }
        if (mFingerprintManager.hasEnrolledFingerprints()){
            Log.d("debug", "hasEnrolledFingerprints = true");
        }else{
            Log.d("debug", "hasEnrolledFingerprints = false");
        }

        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        Log.d("debug", "startListening ");
        Log.d("debug2", "cryptoObject "+cryptoObject.toString());
        Log.d("debug", "getSignature "+cryptoObject.getSignature());
        Log.d("debug", "getCipher "+cryptoObject.getCipher());
        Log.d("debug", "getMac "+cryptoObject.getMac());
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        mCancellationSignal = new CancellationSignal();
        StatisGlobal.isCancelFragment=false;
        mSelfCancelled = false;

        Log.d("debug", "mFingerprintManager authenticate");
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);
        Log.d("debug", "mFingerprintManager authenticate Success");
        //mIcon.setImageResource(R.drawable.ic_fp_40px);
        mIcon.setImageResource(R.drawable.ic_menu_send);
    }

    public void stopListening() {
        Log.d("debug", "stopListening 1");
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            StatisGlobal.isCancelFragment=true;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d("debug", "onAuthenticationError ");
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Log.d("debug", "onAuthenticationSucceeded ");
        Log.d("debug", "onAuthenticationSucceeded result = "+ result.toString());
        Log.d("debug2", "getCryptoObject "+ result.getCryptoObject());
        Log.d("debug2", "getCryptoObject "+ result.getClass());
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.success_color, null));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));

        mIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onAuthenticated();
                Log.d("debug", mCallback.toString());
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        Log.d("debug", "showError " + error);
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.warning_color, null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    @VisibleForTesting
    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.hint_color, null));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            //mIcon.setImageResource(R.drawable.ic_fp_40px);
            mIcon.setImageResource(R.drawable.ic_menu_send);
        }
    };

    public interface Callback {

        void onAuthenticated();

        void onError();
    }
}
