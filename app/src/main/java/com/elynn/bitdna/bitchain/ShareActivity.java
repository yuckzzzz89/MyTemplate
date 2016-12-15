package com.elynn.bitdna.bitchain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.Files;
import com.elynn.bitdna.bitchain.Model.FileShare;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ShareActivity extends AppCompatActivity {

    private int fileId;
    private FileShare[] fileShares;
    private FileShare selectedFileShares;
    private View mProgressView;
    private View mMainFormView;
    private ListView muserlistView;
    private ArrayList<String> myStringArray = new ArrayList<String>();
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();

        mMainFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.progress_form);
        mEmailView =(EditText) findViewById(R.id.txtemail);
        mPasswordView =(EditText) findViewById(R.id.txtpassword);
        mMessageView =(EditText) findViewById(R.id.txtmessage);
        fileId=intent.getIntExtra("fileId",0);
        Button btnShare= (Button) findViewById(R.id.btnshare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                ShareFileTask();
            }
        });
        Button btnCancel= (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        muserlistView = (ListView) findViewById(R.id.ListUser);
        initUserShareListTask();

        registerForContextMenu(muserlistView);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_menu_revoke, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.revoke:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Are you sure want to revoke ?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DeleteShareTask();
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Toast.makeText(getBaseContext(), "No", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                //Toast.makeText(getBaseContext(), info.position, Toast.LENGTH_LONG).show();
                //deleteNote(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void DeleteShareTask(){
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e("AsyncTask", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Response result=new Response();
                Log.v("AsyncTask", "doInBackground");

                String url = PathConfiguration.REVOKEFILE+"?shareId=" +  selectedFileShares.id + "&token="+ UserLogin.token;

                Log.d("debug", "url = " + url);
                try {
                    API task=new API();
                    result = task.DownloadContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code==200){
                        //redirect to login
                        Log.d("debug", "response = " + result.Response);

                    }else{
                        Log.d("debug", "Failed");
                        return false;
                    }
                } catch (IOException IOEx){
                    Log.d("debug", "IOException = " + IOEx);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                Log.v("debug", "onPostExecute");

                if (success) {
                    myStringArray = new ArrayList<String>();
                    Toast.makeText(getBaseContext(), "Revoke Success", Toast.LENGTH_SHORT).show();
                    initUserShareListTask();
                }else{
                    Toast.makeText(getBaseContext(), "Revoke Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onCancelled() {
                Log.d("debug", "On canceled");
            }
        };

        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    private void initUserShareListTask(){

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                myStringArray = new ArrayList<String>();
                Log.e("debug", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.
                Log.d("debug", "Start background Task");
                Response result=new Response();
                String url = PathConfiguration.LISTSHARED+fileId+"?token="+UserLogin.token;
                Log.d("debug", "url = " + url);
                API task=new API();
                try {
                    result = task.DownloadContent(url);

                    if (result.Code==200){
                        //redirect to login
                        //showProgress(false);
                        Log.d("debug", "response = " + result.Response);

                        JSONArray jsonArr= new JSONArray(result.Response);
                        fileShares=new FileShare[jsonArr.length()];
                        for(int i=0; i<jsonArr.length(); i++){
                            Log.d("debug", "iterate " + i );
                            JSONObject jsonObj= new JSONObject(jsonArr.getString(i));
                            Log.d("debug", "jsonObj " + i + jsonObj.toString() );
                            FileShare data=new FileShare();
                            data.id=jsonObj.getInt("id");
                            //data.fileid=jsonObj.getInt("fileid");
                            data.email=jsonObj.getString("email");
                            //data.fileName=jsonObj.getString("fileName");
                            fileShares[i]=data;
                            myStringArray.add(data.email);
                        }

                    }else{
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        Log.d("debug", "OTP Failed");
                        return false;
                    }
                } catch (IOException IOEx){
                    Log.d("debug", "IOException = " + IOEx);
                    return false;
                }catch (JSONException e){
                    Log.d("debug", "JSONException = " + e);
                    return false;
                }
                // TODO: register the new account here.
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                Log.d("debug", "on Post Execute");
                if (success) {
                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ShareActivity.this, android.R.layout.simple_list_item_1, myStringArray);
                    muserlistView.setAdapter(myAdapter);

                    muserlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            //String item = ((TextView)view).getText().toString();
                            //Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();

                            selectedFileShares=fileShares[position];
                            Log.d("debug", "position = " + position);
                            Log.d("debug", "id = " + id );
                            Log.d("debug", "fileName = " + fileShares[position].id );
                        }
                    });

                    muserlistView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener
                            (){
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int
                                position, long id) {
                            String item = ((TextView)view).getText().toString();
                            //Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
                            selectedFileShares=fileShares[position];
                            Log.d("debug", "position = " + position);
                            Log.d("debug", "id = " + id );
                            Log.d("debug", "fileName = " + fileShares[position].id );
                            return false;
                        }
                    });
                }else{
                }
            }

            @Override
            protected void onCancelled() {
                Log.d("debug", "On canceled");
            }
        };

        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();

    }

    private void ShareFileTask(){
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private String Temail;
            private String Tpassword;
            private String Tmessage;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Temail=mEmailView.getText().toString();
                Tpassword=mPasswordView.getText().toString();
                Tmessage=mMessageView.getText().toString();
                Log.e("AsyncTask", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Response result=new Response();
                Log.v("AsyncTask", "doInBackground");

                String url = PathConfiguration.SHAREFILE + fileId +"?token="+ UserLogin.token ;
                Log.d("debug", "url = " + url);
                try {
                    API task=new API();
                    task.SetParameter("email",Temail);
                    task.SetParameter("password",Tpassword);
                    task.SetParameter("message",Tmessage);
                    result = task.DownloadPostContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code==200){
                        //redirect to login
                        Log.d("debug", "response = " + result.Response);
                    }else{
                        Log.d("debug", "Failed");
                        return false;
                    }
                } catch (IOException IOEx){
                    Log.d("debug", "IOException = " + IOEx);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                Log.v("debug", "onPostExecute");
                if (success) {
                    Toast.makeText(getBaseContext(), "Share Success", Toast.LENGTH_SHORT).show();
                    mEmailView.setText("");
                    mPasswordView.setText("");
                    mMessageView.setText("");
                    initUserShareListTask();
                }else{
                    Toast.makeText(getBaseContext(), "Share Failed", Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
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

            mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
