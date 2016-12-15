package com.elynn.bitdna.bitchain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResetPasswordActivity extends AppCompatActivity {

    private View mProgressView;
    private View mFormView;
    private EditText vTxtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.main_progress);
        vTxtEmail=(EditText) findViewById(R.id.email);

        Button mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        Button mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void resetPassword(){
        boolean cancel = false;
        View focusView = null;
        vTxtEmail.setError(null);

        String email = vTxtEmail.getText().toString();

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
            vTxtEmail.setError(getString(R.string.error_invalid_email));
            focusView = vTxtEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }else{
            resetPasswordAsyncTask task = new resetPasswordAsyncTask();
            if (Build.VERSION.SDK_INT >= 11)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
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

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }

    public class resetPasswordAsyncTask extends AsyncTask<Void, Void, Boolean> {
        Response result=new Response();
        Boolean tResult=true;
        String tMessage="";
        String temail;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
            temail=vTxtEmail.getText().toString();
            Log.e("AsyncTask", "onPreExecute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("AsyncTask", "doInBackground");

            String url = PathConfiguration.RESETPASSWORD+"?email="+temail;
            Log.d("debug", "url = " + url);
            try {
                API task=new API();
                result = task.DownloadContent(url);
                Log.d("debug", "result = " + result);
                if (result.Code==200){
                    //redirect to login
                    Log.d("debug", "response = " + result.Response);
                    JSONObject jsonObj = new JSONObject(result.Response);
                    tResult = true;
                    tMessage  = jsonObj.getString("message");
                }else{
                    Log.d("debug", "Login Failed");
                    tResult = false;
                    return false;
                }
            } catch (IOException IOEx){
                Log.d("debug", "IOException = " + IOEx);
                tResult = false;
                return false;
            } catch (JSONException e){
                Log.d("debug", "JSONException = " + e);
                tResult = false;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //vImageView.setImageBitmap(bitmap);
            Log.d("debug", "onPostExecute Open With");
            showProgress(false);
            if (tResult) {
                if (tMessage.equals("Password Reset Link Successfully Sent")) {
                    Toast.makeText(ResetPasswordActivity.this, "Request link has been sent", Toast.LENGTH_SHORT).show();
                    vTxtEmail.setText("");
                    //Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    finish();
                    //startActivity(intent);
                }else{
                    Toast.makeText(ResetPasswordActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
                    vTxtEmail.setText("");
                }
            }else{
                Toast.makeText(ResetPasswordActivity.this,"Failed to send link", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
