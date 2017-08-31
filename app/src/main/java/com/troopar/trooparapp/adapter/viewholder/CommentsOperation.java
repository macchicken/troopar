package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.troopar.trooparapp.R;

/**
 * Created by Barry on 5/08/2016.
 * comments and like counts item view
 */
public class CommentsOperation extends RecyclerView.ViewHolder{

    private TextView commentsCount;
    private TextView likeCount;


    public CommentsOperation(View itemView) {
        super(itemView);
        commentsCount= (TextView) itemView.findViewById(R.id.commentsCount);
        likeCount= (TextView) itemView.findViewById(R.id.likeCount);
    }

    public void setCommentsCount(String text){
        commentsCount.setText(this.itemView.getContext().getString(R.string.commentsCounts,text));
    }

    public void setLikeCounts(String text){
        likeCount.setText(this.itemView.getContext().getString(R.string.likeCounts,text));
    }


}
