package com.troopar.trooparapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Barry on 10/02/2016.
 */
public class MyAppSharePreference {

    private SharedPreferences sharedPreferences;


    private static class MyAppSharePreferenceHolder{
        private static final MyAppSharePreference my=new MyAppSharePreference();
    }

    private MyAppSharePreference(){
        Log.d("MyAppSharePreference","MyAppSharePreference creation");
    }

    public static MyAppSharePreference getInstance(){
        return MyAppSharePreferenceHolder.my;
    }

    public void setSharedPreferences(Context mCtx) {
        sharedPreferences=mCtx.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    public String getStringValue(String key){
        return sharedPreferences.getString(key,"");
    }

    public void saveStringValue(String key,String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveMultipleStringValues(HashMap<String,String> keyVal){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> keys=keyVal.keySet();
        for (String key : keys){
            editor.putString(key,keyVal.get(key));
        }
        editor.apply();
    }

    public void releaseSharePreference(){
        sharedPreferences=null;
    }


}
