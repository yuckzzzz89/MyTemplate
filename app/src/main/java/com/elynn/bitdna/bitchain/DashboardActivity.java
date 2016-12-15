package com.elynn.bitdna.bitchain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Logic.AstncTask.GetFileTask;
import com.elynn.bitdna.bitchain.Logic.AstncTask.GetListDashboardTask;
import com.elynn.bitdna.bitchain.Logic.AstncTask.UploadFileTask;
import com.elynn.bitdna.bitchain.Logic.GeneralLogic;
import com.elynn.bitdna.bitchain.Logic.LoginLogic;
import com.elynn.bitdna.bitchain.Logic.PermissionManifest;
import com.elynn.bitdna.bitchain.Model.AppConfiguration;
import com.elynn.bitdna.bitchain.Model.DashboardAdapter;
import com.elynn.bitdna.bitchain.Model.Files;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.bugsnag.android.Bugsnag;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Files[] files;
    private ArrayList<String> myStringArray = new ArrayList<String>();
    //private String[] myStringArray;
    ListView mfilelistView;
    private Handler mHandler;
    private Files SelectedFile;

    private String PicturePath;
    private Bitmap bitmap;
    private ProgressDialog progress;

    private Uri selectedImage;
    private View mProgressView;
    private View mFormView;
    private DrawerLayout mDrawerLayout;
    private View mNavView;
    private View mtoolbar;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private MenuInflater inflater;

    private FloatingActionButton vfabGalery;
    private TextView vLblGalery;
    private FloatingActionButton vfabCamera;
    private TextView vLblCamera;
    private FloatingActionButton fab;

    private Menu dashboardMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bugsnag.init(this);
        LoginLogic.getValidLoginDevice(this);
        setContentView(R.layout.activity_dashboard);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PermissionManifest.verifyAllPermissions(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFloatingActionMenu();
                //Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, 2);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        vLblGalery=(TextView) findViewById(R.id.lblGalery);
        vfabGalery=(FloatingActionButton) findViewById(R.id.fabgalery);
        vfabGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionManifest.verifyStoragePermissions(DashboardActivity.this);
                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
                toggleFloatingActionMenu();
            }
        });

        vLblCamera=(TextView) findViewById(R.id.lblCamera);
        vfabCamera=(FloatingActionButton) findViewById(R.id.fabcamera);
        vfabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, 0);
                toggleFloatingActionMenu();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mfilelistView = (ListView) findViewById(R.id.file_list);
        mfilelistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                //String item = ((TextView)view).getText().toString();
                //Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();

                SelectedFile=files[position];
                Log.d("debug", "position = " + position);
                Log.d("debug", "id = " + id );
                Log.d("debug", "fileName = " + files[position].fileName );
            }
        });

        mfilelistView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener
                (){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int
                    position, long id) {
                String item = ((TextView)view).getText().toString();
                //Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
                SelectedFile=files[position];
                Log.d("debug", "position = " + position);
                Log.d("debug", "id = " + id );
                Log.d("debug", "fileName = " + files[position].fileName );
                return false;
            }
        });

        mFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.main_progress);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView =  findViewById(R.id.nav_view);
        mtoolbar =findViewById(R.id.toolbar);

        this.mHandler = new Handler();
        m_Runnable.run();
        myStringArray = new ArrayList<String>();
        //Log.d("debug", "onCreate initDashboard");
        //initDashboard();
        registerForContextMenu(mfilelistView);
    }

    private void toggleFloatingActionMenu(){
        vfabGalery.setVisibility(vfabGalery.getVisibility()==View.GONE ? vfabGalery.VISIBLE : vfabGalery.GONE);
        vLblGalery.setVisibility(vLblGalery.getVisibility()==View.GONE ? vLblGalery.VISIBLE : vLblGalery.GONE);
        vfabCamera.setVisibility(vfabCamera.getVisibility()==View.GONE ? vfabCamera.VISIBLE : vfabCamera.GONE);
        vLblCamera.setVisibility(vLblCamera.getVisibility()==View.GONE ? vLblCamera.VISIBLE : vLblCamera.GONE);
        mFormView.setAlpha(vfabGalery.getVisibility()==View.GONE ? (float)1:(float)0.2);
        mNavView.setAlpha(vfabGalery.getVisibility()==View.GONE ? (float)1:(float)0.2);
        mtoolbar.setAlpha(vfabGalery.getVisibility()==View.GONE ? (float)1:(float)0.2);

        Log.d("debug","getHeaderCount " + navigationView.getChildCount());
        if (vfabGalery.getVisibility()==View.GONE){
            registerForContextMenu(mfilelistView);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            dashboardMenu.findItem(R.id.action_change_password).setEnabled(true);
            dashboardMenu.findItem(R.id.action_settings).setEnabled(true);
            dashboardMenu.findItem(R.id.action_Logout).setEnabled(true);
        }else{
            unregisterForContextMenu(mfilelistView);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            dashboardMenu.findItem(R.id.action_change_password).setEnabled(false);
            dashboardMenu.findItem(R.id.action_settings).setEnabled(false);
            dashboardMenu.findItem(R.id.action_Logout).setEnabled(false);
        }
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            if (UserLogin.userId!=0) {
                Log.d("debug", "Runnable initDashboard");

                initDashboard();
                DashboardActivity.this.mHandler.postDelayed(m_Runnable, 60000);
            }else{
                Toast.makeText(getBaseContext(), "Cache has been cleared", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    };

    private void initDashboard(){
        GetListDashboardTask task=new GetListDashboardTask(DashboardActivity.this,getApplicationContext());
        task.mCallback=new GetListDashboardTask.Callback() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void onPostExecute(Boolean success, Files[] files) {
                if (success) {
                    DashboardAdapter myAdapter = new DashboardAdapter(DashboardActivity.this, files);
                    myAdapter.mCallback=new DashboardAdapter.Callback() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item, Files vSelectedFile) {
                            return onListDashboardItemSelected(item,vSelectedFile);
                        }
                    };
                    mfilelistView.setAdapter(myAdapter);
                    Log.d("debug setAdapter", "setAdapter = dashboardListAdapter");
                }else{
                    Log.d("debug setAdapter", "Refresh Failed");
                }
            }
        };
        task.ExecuteTask();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            PicturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image

            Log.d("debug", "selected picture"+PicturePath);
            //try {
            //    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //InputStream input = this.getContentResolver().openInputStream(selectedImage);
                //bitmap = BitmapFactory.decodeStream(input);
            //}catch (FileNotFoundException ex){

            //}catch (IOException ex){

            //}
            //Log.d("debug", "URI = "+selectedImage.toString());
            //bitmap =BitmapFactory.decodeFile(selectedImage.toString());
            //Bitmap bitmap = myView.getBitmap();
            //Log.d("debug", "bitmap Width = "+bitmap.getWidth());
            //Log.d("debug", "bitmap Height = "+bitmap.getHeight());
            PermissionManifest.verifyStoragePermissions(this);

            progress=new ProgressDialog(DashboardActivity.this);
            progress.setMessage("Uploading Image");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(false);
            progress.setMax(100);
            progress.setProgress(0);
            progress.setCancelable(false);
            progress.show();
            UploadFile();

            //UploadFileAsync task=new UploadFileAsync();
            //if(Build.VERSION.SDK_INT >= 11)
            //    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //else
            //    task.execute();
            //UploadFileAsync();

            // Show the Selected Image on ImageView
            //ImageView imageView = (ImageView) findViewById(R.id.imageViewGallery);
            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            //ImageView imageView = new ImageView(getApplicationContext());
            //imageView.setImageBitmap(BitmapFactory.decodeFile(PicturePath));

        }else if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = GeneralLogic.getImageUri(getApplicationContext(), imageBitmap);
            Log.d("debug", "tempUri" + tempUri.toString());

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));
            Log.d("debug", "finalFile" + finalFile.toString());
            PicturePath=getRealPathFromURI(tempUri);

            PermissionManifest.verifyStoragePermissions(this);

            progress=new ProgressDialog(DashboardActivity.this);
            progress.setMessage("Uploading Image");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(false);
            progress.setMax(100);
            progress.setProgress(0);
            progress.setCancelable(false);
            progress.show();
            UploadFile();
            //UploadFileAsync task=new UploadFileAsync();
            //if(Build.VERSION.SDK_INT >= 11)
            //    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //else
            //    task.execute();
        }
    }

    public void UploadFile(){
        UploadFileTask task=new UploadFileTask(this,getApplicationContext(),PicturePath);
        task.mCallback=new UploadFileTask.Callback() {
            @Override
            public void onProgressUpdate(int intprogress) {
                if (intprogress==100){
                    progress.hide();
                }else {
                    progress.setProgress(intprogress);
                }
            }

            @Override
            public void onPreExecute() {

            }

            @Override
            public void onPostExecute(Boolean success,String status) {
                if (status.equals("true")){
                    initDashboard();
                }else if(status.equals("failed")){
                    Toast.makeText(getBaseContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                }else if (status.equals("exists")){
                    Toast.makeText(getBaseContext(), "File Already Exists", Toast.LENGTH_SHORT).show();
                }else if (status.equals("Error Tierion")){
                    Toast.makeText(getBaseContext(), "Failed Uploading to Blockchain", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getBaseContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.ExecuteTask();
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        Log.d("debug", "cursor" + cursor.getString(idx));
        return cursor.getString(idx);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu_child, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        return onListDashboardItemSelected(item,SelectedFile);
    }

    private boolean onListDashboardItemSelected(MenuItem item,Files SelectedFile){
        Intent intent;
        switch (item.getItemId()) {
            case R.id.share:
                intent = new Intent(DashboardActivity.this, ShareActivity.class);
                intent.putExtra("fileId",SelectedFile.id);
                //finish();
                startActivity(intent);
                return true;
            case R.id.delete:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Are you sure want to delete "+SelectedFile.fileName+" file ?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DeleteFileTask();
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
                return true;
            case R.id.view:
                PermissionManifest.verifyStoragePermissions(DashboardActivity.this);
                GetFileTask task=new GetFileTask(this,getApplicationContext(),SelectedFile);
                task.mCallback=new GetFileTask.Callback() {
                    @Override
                    public void onPreExecute() {
                        showProgress(true);
                    }

                    @Override
                    public void onPostExecute(Bitmap bitmap) {
                        Log.d("debug", "onPostExecute Open With");
                        if (bitmap!=null) {
                            Uri targetUri = GeneralLogic.getImageUri(getApplicationContext(), bitmap);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(targetUri, "application/*");
                            showProgress(false);
                            startActivity(intent);
                        }else{
                            Toast.makeText(DashboardActivity.this,"File not found",Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }
                };
                task.ExecuteTask();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        LoginLogic.getValidLoginDevice(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LoginLogic.getValidLoginDevice(this);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater=getMenuInflater();
        inflater.inflate(R.menu.dashboard, menu);
        dashboardMenu=menu;
        initMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LoginLogic.getValidLoginDevice(this);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            //finish();
            startActivity(intent);
            return true;
        }else if (id == R.id.action_change_password) {
            Intent intent = new Intent(DashboardActivity.this, ChangePasswordActivity.class);
            //finish();
            startActivity(intent);
            return true;
        }else if (id == R.id.action_Logout) {
            LoginLogic.Logout();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        LoginLogic.getValidLoginDevice(this);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            //finish();
            startActivity(intent);
        } else if (id == R.id.nav_upload) {
            Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2);
        } else if (id == R.id.nav_camera) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent, 0);
        } else if (id == R.id.nav_change_password) {
            Intent intent = new Intent(DashboardActivity.this, ChangePasswordActivity.class);
            //finish();
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            LoginLogic.Logout();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initMenu(){
        Log.d("debug", "set init text");
        TextView mUserNameView = (TextView) findViewById(R.id.username);
        Log.d("debug", "set init mUserNameView");
        TextView mEmailView = (TextView) findViewById(R.id.email);
        Log.d("debug", "set init mEmailView");
        mUserNameView.setText(UserLogin.firstName+ " "+ UserLogin.lastName);
        Log.d("debug", "set init getName");
        mEmailView.setText(UserLogin.email);
        Log.d("debug", "set init email");
    }

    private void DeleteFileTask(){
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            Response result=new Response();
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(true);
                Log.e("AsyncTask", "onPreExecute");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Log.v("AsyncTask", "doInBackground");


                String url = PathConfiguration.DELETEFILE+"?id=" +  SelectedFile.id +"&filename=" + SelectedFile.fileName + "&userid=13&token="+ UserLogin.token;
                Log.d("debug", "url = " + url);
                try {
                    API task=new API();
                    result = task.DownloadContent(url);
                    Log.d("debug", "result = " + result);
                    if (result.Code==200){
                        //redirect to login
                        Log.d("debug", "response = " + result.Response);

                    }else{
                        Log.d("debug", "Login Failed");
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
                    showProgress(false);
                    myStringArray = new ArrayList<String>();
                    initDashboard();
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

    public class UploadFileAsync extends AsyncTask<Void, Void, Boolean> {
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
            if (status.equals("true")){
                initDashboard();
            }else if(status.equals("failed")){
                Toast.makeText(getBaseContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
            }else if (status.equals("exists")){
                Toast.makeText(getBaseContext(), "File Already Exists", Toast.LENGTH_SHORT).show();
            }else if (status.equals("Error Tierion")){
                Toast.makeText(getBaseContext(), "Failed Uploading to Blockchain", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Unknown Error", Toast.LENGTH_SHORT).show();
            }
        }

        public void doProgress(int value){
            intprogress=value*100/42;
            Log.d("debug", "value = " + value);
            Log.d("debug", "intprogress = " + intprogress);
            publishProgress();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (intprogress==100){
                progress.hide();
            }else {
                progress.setProgress(intprogress);
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            fab.setVisibility(show ? View.GONE : View.VISIBLE);
            vfabGalery.setVisibility(vfabGalery.GONE);
            vfabCamera.setVisibility(vfabCamera.GONE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    fab.setVisibility(show ? View.GONE : View.VISIBLE);
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
            fab.setVisibility(show ? View.GONE : View.VISIBLE);
            vfabGalery.setVisibility(vfabGalery.GONE);
            vfabCamera.setVisibility(vfabCamera.GONE);
        }
    }


}
