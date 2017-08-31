package com.troopar.trooparapp.activity.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class UploadPhotosService extends IntentService {

    public UploadPhotosService() {
        super("UploadPhotosService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
        String url=BuildConfig.API_WRITEHOST+"/photo/upload_photo.php";
        String[] images=intent.getStringArrayExtra("images");
        String userId=intent.getStringExtra("userId");
        String createdDate=intent.getStringExtra("createdDate");
        String description=intent.getStringExtra("description");
        String type=intent.getStringExtra("type");
        int imageCount=intent.getIntExtra("imageCount",0);
        MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                .addFormDataPart("userId",userId).addFormDataPart("description",description).addFormDataPart("createdDate",createdDate).addFormDataPart("type",type);
        for (int i=0;i<9;i++){
            if (images[i]!=null){
                formBody.addFormDataPart("photos", images[i], RequestBody.create(MediaType.parse("text/plain"), new File(images[i])));
                Request request=new Request.Builder().url(url).post(formBody.build()).build();
                Response response = null;
                try {
                    response=client.newCall(request).execute();
                    --imageCount;
                    if (imageCount==0){
                        if (response.isSuccessful()){
                            publishResults(new JSONObject(response.body().string()));
                        }else{
                            publishResults(null);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (response!=null){
                        response.body().close();
                    }
                }
            }
        }
    }

    private void publishResults(JSONObject jsonObject){
        Context appContext=ApplicationContextStore.getInstance().getContext();
        if (appContext!=null){
            if (jsonObject==null){
                Log.d("UploadPhotosService","server error");
            }else{
                Log.d("UploadPhotosService",jsonObject.toString());
            }
        }
    }


}
