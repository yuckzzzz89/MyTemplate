package com.elynn.bitdna.bitchain.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.DashboardActivity;
import com.elynn.bitdna.bitchain.Logic.AstncTask.GetFileTask;
import com.elynn.bitdna.bitchain.Logic.GeneralLogic;
import com.elynn.bitdna.bitchain.Logic.PermissionManifest;
import com.elynn.bitdna.bitchain.R;
import com.elynn.bitdna.bitchain.ShareActivity;

/**
 * Created by Aryo on 27/10/2016.
 */

public class DashboardAdapter  extends BaseAdapter {
    //String [] result;
    private DashboardActivity activity;
    private Context context;
    //int [] imageId;
    private Files[] listFiles;
    private ContextMenu menu;
    private static LayoutInflater inflater=null;
    public Callback mCallback;

    public DashboardAdapter(DashboardActivity mainActivity, Files[] files) {
        // TODO Auto-generated constructor stub
        //result=prgmNameList;
        context=mainActivity;
        activity=mainActivity;
        //imageId=prgmImages;
        listFiles=files;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listFiles.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View rowView;
        rowView = inflater.inflate(R.layout.row_dashboard, null);
        TextView txtfilename=(TextView) rowView.findViewById(R.id.filename);
        TextView desc=(TextView) rowView.findViewById(R.id.desc);
        txtfilename.setText(listFiles[position].fileName);
        if (listFiles[position].file_status_id==1){
            desc.setText("(Need Approval)");
        }else if(listFiles[position].file_status_id==2){
            desc.setText("(Approved) "+listFiles[position].remark);
        }else if(listFiles[position].file_status_id==3) {
            desc.setText("(Rejected) " + listFiles[position].remark);
        }else{
            desc.setText("");
        }
        ImageButton mbtnListDashboardMenu = (ImageButton) rowView.findViewById(R.id.btnListDashboardMenu);

        txtfilename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GetFileTask task=new GetFileTask(activity,context.getApplicationContext(),listFiles[position]);
                task.mCallback=new GetFileTask.Callback() {
                    @Override
                    public void onPreExecute() {
                        activity.showProgress(true);
                    }

                    @Override
                    public void onPostExecute(Bitmap bitmap) {
                        Log.d("debug", "onPostExecute Open With");
                        if (bitmap!=null) {
                            Uri targetUri = GeneralLogic.getImageUri(context.getApplicationContext(), bitmap);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(targetUri, "application/*");
                            activity.showProgress(false);
                            activity.startActivity(intent);
                        }else{
                            Toast.makeText(context,"File not found",Toast.LENGTH_SHORT).show();
                            activity.showProgress(false);
                        }
                    }
                };
                task.ExecuteTask();
            }
        });

        mbtnListDashboardMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(activity, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mCallback.onMenuItemClick(item,listFiles[position]);
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.dashboard_menu_child);
                popupMenu.show();
            }
        });
        return rowView;
    }

    public interface Callback{
        boolean onMenuItemClick(MenuItem item,Files SelectedFile);
    }
}
