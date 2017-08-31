package com.troopar.trooparapp.activity.fragment;

import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DisplayDiningsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayDiningsFragment extends Fragment {

    private static NearbyBaseFragment my;


    public static NearbyBaseFragment newInstance(int page, String title, int viewResId) {
        return my=NearbyBaseFragment.newInstance(page, title, viewResId);
    }

    public static NearbyBaseFragment getInstance(){
        return my;
    }


}
