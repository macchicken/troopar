package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.troopar.trooparapp.R;

/**
 * Created by Barry on 8/07/2016.
 */
public class NearbyUsersViewHolder extends RecyclerView.ViewHolder{

    private TextView tvUserName;
    private ImageView userHead;
    private LinearLayout userItemContainer;


    public NearbyUsersViewHolder(View itemView){
        super(itemView);
        tvUserName = (TextView) itemView.findViewById(R.id.tv_username);
        userHead = (ImageView) itemView.findViewById(R.id.iv_userhead);
        userItemContainer= (LinearLayout) itemView.findViewById(R.id.userItemContainer);
    }

    public TextView getTvUserName() {
        return tvUserName;
    }

    public ImageView getUserHead() {
        return userHead;
    }

    public void setItemPosition(int position){
        userItemContainer.setContentDescription(String.valueOf(position));
    }

    public void setImageSize(LinearLayout.LayoutParams layoutParams){
        userHead.setLayoutParams(layoutParams);
    }

    public void setViewMargin(LinearLayout.LayoutParams itemLayoutParams){
        this.itemView.setLayoutParams(itemLayoutParams);
    }

    public void setUserImageWithColor(int imageResId,int colorResId){
        userHead.setImageResource(imageResId);
        userHead.setBackgroundColor(this.itemView.getContext().getResources().getColor(colorResId));
    }

}
