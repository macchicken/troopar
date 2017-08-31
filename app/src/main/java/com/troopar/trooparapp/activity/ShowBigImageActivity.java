package com.troopar.trooparapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Tools;

public class ShowBigImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_big_image);
        Intent intent=getIntent();
        if (intent!=null){
            String original=intent.getStringExtra("original");
            if (!Tools.isNullString(original)){
                final ProgressBar progressBar= (ProgressBar) findViewById(R.id.pb_load_local);
                try{
                    Glide.with(ShowBigImageActivity.this).load(original).asBitmap().override(256,256).diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            ((ImageView)findViewById(R.id.originalImage)).setImageBitmap(resource);
                        }
                    });
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }
    }


}
