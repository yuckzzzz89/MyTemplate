package com.elynn.bitdna.bitchain.Logic.Fingerprint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Aryo on 15/11/2016.
 */
public class FingerprintHandler extends
        FingerprintManager.AuthenticationCallback {

    public Callback mCallback;
    private CancellationSignal cancellationSignal;
    private Context appContext;

    public FingerprintHandler(Context context) {
        appContext = context;
    }

    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }
    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        Log.d("debug", "onAuthenticationError " + errMsgId + " : " + errString);
        mCallback.onError(errMsgId,errString);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Log.d("debug", "onAuthenticationHelp " + helpMsgId + " : " + helpString);
        mCallback.onHelp(helpMsgId,helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext,
                "Authentication failed.",
                Toast.LENGTH_SHORT).show();
        mCallback.onFailed();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        Toast.makeText(appContext,
                "Authentication succeeded.",
                Toast.LENGTH_SHORT).show();

        mCallback.onAuthenticated();
    }

    public interface Callback {

        void onAuthenticated();

        void onError(int errMsgId, CharSequence errString);

        void onFailed();

        void onHelp(int helpMsgId, CharSequence helpString);
    }
}