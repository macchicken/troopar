package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;

import com.troopar.trooparapp.utils.Tools;

import org.json.JSONObject;


import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.Constants;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Barry on 17/02/2016.
 * used in change user profile
 */
public class ChangeUserProfileTask extends AsyncTask<Void, Void, JSONObject> {

    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String password;
    private String newPassword;
    private String equipId;
    private String signature;
    private UIAction uiAction;
    private String imageFile;


    public ChangeUserProfileTask(String email, String firstName, String gender, String lastName, String password, String newPassword,String username,UIAction uiAction,String imageFile) {
        this.email = email;
        this.firstName = firstName;
        this.gender = gender;
        this.lastName = lastName;
        this.password = password;
        this.newPassword = newPassword;
        this.username = username;
        userId=Constants.USERID;
        equipId=Constants.DEVEICEIDVALUE;
        signature=Constants.SIGNATUREVALUE;
        this.uiAction=uiAction;
        this.imageFile=imageFile;
    }

    @Override
    protected void onPreExecute() {
        uiAction.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder builder= new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(Constants.SIGNATURE,signature).addFormDataPart(Constants.EQUIPID,equipId)
                    .addFormDataPart("userId",userId).addFormDataPart("username",username).addFormDataPart("firstName",firstName).addFormDataPart("lastName",lastName).addFormDataPart("gender",gender)
                    .addFormDataPart("email",email).addFormDataPart("password",Tools.sha1OfStr(password));
            if (imageFile!=null){
                File file= new File(imageFile);
                builder.addFormDataPart("avatar", file.getName(), RequestBody.create(MediaType.parse("text/plain"), file));
            }
            Request request=new Request.Builder().url(BuildConfig.API_WRITEHOST + "/user/change_profile.php").post(builder.build()).build();
            Response response=client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject result=new JSONObject(response.body().string());
                if (Constants.TAG_SUCCESS.equals(result.getString("status"))){
                    if (!password.equals(newPassword)){
                        response.body().close();
                        MultipartBody.Builder builder3= new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(Constants.SIGNATURE,signature).addFormDataPart(Constants.EQUIPID,equipId)
                                .addFormDataPart("password",Tools.sha1OfStr(password)).addFormDataPart("newPassword",Tools.sha1OfStr(newPassword)).addFormDataPart("email",email);
                        Request request3=new Request.Builder().url(BuildConfig.API_READHOST + "/user/change_password.php").post(builder3.build()).build();
                        Response response3=client.newCall(request3).execute();
                        if (response3.isSuccessful()){
                            JSONObject jsonObject3=new JSONObject(response3.body().string());
                            response3.body().close();
                            return jsonObject3;
                        }else{
                            if (response3.body()!=null){
                                response3.body().close();
                            }
                            return null;
                        }
                    }
                }
                response.body().close();
                return result;
            }
            if (response.body()!=null){
                response.body().close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        uiAction.onPostExecute(jsonObject);
    }

    public interface UIAction{
        void onPreExecute();
        void onPostExecute(JSONObject jsonObject);
    }


}
