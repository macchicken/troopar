package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.m4m.MediaComposer;

/**
 * Created by Barry on 11/06/2016.
 */
public class MediaProgressListener implements org.m4m.IProgressListener{

    private org.m4m.MediaComposer mediaComposer;
    private SendVideoTask sendVideoTask;


    public MediaProgressListener() {
    }

    @Override
    public void onMediaStart() {
        Log.d("MediaProgressListener ","onMediaStart");
    }

    @Override
    public void onMediaProgress(float progress) {
        Log.d("MediaProgressListener ",String.format("onMediaProgress %f",progress));
    }

    @Override
    public void onMediaDone() {
        Log.d("MediaProgressListener ","onMediaDone");
        mediaComposer.stop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sendVideoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            sendVideoTask.execute();
        }
    }

    @Override
    public void onMediaPause() {

    }

    @Override
    public void onMediaStop() {
        Log.d("MediaProgressListener ","onMediaStop");
    }

    @Override
    public void onError(Exception exception) {
        exception.printStackTrace();
        mediaComposer.stop();
    }

    public void setMediaComposer(MediaComposer mediaComposer) {
        this.mediaComposer = mediaComposer;
    }

    public void setSendVideoTask(SendVideoTask sendVideoTask) {
        this.sendVideoTask = sendVideoTask;
    }


}
