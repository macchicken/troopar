package com.troopar.trooparapp.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.viewholder.ActivityViewHolder;
import com.troopar.trooparapp.adapter.viewholder.CommentsOperation;
import com.troopar.trooparapp.adapter.viewholder.CommentsViewHolder;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.ActivityReview;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.Review;
import com.troopar.trooparapp.model.UploadPhoto;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Barry on 4/08/2016.
 * used in displaying the comments
 */
public class ComplexCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ActivityReview> mComments;
    private ActivityModel mActivityModel;
    private final int REVIEW = 0, CREATEDEVENT = 1, PHOTOACT = 2, SHAREEVT = 4, JOINEVT = 5, UPLOADPHOTOACT = 7, POSTACT = 8, COMMENTSOPER=10, COMMENTACT=9;
    private int lastVisibleItem, totalItemCount;
    private AtomicBoolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean noMore = false;


    public ComplexCommentsAdapter(ArrayList<ActivityReview> comments, ActivityModel activityModel, RecyclerView recyclerView) {
        loading = new AtomicBoolean(false);
        mComments = comments;
        mActivityModel=activityModel;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();//index of position start from zero
                if (noMore) {return;}
                if (totalItemCount > 0 && totalItemCount == (lastVisibleItem + 2)) {// End has been reached
                    if (loading.compareAndSet(false, true)) {
                        Log.d("ComplexCommentsAdapter", String.format("totalItemCount %d, lastVisibleItem %d", totalItemCount, lastVisibleItem));
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            }
        });
    }

    /**
     * This method creates different RecyclerView.ViewHolder objects based on the item view type.\
     *
     * @return viewHolder to be inflated
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType==COMMENTACT){
            View view=inflater.inflate(R.layout.item_comment,parent,false);
            viewHolder=new CommentsViewHolder(view);
        }else if (viewType==COMMENTSOPER){
            View view=inflater.inflate(R.layout.commnets_operaiton,parent,false);
            viewHolder=new CommentsOperation(view);
        } else {
            View v2 = inflater.inflate(R.layout.item_viewholder_useractivity, parent, false);
            viewHolder = new ActivityViewHolder(v2);
        }
        return viewHolder;
    }

    /**
     * This method internally calls onBindViewHolder(ViewHolder, int) to update the
     * RecyclerView.ViewHolder contents with the item at the given position
     * and also sets up some private fields to be used by RecyclerView.
     *
     * @param position Item position in the viewgroup.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();
        if (itemType==COMMENTACT){
            configureCommentsViewHolder((CommentsViewHolder)holder,position);
        }else if (itemType==COMMENTSOPER){
            configureCommentsOperationViewHolder((CommentsOperation) holder);
        } else {
            String keywords = "";
            switch (itemType) {
                case REVIEW:
                    keywords = "submit a review";
                    break;
                case CREATEDEVENT:
                    keywords = "submit a event";
                    break;
                case PHOTOACT:
                    keywords = "upload photos";
                    break;
                case SHAREEVT:
                    keywords = "share a event";
                    break;
                case JOINEVT:
                    keywords = "join a event";
                    break;
                case UPLOADPHOTOACT:
                    keywords = "user upload photos";
                    break;
                case POSTACT:
                    keywords = "post";
                    break;
                default:
                    break;
            }
            configureActivityViewHolder((ActivityViewHolder) holder, keywords, position);
        }
    }

    private void configureCommentsViewHolder(CommentsViewHolder vh,int position){
        ActivityReview activityReview=mComments.get(position);
        String positionStr=String.valueOf(position);
        vh.setCommentContent(activityReview.getContent().replaceAll("\n",""));
        User user=activityReview.getUser();
        vh.setCommentPoster(Tools.isNullString(user.getFirstName())||Tools.isNullString(user.getLastName())?user.getUserName():String.format("%s %s",user.getFirstName(),user.getLastName()));
        vh.setCommentPostTime(Tools.calculateTimeElapsed(activityReview.getCreatedDate()));
        vh.setUserImage(user.getAvatarStandard());
        vh.setUserImageDiscription(positionStr);
        vh.setItemViewDiscription(positionStr);
        vh.setlikeBtnDiscription(positionStr);
        vh.setLikeState(activityReview.isLiked());
    }

    private void configureCommentsOperationViewHolder(CommentsOperation vh){
        int reviewNumber=mComments.size()<3?0:mComments.size()-2;
        vh.setCommentsCount(String.valueOf(reviewNumber));
        vh.setLikeCounts(String.valueOf(mActivityModel.getTotalLikes()));
    }

    private void configureActivityViewHolder(ActivityViewHolder vh, String words, int position) {
        vh.hideBottomPart();
        if (mActivityModel != null) {
            EventModel event = mActivityModel.getEvent();
            if (event == null) {
                Log.d("ComplexCommentsAdapter", String.format("configureActivityViewHolder activity id %s event or review content is null", mActivityModel.getId()));
                return;
            }
            User user = event.getUser();
            if (user == null) {
                Log.d("ComplexCommentsAdapter", String.format("configureActivityViewHolder activity id %s user content is null", mActivityModel.getId()));
                return;
            }
            String positionStr = String.valueOf(position);
            vh.setShareBtnDescription(positionStr);
            vh.setEventImageContainerDescription(positionStr);
            vh.setJoinBtnDescription(positionStr);
            vh.setReviewBtnDescription(positionStr);
            vh.setUserImageDescription(positionStr);
            String displayName = Tools.isNullString(user.getFirstName()) && Tools.isNullString(user.getLastName()) ? user.getUserName() : String.format("%s %s", user.getFirstName(), user.getLastName());
            try {
                String userImagePath = user.getAvatarStandard();
                if (Tools.isNullString(userImagePath)) {
                    vh.setUserImageWithColor(R.drawable.user_image, R.color.appElementColor);
                    ImageDownloader.getInstance().getRequestManager().load(userImagePath).override(70, 70).diskCacheStrategy(DiskCacheStrategy.ALL).into(vh.getUserImage());
                } else {
                    ImageDownloader.getInstance().getRequestManager().load(userImagePath).override(70, 70).diskCacheStrategy(DiskCacheStrategy.ALL).into(vh.getUserImage());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            vh.getEventReviewPoster().setText(displayName);
            if ("user upload photos".equals(words)) {
                vh.getEventReviewContent().setVisibility(View.GONE);
                List<UploadPhoto> userUploadPhotos = mActivityModel.getUploadPhotos();
                int totalPhotos = 0;
                if (userUploadPhotos != null && (totalPhotos = userUploadPhotos.size()) > 0) {
                    vh.getUserUploadPhotos().setVisibility(View.VISIBLE);
                    vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(userUploadPhotos.get(0).getCreatedDate()));
                    vh.getUserUploadPhotos().setImagesData(userUploadPhotos);
                }
                vh.getActivityKeywords().setText(totalPhotos < 2 ? R.string.uploadAPhoto : R.string.uploadPhotos);
                vh.hideEventPart();
                vh.hideEventPeopleJoinedProgress();
            } else if ("post".equals(words)) {
                vh.getEventReviewContent().setText(event.getDescription());
                if (mActivityModel.getCreatedTime() != null) {
                    vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(mActivityModel.getCreatedTime()));
                }
                vh.getActivityKeywords().setText(R.string.typePost);
                vh.hideEventPart();
                vh.hideEventPeopleJoinedProgress();
            } else {
                switch (words) {
                    case "submit a review":
                        Review review = mActivityModel.getReview();
                        vh.getEventReviewContent().setText(review.getContent());
                        if (review.getReviewTime() != null) {
                            vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(review.getReviewTime()));
                        }
                        vh.getActivityKeywords().setText(R.string.typeReview);
                        break;
                    case "submit a event":
                        vh.getEventReviewContent().setVisibility(View.GONE);
                        vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(event.getCreatedDate()));
                        vh.getActivityKeywords().setText(R.string.typeTrooped);
                        break;
                    case "upload photos":
                        vh.getEventReviewContent().setVisibility(View.GONE);
                        List<UploadPhoto> uploadPhotos = mActivityModel.getUploadPhotos();
                        if (uploadPhotos != null && uploadPhotos.size() > 0) {
                            vh.getUserUploadPhotos().setVisibility(View.VISIBLE);
                            vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(uploadPhotos.get(0).getCreatedDate()));
                            vh.getUserUploadPhotos().setImagesData(uploadPhotos);
                        }
                        vh.getActivityKeywords().setText(R.string.typePhoto);
                        break;
                    case "share a event":
                        vh.getEventReviewContent().setText(mActivityModel.getReview().getContent());
                        vh.getEventReviewPostTime().setText(Tools.calculateTimeElapsed(mActivityModel.getCreatedTime()));
                        vh.getActivityKeywords().setText(R.string.typeShare);
                        break;
                    default:
                        vh.getActivityKeywords().setText(R.string.typeEvent);
                        break;
                }
                String eventStartDate = event.getStartDate();
                String formatTime = Tools.formatTime(eventStartDate);
                String eventEndDate = event.getEndDate();
                String startDateOfWeek = Tools.getDateOfWeekFromDate(eventStartDate);
                vh.getEventTitle().setText(String.format("%s", event.getName()));
                vh.getEventSnippet().setText(Html.fromHtml("by <u>" + displayName + "</u> on <u>" + formatTime + "</u> at " + Tools.formatTimeToMinutes(eventStartDate)));
                vh.getEventStartTime().setText(String.format("%s,%s (%s)", startDateOfWeek, formatTime, Tools.calculateTimeRange(eventStartDate, eventEndDate)));
                vh.getEventPlace().setText(Html.fromHtml(" <u>" + event.getLocation() + "</u>"));
                try {
                    ImageView imageView = vh.getEventSmallImage();
                    if (Tools.isNullString(event.getThumbnailImageUrl())) {
                        ImageDownloader.getInstance().getRequestManager().load(R.drawable.troopar_logo_red_square_t).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
                    } else {
                        ImageDownloader.getInstance().getRequestManager().load(event.getThumbnailImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                vh.setTotalLikes(String.valueOf(mActivityModel.getTotalLikes()));
                vh.setEventPeopleJoinedProgress(event.getMaxJoinedPeople() == 0 ? 10 : event.getMaxJoinedPeople(), event.getJoinedProgress());
                vh.setTotalJoiners(String.valueOf(event.getJoinedProgress()));
                vh.setTotalReview(String.valueOf(mActivityModel.getReviewNum()));
                vh.setTotalShare(String.valueOf(mActivityModel.getShareNum()));
                vh.setCategoryImage(getCategoryImage(event.getCategory()));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mComments == null) {
            return 0;
        }
        return mComments.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            switch (mActivityModel.getType()) {
                case Constants.REVIEWACT:
                    return REVIEW;
                case Constants.CREATEEVENT:
                    return CREATEDEVENT;
                case Constants.PHOTOACT:
                    return PHOTOACT;
                case Constants.SHAREEVENT:
                    return SHAREEVT;
                case Constants.JOINEVENT:
                    return JOINEVT;
                case Constants.UPLOADPHOTOACT:
                    return UPLOADPHOTOACT;
                case Constants.POSTACT:
                    return POSTACT;
                default:
                    return -1;
            }
        }
        if (position==1){
            return COMMENTSOPER;
        }
        return COMMENTACT;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoaded() {
        loading.set(false);
    }

    public boolean setLoading() {
        return loading.compareAndSet(false, true);
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private int getCategoryImage(String category) {
        switch (category) {
            default:
                return R.drawable.event_category_icon;
        }
    }
    
    
}
