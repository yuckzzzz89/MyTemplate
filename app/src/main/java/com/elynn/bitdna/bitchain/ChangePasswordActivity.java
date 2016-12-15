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

public class ChangePasswordActivity extends AppCompatActivity {

    private View mProgressView;
    private View mFormView;
    private EditText vTxtOldpassword;
    private EditText vTxtNewpassword;
    private EditText vTxtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.main_progress);
        vTxtOldpassword=(EditText) findViewById(R.id.txtOldPassword);
        vTxtNewpassword=(EditText) findViewById(R.id.txtNewPassword);
        vTxtConfirmPassword=(EditText) findViewById(R.id.txtConfirmPassword);


        Button mSubmitButton = (Button) findViewById(R.id.submit_button);
        Button mCancelButton = (Button) findViewById(R.id.cancel_button);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordUser();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ChangePasswordActivity.this, DashboardActivity.class);
                finish();
                //startActivity(intent);
            }
        });
    }

    private void changePasswordUser(){
        boolean cancel = false;
        View focusView = null;
        vTxtOldpassword.setError(null);
        vTxtNewpassword.setError(null);
        vTxtConfirmPassword.setError(null);

        String oldPassword = vTxtOldpassword.getText().toString();
        String newPassword = vTxtNewpassword.getText().toString();
        String confirmPassword = vTxtConfirmPassword.getText().toString();

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(oldPassword) && !isPasswordValid(oldPassword)) {
            vTxtOldpassword.setError(getString(R.string.error_invalid_password));
            focusView = vTxtOldpassword;
            cancel = true;
        }
        if (!TextUtils.isEmpty(newPassword) && !isPasswordValid(newPassword)) {
            vTxtNewpassword.setError(getString(R.string.error_invalid_password));
            focusView = vTxtNewpassword;
            cancel = true;
        }
        if (!TextUtils.isEmpty(confirmPassword) && !isPasswordValid(confirmPassword)) {
            vTxtConfirmPassword.setError(getString(R.string.error_invalid_password));
            focusView = vTxtConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }else{
            changePasswordAsyncTask task = new changePasswordAsyncTask();
            if (Build.VERSION.SDK_INT >= 11)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public class changePasswordAsyncTask extends AsyncTask<Void, Void, Boolean> {
        Response result=new Response();
        String tResult="";
        String tMessage="";
        String tOldPassword;
        String tNewPassword;
        String tConfirmPassword;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
            tOldPassword=vTxtOldpassword.getText().toString();
            tNewPassword=vTxtNewpassword.getText().toString();
            tConfirmPassword=vTxtConfirmPassword.getText().toString();
            Log.e("AsyncTask", "onPreExecute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("AsyncTask", "doInBackground");

            String url = PathConfiguration.CHANGEPASSWORD+"?id="+ UserLogin.userId+"&oldpassword="+ tOldPassword+ "&newpassword=" + tNewPassword + "&confirmpassword=" + tConfirmPassword;
            Log.d("debug", "url = " + url);
            try {
                API task=new API();
                result = task.DownloadContent(url);
                Log.d("debug", "result = " + result);
                if (result.Code==200){
                    //redirect to login
                    Log.d("debug", "response = " + result.Response);
                    JSONObject jsonObj = new JSONObject(result.Response);
                    tResult = jsonObj.getString("result");
                    tMessage  = jsonObj.getString("message");
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
            //vImageView.setImageBitmap(bitmap);
            Log.d("debug", "onPostExecute Open With");
            showProgress(false);
            if (tResult.equals("success")) {
                Toast.makeText(ChangePasswordActivity.this,"Change Password Success", Toast.LENGTH_SHORT).show();
                vTxtOldpassword.setText("");
                vTxtNewpassword.setText("");
                vTxtConfirmPassword.setText("");
            }else{
                Toast.makeText(ChangePasswordActivity.this,"Change Password Failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
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
}
