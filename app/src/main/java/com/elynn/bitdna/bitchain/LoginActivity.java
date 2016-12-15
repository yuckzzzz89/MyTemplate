package com.elynn.bitdna.bitchain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
//import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentManager;


import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

import com.elynn.bitdna.bitchain.Logic.Fingerprint.FingerprintHandler;
import com.elynn.bitdna.bitchain.Logic.Fingerprint.S7EdgeFingerprintHandler;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.StatisGlobal;
import com.elynn.bitdna.bitchain.Model.UserLogin;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;

import org.json.JSONException;
import org.json.JSONObject;


import javax.inject.Inject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    public static final String KEY_NAME = "my_key";
    FragmentManager fm = getSupportFragmentManager();

    //@Inject KeyguardManager mKeyguardManager;
    @Inject FingerprintManager mFingerprintManager;
    //@Inject FingerprintAuthenticationDialogFragment mFragment;
    //public FingerprintFragment mFragment;
    @Inject KeyStore mKeyStore;
    @Inject KeyPairGenerator mKeyPairGenerator;
    @Inject Signature mSignature;
    @Inject SharedPreferences mSharedPreferences;
    private FingerprintManager.CryptoObject cryptoObject;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String Logindevice_id;
    private AlertDialog alertDialog;
    private FingerprintHandler fingerPrintHelper = new FingerprintHandler(this);
    private S7EdgeFingerprintHandler S7EdgeFingerprintHelper = new S7EdgeFingerprintHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug", "Inject " + this.toString());

        ((InjectedApplication) getApplication()).inject(this);
        Log.d("debug", "Inject Success");

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        Button mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        UserLogin.deviceId = Settings.Secure.getString(LoginActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Logindevice_id = Settings.Secure.getString(LoginActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //mEmailView.setText("dev.android@rumahbilling.com");
        //mPasswordView.setText("popmie2010");


        //noinspection ResourceType
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            //purchaseButton.setEnabled(false);
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }else {
            createKeyPair();
            initSignature();

            boolean useFingerprintPreference = mSharedPreferences
                    .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                            true);
            if (useFingerprintPreference) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPreferences.getString(StatisGlobal.email, "") != "") {
                    LayoutInflater inflater = getLayoutInflater();
                    View alertLayout = inflater.inflate(R.layout.fingerprint_dialog_content, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Login");
                    alert.setView(alertLayout);
                    alert.setCancelable(false);
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.hide();
                        }
                    });

                    alertDialog = alert.create();
                    alertDialog.show();
                    if (initSignature()) {
                        LoginFingerPrint();
                    }
                }
            }


            //if (initSignature()) {

            /*Log.d("debug", "mSignature = " + mSignature.toString());
            // Show the fingerprint dialog. The user has the option to use the fingerprint with
            // crypto, or you can fall back to using a server-side verified password.
            mFragment.setCryptoObject(new FingerprintManager.CryptoObject(mSignature));
            Log.d("debug", "mFragment = " + mFragment.toString());
            boolean useFingerprintPreference = mSharedPreferences
                    .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                            true);
            Log.d("debug", "mSharedPreferences = " + mSharedPreferences.toString());
            if (useFingerprintPreference) {
                mFragment.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPreferences.getString(StatisGlobal.email, "") != "") {
                mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
            Log.d("debug", "mSharedPreferences = " + mFragment.toString());
            //} else {
            // This happens if the lock screen has been disabled or or a fingerprint got
            // enrolled. Thus show the dialog to authenticate with their password first
            // and ask the user if they want to authenticate with fingerprints in the
            // future
            // mFragment.setStage(
            //     FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
            // mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            //}
            */
        }
    }

    private void LoginFingerPrint(){
        Log.d("debug", "phone Model = " + Build.MODEL);
        switch (Build.MODEL){
            case "SM-G935F":
                S7EdgeFingerPrint();
                break;
            case "Nexus 5X":
                AndroidDefaultFingerprint();
                break;
            default:
                AndroidDefaultFingerprint();
                break;
        }
    }

    private void S7EdgeFingerPrint(){
        //S7EdgeFingerprintHelper.registerBroadcastReceiver(getBaseContext());
        S7EdgeFingerprintHelper.initSpass(LoginActivity.this);
        S7EdgeFingerprintHelper.startIdentify();
        S7EdgeFingerprintHelper.mCallback=new S7EdgeFingerprintHandler.Callback() {
            @Override
            public void onSuccess() {
                Log.d("debug", "onSuccess");
                alertDialog.hide();
                FingerPrintLogin();
            }

            @Override
            public void onCancel() {
                Log.d("debug", "onCancel");
                alertDialog.hide();
            }

            @Override
            public void onTimeOut() {
                Log.d("debug", "onTimeOut");
                alertDialog.hide();
            }

            @Override
            public void onFailed() {
                Log.d("debug", "onFailed");
                Toast.makeText(getBaseContext(),"Authentication failed.",Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        S7EdgeFingerprintHelper.startIdentify();
                        //alertDialog.show();
                        //Do something after 100ms
                    }
                }, 1000);
                //S7EdgeFingerprintHelper.initSpass(LoginActivity.this);
                //S7EdgeFingerprintHelper.registerBroadcastReceiver();
                //S7EdgeFingerprintHelper.startIdentify();
            }

            @Override
            public void onAttempt(int code,String msg){
                Log.d("debug","onAttempt");
                if (code==1){
                    Log.d("debug","code = "+code);
                    Log.d("debug","msg = "+msg);
                    Toast.makeText(getBaseContext(),"Too many attempts.\n Try again later.",Toast.LENGTH_SHORT).show();
                    S7EdgeFingerprintHelper.cancelIdentify();
                    alertDialog.hide();
                }
            }
        };

    }

    private void AndroidDefaultFingerprint(){
        cryptoObject = new FingerprintManager.CryptoObject(mSignature);
        fingerPrintHelper.startAuth(mFingerprintManager, cryptoObject);
        fingerPrintHelper.mCallback=new FingerprintHandler.Callback() {
            @Override
            public void onAuthenticated() {
                alertDialog.hide();
                FingerPrintLogin();
            }

            @Override
            public void onError(int errMsgId, CharSequence errString) {
                if (errMsgId==7){
                    Toast.makeText(getBaseContext(),"Too many attempts.\n Try again later.",Toast.LENGTH_SHORT).show();
                    alertDialog.hide();
                }
            }

            @Override
            public void onFailed() {
                fingerPrintHelper.startAuth(mFingerprintManager, cryptoObject);
                alertDialog.show();
            }

            @Override
            public void onHelp(int helpMsgId, CharSequence helpString){
                Log.d("debug", "helpMsgId"  + helpMsgId);
            }
        };
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password);
            LoginTask();
            //mAuthTask.execute((Void) null);
        }
    }

    private void registerUser(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        //finish();
        startActivity(intent);
    }

    private void resetPassword(){
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        //finish();
        startActivity(intent);
    }

    private void LoginTask(){
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            protected String Temail;
            protected String Tpassword;
            protected String Tdeviceid;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Temail = mEmailView.getText().toString();
                Tpassword = mPasswordView.getText().toString();
                Tdeviceid=Logindevice_id;
                Log.e("AsyncTask", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Response result=new Response();
                Log.v("AsyncTask", "doInBackground");


                String url = PathConfiguration.LOGIN+"?email=" + Temail + "&password="+Tpassword+"&deviceid="+ Tdeviceid;
                Log.d("debug", "email = " + Temail);
                Log.d("debug", "password = " + Tpassword);
                Log.d("debug", "url = " + url);
                try {
                    API task=new API();
                    result = task.DownloadContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code==200){
                        //redirect to login
                        JSONObject jsonObj = new JSONObject(result.Response);

                        UserLogin.token = jsonObj.getString("token");
                        JSONObject userObject = jsonObj.getJSONObject("user");
                        UserLogin.userId = userObject.getInt("id");
                        UserLogin.email = Temail;
                        UserLogin.password = Tpassword;
                        UserLogin.firstName = userObject.getString("first_name");
                        UserLogin.lastName = userObject.getString("last_name");
                        UserLogin.dob = userObject.getString("dob");
                        UserLogin.mobilePhone = userObject.getString("mobile_phone");
                        UserLogin.google2FAsecret = userObject.getString("google2fa_secret");
                        UserLogin.enable = userObject.getInt("isEnabled")==1;
                        UserLogin.validSecret = userObject.getInt("isValidSecret")==1;
                        UserLogin.isFingerPrintEnabled = userObject.getInt("isFingerPrintEnabled")==1;
                    }else{
                        Log.d("debug", "Login Failed");
                        return false;
                    }
                } catch (IOException IOEx){
                    Log.d("debug", "IOException = " + IOEx);
                    return false;
                } catch (JSONException e){
                    Log.d("debug", "JSONException = " + e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                Log.v("debug", "onPostExecute");
                showProgress(false);

                if (success) {
                    if (UserLogin.enable){
                        Intent intent = new Intent(LoginActivity.this, GoogleOTPActivity.class);
                        finish();
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }else{
                    mEmailView.setError("Login failed");
                    mEmailView.setText("");
                    mPasswordView.setText("");
                }
            }

            @Override
            protected void onCancelled() {
                Log.d("debug", "On canceled");
                showProgress(false);
            }
        };

        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    private void  invalidLogin(){
        mEmailView.setError("Login invalid");
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Generates an asymmetric key pair in the Android Keystore. Every use of the private key must
     * be authorized by the user authenticating with fingerprint. Public key use is unrestricted.
     */
    public void createKeyPair() {
        Log.d("debug", "MainActivity createKeyPair");
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            // Require the user to authenticate with a fingerprint to authorize
                            // every use of the private key
                            .setUserAuthenticationRequired(true)
                            .build());
            Log.d("debug", "mKeyPairGenerator = " + mKeyPairGenerator.toString());
            mKeyPairGenerator.generateKeyPair();
            Log.d("debug", "mKeyPairGenerator generate = " + mKeyPairGenerator.toString());
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the {@link Signature} instance with the created key in the
     * {@link #createKeyPair()} method.
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initSignature() {
        Log.d("debug", "MainActivity initSignature");
        try {
            mKeyStore.load(null);
            PrivateKey key = (PrivateKey) mKeyStore.getKey(KEY_NAME, null);
            mSignature.initSign(key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void FingerPrintLogin(byte[] result){
        FingerPrintLogin();
    }

    private void FingerPrintLogin(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString(StatisGlobal.email, "")!="") {
            showProgress(true);

            mEmailView.setText(sharedPreferences.getString(StatisGlobal.email, ""));
            mPasswordView.setText(sharedPreferences.getString(StatisGlobal.password, ""));
            Logindevice_id=sharedPreferences.getString(StatisGlobal.device_ID, "");
            if (!StatisGlobal.isCancelFragment){
                LoginFingerPrintTask();
            }
        }else{
            Toast.makeText(this, "Finger Print is Enable",
                    Toast.LENGTH_LONG).show();
            mEmailView.setText("");
            mPasswordView.setText("");
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private void LoginFingerPrintTask() {
        Log.d("debug", "login finger print task ");
        Log.d("debug", "login finger print task "+StatisGlobal.isCancelFragment);

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            protected String Temail;
            protected String Tpassword;
            protected String Tdeviceid;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Temail = mEmailView.getText().toString();
                Tpassword = mPasswordView.getText().toString();
                Tdeviceid = Logindevice_id;
                Log.e("AsyncTask", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Response result = new Response();
                Log.v("AsyncTask", "doInBackground");

                String url = PathConfiguration.LOGINVIAFINGERPRINT + "?email=" + Temail + "&password=" + Tpassword + "&deviceid=" + Tdeviceid;
                Log.d("debug", "email = " + Temail);
                Log.d("debug", "password = " + Tpassword);
                Log.d("debug", "url = " + url);
                try {
                    API task = new API();
                    result = task.DownloadContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code == 200) {
                        //redirect to login
                        JSONObject jsonObj = new JSONObject(result.Response);

                        UserLogin.token = jsonObj.getString("token");
                        JSONObject userObject = jsonObj.getJSONObject("user");
                        UserLogin.userId = userObject.getInt("id");
                        UserLogin.email = Temail;
                        UserLogin.password = Tpassword;
                        UserLogin.firstName = userObject.getString("first_name");
                        UserLogin.lastName = userObject.getString("last_name");
                        UserLogin.dob = userObject.getString("dob");
                        UserLogin.mobilePhone = userObject.getString("mobile_phone");
                        UserLogin.google2FAsecret = userObject.getString("google2fa_secret");
                        UserLogin.enable = userObject.getInt("isEnabled") == 1;
                        UserLogin.validSecret = userObject.getInt("isValidSecret") == 1;
                        UserLogin.isFingerPrintEnabled = userObject.getInt("isFingerPrintEnabled") == 1;
                    } else {
                        Log.d("debug", "Login Failed");
                        return false;
                    }
                } catch (IOException IOEx) {
                    Log.d("debug", "IOException = " + IOEx);
                    return false;
                } catch (JSONException e) {
                    Log.d("debug", "JSONException = " + e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                Log.v("debug", "onPostExecute");
                showProgress(false);

                if (success) {
                    if (UserLogin.enable) {
                        Intent intent = new Intent(LoginActivity.this, GoogleOTPActivity.class);
                        finish();
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        finish();
                        startActivity(intent);
                    }
                } else {
                    mEmailView.setError("Login failed");
                    mEmailView.setText("");
                    mPasswordView.setText("");
                }
            }

            @Override
            protected void onCancelled() {
                Log.d("debug", "On canceled");
                showProgress(false);
            }
        };

    if (Build.VERSION.SDK_INT >= 11)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    else
        task.execute();
    }
}

