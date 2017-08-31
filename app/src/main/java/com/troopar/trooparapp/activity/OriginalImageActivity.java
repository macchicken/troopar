package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.troopar.trooparapp.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;

import com.troopar.trooparapp.utils.JSONParser;

public class OriginalImageActivity extends AppCompatActivity {

    private String imageId;
    private boolean isLiked;
    private int arrIndex;
    private String userId;
    private String deviceId;
    private String signature;
    private String url= BuildConfig.API_READHOST+"/review/like_review.php";
    private JSONParser jParser = new JSONParser();
    private ImageView likeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_original_image);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        likeBtn= (ImageView) findViewById(R.id.likeBtn);
        userId=Constants.USERID;
        deviceId=Constants.DEVEICEIDVALUE;
        signature=Constants.SIGNATUREVALUE;
        Intent tintent=getIntent();
        if (tintent!=null){
            imageId=tintent.getStringExtra("imageId");
            isLiked=tintent.getBooleanExtra("isLiked", false);
            arrIndex=tintent.getIntExtra("arrIndex", 0);
            try {
                Glide.with(OriginalImageActivity.this).load(tintent.getStringExtra("originalImageUrl")).asBitmap().override(256,256).diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ((ImageView)findViewById(R.id.originalImageContainer)).setImageBitmap(resource);
                    }
                });
            }catch (Throwable t){
                t.printStackTrace();
            }
            if (isLiked){
                ((TextView)findViewById(R.id.likeCount)).setText("1");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void likePhotoAction(View v){
        AsyncTask<Void,Void,Boolean> task=new AsyncTask<Void, Void,Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                likeBtn.setClickable(false);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    HashMap<String,String> parameters3=new HashMap<>();
                    parameters3.put("itemId", imageId);
                    parameters3.put("userId", userId);
                    parameters3.put("type", "photo");
                    parameters3.put(Constants.EQUIPID, deviceId);
                    parameters3.put(Constants.SIGNATURE, signature);
                    JSONObject obj=jParser.makeRequestForHttp(url,"POST",parameters3);
                    if (obj!=null){
                        try {
                            return Constants.TAG_SUCCESS.equals(obj.get("status"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (likeBtn!=null){
                    likeBtn.setClickable(true);
                    if (!isLiked){
                        ((TextView)findViewById(R.id.likeCount)).setText("1");
                        isLiked=true;
                    }else{
                        ((TextView)findViewById(R.id.likeCount)).setText("0");
                        isLiked=false;
                    }
                    Intent data=new Intent(Constants.eventPhotosBroadcastId);
                    data.putExtra("isLiked",isLiked);
                    data.putExtra("arrIndex",arrIndex);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(data);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }


}
