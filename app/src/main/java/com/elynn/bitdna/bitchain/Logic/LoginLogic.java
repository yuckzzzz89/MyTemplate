package com.elynn.bitdna.bitchain.Logic;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.elynn.bitdna.bitchain.DashboardActivity;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Aryo on 01/11/2016.
 */

public class LoginLogic {
    public static void Logout(){
        UserLogin.token = "";
        UserLogin.userId = 0;
        UserLogin.email = "";
        UserLogin.firstName = "";
        UserLogin.lastName = "";
        UserLogin.dob = "";
        UserLogin.mobilePhone = "";
        UserLogin.google2FAsecret = "";
        UserLogin.enable = false;
        UserLogin.validSecret = false;
    }

    private static Activity currentActivity;

    public static boolean isValidLoginDevice;
    public static void getValidLoginDevice(Activity current){
        currentActivity=current;
        ValidLoginDeviceTask();
    }

    private static void ValidLoginDeviceTask(){
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


                String url = PathConfiguration.CHECKREGISTEREDDEVICEID+"?userid=" + UserLogin.userId + "&deviceid="+ UserLogin.deviceId;
                try {
                    API task=new API();
                    result = task.DownloadContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code==200){
                        //redirect to login
                        JSONObject jsonObj = new JSONObject(result.Response);
                        UserLogin.isRemoteKick = jsonObj.getInt("result")==1;
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
                Log.v("debug", "Logic onPostExecute");

                if (success) {
                    if (UserLogin.isRemoteKick){
                        Log.d("debug", "Remote Kick");
                        LoginLogic.Logout();
                        currentActivity.finish();
                    }
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
}
