package com.troopar.trooparapp.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

/**
 * Created by Jin on 7/07/2016.
 */
public class ImageDownloader {
    private static ImageDownloader ourInstance = new ImageDownloader();
    private Context context;

    public static ImageDownloader getInstance() {
        return ourInstance;
    }

    private ImageDownloader() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void releaseContext(){
        this.context=null;
    }

    public RequestManager getRequestManager(){
        return Glide.with(context);
    }

}
