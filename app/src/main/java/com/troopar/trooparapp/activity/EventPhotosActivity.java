package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;

import com.troopar.trooparapp.adapter.EventPhotoWallAdapter;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.model.UploadPhoto;

public class EventPhotosActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("EventPhotosActivity","EventPhotosActivity on create");
        setContentView(R.layout.activity_event_photos);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        GridView mPhotoWall = (GridView) findViewById(R.id.photo_wall);
        Intent tintent=getIntent();
        if (tintent!=null){
            if (tintent.hasExtra("images")){
                UploadPhoto[] images = (UploadPhoto[]) tintent.getSerializableExtra("images");
                EventPhotoWallAdapter adapter = new EventPhotoWallAdapter(this, 0, images, mPhotoWall);
                mPhotoWall.setAdapter(adapter);
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


}
