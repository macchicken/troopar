package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.OriginalImageActivity;
import com.troopar.trooparapp.model.UploadPhoto;

/**
 * Created by Barry on 2/02/2016.
 */
public class EventPhotoWallAdapter extends ArrayAdapter<UploadPhoto> implements AbsListView.OnScrollListener {

    private GridView mPhotoWall;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private boolean isFirstEnter = true;
    private UploadPhoto[] mEventsPhotos;


    public EventPhotoWallAdapter(Context context, int textViewResourceId, UploadPhoto[] eventsPhotos, GridView photoWall) {
        super(context, textViewResourceId, eventsPhotos);
        Log.d("EventPhotoWallAdapter","EventPhotoWallAdapter creation");
        mPhotoWall = photoWall;
        mPhotoWall.setOnScrollListener(this);
        mEventsPhotos=eventsPhotos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UploadPhoto t=getItem(position);
        String smallImagePath = t.getSmallImagePath();
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.eventphoto_layout, null);
        } else {
            view = convertView;
        }
        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        Glide.with(getContext()).load(smallImagePath).into(photo);
        final int arrIndex=position;
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx=v.getContext();
                Intent intent = new Intent(v.getContext(), OriginalImageActivity.class);
                intent.putExtra("originalImageUrl",  t.getPhotoPath());
                intent.putExtra("imageId", t.getId());
                intent.putExtra("isLiked", (mEventsPhotos[arrIndex]).isLiked());
                intent.putExtra("arrIndex", arrIndex);
                ctx.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        if (isFirstEnter && visibleItemCount > 0) {
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                String smallImagePath=(mEventsPhotos[i]).getSmallImagePath();
                ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(smallImagePath);
                if (imageView != null) {
                    Glide.with(getContext()).load(smallImagePath).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
