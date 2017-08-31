package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.myview.NineGridlayout;


/**
 * Created by Barry on 29/01/2016.
 * view holder for activities item
 */
public class ActivityViewHolder extends RecyclerView.ViewHolder {

    private TextView eventTitle;
    private TextView eventSnippet;
    private ImageView eventSmallImage;
    private ImageView userImage;
    private TextView eventReviewPoster;
    private TextView eventReviewPostTime;
    private TextView eventReviewContent;
    private TextView eventStartTime;
    private TextView eventPlace;
    private ImageView eventCategoryText;
    private TextView eventTotalLikes;
    private RelativeLayout eventImageContainer;
    private LinearLayout bottomPart;
    private TextView activityKeywords;
    private NineGridlayout userUploadPhotos;
    private View joinBtn,shareBtn,reviewBtn,likeBtn;
    private LinearLayout eventPart;
    private ProgressBar eventPeopleJoinedProgress;
    private TextView eventTotalJoiners,eventTotalShare,eventTotalReview;


    public ActivityViewHolder(View itemView) {
        super(itemView);
        eventTitle= (TextView) itemView.findViewById(R.id.eventTitle);
        eventSnippet= (TextView) itemView.findViewById(R.id.eventSnippet);
        eventSmallImage= (ImageView) itemView.findViewById(R.id.eventSmallImage);
        userImage= (ImageView) itemView.findViewById(R.id.userImage);
        eventReviewPoster= (TextView) itemView.findViewById(R.id.eventReviewPoster);
        eventReviewPostTime= (TextView) itemView.findViewById(R.id.eventReviewPostTime);
        eventReviewContent= (TextView) itemView.findViewById(R.id.eventReviewContent);
        eventStartTime= (TextView) itemView.findViewById(R.id.eventStartTime);
        eventPlace= (TextView) itemView.findViewById(R.id.eventPlace);
        eventCategoryText= (ImageView) itemView.findViewById(R.id.eventCategoryText);
        eventTotalLikes= (TextView) itemView.findViewById(R.id.eventTotalLikes);
        activityKeywords= (TextView) itemView.findViewById(R.id.activityKeywords);
        eventImageContainer= (RelativeLayout) itemView.findViewById(R.id.eventImageContainer);
        bottomPart= (LinearLayout) itemView.findViewById(R.id.bottomPart);
        eventPart= (LinearLayout) itemView.findViewById(R.id.eventPart);
        userUploadPhotos= (NineGridlayout) itemView.findViewById(R.id.userUploadPhotos);
        joinBtn= itemView.findViewById(R.id.eventJoinBtn);
        shareBtn= itemView.findViewById(R.id.eventShareBtn);
        reviewBtn= itemView.findViewById(R.id.reviewBtn);
        likeBtn= itemView.findViewById(R.id.likeBtn);
        eventPeopleJoinedProgress= (ProgressBar) itemView.findViewById(R.id.eventPeopleJoinedProgress);
        eventTotalJoiners= (TextView) itemView.findViewById(R.id.eventTotalJoiners);
        eventTotalShare= (TextView) itemView.findViewById(R.id.eventTotalShare);
        eventTotalReview= (TextView) itemView.findViewById(R.id.eventTotalReview);
    }

    public TextView getEventReviewContent() {
        return eventReviewContent;
    }

    public TextView getEventReviewPoster() {
        return eventReviewPoster;
    }

    public TextView getEventReviewPostTime() {
        return eventReviewPostTime;
    }

    public ImageView getEventSmallImage() {
        return eventSmallImage;
    }

    public TextView getEventSnippet() {
        return eventSnippet;
    }

    public TextView getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(TextView eventTitle) {
        this.eventTitle = eventTitle;
    }

    public void setCategoryImage(int resId){
        if (eventCategoryText!=null){
            eventCategoryText.setImageResource(resId);
        }
    }

    public void setTotalLikes(String text){
        if (eventTotalLikes!=null){
            eventTotalLikes.setText(text);
        }
    }

    public TextView getEventPlace() {
        return eventPlace;
    }

    public TextView getEventStartTime() {
        return eventStartTime;
    }

    public ImageView getUserImage() {
        return userImage;
    }

    public void setUserImage(ImageView userImage) {
        this.userImage = userImage;
    }

    public TextView getActivityKeywords() {
        return activityKeywords;
    }

    public NineGridlayout getUserUploadPhotos() {
        return userUploadPhotos;
    }

    public void setShareBtnDescription(String value){
        shareBtn.setContentDescription(value);
    }

    public void setReviewBtnDescription(String value){
        reviewBtn.setContentDescription(value);
    }

    public void setEventImageContainerDescription(String value){
        eventImageContainer.setContentDescription(value);
    }

    public void setJoinBtnDescription(String value){
        joinBtn.setContentDescription(value);
    }

    public void setJoinBtnState(int resId){
        joinBtn.setBackgroundResource(resId);
    }

    public void setLikeBtnDescription(String value){
        likeBtn.setContentDescription(value);
    }

    public void setUserImageDescription(String value){
        userImage.setContentDescription(value);
    }

    public void hideBottomPart() {
        if (bottomPart.getVisibility()==View.VISIBLE){
            bottomPart.setVisibility(View.GONE);
        }
    }

    public void hideEventPart() {
        if (eventPart.getVisibility()==View.VISIBLE){
            eventPart.setVisibility(View.GONE);
        }
    }

    public void hideEventPeopleJoinedProgress(){
        if (eventPeopleJoinedProgress!=null&&eventPeopleJoinedProgress.getVisibility()==View.VISIBLE){
            eventPeopleJoinedProgress.setVisibility(View.GONE);
        }
    }

    public void setEventPeopleJoinedProgress(int max,int progress){
        if (eventPeopleJoinedProgress!=null){
            eventPeopleJoinedProgress.setMax(max);
            eventPeopleJoinedProgress.setProgress(progress);
        }
    }

    public void setTotalJoiners(String text){
        if (eventTotalJoiners!=null){
            eventTotalJoiners.setText(text);
        }
    }

    public void setTotalReview(String text){
        if (eventTotalReview!=null){
            eventTotalReview.setText(text);
        }
    }

    public void setTotalShare(String text){
        if (eventTotalShare!=null){
            eventTotalShare.setText(text);
        }
    }

    public void setUserImageWithColor(int imageResId,int colorResId){
        userImage.setImageResource(imageResId);
        userImage.setBackgroundColor(this.itemView.getContext().getResources().getColor(colorResId));
    }


}
