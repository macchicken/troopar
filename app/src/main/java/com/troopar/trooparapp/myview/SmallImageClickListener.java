package com.troopar.trooparapp.myview;

import android.content.Intent;
import android.view.View;

import com.troopar.trooparapp.activity.ShowBigImageActivity;

/**
 * Created by Jin on 10/07/2016.
 */
public class SmallImageClickListener implements View.OnClickListener {

    private String mOriginal;


    SmallImageClickListener(String original){
        mOriginal=original;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), ShowBigImageActivity.class);
        intent.putExtra("original",mOriginal);
        v.getContext().startActivity(intent);
    }


}
