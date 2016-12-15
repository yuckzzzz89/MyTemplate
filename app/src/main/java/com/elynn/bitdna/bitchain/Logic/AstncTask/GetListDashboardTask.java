package com.elynn.bitdna.bitchain.Logic.AstncTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.elynn.bitdna.bitchain.DashboardActivity;
import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.DashboardAdapter;
import com.elynn.bitdna.bitchain.Model.Files;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Aryo on 18/11/2016.
 */

public class GetListDashboardTask {
    private Context context;
    private Activity activity;
    private Files[] files;
    public Callback mCallback;

    public GetListDashboardTask(Activity inActivity, Context inContext){
        this.context=inContext;
        this.activity=inActivity;
    }

    public void ExecuteTask(){
        Task task=new Task();
        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    public class Task extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallback.onPreExecute();
            //myStringArray=new ArrayList<>();
            Log.e("debug", "onPreExecute");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Log.d("debug", "Start background Task");
            Response result=new Response();
            String url = PathConfiguration.GETFILES+"?token="+UserLogin.token;
            Log.d("debug", "url = " + url);
            API task=new API();
            try {
                result = task.DownloadContent(url);

                if (result.Code==200){
                    Log.d("debug", "response = " + result.Response);

                    JSONArray jsonArr= new JSONArray(result.Response);
                    files=new Files[jsonArr.length()];
                    for(int i=0; i<jsonArr.length(); i++){
                        Log.d("debug", "iterate " + i );
                        JSONObject jsonObj= new JSONObject(jsonArr.getString(i));
                        Log.d("debug", "jsonObj " + i + jsonObj.toString() );
                        Files data=new Files();
                        data.id=jsonObj.getInt("id");
                        data.userId=jsonObj.getInt("user_id");
                        data.fileParent=jsonObj.getInt("file_parent");
                        if (data.userId==UserLogin.userId){
                            data.fileName=jsonObj.getString("file_name");
                        }else{
                            data.fileName=jsonObj.getString("file_name")+"(shared)";
                        }
                        data.depth=jsonObj.getInt("depth");
                        data.file_status_id=jsonObj.getInt("file_status_id");
                        if (jsonObj.isNull("approval_user_id")){
                            data.approval_user_id=0;
                        }else{
                            data.depth=jsonObj.getInt("approval_user_id");
                        }
                        if (jsonObj.isNull("remark")) {
                            data.remark="";
                        }else{
                            data.remark=jsonObj.getString("remark");
                        }
                        files[i]=data;
                        //myStringArray.add(data.fileName);
                    }
                }else{
                    //Intent intent = getIntent();
                    //finish();
                    //startActivity(intent);
                    Log.d("debug", "Connection Failed");
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
            mCallback.onPostExecute(success,files);
        }

        @Override
        protected void onCancelled() {
            Log.d("debug", "On canceled");
        }
    }

    public interface Callback{
        void onPreExecute();
        void onPostExecute(Boolean success,Files[] files);
    }
}


//old Logic

    /*private void initDashboard(){

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                myStringArray=new ArrayList<>();
                Log.e("debug", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.
                Log.d("debug", "Start background Task");
                Response result=new Response();
                String url = PathConfiguration.GETFILES+"?token="+UserLogin.token;
                Log.d("debug", "url = " + url);
                API task=new API();
                try {
                    result = task.DownloadContent(url);

                    if (result.Code==200){
                        //redirect to login
                        //showProgress(false);
                        Log.d("debug", "response = " + result.Response);

                        JSONArray jsonArr= new JSONArray(result.Response);
                        files=new Files[jsonArr.length()];
                        for(int i=0; i<jsonArr.length(); i++){
                            Log.d("debug", "iterate " + i );
                            JSONObject jsonObj= new JSONObject(jsonArr.getString(i));
                            Log.d("debug", "jsonObj " + i + jsonObj.toString() );
                            Files data=new Files();
                            data.id=jsonObj.getInt("id");
                            data.userId=jsonObj.getInt("user_id");
                            data.fileParent=jsonObj.getInt("file_parent");
                            if (data.userId==UserLogin.userId){
                                data.fileName=jsonObj.getString("file_name");
                            }else{
                                data.fileName=jsonObj.getString("file_name")+"(shared)";
                            }
                            data.depth=jsonObj.getInt("depth");
                            data.file_status_id=jsonObj.getInt("file_status_id");
                            if (jsonObj.isNull("approval_user_id")){
                                data.approval_user_id=0;
                            }else{
                                data.depth=jsonObj.getInt("approval_user_id");
                            }
                            if (jsonObj.isNull("remark")) {
                                data.remark="";
                            }else{
                                data.remark=jsonObj.getString("remark");
                            }
                            files[i]=data;
                            myStringArray.add(data.fileName);
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
                    //ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(DashboardActivity.this, android.R.layout.simple_list_item_1, myStringArray);
                    //ArrayAdapter<File> myAdapter = new ArrayAdapter<File>(DashboardActivity.this, android.R.layout.simple_list_item_1, files);

                    DashboardAdapter myAdapter = new DashboardAdapter(DashboardActivity.this, files);

                    //ListView mfilelistView = (ListView) findViewById(R.id.file_list);
                    //mfilelistView.setAdapter(new DashboardAdapter(this, prgmNameList,prgmImages));
                    //mfilelistView.setAdapter(myAdapter);
                    mfilelistView.setAdapter(myAdapter);
                    Log.d("debug setAdapter", "setAdapter = dashboardListAdapter");


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

    }*/