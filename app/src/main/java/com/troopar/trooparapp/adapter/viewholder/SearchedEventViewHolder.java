package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.troopar.trooparapp.R;

/**
 * Created by Barry on 03/02/2016.
 */
public class SearchedEventViewHolder extends RecyclerView.ViewHolder{

    private TextView evName;
    private TextView tvTime;
    private ImageView image,eventCategory;
    private RelativeLayout tvImageContainer;
    private TextView eventDistance;
    private ProgressBar eventPeopleJoinedProgress;


    public SearchedEventViewHolder(View itemView) {
        super(itemView);
        evName= (TextView) itemView.findViewById(R.id.tvEventName);
        tvTime= (TextView) itemView.findViewById(R.id.tvTime);
        image= (ImageView) itemView.findViewById((R.id.tvImageView));
        tvImageContainer= (RelativeLayout) itemView.findViewById(R.id.tvImageContainer);
        eventDistance= (TextView) itemView.findViewById(R.id.eventDistance);
        eventPeopleJoinedProgress= (ProgressBar) itemView.findViewById(R.id.eventPeopleJoinedProgress);
        eventCategory= (ImageView) itemView.findViewById(R.id.eventCategory);
    }


    public TextView getEvName() {
        return evName;
    }

    public void setEvName(TextView evName) {
        this.evName = evName;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public TextView getEventDistance() {
        return eventDistance;
    }

    public void setEventDistance(TextView eventDistance) {
        this.eventDistance = eventDistance;
    }

    public ProgressBar getEventPeopleJoinedProgress() {
        return eventPeopleJoinedProgress;
    }

    public void setEventPeopleJoinedProgress(ProgressBar eventPeopleJoinedProgress) {
        this.eventPeopleJoinedProgress = eventPeopleJoinedProgress;
    }

    public RelativeLayout getTvImageContainer() {
        return tvImageContainer;
    }

    public void setTvImageContainer(RelativeLayout tvImageContainer) {
        this.tvImageContainer = tvImageContainer;
    }

    public TextView getTvTime() {
        return tvTime;
    }

    public void setTvTime(TextView tvTime) {
        this.tvTime = tvTime;
    }

    public void setTvImageContainerDescription(String value){
        tvImageContainer.setContentDescription(value);
    }

    public void setEventCategory(int resId){
        if (eventCategory!=null){
            eventCategory.setImageResource(resId);
        }
    }


}
