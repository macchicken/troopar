package com.troopar.trooparapp.adapter.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.troopar.trooparapp.R;

/**
 * Created by Barry on 03/02/2016.
 */
public class EventViewHolder extends RecyclerView.ViewHolder{

    private TextView evTime;
    private TextView evCategory;
    private TextView evName;
    private ImageView image,tvImageCover;
    private ImageView userImage;
    private TextView eventDistance;
    private ProgressBar eventPeopleJoinedProgress;
    private TextView eventJoinedPeople;
    private TextView eventDaysToGo;
    private TextView eventCost;
    private TextView requireType;
    private TextView eventPosterName;
    private TextView eventPostedTime;
    private LinearLayout listOfJoiners;
    private View joinBtn,shareBtn;


    public EventViewHolder(View itemView) {
        super(itemView);
        evName= (TextView) itemView.findViewById(R.id.tvEventName);
        image= (ImageView) itemView.findViewById((R.id.tvImageView));
        tvImageCover= (ImageView) itemView.findViewById((R.id.tvImageCover));
        userImage= (ImageView) itemView.findViewById(R.id.userImage);
        evTime= (TextView) itemView.findViewById(R.id.tvTime);
        evCategory= (TextView) itemView.findViewById(R.id.tvCategory);
        eventDistance= (TextView) itemView.findViewById(R.id.eventDistance);
        eventPosterName= (TextView) itemView.findViewById(R.id.eventPosterName);
        eventPostedTime= (TextView) itemView.findViewById(R.id.eventPostedTime);
        evCategory.setPaintFlags(evCategory.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        joinBtn= itemView.findViewById(R.id.joinBtn);
        shareBtn= itemView.findViewById(R.id.eventShareBtn);
        eventPeopleJoinedProgress= (ProgressBar) itemView.findViewById(R.id.eventPeopleJoinedProgress);
        eventJoinedPeople= (TextView) itemView.findViewById(R.id.eventJoinedPeople);
        eventDaysToGo= (TextView) itemView.findViewById(R.id.eventDaysToGo);
        eventCost= (TextView) itemView.findViewById(R.id.eventCost);
        requireType= (TextView) itemView.findViewById(R.id.requireType);
        listOfJoiners= (LinearLayout) itemView.findViewById(R.id.listOfJoiners);
    }

    public TextView getEvCategory() {
        return evCategory;
    }

    public TextView getEvName() {
        return evName;
    }

    public TextView getEvTime() {
        return evTime;
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

    public ProgressBar getEventPeopleJoinedProgress() {
        return eventPeopleJoinedProgress;
    }

    public TextView getEventCost() {
        return eventCost;
    }

    public TextView getRequireType() {
        return requireType;
    }

    public TextView getEventDaysToGo() {
        return eventDaysToGo;
    }

    public TextView getEventJoinedPeople() {
        return eventJoinedPeople;
    }

    public void setJoinedBtnText(boolean joinedEvent) {
    }

    public TextView getEventPosterName() {
        return eventPosterName;
    }

    public TextView getEventPostedTime() {
        return eventPostedTime;
    }

    public ImageView getUserImage() {
        return userImage;
    }

    public void setUserImageWithColor(int imageResId,int color){
        userImage.setImageResource(imageResId);
        userImage.setBackgroundColor(color);
    }

    public void setUserImage(ImageView userImage) {
        this.userImage = userImage;
    }

    public LinearLayout getListOfJoiners() {
        listOfJoiners.removeAllViews();
        return listOfJoiners;
    }

    public void setShareBtnDescription(String value){
        shareBtn.setContentDescription(value);
    }

    public void setTvImageCoverDescription(String value){
        tvImageCover.setContentDescription(value);
    }

    public void setJoinBtnDescription(String value){
        joinBtn.setContentDescription(value);
    }

    public void setJoinBtnState(int resId){
        joinBtn.setBackgroundResource(resId);
    }

    public void setUserImageDescription(String value){
        userImage.setContentDescription(value);
    }

    public Context getCtx(){
        return this.itemView.getContext();
    }


}
