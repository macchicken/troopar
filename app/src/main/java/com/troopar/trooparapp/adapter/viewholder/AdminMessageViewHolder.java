package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Tools;

/**
 * Created by Barry on 10/08/2016.
 * used in display the admin messages
 */
public class AdminMessageViewHolder extends RecyclerView.ViewHolder{

    private TextView userName;
    private CircleImageView userHead;
    private TextView messageContent;
    private TextView messageTime;


    public AdminMessageViewHolder(View itemView) {
        super(itemView);
        userName= (TextView) itemView.findViewById(R.id.tv_username);
        messageContent= (TextView) itemView.findViewById(R.id.tv_message);
        messageTime= (TextView) itemView.findViewById(R.id.tv_messageTime);
        userHead= (CircleImageView) itemView.findViewById(R.id.iv_userhead);
    }

    public void setUserName(String text){
        userName.setText(text);
    }

    public void setMessageContent(String text){
        messageContent.setText(text);
    }

    public void setMessageTime(String text){
        messageTime.setText(text);
    }

    public void setUserHead(String userHeadUrl){
        if (Tools.isNullString(userHeadUrl)){
            userHead.setImageResource(R.drawable.user_image);
            userHead.setColorFilter(this.itemView.getResources().getColor(R.color.appMainColor));
        }else{
            Glide.with(this.itemView.getContext()).load(userHeadUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(userHead);
        }
    }


}
