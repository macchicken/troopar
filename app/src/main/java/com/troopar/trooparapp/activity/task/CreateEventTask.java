package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Barry on 16/02/2016.
 * creating a event
 */
public class CreateEventTask extends AsyncTask<Void, Integer, JSONObject> {

    private String eventName;
    private String eventDescription;
    private String startTime;
    private String endTime;
    private String location;
    private String city;
    private String suburb;
    private double myLatitude;
    private double myLongitude;
    private String userId;
    private String equipId;
    private String signature;
    private String eventImagePath;
    private String availability;
    private String approveToJoin;
    private String requires;
    private String maxNumPeople;
    private ExecuteCallBack executeCallBack;


    public CreateEventTask(String eventName,String eventDescription, String location, String startTime,String endTime,String city,String suburb,double myLatitude,double myLongitude,String availability,String approveToJoin) {
        super();
        this.city = city;
        this.endTime = endTime;
        this.eventDescription = eventDescription;
        this.eventName = eventName;
        this.location = location;
        this.startTime = startTime;
        this.suburb = suburb;
        this.myLatitude = myLatitude;
        this.myLongitude = myLongitude;
        this.availability = availability;
        this.approveToJoin = approveToJoin;
        userId=Constants.USERID;
        equipId=Constants.DEVEICEIDVALUE;
        signature=Constants.SIGNATUREVALUE;
    }

    @Override
    protected void onPreExecute() {
        executeCallBack.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        executeCallBack.onProgressUpdate(values[0]);
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        Response response=null;
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder builder= new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("name",eventName).addFormDataPart("description",eventDescription).addFormDataPart("startDate",startTime)
                    .addFormDataPart("endDate",endTime).addFormDataPart("location",location).addFormDataPart("city",city).addFormDataPart("suburb",suburb).addFormDataPart("contact","")
                    .addFormDataPart("userId",userId).addFormDataPart("latitude",String.valueOf(myLatitude)).addFormDataPart("longitude",String.valueOf(myLongitude)).addFormDataPart("availability",availability)
                    .addFormDataPart("approveToJoin",approveToJoin).addFormDataPart("requires",requires).addFormDataPart("maxNum",maxNumPeople).addFormDataPart(Constants.EQUIPID,equipId).addFormDataPart(Constants.SIGNATURE, signature);
            if (eventImagePath!=null){
                builder.addFormDataPart("photos",eventImagePath, RequestBody.create(MediaType.parse("text/plain"), new File(eventImagePath)));
            }
            Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/event/add_event.php").post(builder.build()).build();
            response=client.newCall(request).execute();
            if (response.isSuccessful()){
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
    protected void onPostExecute(JSONObject jsonObject) {
        executeCallBack.onPostExecute(jsonObject);
    }

    public void setEventImagePath(String eventImagePath) {
        this.eventImagePath = eventImagePath;
    }

    public interface ExecuteCallBack{
        void onPreExecute();
        void onProgressUpdate(Integer value);
        void onPostExecute(JSONObject jsonObject);
    }

    public void setExecuteCallBack(ExecuteCallBack executeCallBack) {
        this.executeCallBack = executeCallBack;
    }

    public void setRequires(String requires) {
        this.requires = requires;
    }

    public void setMaxNumPeople(String maxNumPeople) {
        this.maxNumPeople = maxNumPeople;
    }


}
