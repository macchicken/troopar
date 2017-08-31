package com.troopar.trooparapp.activity.service;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Created by Barry on 6/04/2016.
 * used in retrieving the event data
 */
public class EventService {
    private OkHttpClient client;
    private String mapEventUrl;


    private static EventService ourInstance = new EventService();

    public static EventService getInstance() {
        return ourInstance;
    }

    private EventService() {
        Log.d("EventService","EventService creation");
        client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
        mapEventUrl= BuildConfig.API_READHOST+"/event.php";
    }

    public ArrayList<EventModel> retrieveEvents(LatLngBounds marker,LatLng center,int limit,int counter,String category){
        MultipartBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("userId",Constants.USERID)
                .addFormDataPart(Constants.EQUIPID,Constants.DEVEICEIDVALUE)
                .addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                .addFormDataPart("myLatitude",String.valueOf(center.latitude))
                .addFormDataPart("myLongitude",String.valueOf(center.longitude))
                .addFormDataPart("neLatitude",String.valueOf(marker.northeast.latitude))
                .addFormDataPart("neLongitude",String.valueOf(marker.northeast.longitude))
                .addFormDataPart("swLatitude",String.valueOf(marker.southwest.latitude))
                .addFormDataPart("swLongitude",String.valueOf(marker.southwest.longitude))
                .addFormDataPart("limit",String.valueOf(limit))
                .addFormDataPart("category",category==null?"":category)
                .addFormDataPart("offSet",String.valueOf(counter)).build();
        Request request=new Request.Builder().url(mapEventUrl).post(formBody).build();
        ResponseBody response = null;
        JSONObject obj=null;
        try {
            response=client.newCall(request).execute().body();
            obj=new JSONObject(response.string());
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            if (response!=null){
                response.close();
            }
        }
        if (obj==null){return null;}
        try {
            String status = (String) obj.get("status");
            if (!Constants.TAG_SUCCESS.equals(status)){return new ArrayList<>(); }
            JSONArray result=obj.getJSONArray("result");
            int total=result.length();
            if (total==0){return new ArrayList<>();}
            ArrayList<EventModel> events=new ArrayList<>(total);
            for (int i = 0; i < total; i++){
                JSONObject c = null;
                try{
                    c = result.getJSONObject(i);
                    String contact=c.isNull("contact")?"":c.getString("contact");
                    int distance = c.isNull("distance")||c.getString("distance").equals("N/A")?0:c.getInt("distance");
                    JSONObject user=c.getJSONObject("user");
                    String eventStartTime=c.getString("startDate").replaceAll(",","");
                    String eventEndTime=c.getString("endDate").replaceAll(",","");
                    EventModel eventModel=new EventModel(c.getString("id"), c.getString("name"), c.getString("description"), eventStartTime, eventEndTime, c.getString("location"), c.getString("image"),
                            c.getString("category"), c.getString("longitude"), c.getString("latitude"),contact,distance,c.getInt("reviewNum"),c.getInt("maxNum"),c.getInt("minNum"),c.getInt("progress"),
                            c.getBoolean("joined"),c.getString("hex"),new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),user.getString("username"),user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""),c.getString("createdDate"));
                    eventModel.setCreatedDate(c.getString("createdDate"));
                    eventModel.setColorHex(c.getString("hex"));
                    eventModel.setSmallImageUrl(c.getString("smallImageUrl"));
                    eventModel.setThumbnailImageUrl(c.getString("thumbnailImageUrl"));
                    eventModel.setMediumImageUrl(c.getString("mediumImageUrl"));
                    eventModel.setAbbreviation(c.getString("abbreviation"));
                    eventModel.setRequires(c.getString("requires"));
                    eventModel.setRequireType(c.getString("requireType"));
                    eventModel.setShareNum(c.getInt("shareNum"));
                    JSONArray joiners=c.getJSONArray("joiners");
                    if (joiners!=null){
                        int len=joiners.length();
                        for (int j=0;j<len;j++){
                            JSONObject joiner = joiners.getJSONObject(j);
                            eventModel.addJoiners(new User(joiner.getString("firstName"),joiner.getString("gender"),joiner.getInt("id"),joiner.getString("lastName"),joiner.getString("username"),joiner.has("avatarOrigin")?joiner.getString("avatarOrigin"):"",joiner.has("avatarStandard")?joiner.getString("avatarStandard"):""));
                        }
                    }
                    events.add(eventModel);
                }catch (JSONException e){
                    if (c!=null){
                        Log.d("EventService",c.toString());
                    }
                    e.printStackTrace();
                }
            }
            return events;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<EventModel> searchEvents(Location location,String searchKeywords,String sortKw,int offSet,boolean continued){
        ResponseBody response = null;
        try {
            MultipartBody.Builder formBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("userId",Constants.USERID)
                    .addFormDataPart(Constants.EQUIPID,Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                    .addFormDataPart("myLongitude",String.valueOf(location.getLongitude()))
                    .addFormDataPart("myLatitude",String.valueOf(location.getLatitude()))
                    .addFormDataPart("longitude",String.valueOf(location.getLongitude()))
                    .addFormDataPart("latitude",String.valueOf(location.getLatitude()))
                    .addFormDataPart("longitudeDelta","0.5")
                    .addFormDataPart("latitudeDelta","0.5")
                    .addFormDataPart("sort",sortKw)
                    .addFormDataPart("offSet",String.valueOf(offSet));
            if (!Tools.isNullString(searchKeywords)){
                formBodyBuilder.addFormDataPart("keyword",searchKeywords);
            }
            Request request=new Request.Builder().url(mapEventUrl).post(formBodyBuilder.build()).build();
            response=client.newCall(request).execute().body();
            JSONObject obj=new JSONObject(response.string());
            response.close();
            String status = (String) obj.get("status");
            if (!Constants.TAG_SUCCESS.equals(status)){return new ArrayList<>();}
            JSONArray result=obj.getJSONArray("result");
            int total=result.length();
            if (total==0){return null;}
            ArrayList<EventModel> events=new ArrayList<>();
            for (int i = 0; i < total; i++){
                JSONObject c = result.getJSONObject(i);
                String contact=c.isNull("contact")?"":c.getString("contact");
                int distance = c.isNull("distance")||c.getString("distance").equals("N/A")?0:c.getInt("distance");
                JSONObject user=c.getJSONObject("user");
                String eventStartTime=c.getString("startDate").replaceAll(",","");
                String eventEndTime=c.getString("endDate").replaceAll(",","");
                EventModel eventModel=new EventModel(c.getString("id"), c.getString("name"), c.getString("description"), eventStartTime, eventEndTime, c.getString("location"), c.getString("image"),
                        c.getString("category"), c.getString("longitude"), c.getString("latitude"),contact,distance,c.getInt("reviewNum"),c.getInt("maxNum"),c.getInt("minNum"),c.getInt("progress"),
                        c.getBoolean("joined"),c.getString("hex"),new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),user.getString("username"),user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""),c.getString("createdDate"));
                eventModel.setCreatedDate(c.getString("createdDate"));
                eventModel.setColorHex(c.getString("hex"));
                eventModel.setSmallImageUrl(c.getString("smallImageUrl"));
                eventModel.setThumbnailImageUrl(c.getString("thumbnailImageUrl"));
                eventModel.setAbbreviation(c.getString("abbreviation"));
                eventModel.setRequires(c.getString("requires"));
                eventModel.setRequireType(c.getString("requireType"));
                eventModel.setShareNum(c.getInt("shareNum"));
                JSONArray joiners=c.getJSONArray("joiners");
                if (joiners!=null){
                    int len=joiners.length();
                    for (int j=0;j<len;j++){
                        JSONObject joiner = joiners.getJSONObject(j);
                        eventModel.addJoiners(new User(joiner.getString("firstName"),joiner.getString("gender"),joiner.getInt("id"),joiner.getString("lastName"),joiner.getString("username"),joiner.has("avatarOrigin")?joiner.getString("avatarOrigin"):"",joiner.has("avatarStandard")?joiner.getString("avatarStandard"):""));
                    }
                }
                events.add(eventModel);
            }
            return events;
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            if (response!=null){
                response.close();
            }
        }
        return null;
    }


}
