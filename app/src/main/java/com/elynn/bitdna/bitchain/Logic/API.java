package com.elynn.bitdna.bitchain.Logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.elynn.bitdna.bitchain.DashboardActivity;
import com.elynn.bitdna.bitchain.Logic.AstncTask.UploadFileTask;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Aryo on 27/10/2016.
 */

public class API {
    private ArrayList<String> param = new ArrayList<String>();
    private ArrayList<String> val = new ArrayList<String>();
    int length = 100000;


    String attachmentName = "file";
    String crlf = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";
    public Callback mCallback;

    public interface Callback{
        void Progress(int progress);
    }

    public void SetParameter(String parameter,String value){
        param.add(parameter);
        val.add(value);
    }

    public Response multipost(String urlString,String PicturePath) throws IOException {

        mCallback.Progress(4);
        Response result=new Response();
        mCallback.Progress(5);
        InputStream is = null;
        mCallback.Progress(6);

        HttpURLConnection conn = null;
        mCallback.Progress(7);
        DataOutputStream dos = null;
        mCallback.Progress(8);
        String lineEnd = "\r\n";
        mCallback.Progress(9);
        String twoHyphens = "--";
        mCallback.Progress(10);
        String boundary = "*****";
        mCallback.Progress(11);
        int bytesRead, bytesAvailable, bufferSize;
        mCallback.Progress(12);
        byte[] buffer;
        mCallback.Progress(13);
        int maxBufferSize = 1 * 1024 * 1024;
        mCallback.Progress(14);
        File sourceFile = new File(PicturePath);
        mCallback.Progress(15);
        //String upLoadServerUri = PathConfiguration.UPLOADFILE+"?userid="+ UserLogin.userId +"&token="+ UserLogin.token;
        Log.d("debug", "PicturePath = " + PicturePath);

        // open a URL connection to the Servlet
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        mCallback.Progress(16);
        URL url = new URL(urlString);
        mCallback.Progress(17);

        // Open a HTTP connection to the URL
        conn = (HttpURLConnection) url.openConnection();
        mCallback.Progress(18);
        conn.setDoInput(true); // Allow Inputs
        mCallback.Progress(19);
        conn.setDoOutput(true); // Allow Outputs
        mCallback.Progress(20);
        conn.setUseCaches(false); // Don't use a Cached Copy
        mCallback.Progress(21);
        conn.setRequestMethod("POST");
        mCallback.Progress(22);
        conn.setRequestProperty("Connection", "Keep-Alive");
        mCallback.Progress(23);
        conn.setRequestProperty("ENCTYPE",
                "multipart/form-data");
        mCallback.Progress(24);
        conn.setRequestProperty("Content-Type",
                "multipart/form-data;boundary=" + boundary);
        mCallback.Progress(25);
        conn.setRequestProperty("file", PicturePath);
        mCallback.Progress(26);

        dos = new DataOutputStream(conn.getOutputStream());
        mCallback.Progress(27);

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        mCallback.Progress(28);
        dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                + PicturePath + "\"" + lineEnd);
        mCallback.Progress(29);

        dos.writeBytes(lineEnd);
        mCallback.Progress(30);

        // create a buffer of maximum size
        bytesAvailable = fileInputStream.available();
        mCallback.Progress(31);

        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        mCallback.Progress(32);
        buffer = new byte[bufferSize];
        mCallback.Progress(33);

        // read file and write it into form...
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        mCallback.Progress(34);

        Log.d("debug", "Write Input Stream ");
        Log.d("debug", "bufferSize = " + bufferSize);
        //int i =0;
        mCallback.Progress(35);
        while (bytesRead > 0) {
            //Log.d("debug", "Progress = " + i);
            dos.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math
                    .min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0,
                    bufferSize);
            //i++;
        }
        mCallback.Progress(36);

        // send multipart form data necesssary after file
        // data...
        dos.writeBytes(lineEnd);
        mCallback.Progress(37);
        dos.writeBytes(twoHyphens + boundary + twoHyphens
                + lineEnd);
        mCallback.Progress(38);

        // Responses from the server (code and message)
        result.Code = conn.getResponseCode();
        mCallback.Progress(39);
        String serverResponseMessage = conn.getResponseMessage();
        mCallback.Progress(40);
        is = conn.getInputStream();
        mCallback.Progress(41);
        result.Response = API.convertInputStreamToString(is);
        mCallback.Progress(42);

        Log.d("debug", "serverResponseCode = " + result.Code);
        if (result.Code == 200) {
            Log.d("debug", "serverResponseMessage = " + serverResponseMessage);
            Log.d("debug", "Response = " + result.Response);
        }else{

        }

        // close the streams //
        fileInputStream.close();
        dos.flush();
        dos.close();

        return result;
    }

    public Response DownloadPostContent(String myurl) throws IOException {
        Response result=new Response();
        InputStream is = null;

        try {
            //enter statements that can cause exceptions
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
                //conn.setRequestProperty(param.get(i),val.get(i));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<param.size();i++){
                sb.append(URLEncoder.encode(param.get(i), "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(val.get(i), "UTF-8"));
                if (i != param.size()-1) {
                    sb.append("&");
                }
            }
            Log.d("Debug", "The OutputStream is: " + sb.toString());
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            result.Code = conn.getResponseCode();
            Log.d("Debug", "The response is: " + result.Code);
            is = conn.getInputStream();

            int hasRead = 0;

            // Convert the InputStream into a string
            //result.Response = convertInputStreamToString(is, length);
            result.Response = convertInputStreamToString(is);
            Log.d("Debug", "The content is: " + result.Response);
        }catch(MalformedURLException ex) {
            //Handles an incorrectly entered URL
            result.Code=400;
            result.Response=ex.getMessage();
            Log.d("debug", "MalformedURLException = " + ex);
        }
        catch(SocketTimeoutException STEx) {
            //Handles URL access timeout.
            result.Code=400;
            result.Response=STEx.getMessage();
            Log.d("debug", "SocketTimeoutException = " + STEx);
        }
        catch (IOException IOEx) {
            //Handles input and output errors
            result.Code=400;
            result.Response=IOEx.getMessage();
            Log.d("debug", "IOException = " + IOEx);
        }finally {
            if (is != null) {
                is.close();
            }
        }
        return result;
    }

    public Response DownloadContent(String myurl) throws IOException {
        Response result=new Response();
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            result.Code = conn.getResponseCode();
            Log.d("Debug", "The response is: " + result.Code);
            is = conn.getInputStream();

            int hasRead = 0;

            // Convert the InputStream into a string
            //result.Response = convertInputStreamToString(is, length);
            result.Response = convertInputStreamToString(is);
            Log.d("Debug", "The content is: " + result.Response);
        }catch (IOException IOEx){
            result.Code=400;
            result.Response=IOEx.getMessage();
            Log.d("debug", "IOException = " + IOEx);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return result;
    }

    /*public String convertInputStreamToString(InputStream stream, int length) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[length];
        reader.read(buffer);
        return new String(buffer);
    }*/

    public static String convertInputStreamToString(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}
