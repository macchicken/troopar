package com.troopar.trooparapp.utils;

import android.annotation.TargetApi;
import android.view.View;

/**
 * Created by Barry on 6/07/2016.
 */
@TargetApi(16)
public class SDK16 {

    public static void postOnAnimation(View view, Runnable r) {
        view.postOnAnimation(r);
    }

}
