package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.ActivitiesService;
import com.troopar.trooparapp.adapter.MyActivityInfoAdapter;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class DisplayRecentActivitiesActivity extends AppCompatActivity {

    private int counter;
    private ArrayList<ActivityModel> activityModels;
    private MyActivityInfoAdapter activityInfoAdapter;
    private SwipeRefreshLayout refreshableView;
    private RecyclerView listView;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private ActivitiesService activitiesService;
    private User myUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recent_activities);
        activitiesService=ActivitiesService.getInstance();
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_displayActToolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent data=getIntent();
        myUser= (User) data.getSerializableExtra("user");
        listView=(RecyclerView) findViewById(R.id.displayActlistView);
        listView.setLayoutManager(new LinearLayoutManager(DisplayRecentActivitiesActivity.this));
        activityModels=new ArrayList<>();
        activityInfoAdapter=new MyActivityInfoAdapter(activityModels, listView);
        listView.setAdapter(activityInfoAdapter);
        refreshableView= (SwipeRefreshLayout) findViewById(R.id.displayActRefreshable_view);
        refreshListener=new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (!activityInfoAdapter.setLoading()){
                    return;
                }
                GetUserProfileWithType task=new GetUserProfileWithType(0,10,false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        };
        refreshableView.setOnRefreshListener(refreshListener);
        activityInfoAdapter.setOnLoadMoreListener(new MyActivityInfoAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                activityModels.add(null);
                activityInfoAdapter.notifyItemInserted(activityModels.size() - 1);
                GetUserProfileWithType task=new GetUserProfileWithType(counter,10,true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        });
        refreshableView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshableView.setRefreshing(true);
                refreshListener.onRefresh();
            }
        },100);
        TextView textView= (TextView) findViewById(R.id.toolbar_logo);
        textView.setText(getString(R.string.activitiesTitle, Tools.isNullString(myUser.getFirstName())||Tools.isNullString(myUser.getLastName())?myUser.getUserName():String.format("%s %s",myUser.getFirstName(),myUser.getLastName())));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetUserProfileWithType extends AsyncTask<Void,Void,ArrayList> {

        private int mOffset;
        private int mLimit;
        private boolean mMore;

        GetUserProfileWithType(int offset,int limit,boolean more){
            mOffset=offset;
            mLimit=limit;
            mMore=more;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityInfoAdapter.setLoading();
            if (!mMore&&!refreshableView.isRefreshing()){
                refreshableView.setRefreshing(true);
            }
        }

        @Override
        protected ArrayList doInBackground(Void... params) {
            return activitiesService.retrieveActivities(null, mOffset, mLimit, String.valueOf(myUser.getId()));
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            try {
                if (activityInfoAdapter!=null){
                    if (!mMore){
                        refreshableView.setRefreshing(false);
                    }
                    if (result==null||result.size()<=0){
                        if (mMore){
                            activityModels.remove(activityModels.size() - 1);
                            activityInfoAdapter.notifyItemRemoved(activityModels.size());
                            activityModels.add(new ActivityModel(Constants.NOMOREDATA));
                            activityInfoAdapter.notifyItemInserted(activityModels.size()-1);
                            activityInfoAdapter.setNoMore(true);
                        }
                        return;
                    }
                    if (mMore){
                        activityModels.remove(activityModels.size() - 1);
                        activityInfoAdapter.notifyItemRemoved(activityModels.size());
                    }else{
                        counter=0;
                        activityModels.clear();
                    }
                    int total=result.size();
                    if (total<1){
                        activityModels.add(new ActivityModel(Constants.NOMOREDATA));
                        activityInfoAdapter.notifyItemInserted(activityModels.size()-1);
                        activityInfoAdapter.setNoMore(true);
                    }else{
                        activityModels.addAll(result);
                        if (mMore){
                            activityInfoAdapter.notifyItemRangeInserted(counter,total);
                        }else{
                            activityInfoAdapter.notifyDataSetChanged();
                        }
                        activityInfoAdapter.setNoMore(false);
                        counter+=total;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }finally {
                if (activityInfoAdapter!=null){
                    activityInfoAdapter.setLoaded();
                }
            }
        }
    }

    public void loadMore(View view){// for the button in the more data footer
        activityInfoAdapter.setNoMore(false);
        activityModels.remove(activityModels.size() - 1);
        activityInfoAdapter.notifyItemRemoved(activityModels.size());
        activityModels.add(null);
        activityInfoAdapter.notifyItemInserted(activityModels.size() - 1);
        GetUserProfileWithType task=new GetUserProfileWithType(counter, 10, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void eventDetailAction(View view){
        String eventPosition=view.getContentDescription().toString();
        ActivityModel activityModel=activityModels.get(Integer.parseInt(eventPosition));
        if (activityModel.getType().equals("post")||activityModel.getType().equals("upload_photo")){
            return;
        }
        Intent intent=new Intent(DisplayRecentActivitiesActivity.this,EventDetailActivity.class);
        intent.putExtra("eventModel",activityModel.getEvent());
        intent.putExtra("eventPosition",eventPosition);
        startActivity(intent);
    }

    public void userImageAction(View view){
        int position=Integer.parseInt(view.getContentDescription().toString());
        Intent intent=new Intent(DisplayRecentActivitiesActivity.this,UserDetailActivity.class);
        intent.putExtra("user",activityModels.get(position).getEvent().getUser());
        startActivity(intent);
    }

    public void eventShareAction(View view){

    }

    public void joinEventAction(View view){

    }

    public void userCommentsAction(View view){
        String position=view.getContentDescription().toString();
        ActivityModel activityModel=activityModels.get(Integer.parseInt(position));
        Intent intent=new Intent(DisplayRecentActivitiesActivity.this,CommentsActivity.class);
        intent.putExtra("activityModel",activityModel);
        intent.putExtra("actPosition",position);
        startActivity(intent);
    }

    public void likeCommentAction(View view){
        int position=Integer.parseInt(view.getContentDescription().toString());
        ActivityModel activityModel=activityModels.get(position);
        String type=activityModel.getType();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new LikeActivityTask("review".equals(type)?activityModel.getReview().getId():activityModel.getId(),Constants.USERID,position,type).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            new LikeActivityTask("review".equals(type)?activityModel.getReview().getId():activityModel.getId(),Constants.USERID,position,type).execute();
        }
    }

    private class LikeActivityTask extends AsyncTask<Void,Void,JSONObject>{

        private int mPosition;
        private String mItemId;
        private String mType;
        private String mMyUserId;

        public LikeActivityTask(String itemId, String myUserId, int position, String type) {
            this.mItemId = itemId;
            this.mMyUserId = myUserId;
            this.mPosition = position;
            this.mType = type;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            ResponseBody response = null;
            try{
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
                MultipartBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("itemId",mItemId).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                        .addFormDataPart("type",mType).addFormDataPart("userId",mMyUserId).build();
                Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/review/like_review.php").post(formBody).build();
                response=client.newCall(request).execute().body();
                JSONObject jsonObject=new JSONObject(response.string());
                String status = (String) jsonObject.get("status");
                if (Constants.TAG_SUCCESS.equals(status)){
                    return jsonObject;
                }
            }catch (Throwable t){
                t.printStackTrace();
            }finally {
                if (response!=null){
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject==null){
                Toast.makeText(DisplayRecentActivitiesActivity.this, "server error", Toast.LENGTH_SHORT).show();
            }else{
                try {
                    ActivityModel activityModel=activityModels.get(mPosition);
                    activityModel.setTotalLikes(jsonObject.getInt("totalLikes"));
                    activityModels.set(mPosition,activityModel);
                    activityInfoAdapter.notifyItemChanged(mPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("DisplayActActivity", "DisplayActivitiesActivity on destroy");
        super.onDestroy();
        activityInfoAdapter.setOnLoadMoreListener(null);
        RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
        recycledViewPool.clear();
        activityModels=null;
        listView.removeAllViews();
        refreshableView.removeAllViews();
        refreshableView.setOnRefreshListener(null);
        refreshListener=null;
        refreshableView=null;
        listView=null;
        activityInfoAdapter=null;
    }


}
