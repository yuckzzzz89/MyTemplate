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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private  EditText vTxtemail;
    private  EditText vTxtpassword;
    private  EditText vTxtFirstName;
    private  EditText vTxtLastName;
    private  EditText vTxtDOB;
    private  EditText vTxtPhone;
    private View mProgressView;
    private View mFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.main_progress);
        vTxtemail=(EditText) findViewById(R.id.txtemail);
        vTxtpassword=(EditText) findViewById(R.id.txtpassword);
        vTxtFirstName=(EditText) findViewById(R.id.txtFirstName);
        vTxtLastName=(EditText) findViewById(R.id.txtLastName);
        vTxtDOB=(EditText) findViewById(R.id.txtDOB);
        vTxtPhone=(EditText) findViewById(R.id.txtPhone);

        vTxtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });


        Button mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                finish();
                //startActivity(intent);
            }
        });
    }

    private void registerUser(){

        vTxtemail.setError(null);
        vTxtpassword.setError(null);
        vTxtFirstName.setError(null);
        vTxtLastName.setError(null);
        vTxtDOB.setError(null);
        vTxtPhone.setError(null);

        String email = vTxtemail.getText().toString();
        String password = vTxtpassword.getText().toString();
        String firstName = vTxtFirstName.getText().toString();
        String lastName = vTxtLastName.getText().toString();
        String DOB = vTxtDOB.getText().toString();
        String phone = vTxtPhone.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            vTxtpassword.setError(getString(R.string.error_invalid_password));
            focusView = vTxtpassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            vTxtemail.setError(getString(R.string.error_field_required));
            focusView = vTxtemail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            vTxtemail.setError(getString(R.string.error_invalid_email));
            focusView = vTxtemail;
            cancel = true;
        }

        // Check for a valid first name.
        if (TextUtils.isEmpty(firstName)) {
            vTxtFirstName.setError(getString(R.string.error_field_required));
            focusView = vTxtFirstName;
            cancel = true;
        } else if (!isFirstNameValid(firstName)) {
            vTxtFirstName.setError(getString(R.string.error_invalid_first_name));
            focusView = vTxtFirstName;
            cancel = true;
        }

        // Check for a valid last name.
        if (TextUtils.isEmpty(lastName)) {
            vTxtLastName.setError(getString(R.string.error_field_required));
            focusView = vTxtLastName;
            cancel = true;
        } else if (!isLastNameValid(lastName)) {
            vTxtLastName.setError(getString(R.string.error_invalid_last_name));
            focusView = vTxtLastName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }else{
            registerUserAsyncTask task = new registerUserAsyncTask();
            if (Build.VERSION.SDK_INT >= 11)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        }
    }

    private boolean isLastNameValid(String lastname) {
        //TODO: Replace this with your own logic
        return lastname.length() > 1;
    }

    private boolean isFirstNameValid(String firstname) {
        //TODO: Replace this with your own logic
        return firstname.length() > 1;
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

    public class registerUserAsyncTask extends AsyncTask<Void, Void, Boolean> {
        Response result=new Response();
        int tId=0;
        String tEmail;
        String tPassword;
        String tFirstName;
        String tLastName;
        String tDob;
        String tPhone;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
            tEmail=vTxtemail.getText().toString();
            tPassword=vTxtpassword.getText().toString();
            tFirstName=vTxtFirstName.getText().toString();
            tLastName=vTxtLastName.getText().toString();
            tDob=vTxtDOB.getText().toString();
            tPhone=vTxtPhone.getText().toString();
            Log.e("AsyncTask", "onPreExecute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("AsyncTask", "doInBackground");

            String url = PathConfiguration.REGISTER;
            Log.d("debug", "url = " + url);
            try {

                API task=new API();
                task.SetParameter("email",tEmail);
                task.SetParameter("password",tPassword);
                task.SetParameter("first_name",tFirstName);
                task.SetParameter("last_name",tLastName);
                task.SetParameter("dob",tDob);
                task.SetParameter("phone",tPhone);
                result = task.DownloadPostContent(url);
                Log.d("debug", "result = " + result);
                if (result.Code==200){
                    //redirect to login
                    Log.d("debug", "response = " + result.Response);
                    JSONObject jsonObj = new JSONObject(result.Response);
                    tId = jsonObj.getInt("id");

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
            if (tId!=0) {
                Toast.makeText(RegisterActivity.this,"Register Success", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                finish();
                //startActivity(intent);
            }else{
                Toast.makeText(RegisterActivity.this,"Register Failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
