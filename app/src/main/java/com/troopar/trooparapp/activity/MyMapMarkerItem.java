package com.troopar.trooparapp.activity;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.troopar.trooparapp.model.EventModel;

/**
 * Created by Barry on 21/06/2016.
 */
public class MyMapMarkerItem implements ClusterItem {

    private final LatLng mPosition;
    private EventModel eventModel;
    private Bitmap bitmap;

    public MyMapMarkerItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public EventModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


}
