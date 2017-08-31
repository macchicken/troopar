package com.troopar.trooparapp.activity.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class LikeActivityIntentService extends IntentService {

    private String position;
    private String pageCode;


    public LikeActivityIntentService() {
        super("LikeActivityIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent==null){return;}
        ResponseBody response = null;
        try{
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            String itemId=intent.getStringExtra("itemId");
            String type=intent.getStringExtra("type");
            String myUserId=intent.getStringExtra("myUserId");
            position=intent.getStringExtra("position");
            pageCode=intent.getStringExtra("pageCode");
            MultipartBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("itemId",itemId).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                    .addFormDataPart("type",type).addFormDataPart("userId",myUserId).build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/review/like_review.php").post(formBody).build();
            response=client.newCall(request).execute().body();
            JSONObject jsonObject=new JSONObject(response.string());
            String status = (String) jsonObject.get("status");
            if (Constants.TAG_SUCCESS.equals(status)){
                publishResults(jsonObject.getBoolean("addLike"),jsonObject.getInt("totalLikes"),Constants.TAG_SUCCESS);
                return;
            }
        }catch (Throwable t){
            t.printStackTrace();
        }finally {
            if (response!=null){
                response.close();
            }
        }
        publishResults(false,-1,"error");
    }

    private void publishResults(boolean addLike,int totalLikes,String status){
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("addLike",addLike);
        broadcastIntent.putExtra("totalLikes",totalLikes);
        broadcastIntent.putExtra("status",status);
        broadcastIntent.putExtra("position",position);
        broadcastIntent.putExtra("operationCode","likeActivity");
        if ("CommentsActivity".equals(pageCode)){
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.setAction("CommentsActivity.handler.load.reviews.data");
            sendBroadcast(broadcastIntent);
        }else{
            broadcastIntent.setAction("MyActivity.handler.load.more.data");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
        }
    }


}
