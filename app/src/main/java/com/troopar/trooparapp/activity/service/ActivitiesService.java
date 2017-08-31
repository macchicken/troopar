package com.troopar.trooparapp.activity.service;

import android.location.Location;
import android.util.Log;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.Review;
import com.troopar.trooparapp.model.UploadPhoto;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Created by Barry on 6/04/2016.
 * for retrieving the activities data
 */
public class ActivitiesService {

    private String url;
    private OkHttpClient client;


    private static ActivitiesService ourInstance = new ActivitiesService();

    public static ActivitiesService getInstance() {
        return ourInstance;
    }

    private ActivitiesService() {
        Log.d("ActivitiesService","ActivitiesService creation");
        url= BuildConfig.API_READHOST+"/activity.php";
        client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
    }

    public ArrayList<ActivityModel> retrieveActivities(Location location,int counter,int limit,String userId){
        ResponseBody response = null;
        try {
            MultipartBody.Builder formBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("limit",String.valueOf(limit)).addFormDataPart("offset", String.valueOf(counter));
            if (location!=null){
                formBodyBuilder.addFormDataPart("myLatitude",String.valueOf(location.getLatitude())).addFormDataPart("myLongitude",String.valueOf(location.getLongitude()));
            }
            if (userId!=null){
                formBodyBuilder.addFormDataPart("userId",userId);
            }
            formBodyBuilder.addFormDataPart(Constants.EQUIPID,Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
            Request request=new Request.Builder().url(url).post(formBodyBuilder.build()).build();
            response=client.newCall(request).execute().body();
            JSONObject result=new JSONObject(response.string());
            response.close();
            if (!Constants.TAG_SUCCESS.equals(result.getString("status"))){
                return null;
            }
            JSONArray activities = result.getJSONArray("activities");
            int resultTotal = activities.length();
            if (resultTotal<=0){return null;}
            Log.d("ActivitiesService", "get activity total number: " + resultTotal);
            ArrayList<ActivityModel> activityModels=new ArrayList<>(resultTotal);
            int actualSize = 0;
            for (int i = 0; i < resultTotal; i++) {
                JSONObject obj = activities.getJSONObject(i);
                String type = obj.getString("type");
                JSONObject user,event;
                EventModel eventModel;
                try{
                    user = obj.getJSONObject("user");
                }catch (JSONException t){
                    Log.d("ActivitiesService",obj.toString());
                    t.printStackTrace();
                    continue;
                }
                ActivityModel activityModel = null;
                String userName=user.getString("username");
                if ("post".equals(type)){
                    eventModel=new EventModel();
                    eventModel.setDescription(obj.getString("description"));// people posted message
                    eventModel.setUser(new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),userName,user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""));
                    activityModel=new ActivityModel(userName,obj.getString("activityId"), type, eventModel, null, null);
                }else if ("upload_photo".equals(type)){
                    eventModel=new EventModel();
                    eventModel.setDescription(obj.getString("description"));// people posted message
                    eventModel.setUser(new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),userName,user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""));
                    JSONArray uploadPhotos = obj.getJSONArray("photos");// people upload photos
                    int total = uploadPhotos.length();
                    if (total > 0) {
                        List<UploadPhoto> uploadPhotos1 = new ArrayList<>();
                        for (int j = 0; j < total; j++) {
                            JSONObject photo = uploadPhotos.getJSONObject(j);
                            uploadPhotos1.add(new UploadPhoto(photo.getString("id"), photo.getString("createdDate"), photo.getString("description"), photo.getString("photoPath"), photo.getString("smallImagePath"), photo.getString("totalLikes")));
                        }
                        activityModel=new ActivityModel(userName,obj.getString("activityId"), type, eventModel, null, uploadPhotos1);
                    }
                }else{
                    try{
                        event = obj.getJSONObject("event");
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.d("ActivitiesService",obj.toString());
                        continue;
                    }
                    String eventStartTime=event.getString("startDate").replaceAll(",","");
                    String eventEndTime=event.getString("endDate").replaceAll(",","");
                    eventModel = new EventModel(event.getString("id"), event.getString("name"), event.getString("description"), eventStartTime, eventEndTime, event.getString("location"),
                            event.getString("imagePath"), event.getString("category"), event.getString("longitude"), event.getString("latitude"), event.getString("contact"), event.getInt("distance"),event.getInt("reviewNum"),event.getInt("maxNum"),
                            event.getInt("minNum"),event.getInt("progress"), event.getBoolean("joined"),event.getString("hex"),new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),userName,user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""),event.getString("createdDate"));
                    eventModel.setCreatedDate(event.getString("createdDate"));
                    eventModel.setColorHex(event.getString("hex"));
                    eventModel.setSmallImageUrl(event.getString("smallImageUrl"));
                    eventModel.setMediumImageUrl(event.getString("mediumImageUrl"));
                    eventModel.setThumbnailImageUrl(event.getString("thumbnailImageUrl"));
                    eventModel.setAbbreviation(event.getString("abbreviation"));
                    eventModel.setShareNum(event.getInt("shareNum"));
                    JSONArray joiners=event.getJSONArray("joiners");
                    if (joiners!=null){
                        int len=joiners.length();
                        for (int j=0;j<len;j++){
                            JSONObject joiner = joiners.getJSONObject(j);
                            eventModel.addJoiners(new User(joiner.getString("firstName"),joiner.getString("gender"),joiner.getInt("id"),joiner.getString("lastName"),joiner.getString("username"),joiner.has("avatarOrigin")?joiner.getString("avatarOrigin"):"",joiner.has("avatarStandard")?joiner.getString("avatarStandard"):""));
                        }
                    }
                    switch (type) {
                        case Constants.REVIEWACT:
                            eventModel.setTotalLikes(obj.getInt("totalLikes"));
                            Review review = new Review(userName, obj.getString("createdDate"), obj.getString("content"), obj.getString("rate"),null);
                            review.setId(obj.getString("reviewId"));
                            activityModel=new ActivityModel(userName,obj.getString("activityId"), type, eventModel, review, null);
                            break;
                        case Constants.PHOTOACT:
                            eventModel.setTotalLikes(obj.getInt("totalLikes"));
                            JSONArray uploadPhotos = obj.getJSONArray("photos");
                            int total = uploadPhotos.length();
                            if (total > 0) {
                                List<UploadPhoto> uploadPhotos1 = new ArrayList<>();
                                for (int j = 0; j < total; j++) {
                                    JSONObject photo = uploadPhotos.getJSONObject(j);
                                    uploadPhotos1.add(new UploadPhoto(photo.getString("id"), photo.getString("createdDate"), photo.getString("description"), photo.getString("photoPath"), photo.getString("smallImagePath"), photo.getString("totalLikes")));
                                }
                                activityModel=new ActivityModel(userName,obj.getString("activityId"), type, eventModel, null, uploadPhotos1);
                            }
                            break;
                        case Constants.CREATEEVENT:
                            eventModel.setTotalLikes(obj.getInt("totalLikes"));
                            activityModel=new ActivityModel(userName, obj.getString("activityId"), type, eventModel, null, null);
                            break;
                        case Constants.SHAREEVENT:
                            activityModel=new ActivityModel(userName, obj.getString("activityId"), type, eventModel, new Review(userName, obj.getString("createdDate"), obj.getString("description"), "0", null), null);
                            break;
                        default:break;
                    }
                }
                if (activityModel!=null){
                    activityModel.setCreatedTime(obj.getString("createdDate"));
                    activityModel.setReviewNum(obj.optInt("review_num"));
                    activityModel.setShareNum(obj.optInt("shared_num"));
                    activityModel.setTotalLikes(obj.optInt("activity_like_num"));
                    actualSize++;
                    activityModels.add(activityModel);
                }
            }
            if (actualSize>0){
                Log.d("ActivitiesService",String.format("%s insert local db %s",userId==null?"100abc":userId,actualSize));
                LocalDBHelper.getInstance().insertActivity(userId==null?"100abc":userId,activityModels);
            }
            return activityModels;
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            if (response!=null){
                response.close();
            }
        }
        return null;
    }


}
