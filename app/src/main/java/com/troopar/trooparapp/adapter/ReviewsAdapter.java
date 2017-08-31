package com.troopar.trooparapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.model.Review;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;

import com.troopar.trooparapp.R;

/**
 * Created by Barry on 15/01/2016.
 * used in display reviews
 */
public class ReviewsAdapter extends ArrayAdapter<Review> {

    private static class ViewHolder {
        TextView eventReviewPoster;
        TextView eventReviewPostTime;
        TextView eventReviewContent;
        ImageView userImage;
    }

    public ReviewsAdapter(Context context, ArrayList<Review> messages){
        super(context, R.layout.item_review,messages);
    }

    public ReviewsAdapter(Context context){
        super(context, R.layout.item_review);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review ev=getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_review, parent, false);
            viewHolder.eventReviewPoster= (TextView) convertView.findViewById(R.id.eventReviewPoster);
            viewHolder.eventReviewPostTime= (TextView) convertView.findViewById(R.id.eventReviewPostTime);
            viewHolder.userImage= (ImageView) convertView.findViewById(R.id.userImage);
            viewHolder.eventReviewContent= (TextView) convertView.findViewById((R.id.eventReviewContent));
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Lookup view for data population
        viewHolder.eventReviewPoster.setText(ev.getUserName());
//        viewHolder.eventReviewRating.setRating(Float.valueOf(ev.getRating()));
        viewHolder.eventReviewContent.setText(ev.getContent());
        // Return the completed view to render on screen
        viewHolder.eventReviewPostTime.setText(Tools.calculateTimeElapsed(ev.getReviewTime()));
        if (Tools.isNullString(ev.getUserImage())){
            viewHolder.userImage.setImageResource(R.drawable.user_image);
            viewHolder.userImage.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.appElementColor));
        }else{
            Glide.with(convertView.getContext()).load(ev.getUserImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.userImage);
        }
        return convertView;
    }


}
