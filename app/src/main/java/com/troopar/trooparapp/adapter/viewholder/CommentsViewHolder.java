package com.troopar.trooparapp.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Tools;

/**
 * Created by Barry on 5/08/2016.
 */
public class CommentsViewHolder extends RecyclerView.ViewHolder{

    private TextView commentPoster;
    private TextView commentPostTime;
    private TextView commentContent;
    private ImageView userImage;
    private ImageView likeBtn;


    public CommentsViewHolder(View itemView) {
        super(itemView);
        commentPoster= (TextView) itemView.findViewById(R.id.eventReviewPoster);
        commentPostTime= (TextView) itemView.findViewById(R.id.eventReviewPostTime);
        commentContent= (TextView) itemView.findViewById(R.id.eventReviewContent);
        userImage=(ImageView) itemView.findViewById(R.id.userImage);
        likeBtn= (ImageView) itemView.findViewById(R.id.likeBtn);
    }

    public void setCommentPoster(String poster){
        commentPoster.setText(poster);
    }

    public void setCommentPostTime(String postTime){
        commentPostTime.setText(postTime);
    }

    public void setCommentContent(String content){
        commentContent.setText(content);
    }

    public void setUserImage(String userImageUrl){
        if (Tools.isNullString(userImageUrl)){
            userImage.setImageResource(R.drawable.user_image);
            userImage.setBackgroundColor(this.itemView.getContext().getResources().getColor(R.color.appElementColor));
        }else{
            Glide.with(this.itemView.getContext()).load(userImageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(userImage);
        }
    }

    public void setUserImageDiscription(String text){
        userImage.setContentDescription(text);
    }

    public void setItemViewDiscription(String text){
        this.itemView.setContentDescription(text);
    }

    public void setlikeBtnDiscription(String text){
        likeBtn.setContentDescription(text);
    }

    public void setLikeState(boolean isLiked){
        if (isLiked){
            likeBtn.setColorFilter(this.itemView.getContext().getResources().getColor(R.color.appMainColor),android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }


}
