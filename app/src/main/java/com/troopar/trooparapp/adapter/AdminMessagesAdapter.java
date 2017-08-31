package com.troopar.trooparapp.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.viewholder.AdminMessageViewHolder;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Barry on 29/01/2016.
 * used in the admin message display
 */
public class AdminMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<MessageModel> mMessageModels;
    private final int NOMOREDATA=6,LOADMORE=3;
    private int lastVisibleItem, totalItemCount;
    private AtomicBoolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean noMore=false;


    public AdminMessagesAdapter(ArrayList<MessageModel> messageModels, RecyclerView recyclerView) {
        loading=new AtomicBoolean(false);
        mMessageModels = messageModels;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (noMore){return;}
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();//index of position start from zero
                if (totalItemCount>0 && totalItemCount == (lastVisibleItem+5)) {// End has been reached
                    if (loading.compareAndSet(false,true)){
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
        if (viewType==LOADMORE) {
            View v = inflater.inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressViewHolder(v);
        }
        else if (viewType==NOMOREDATA) {
            View v1 = inflater.inflate(R.layout.more_datas_footer, parent, false);
            viewHolder = new NoMoreDataViewHolder(v1);
        }
        else {
            View v2 = inflater.inflate(R.layout.admin_message_item_view, parent, false);
            viewHolder = new AdminMessageViewHolder(v2);
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
        int itemType=holder.getItemViewType();
        if (itemType==LOADMORE) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
        else if (itemType==NOMOREDATA) {
            ((NoMoreDataViewHolder)holder).moreDataText.setText("no more activities");
        }
        else {
            configureActivityViewHolder((AdminMessageViewHolder) holder, position);
        }
    }

    private void configureActivityViewHolder(AdminMessageViewHolder vh,int position) {
        MessageModel messageModel=mMessageModels.get(position);
        if (messageModel!=null){
            String type=messageModel.getType();
            try {
                JSONObject content=new JSONObject(messageModel.getContent());
                JSONObject user=content.getJSONObject("user");
                vh.setUserHead(user.getString("avatarStandard"));
                vh.setUserName(user.getString("username"));
                vh.setMessageTime(Tools.calculateTimeElapsed(messageModel.getCreatedTime()));
                if ("comment".equals(type)){
                    JSONObject event=content.getJSONObject("activity").getJSONObject("event");
                    vh.setMessageContent(String.format("comments %s [Event] %s",content.getString("message"),event.getString("name")));
                }else if ("like".equals(type)){
                    JSONObject event=content.getJSONObject("activity").getJSONObject("event");
                    vh.setMessageContent(String.format("%s [Event] %s",content.getString("message"),event.getString("name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mMessageModels==null){
            return 0;
        }
        return mMessageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel tmp=mMessageModels.get(position);
        if (tmp==null){return LOADMORE;}
        return -1;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.loadMoreprogressBar);
        }
    }

    private static class NoMoreDataViewHolder extends RecyclerView.ViewHolder {
        TextView moreDataText;

        NoMoreDataViewHolder(View v) {
            super(v);
            moreDataText= (TextView) v.findViewById(R.id.moreDataText);
        }
    }

    public void setLoaded() {
        loading.set(false);
    }

    public boolean setLoading(){
        return loading.compareAndSet(false,true);
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


}
