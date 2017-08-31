package com.troopar.trooparapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.troopar.trooparapp.adapter.viewholder.SearchedEventViewHolder;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.troopar.trooparapp.R;

/**
 * Created by Barry on 15/02/2016.
 * used in displaying the events in the search page
 */
public class ComplexSearchedEventInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<EventModel> events;
    private final int EVENT = 0, LOADMORE=3;
    private int lastVisibleItem, totalItemCount;
    private AtomicBoolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean noMore=false;


    public ComplexSearchedEventInfoAdapter(ArrayList<EventModel> activityModels,RecyclerView recyclerView) {
        Log.d("ComplexSearEventAdapter","ComplexSearchedEventInfoAdapter creation");
        loading=new AtomicBoolean(false);
        events = activityModels;
        final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = staggeredGridLayoutManager.getItemCount();
                lastVisibleItem = staggeredGridLayoutManager.findLastVisibleItemPositions(null)[1];//index of position start from zero
                if (noMore){return;}
                if (totalItemCount>0 && totalItemCount == (lastVisibleItem+1)) {// End has been reached
                    if (loading.compareAndSet(false,true)){
                        Log.d("ComplexSearEventAdapter",String.format("totalItemCount %d, lastVisibleItem %d",totalItemCount,lastVisibleItem));
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            }
        });
    }

    /**
     * This method creates different RecyclerView.ViewHolder objects based on the item view type.\
     *
     * @return viewHolder to be inflated
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case EVENT:
                View v1 = inflater.inflate(R.layout.item_searched_event, parent, false);
                viewHolder = new SearchedEventViewHolder(v1);
                break;
            default:
                View v = inflater.inflate(R.layout.progressbar_item, parent, false);
                viewHolder = new ProgressViewHolder(v);
                break;
        }
        return viewHolder;
    }

    /**
     * This method internally calls onBindViewHolder(ViewHolder, int) to update the
     * RecyclerView.ViewHolder contents with the item at the given position
     * and also sets up some private fields to be used by RecyclerView.
     *
     * @param position Item position in the viewgroup.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case EVENT:
                SearchedEventViewHolder vh2 = (SearchedEventViewHolder) holder;
                configureEventViewHolder(vh2, position);
                break;
            default:
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
                break;
        }
    }

    private void configureEventViewHolder(SearchedEventViewHolder vh, int position) {
        EventModel eventModel=events.get(position);
        String positionStr=Integer.toString(position);
        vh.setTvImageContainerDescription(positionStr);
        vh.getEvName().setText(eventModel.getName());
        String startTime=eventModel.getStartDate();
        String endTime=eventModel.getEndDate();
        String temp[]=startTime.split(" ");
        String[] startTimeDate=temp[0].split("-");
        String[] startTimeClock=temp[1].split(":");
        vh.getTvTime().setText(String.format("%s %s %s, %s:%s (%s)", startTimeDate[2], Tools.parseDateOfMonth(startTimeDate[1]),startTimeDate[0],startTimeClock[0],startTimeClock[1],Tools.calculateTimeRange(startTime,endTime)));
        String distanceKm=String.format("%.2f",((float)eventModel.getDistance())/1000);
        vh.getEventDistance().setText(String.format("%s km", distanceKm));
        vh.getEventPeopleJoinedProgress().setMax(eventModel.getMaxJoinedPeople() == 0 ? 10 : eventModel.getMaxJoinedPeople());
        vh.getEventPeopleJoinedProgress().setProgress(eventModel.getJoinedProgress());
        ImageView imageView=vh.getImage();
        try {
            if (Tools.isNullString(eventModel.getThumbnailImageUrl())){
                imageView.setImageResource(R.drawable.troopar_logo_red_square_t);
            }else{
                ImageDownloader.getInstance().getRequestManager().load(eventModel.getThumbnailImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        vh.setEventCategory(getCategoryImage(eventModel.getCategory()));
    }

    @Override
    public int getItemCount() {
        if (events==null){
            return 0;
        }
        return events.size();
    }

    @Override
    public int getItemViewType(int position) {
        EventModel tmp=events.get(position);
        if (tmp==null){return LOADMORE;}
        switch (tmp.getType()){
            case Constants.EVENT:
                return EVENT;
            default: return -1;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.loadMoreprogressBar);
        }
    }

    public void setLoaded() {
        loading.set(false);
    }

    public boolean setLoading(){
        return loading.compareAndSet(false,true);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }

    private int getCategoryImage(String category){
        switch (category){
            default:return R.drawable.event_category_icon;
        }
    }


}
