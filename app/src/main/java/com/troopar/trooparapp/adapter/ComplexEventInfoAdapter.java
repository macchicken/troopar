package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.activity.UserDetailActivity;
import com.troopar.trooparapp.adapter.viewholder.EventViewHolder;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import com.troopar.trooparapp.R;

import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;

/**
 * Created by Barry on 29/01/2016.
 */
public class ComplexEventInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<EventModel> events;
    private final int EVENT = 0, LOADMORE=3;
    private int lastVisibleItem, totalItemCount, firstVisibleItem;
    private AtomicBoolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private ViewGroup.MarginLayoutParams marginLayoutParams;
    private ViewGroup.LayoutParams layoutParams;
    private boolean noMore=false;


    public ComplexEventInfoAdapter(ArrayList<EventModel> activityModels, RecyclerView recyclerView) {
        events = activityModels;
        loading=new AtomicBoolean(false);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();//index of position start from zero
                firstVisibleItem=linearLayoutManager.findFirstVisibleItemPosition();
                if (noMore){return;}
                if (totalItemCount>0 && totalItemCount == (lastVisibleItem+2)) {// End has been reached
                    if (loading.compareAndSet(false,true)){
                        Log.d("ComplexEventInfoAdapter",String.format("totalItemCount %d, lastVisibleItem %d, firstVisibleItem %d",totalItemCount,lastVisibleItem,firstVisibleItem));
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            }
        });
        layoutParams=new ViewGroup.LayoutParams(150, 150);//TODO value for different screen resolution
        marginLayoutParams=new ViewGroup.MarginLayoutParams(170, 170);//TODO value for different screen resolution
        marginLayoutParams.setMargins(6, 6, 6, 6);
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
                View v1 = inflater.inflate(R.layout.item_event, parent, false);
                int width = Constants.WidthPixels - 120;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 30, 0, 30);
                v1.setLayoutParams(layoutParams);
                viewHolder = new EventViewHolder(v1);
                break;
            default:
                View v = inflater.inflate(R.layout.loadingprogressbar_item, parent, false);
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
                EventViewHolder vh2 = (EventViewHolder) holder;
                configureEventViewHolder(vh2, position);
                break;
            default:
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
                break;
        }
    }

    private void configureEventViewHolder(EventViewHolder vh, int position) {
        final Context context=vh.getCtx();
        String positionStr=Integer.toString(position);
        vh.setShareBtnDescription(positionStr);
        vh.setTvImageCoverDescription(positionStr);
        vh.setJoinBtnDescription(positionStr);
        vh.setUserImageDescription(positionStr);
        EventModel eventModel=events.get(position);
        User user=eventModel.getUser();
        vh.getEvName().setText(eventModel.getName());
        String eventStartDate=eventModel.getStartDate();
        String eventEndDate=eventModel.getEndDate();
        String startDateOfWeek= Tools.getDateOfWeekFromDate(eventStartDate);
//        System.out.println(eventStartDate);
//        System.out.println(startDateOfWeek);
        vh.getEvTime().setText(String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate)));
        vh.getEvCategory().setText(Html.fromHtml(" <u>" + eventModel.getCategory() + "</u>"));
        vh.getEvCategory().setTextColor(Color.parseColor(eventModel.getColorHex()));
        try{
            String userImagePath=user.getAvatarStandard();
            if (!Tools.isNullString(userImagePath)){
                ImageDownloader.getInstance().getRequestManager().load(userImagePath).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(vh.getUserImage());
            }else{
                vh.setUserImageWithColor(R.drawable.user_image,context.getResources().getColor(R.color.appElementColor));
            }
            ImageView backgroundView=vh.getImage();
            if (Tools.isNullString(eventModel.getThumbnailImageUrl())){
                ImageDownloader.getInstance().getRequestManager().load(R.drawable.troopar_logo_red_square_t).diskCacheStrategy(DiskCacheStrategy.ALL).into(backgroundView);
            }else{
                ImageDownloader.getInstance().getRequestManager().load(eventModel.getMediumImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(backgroundView);
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        if (eventModel.isJoined()){
            vh.setJoinBtnState(R.drawable.bg_oval_for_event_joined_with_gradient);
        }else{
            vh.setJoinBtnState(R.drawable.bg_oval_for_event_with_gradient);
        }
        String distanceKm=String.format("%.2f",((float)eventModel.getDistance())/1000);
        vh.getEventDistance().setText(String.format("%s km", distanceKm));
        vh.getEventPeopleJoinedProgress().setMax(eventModel.getMaxJoinedPeople() == 0 ? 10 : eventModel.getMaxJoinedPeople());
        vh.getEventPeopleJoinedProgress().setProgress(eventModel.getJoinedProgress());
        vh.getEventJoinedPeople().setText(String.format("%s/%s", String.valueOf(eventModel.getJoinedProgress()), String.valueOf(eventModel.getMaxJoinedPeople())));
        Calendar now= Calendar.getInstance();
        int month=now.get(Calendar.MONTH)+1;
        String temp=String.format("%s-%s-%s 00:00:00",String.valueOf(now.get(Calendar.YEAR)),month<10?"0"+String.valueOf(month):String.valueOf(month),now.get(Calendar.DAY_OF_MONTH)<10?"0"+String.valueOf(now.get(Calendar.DAY_OF_MONTH)):String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
        vh.getEventDaysToGo().setText(String.valueOf(Tools.calculateTimeDiffByDay(eventStartDate, temp)));
        String displayName=Tools.isNullString(user.getFirstName())&&Tools.isNullString(user.getLastName())?user.getUserName():String.format("%s %s", user.getFirstName(), user.getLastName());
        vh.getEventPosterName().setText(String.format("%s",displayName));
        vh.getEventPostedTime().setText(String.format("%s", Tools.formatTime(eventModel.getCreatedDate())));
        vh.getEventCost().setText(eventModel.getRequires().equals("")?"Free":eventModel.getRequires());
        vh.getRequireType().setText(eventModel.getRequireType().equals("")?"Entry fee":eventModel.getRequireType());
        final LinearLayout listOfJoiners=vh.getListOfJoiners();
        ArrayList<User> joiners=eventModel.getJoiners();
        if (joiners.size()==0){
            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(marginLayoutParams);
            listOfJoiners.addView(layout);
            return;
        }
        try{
            for (final User joiner:joiners){
                final LinearLayout layout = new LinearLayout(context);
                layout.setLayoutParams(marginLayoutParams);
                if (Tools.isNullString(joiner.getAvatarStandard())){
                    CircleImageView imageView = new CircleImageView(context);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setImageResource(R.drawable.user_image);
                    imageView.setBackgroundColor(context.getResources().getColor(R.color.appElementColor));
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context1=v.getContext();
                            Intent intent = new Intent(context1,UserDetailActivity.class);
                            intent.putExtra("user", joiner);
                            context1.startActivity(intent);
                        }
                    });
                    layout.addView(imageView);
                    listOfJoiners.addView(layout);
                    continue;
                }
                ImageDownloader.getInstance().getRequestManager().load(joiner.getAvatarStandard()).asBitmap().override(70,70).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {//TODO value for different screen resolution
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        CircleImageView imageView = new CircleImageView(context);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageBitmap(resource);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Context context1 = v.getContext();
                                Intent intent = new Intent(context1, UserDetailActivity.class);
                                intent.putExtra("user", joiner);
                                context1.startActivity(intent);
                            }
                        });
                        layout.addView(imageView);
                        listOfJoiners.addView(layout);
                    }
                });
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
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

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.loadMoreProgressBar);
        }
    }

    public void setLoaded() {
        loading.set(false);
    }

    public boolean setLoading(){
        return loading.compareAndSet(false,true);
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


}
