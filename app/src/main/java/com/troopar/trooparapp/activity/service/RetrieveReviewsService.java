package com.troopar.trooparapp.activity.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.model.ActivityReview;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class RetrieveReviewsService extends IntentService {

    public RetrieveReviewsService() {
        super("RetrieveReviewsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ResponseBody response = null;
            try{
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
                String activityId=intent.getStringExtra("activityId");
                MultipartBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("activityId",activityId).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE).build();
                Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/activity/get_activity_reviews.php").post(formBody).build();
                response=client.newCall(request).execute().body();
                JSONObject obj=new JSONObject(response.string());
                String status = (String) obj.get("status");
                if (Constants.TAG_SUCCESS.equals(status)){
                    JSONArray result=obj.getJSONArray("activity_reviews");
                    int total=result.length();
                    if (total>0){
                        LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
                        ArrayList<ActivityReview> activityReviews=new ArrayList<>(total);
                        for (int i=0;i<total;i++){
                            JSONObject review=result.getJSONObject(i);
                            JSONObject jsonObjectUser=review.getJSONObject("user");
                            User user=localDBHelper.getCacheUser(jsonObjectUser.getString("id"));
                            if (user==null){
                                user=new User(jsonObjectUser.getString("firstName"),jsonObjectUser.getString("gender"),jsonObjectUser.getInt("id"),jsonObjectUser.getString("lastName"),jsonObjectUser.getString("username"),jsonObjectUser.getString("avatarOrigin"),jsonObjectUser.getString("avatarStandard"));
                            }
                            activityReviews.add(new ActivityReview(review.getInt("aId"),review.getString("content"),review.getString("createdDate"),review.getInt("id"),review.getInt("respondToUserId"),user,review.getString("type")));
                        }
                        publishResults(activityReviews,Constants.TAG_SUCCESS);
                    }else{
                        publishResults(null,Constants.TAG_SUCCESS);
                    }
                    return;
                }
            }catch (Throwable t){
                t.printStackTrace();
            }finally {
                if (response!=null){
                    response.close();
                }
            }
        }
        publishResults(null,"error");
    }

    private void publishResults(ArrayList<ActivityReview> activityReviews,String status){
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction("CommentsActivity.handler.load.reviews.data");
        broadcastIntent.putExtra("activityReviews",activityReviews);
        broadcastIntent.putExtra("status",status);
        broadcastIntent.putExtra("operationCode","retrieveReviews");
        sendBroadcast(broadcastIntent);
    }

}
