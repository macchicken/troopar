package com.troopar.trooparapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.viewholder.InvitePeopleViewHolder;
import com.troopar.trooparapp.adapter.viewholder.NearbyUsersViewHolder;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;

/**
 * Created by Barry on 8/07/2016.
 * used in display nearby users and group users page
 */
public class NearybyUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<User> users;
    private int viewId;
    private final int ADDPEOPLE=1;
    private LinearLayout.LayoutParams layoutParams;
    private LinearLayout.LayoutParams itemLayoutParams;
    private boolean staggered;


    public NearybyUsersAdapter(ArrayList<User> users,int viewId,int dipValue,boolean staggered) {
        this.users = users;
        this.viewId = viewId;
        this.staggered=staggered;
        int layoutSize=(int) (dipValue * Constants.DENSITYSCALE + 0.5f);
        int itemLayoutSize=(int) (30 * Constants.DENSITYSCALE + 0.5f);
        int itemBottomLayoutSize=(int) (16 * Constants.DENSITYSCALE + 0.5f);
        layoutParams=new LinearLayout.LayoutParams(layoutSize, layoutSize);
        itemLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemLayoutParams.setMargins(0, itemLayoutSize, 0, itemBottomLayoutSize);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType!=ADDPEOPLE){
            View view = LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false);
            return new NearbyUsersViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_people_layout,parent,false);
            return new InvitePeopleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType=holder.getItemViewType();
        if (itemType==ADDPEOPLE){
            InvitePeopleViewHolder invitePeopleViewHolder=(InvitePeopleViewHolder)holder;
            invitePeopleViewHolder.setImageSize(layoutParams);
        }else{
            NearbyUsersViewHolder nearbyUsersViewHolder=(NearbyUsersViewHolder) holder;
            User user=users.get(position);
            if (user==null){return;}
            nearbyUsersViewHolder.setItemPosition(position);
            nearbyUsersViewHolder.getTvUserName().setText(String.format("%s %s",user.getFirstName(),user.getLastName()));
            nearbyUsersViewHolder.setImageSize(layoutParams);
            if (Tools.isNullString(user.getAvatarOrigin())){
                nearbyUsersViewHolder.setUserImageWithColor(R.drawable.user_image,R.color.appElementColor);
            }else{
                ImageDownloader.getInstance().getRequestManager().load(user.getAvatarOrigin()).override(60,60).diskCacheStrategy(DiskCacheStrategy.ALL).into(nearbyUsersViewHolder.getUserHead());
            }
            if (staggered&&(position>0&&(position-1)%3==0)){
                nearbyUsersViewHolder.setViewMargin(itemLayoutParams);
            }
        }
    }

    @Override
    public int getItemCount() {
        return users==null?0:users.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (users.get(position).getId()){
            case -6: return ADDPEOPLE;
            default: return 0;
        }
    }


}
