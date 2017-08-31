package com.troopar.trooparapp.activity.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.troopar.trooparapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DisplayEntertainmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayEntertainmentFragment extends Fragment {

    private static NearbyBaseFragment my;


    public static NearbyBaseFragment newInstance(int page, String title, int viewResId) {
        return my=NearbyBaseFragment.newInstance(page, title, viewResId);
    }

    public static NearbyBaseFragment getInstance(){
        return my;
    }


}
