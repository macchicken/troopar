package com.troopar.trooparapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LikeActivityIntentService;
import com.troopar.trooparapp.activity.service.RetrieveReviewsService;
import com.troopar.trooparapp.adapter.ComplexCommentsAdapter;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.ActivityReview;
import com.troopar.trooparapp.myview.EventSharePopupWindow;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private ActivityModel activityModel;
    private String actPosition;
    private final int EVENT_DETAIL_REQUEST_CODE = 610;
    private final int POST_COMMENT_REQUEST_CODE = 631;
    private final int POST_REVIEW_COMMENT_REQUEST_CODE = 632;
    private int counter;
    private ArrayList<ActivityReview> comments;
    private ComplexCommentsAdapter commentsAdapter;
    private SwipeRefreshLayout refreshableView;
    private RecyclerView listView;
    private RetrieveReviewsReceiver retrieveReviewsReceiver;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private ShareItemOnClickListener itemOnClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Intent data=getIntent();
        activityModel= (ActivityModel) data.getSerializableExtra("activityModel");
        actPosition=data.getStringExtra("actPosition");
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        refreshableView = (SwipeRefreshLayout) findViewById(R.id.myRefreshable_view);
        listView = (RecyclerView) findViewById(R.id.myListView);
        comments=new ArrayList<>();
        listView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        commentsAdapter=new ComplexCommentsAdapter(comments,activityModel,listView);
        listView.setAdapter(commentsAdapter);
        refreshListener=new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Tools.checkNetworkConnected(CommentsActivity.this)){
                    if (commentsAdapter.setLoading()){
                        getActivityReviews();
                    }
                }else{
                    refreshableView.setRefreshing(false);
                }
            }
        };
        refreshableView.setOnRefreshListener(refreshListener);
        IntentFilter filter = new IntentFilter("CommentsActivity.handler.load.reviews.data");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        retrieveReviewsReceiver=new RetrieveReviewsReceiver();
        registerReceiver(retrieveReviewsReceiver,filter);
        refreshableView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshableView.setRefreshing(true);
                refreshListener.onRefresh();
            }
        },100);
        ShareDialog shareDialog = new ShareDialog(CommentsActivity.this);
        itemOnClickListener=new ShareItemOnClickListener(CommentsActivity.this,shareDialog);
        ((TextView)findViewById(R.id.eventTotalShare)).setText(String.valueOf(activityModel.getShareNum()));
    }

    private void getActivityReviews(){
        Intent serviceIntent=new Intent(CommentsActivity.this, RetrieveReviewsService.class);
        serviceIntent.putExtra("activityId",activityModel.getId());
        startService(serviceIntent);
    }

    public class RetrieveReviewsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                String status=intent.getStringExtra("status");
                if (Constants.TAG_SUCCESS.equals(status)){
                    switch (intent.getStringExtra("operationCode")){
                        case "retrieveReviews":
                            Serializable object=intent.getSerializableExtra("activityReviews");
                            comments.clear();
                            counter=0;
                            if (object==null){
                                comments.add(new ActivityReview());
                                comments.add(new ActivityReview());
                                Toast.makeText(CommentsActivity.this, "no comments", Toast.LENGTH_SHORT).show();
                            }else{
                                ArrayList<ActivityReview> activityReviews= (ArrayList<ActivityReview>) object;
                                comments.add(new ActivityReview());
                                comments.add(new ActivityReview());
                                comments.addAll(activityReviews);
                                counter=activityReviews.size();
                            }
                            commentsAdapter.setNoMore(true);
                            commentsAdapter.notifyDataSetChanged();
                            ((TextView)findViewById(R.id.eventTotalReview)).setText(String.valueOf(counter));
                            ((TextView)findViewById(R.id.eventTotalLikes)).setText(String.valueOf(activityModel.getTotalLikes()));
                            activityModel.setReviewNum(counter);
                            break;
                        case "likeActivity":
                            int totalLikes=intent.getIntExtra("totalLikes",0);
                            String position=intent.getStringExtra("position");
                            if (position==null){
                                TextView likeCount=(TextView) findViewById(R.id.likeCount);
                                likeCount.setText(getString(R.string.likeCounts,String.valueOf(totalLikes)));
                                ((TextView)findViewById(R.id.eventTotalLikes)).setText(String.valueOf(totalLikes));
                                activityModel.setTotalLikes(totalLikes);
                            }else{
                                int index=Integer.parseInt(position);
                                ActivityReview activityReview=comments.get(index);
                                activityReview.setLiked(intent.getBooleanExtra("addLike",false));
                                comments.set(index,activityReview);
                                commentsAdapter.notifyItemChanged(index);
                            }
                            break;
                    }
                }else{
                    Toast.makeText(CommentsActivity.this, "server error", Toast.LENGTH_SHORT).show();
                }
            }catch (Throwable t){
                t.printStackTrace();
            }finally {
                if (refreshableView.isRefreshing()){
                    refreshableView.setRefreshing(false);
                    commentsAdapter.setLoaded();
                }
            }
        }
    }

    public void userImageAction(View view){
    }

    public void userDetailAction(View view){
        Intent intent=new Intent(CommentsActivity.this,UserDetailActivity.class);
        String position=view.getContentDescription().toString();
        intent.putExtra("user",comments.get(Integer.parseInt(position)).getUser());
        startActivity(intent);
    }

    public void eventDetailAction(View view){
        if (activityModel.getType().equals("post")||activityModel.getType().equals("upload_photo")){
            return;
        }
        Intent intent=new Intent(CommentsActivity.this,EventDetailActivity.class);
        intent.putExtra("eventModel",activityModel.getEvent());
        startActivityForResult(intent,EVENT_DETAIL_REQUEST_CODE);
    }

    public void postCommentAction(View view){
        Intent intent=new Intent(CommentsActivity.this,PostCommentActivity.class);
        intent.putExtra("activityId",activityModel.getId());
        intent.putExtra("myUserId", Constants.USERID);
        intent.putExtra("operationCode","activity_review");
        startActivityForResult(intent,POST_COMMENT_REQUEST_CODE);
    }

    public void postReviewCommentAction(View view){
        int position=Integer.parseInt(view.getContentDescription().toString());
        Intent intent=new Intent(CommentsActivity.this,PostCommentActivity.class);
        intent.putExtra("reviewId",comments.get(position).getId());
        intent.putExtra("myUserId", Constants.USERID);
        intent.putExtra("operationCode","review_comment");
        startActivityForResult(intent,POST_REVIEW_COMMENT_REQUEST_CODE);
    }

    public void likeCommentAction(View view){
        Intent serviceIntent=new Intent(CommentsActivity.this, LikeActivityIntentService.class);
        String type=activityModel.getType();
        serviceIntent.putExtra("itemId","review".equals(type)?activityModel.getReview().getId():activityModel.getId());
        serviceIntent.putExtra("type",type);
        serviceIntent.putExtra("myUserId",Constants.USERID);
        serviceIntent.putExtra("pageCode","CommentsActivity");
        CommentsActivity.this.startService(serviceIntent);
    }

    public void likeCommentItemAction(View view){
        String position=view.getContentDescription().toString();
        Intent serviceIntent=new Intent(CommentsActivity.this, LikeActivityIntentService.class);
        ActivityReview activityReview=comments.get(Integer.parseInt(position));
        serviceIntent.putExtra("itemId",String.valueOf(activityReview.getId()));
        serviceIntent.putExtra("type",activityReview.getType());
        serviceIntent.putExtra("myUserId",Constants.USERID);
        serviceIntent.putExtra("pageCode","CommentsActivity");
        serviceIntent.putExtra("position",position);
        CommentsActivity.this.startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK&&data!=null) {
            switch (requestCode){
                case EVENT_DETAIL_REQUEST_CODE:
                    boolean joined=data.getBooleanExtra("joined",false);
                    activityModel.getEvent().setJoined(joined);
                    break;
                case POST_COMMENT_REQUEST_CODE:
                    ActivityReview activityReview= (ActivityReview) data.getSerializableExtra("activityReview");
                    comments.add(activityReview);
                    commentsAdapter.notifyDataSetChanged();
                    ++counter;
                    activityModel.setReviewNum(counter);
                    ((TextView)findViewById(R.id.eventTotalReview)).setText(String.valueOf(counter));
                    break;
                default:break;
            }
        }
    }

    public void eventShareAction(View view){
        itemOnClickListener.setEvent(activityModel.getEvent());
        EventSharePopupWindow eventSharePopupWindow = new EventSharePopupWindow(CommentsActivity.this, itemOnClickListener);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                eventSharePopupWindow.enableShareApp(info.activityInfo.packageName);
            }
        }
        try {
            getPackageManager().getApplicationInfo("com.instagram.android", 0);
            eventSharePopupWindow.enableShareApp("com.instagram.android");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        itemOnClickListener.setEventSharePopupWindow(eventSharePopupWindow);
        //显示窗口
        eventSharePopupWindow.showAtLocation(CommentsActivity.this.findViewById(R.id.myRefreshable_view), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void loadMore(View view){
    }

    @Override
    protected void onDestroy() {
        Log.d("CommentsActivity", "CommentsActivity on destroy");
        unregisterReceiver(retrieveReviewsReceiver);
        retrieveReviewsReceiver=null;
        super.onDestroy();
        commentsAdapter.setOnLoadMoreListener(null);
        RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
        recycledViewPool.clear();
        itemOnClickListener.setContext(null);
        itemOnClickListener.setShareDialog(null);
        itemOnClickListener.setEvent(null);
        itemOnClickListener.setEventSharePopupWindow(null);
        itemOnClickListener=null;
        comments=null;
        commentsAdapter=null;
        listView.removeAllViews();
        refreshableView.removeAllViews();
        refreshableView.setOnRefreshListener(null);
        refreshListener=null;
        refreshableView=null;
    }


}
