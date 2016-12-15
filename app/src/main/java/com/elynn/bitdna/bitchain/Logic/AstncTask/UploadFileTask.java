package com.elynn.bitdna.bitchain.Logic.AstncTask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.Files;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Aryo on 18/11/2016.
 */

public class UploadFileTask {
    private Context context;
    private Activity activity;
    public Callback mCallback;
    private String PicturePath;

    public UploadFileTask(Activity inActivity, Context inContext,String PicturePath){
        this.context=inContext;
        this.activity=inActivity;
        this.PicturePath=PicturePath;
    }

    public void ExecuteTask(){
        Task task=new Task();
        if(Build.VERSION.SDK_INT >= 11)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    public class Task extends AsyncTask<Void, Void, Boolean> {
        Response result=new Response();
        String status="";
        int intprogress=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showProgress(true);
            Log.e("AsyncTask", "onPreExecute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("AsyncTask", "doInBackground");

            doProgress(0);
            String url = PathConfiguration.UPLOADFILE+"?userid="+ UserLogin.userId +"&token="+ UserLogin.token;
            doProgress(2);
            Log.d("debug", "url = " + url);
            try {
                doProgress(2);
                API task=new API();
                task.mCallback=new API.Callback() {
                    @Override
                    public void Progress(int progress) {
                        doProgress(progress);
                    }
                };
                doProgress(3);
                result = task.multipost(url,PicturePath);
                doProgress(0);
                //publishProgress();
                Log.d("debug", "result = " + result);
                if (result.Code==200){
                    //redirect to login
                    Log.d("debug", "response = " + result.Response);
                    JSONObject jsonObj = new JSONObject(result.Response);
                    status = jsonObj.getString("success");

                }else{
                    Log.d("debug", "Login Failed");
                    return false;
                }
                intprogress=100;
                publishProgress();
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
            //showProgress(false);
            Log.d("debug", "status = " + status);
            mCallback.onPostExecute(success,status);
        }

        public void doProgress(int value){
            intprogress=value*100/42;
            Log.d("debug", "value = " + value);
            Log.d("debug", "intprogress = " + intprogress);
            publishProgress();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            mCallback.onProgressUpdate(intprogress);
        }
    }

    public interface Callback{
        void onProgressUpdate(int progress);
        void onPreExecute();
        void onPostExecute(Boolean success,String status);
    }
}
