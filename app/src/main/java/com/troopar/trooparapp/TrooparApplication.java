package com.troopar.trooparapp;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
//import com.squareup.leakcanary.LeakCanary;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Barry on 20/06/2016.
 */
public class TrooparApplication extends android.support.multidex.MultiDexApplication{

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Log.d("TrooparApplication","TrooparApplication on create");
        ApplicationContextStore.getInstance().setContext(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        LeakCanary.install(this);
    }


}
