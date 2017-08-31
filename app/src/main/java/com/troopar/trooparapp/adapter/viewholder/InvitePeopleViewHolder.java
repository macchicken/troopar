package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.troopar.trooparapp.R;

/**
 * Created by barry on 16/07/2016.
 */
public class InvitePeopleViewHolder extends RecyclerView.ViewHolder{

    private ImageView invitePeopleBtn;


    public InvitePeopleViewHolder(View itemView) {
        super(itemView);
        invitePeopleBtn= (ImageView) itemView.findViewById(R.id.invitePeopleBtn);
    }

    public void setImageSize(LinearLayout.LayoutParams layoutParams){
        invitePeopleBtn.setLayoutParams(layoutParams);
    }

}
