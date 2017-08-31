package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;
import android.util.Log;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Bary on 11/06/2016.
 */
public class SendVideoTask extends AsyncTask<Void, Void, JSONObject> {

    private String output;
    private String thumbFileName;
    private String myUserId;
    private String receiverUserId;
    private String mDeviceId;
    private String mSignature;
    private User mUser;


    public SendVideoTask(User user, String myUserId, String output, String receiverUserId, String thumbFileName) {
        this.myUserId = myUserId;
        this.mUser = user;
        this.output = output;
        this.receiverUserId = receiverUserId;
        this.thumbFileName = thumbFileName;
        this.mDeviceId=Constants.DEVEICEIDVALUE;
        this.mSignature=Constants.SIGNATUREVALUE;
    }


    @Override
    protected JSONObject doInBackground(Void... params) {
        Log.d("SendVideoTask",String.format("video thumbFileName %s",thumbFileName));
        Response response=null;
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
        RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", output, RequestBody.create(MediaType.parse("text/plain"), new File(output)))
                .addFormDataPart("poster", thumbFileName, RequestBody.create(MediaType.parse("text/plain"), new File(thumbFileName)))
                .addFormDataPart("userId", myUserId)
                .addFormDataPart(Constants.EQUIPID, mDeviceId)
                .addFormDataPart(Constants.SIGNATURE, mSignature)
                .build();
        Request request=new Request.Builder().url(BuildConfig.API_WRITEHOST + "/photo/upload_file.php").post(formBody).build();
        try {
            response=client.newCall(request).execute();
            if (response.message().equals("OK")){
                return new JSONObject(response.body().string());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            if (response!=null){
                response.body().close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);
        if (response==null){return;}
        Log.d("SendVideoTask",response.toString());
        try {
            if (Constants.TAG_SUCCESS.equals(response.getString("status"))){
                JSONObject videoFiles=new JSONObject();
                videoFiles.put("origin",output);// video file path
                videoFiles.put("new",thumbFileName);// video thumbnail path
                MessageService.getInstance().send(myUserId,receiverUserId,videoFiles.toString(),"video", Tools.buildGroupChatMetaData(mUser,videoFiles));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
