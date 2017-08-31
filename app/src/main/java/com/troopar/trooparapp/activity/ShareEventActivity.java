package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.task.ShareEventTask;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Draws;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareEventActivity extends AppCompatActivity {

    private EditText sharingDescription;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_event);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trooparevent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sendevent:
                Log.d("ShareEventActivity", "share a event");
                String description=sharingDescription.getText().toString();
                if (Tools.isNullString(description)){
                    Toast.makeText(ShareEventActivity.this, "enter some descriptions", Toast.LENGTH_SHORT).show();
                    return true;
                }
                try {
                    ShareEventTask task=new ShareEventTask(eventId,description);
                    final ProgressDialog[] progressDialog = new ProgressDialog[1];
                    task.setExecuteCallBack(new ShareEventTask.ExecuteCallBack() {
                        @Override
                        public void onPreExecute() {
                            progressDialog[0] =ProgressDialog.show(ShareEventActivity.this, "Sharing...", "Sharing a event :)", false, false);
                        }
                        @Override
                        public void onProgressUpdate(Integer value) {
                            progressDialog[0].setProgress(value);
                        }
                        @Override
                        public void onPostExecute(JSONObject jsonObject) {
                            progressDialog[0].dismiss();
                            try {
                                if (jsonObject==null||Tools.isNullString(jsonObject.getString("message"))){
                                    Toast.makeText(ShareEventActivity.this,"server with no return", Toast.LENGTH_LONG).show();
                                }else{
                                    String message=jsonObject.getString("status");
                                    if (Constants.TAG_SUCCESS.equals(message)){
                                        Toast.makeText(ShareEventActivity.this,"you shared a event", Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                    }else{
                                        Toast.makeText(ShareEventActivity.this,message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    runAsyncTask(task);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return true;
            case android.R.id.home:
                setResult(RESULT_OK, null);
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void runAsyncTask(ShareEventTask task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("ShareEventActivity", "ShareEventActivity on post resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ShareEventActivity", "ShareEventActivity on stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ShareEventActivity", "ShareEventActivity on destroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ShareEventActivity", "ShareEventActivity on pause");
    }

    private void initView(){
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent intent=getIntent();
        EventModel eventModel;
        if (intent==null){
            return;
        }else{
            eventModel= (EventModel) intent.getSerializableExtra("eventModel");
            if (eventModel==null){
                return;
            }
        }
        TextView eventTitle= (TextView) findViewById(R.id.eventTitle);
        TextView eventCategory= (TextView) findViewById(R.id.eventCategory);
        ImageView eventCategoryText= (ImageView) findViewById(R.id.eventCategoryText);
        TextView eventSnippet= (TextView) findViewById(R.id.eventSnippet);
        TextView eventSnippet2= (TextView) findViewById(R.id.eventSnippet2);
        sharingDescription= (EditText) findViewById(R.id.sharingDescription);
        ImageView eventImageView= (ImageView) findViewById(R.id.eventImageView);
        if (Tools.isNullString(eventModel.getThumbnailImageUrl())){
            eventImageView.setImageResource(R.drawable.troopar_logo_red_square_t);
        }else{
            ImageDownloader.getInstance().getRequestManager().load(eventModel.getThumbnailImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(eventImageView);
        }
        eventTitle.setText(eventModel.getName());
        String eventStartDate =eventModel.getStartDate();
        String eventEndDate =eventModel.getEndDate();
        String startDateOfWeek = Tools.getDateOfWeekFromDate(eventStartDate);
        eventCategoryText.setImageResource(getCategoryImage(eventModel.getCategory()));
        eventSnippet.setText(String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate)));
        eventSnippet2.setText(Html.fromHtml("<u>" + eventModel.getLocation() + "</u>"));
        eventCategory.setText(Html.fromHtml("<u>" + eventModel.getCategory() + "</u>"));
        eventId=String.valueOf(eventModel.getId());
    }

    private int getCategoryImage(String category){
        switch (category){
            default:return R.drawable.event_category_icon;
        }
    }


}
