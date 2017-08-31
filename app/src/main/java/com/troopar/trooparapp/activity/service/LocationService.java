package com.troopar.trooparapp.activity.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Barry on 12/04/2016.
 */
public class LocationService {
    private static LocationService ourInstance = new LocationService();
    private Location myLocation;

    public static LocationService getInstance() {
        return ourInstance;
    }

    private LocationService() {
        Log.d("LocationService","LocationService creation");
    }

    public Location getLastKnownLocation() {
        Location bestLocation = null;
        // flag for GPS status
        boolean isGPSEnabled;
        // flag for network status
        boolean isNetworkEnabled;
        try {
            LocationManager locationManager = (LocationManager) ApplicationContextStore.getInstance().getContext().getSystemService(Context.LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("LocationService", "no network provider is enabled");
            } else {
                if (isNetworkEnabled) {
                    Log.d("LocationService", "LOC Network Enabled");
                    try{
                        bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }catch (SecurityException e){
                        e.printStackTrace();
                        bestLocation=null;
                    }
                }
                if (isGPSEnabled&&bestLocation == null) {// if GPS Enabled get lat/long using GPS Services
                    Log.d("LocationService", "RLOC: GPS Enabled");
                    try{
                        bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }catch (SecurityException e){
                        e.printStackTrace();
                        bestLocation=null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bestLocation!=null&&(myLocation==null||myLocation.distanceTo(bestLocation)>500)){
            myLocation=new Location(bestLocation);
            String userId=Constants.USERID;
            if (Tools.isNullString(userId)){
                return bestLocation;
            }
            UpdateLocationToServer task= new UpdateLocationToServer(bestLocation, userId, Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
        return bestLocation;
    }

    public LatLngBounds getLatLngBoundsWithinRange(LatLng myCurrLatLng,int radius){
        return new LatLngBounds(SphericalUtil.computeOffset(myCurrLatLng, radius * Math.sqrt(2.0), 225), SphericalUtil.computeOffset(myCurrLatLng, radius * Math.sqrt(2.0), 45));
    }

    private static class UpdateLocationToServer extends AsyncTask<Void,Void,Void>{

        private String mUserId;
        private String mEquipId;
        private String mSignature;
        private Location myCurrLatLng;

        public UpdateLocationToServer(Location mCurrLatLng,String userId,String equipId,String signature) {
            myCurrLatLng = mCurrLatLng;
            mUserId=userId;
            mEquipId=equipId;
            mSignature=signature;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("LocationService",String.format("LocationService UpdateLocationToServer location [%s]",myCurrLatLng));
            Response response = null;
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new FormBody.Builder()
                    .add("userId", mUserId)
                    .add("myLatitude",Double.toString(myCurrLatLng.getLatitude()))
                    .add("myLongitude", Double.toString(myCurrLatLng.getLongitude()))
                    .add(Constants.EQUIPID, mEquipId)
                    .add(Constants.SIGNATURE, mSignature)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/user/update_user_location.php").post(formBody).build();
            try {
                response=client.newCall(request).execute();
                Log.d("LocationService",String.format("LocationService UpdateLocationToServer %s",response.body().string()));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (response!=null){
                    response.body().close();
                }
            }
            return null;
        }
    }


}
