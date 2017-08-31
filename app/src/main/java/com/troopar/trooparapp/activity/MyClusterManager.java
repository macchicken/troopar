package com.troopar.trooparapp.activity;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterManager;

/**
 * Created by barry on 22/06/2016.
 */
public class MyClusterManager extends ClusterManager<MyMapMarkerItem> {

    private OnCameraChangeListener onCameraChangeListener;

    public MyClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        super.onCameraChange(cameraPosition);
        onCameraChangeListener.onCameraChange(cameraPosition);
    }

    public interface OnCameraChangeListener{
        void onCameraChange(CameraPosition cameraPosition);
    }

    public void setOnCameraChangeListener(OnCameraChangeListener onCameraChangeListener) {
        this.onCameraChangeListener = onCameraChangeListener;
    }


}
