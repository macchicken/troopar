package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.model.ActivityReview;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PostCommentActivity extends AppCompatActivity {

    private String activityId;
    private String myUserId;
    private int respondToUserId;
    private int reviewId;
    private String operationCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent data=getIntent();
        activityId=data.getStringExtra("activityId");
        myUserId=data.getStringExtra("myUserId");
        respondToUserId=data.getIntExtra("respondTo",-1);
        reviewId=data.getIntExtra("reviewId",-1);
        operationCode=data.getStringExtra("operationCode");
        findViewById(R.id.firstRow).setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void cancelAction(View view){
        onBackPressed();
    }

    public void sendComments(View view){
        view.setPressed(true);
        view.setClickable(false);
        String commentDescription=((EditText)findViewById(R.id.commentDescription)).getText().toString();
        if (Tools.isNullString(commentDescription)){return;}
        if ("review_comment".equals(operationCode)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SendReviewCommentTask(commentDescription,myUserId,reviewId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                new SendReviewCommentTask(commentDescription,myUserId,reviewId).execute();
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SendCommentTask(commentDescription,myUserId,activityId,respondToUserId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                new SendCommentTask(commentDescription,myUserId,activityId,respondToUserId).execute();
            }
        }
    }

    private class SendCommentTask extends AsyncTask<Void,Void,JSONObject>{

        private String mActivityId;
        private String mMyUserId;
        private String mContent;
        private int mRespondToUserId;

        public SendCommentTask(String content, String myUserId, String activityId,int respondToUserId) {
            this.mContent = content;
            this.mMyUserId = myUserId;
            this.mActivityId = activityId;
            this.mRespondToUserId = respondToUserId;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("activityId",mActivityId).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                    .addFormDataPart("userId",mMyUserId).addFormDataPart("content",mContent);
            if (mRespondToUserId!=-1){
                formBody.addFormDataPart("respondTo",String.valueOf(mRespondToUserId));
            }
            Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/activity/add_activity_review.php").post(formBody.build()).build();
            ResponseBody response = null;
            try {
                response=client.newCall(request).execute().body();
                return new JSONObject(response.string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (response!=null){
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    JSONObject actReview=jsonObject.getJSONObject("activity_review");
                    User user= LocalDBHelper.getInstance().getCacheUser(mMyUserId);
                    ActivityReview activityReview=new ActivityReview(actReview.getInt("activityId"),actReview.getString("content"),actReview.getString("createdDate"),actReview.getInt("id"),actReview.optInt("respondTo",-1),user,actReview.getString("type"));
                    Intent data=new Intent();
                    data.putExtra("activityReview",activityReview);
                    setResult(RESULT_OK,data);
                    onBackPressed();
                }else{
                    Button button= (Button) findViewById(R.id.saveComments);
                    if (button!=null){
                        button.setPressed(false);
                        button.setClickable(true);
                    }
                    Toast.makeText(PostCommentActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReviewCommentTask extends AsyncTask<Void,Void,JSONObject>{

        private int mReviewId;
        private String mMyUserId;
        private String mContent;

        public SendReviewCommentTask(String content, String myUserId, int reviewId) {
            this.mContent = content;
            this.mMyUserId = myUserId;
            this.mReviewId = reviewId;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("reviewId",String.valueOf(mReviewId)).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                    .addFormDataPart("userId",mMyUserId).addFormDataPart("content",mContent);
            Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/comment/add_comment.php").post(formBody.build()).build();
            ResponseBody response = null;
            try {
                response=client.newCall(request).execute().body();
                return new JSONObject(response.string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (response!=null){
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    onBackPressed();
                }else{
                    Button button= (Button) findViewById(R.id.saveComments);
                    if (button!=null){
                        button.setPressed(false);
                        button.setClickable(true);
                    }
                    Toast.makeText(PostCommentActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
