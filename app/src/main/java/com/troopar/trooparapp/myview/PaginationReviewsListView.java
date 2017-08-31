package com.troopar.trooparapp.myview;

/**
 * Created by Barry on 14/01/2016.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.troopar.trooparapp.R;

public class PaginationReviewsListView extends ListView implements OnScrollListener {

    private View footerView;
    private int totalItemCount = 0;
    private int lastVisibleItem = 0;
    private boolean isLoading = false;

    public PaginationReviewsListView(Context context) {
        super(context);
        initView(context);
    }

    public PaginationReviewsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PaginationReviewsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater mInflater = LayoutInflater.from(context);
        footerView = mInflater.inflate(R.layout.morereviews_view_footer, null);
        this.setOnScrollListener(this);
        this.addFooterView(footerView);
        Log.d("PaginationReviewsList","PaginationReviewsListView init view");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d("PaginationReviewsList", String.format("onScrollStateChanged lastVisibleItem:%s totalItemCount:%s scrollState:%s\n", lastVisibleItem, totalItemCount, scrollState));
        if(lastVisibleItem == totalItemCount && scrollState == SCROLL_STATE_IDLE){
            if(!isLoading){
                isLoading = true;
                footerView.setVisibility(View.VISIBLE);
                startToLoad();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    private OnLoadListener onLoadListener;
    public void setOnLoadListener(OnLoadListener onLoadListener){
        this.onLoadListener = onLoadListener;
    }

    public interface OnLoadListener{
        void onLoad();
    }

    public void loadComplete(){
        footerView.setVisibility(View.GONE);
        isLoading = false;
        this.invalidate();
        Log.d("PaginationReviewsList","PaginationReviewsListView load complete");
    }

    private void startToLoad(){
        Log.d("PaginationReviewsList", "PaginationReviewsListView start to load");
        onLoadListener.onLoad();
    }


}
