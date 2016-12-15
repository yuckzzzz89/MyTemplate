package com.elynn.bitdna.bitchain.Logic.AstncTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Model.Files;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Aryo on 18/11/2016.
 */

public class GetFileTask {
    private Context context;
    private Activity activity;
    private Files SelectedFile;
    private Bitmap bitmap;
    public Callback mCallback;

    public GetFileTask(Activity inActivity, Context inContext,Files inSelectedFile){
        this.context=inContext;
        this.activity=inActivity;
        this.SelectedFile=inSelectedFile;
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallback.onPreExecute();
            Log.e("AsyncTask", "onPreExecute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("AsyncTask", "doInBackground");

            String url = PathConfiguration.GETFILE+SelectedFile.id+"?token="+ UserLogin.token;
            Log.d("debug", "url = " + url);
            try {

                API task=new API();
                result = task.DownloadContent(url);
                Log.d("debug", "result = " + result);
                if (result.Code==200){
                    //redirect to login
                    Log.d("debug", "response = " + result.Response);
                    JSONObject jsonObj = new JSONObject(result.Response);
                    String imageUrl = jsonObj.getString("link");
                    bitmap=null;
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());

                }else{
                    Log.d("debug", "Login Failed");
                    return false;
                }
            } catch (IOException IOEx){
                Log.d("debug", "IOException = " + IOEx.getLocalizedMessage());
                return false;
            } catch (JSONException e){
                Log.d("debug", "JSONException = " + e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCallback.onPostExecute(bitmap);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public interface Callback{
        void onPreExecute();
        void onPostExecute(Bitmap bitmap);
    }
}
