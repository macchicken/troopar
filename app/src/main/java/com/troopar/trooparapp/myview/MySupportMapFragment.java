package com.troopar.trooparapp.myview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Barry on 18/06/2016.
 */
public class MySupportMapFragment extends SupportMapFragment {

    private View mOriginalContentView;
    private TouchableWrapper touchableWrapper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        touchableWrapper = new TouchableWrapper(getActivity());
        touchableWrapper.addView(mOriginalContentView);
        return touchableWrapper;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

    public boolean isTouchUp(){
        return touchableWrapper.isTouchUp();
    }

    public void setTouchUp(boolean touchUp){
        touchableWrapper.setTouchUp(touchUp);
    }


}
