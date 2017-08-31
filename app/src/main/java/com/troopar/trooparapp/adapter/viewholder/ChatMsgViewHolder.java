package com.troopar.trooparapp.adapter.viewholder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.R;



/**
 * Created by Barry on 4/06/2016.
 */
public class ChatMsgViewHolder {

    private TextView tvSendTime;
    private TextView tvContent;
    private CircleImageView userHead;
    private TextView tvUsername;
    private RelativeLayout videoviewContainer;
    private TextView eventTitle;
    private ImageView eventImage;


    public ChatMsgViewHolder(View convertView,String type) {
        tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
        if ("event".equals(type)){
            eventTitle= (TextView) convertView.findViewById(R.id.eventTitle);
            tvContent= (TextView) convertView.findViewById(R.id.tv_chatcontent);
            eventImage= (ImageView) convertView.findViewById(R.id.eventImage);
        }else if (!"video".equals(type)){
            tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
        }else{
            videoviewContainer= (RelativeLayout) convertView.findViewById(R.id.videoviewContainer);
        }
        userHead= (CircleImageView) convertView.findViewById(R.id.iv_userhead);
        tvUsername= (TextView) convertView.findViewById(R.id.tv_username);
    }

    public CircleImageView getUserHead() {
        return userHead;
    }

    public void setUserHeadWithBitmap(Bitmap bitmap){
        userHead.setImageBitmap(bitmap);
    }

    public void setUserHeadWithImageResource(int resourceId){
        userHead.setImageResource(resourceId);
    }

    public void setUserHearBackground(int color){
        userHead.setBackgroundColor(color);
    }

    public void setTvSendTime(String tvSendTimeStr){
        tvSendTime.setText(tvSendTimeStr);
    }

    public void setTvContent(String tvContentStr){
        tvContent.setText(tvContentStr);
    }

    public void setTvContentWithCompoundDrawables(int left,int top,int right,int bottom){
        tvContent.setCompoundDrawablesWithIntrinsicBounds(left,top,right,bottom);
    }

    public void setTvContentWithCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom){
        tvContent.setCompoundDrawablesWithIntrinsicBounds(left,top,right,bottom);
    }

    public void setTvContentPosition(String position){
        tvContent.setContentDescription(position);
    }

    public void setVideoViewContainerPosition(String position){
        videoviewContainer.setContentDescription(position);
    }

    public void setThumbnailImage(Drawable drawable){
        videoviewContainer.setBackground(drawable);
    }

    public void setUserName(String userName){
        tvUsername.setText(userName);
    }

    public void setEventTitle(String text){
        eventTitle.setText(text);
    }

    public void setEventDescription(String text,String eventId){
        tvContent.setText(text);
        eventImage.setContentDescription(eventId);
    }

    public ImageView getEventImage(){
        return eventImage;
    }


}
