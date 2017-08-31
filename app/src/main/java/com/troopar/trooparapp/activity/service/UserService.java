package com.troopar.trooparapp.activity.service;

import android.util.Log;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.Review;
import com.troopar.trooparapp.model.UploadPhoto;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.model.UserProfile;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;
import com.troopar.trooparapp.utils.MyAppSharePreference;
import com.troopar.trooparapp.utils.Tools;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Barry on 12/04/2016.
 */
public class UserService {

    private String url;
    private String profileUrl;
    private static UserService ourInstance = new UserService();
    private JSONParser jParser;


    public static UserService getInstance() {
        return ourInstance;
    }

    private UserService() {
        Log.d("UserService","UserService creation");
        url = BuildConfig.API_READHOST + "/user/login.php";
        profileUrl = BuildConfig.API_READHOST + "/user/user_home.php";
        jParser = new JSONParser();
    }

    public User login(String email, String password, String deviceId,String token) {
        HashMap<String, String> parameters = new HashMap<>();
        try {
            Thread.sleep(1000);
            Constants.DEVEICEIDVALUE=deviceId;
            Constants.SIGNATUREVALUE = Tools.sha1OfStr(deviceId + "ANZStudio");
            parameters.put("email", email);
            parameters.put("password", Tools.sha1OfStr(password));
            parameters.put("deviceToken", token);
            parameters.put(Constants.EQUIPID, deviceId);
            parameters.put(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
            JSONObject jsonObject = jParser.makeRequestForHttp(url, "POST", parameters);
            if (jsonObject == null) {
                return null;
            }
            if (!Constants.TAG_SUCCESS.equals(jsonObject.getString("status")) || !jsonObject.has("user")) {
                User user = new User(null, null, -3, null, null, null, null);
                user.setRemark(jsonObject.getString("message"));
                return user;
            }
            JSONObject jsonObjectUser = jsonObject.getJSONObject("user");
            User user = new User(jsonObjectUser.getString("firstName"), jsonObjectUser.getString("gender"), jsonObjectUser.getInt("id"), jsonObjectUser.getString("lastName"), jsonObjectUser.getString("username"), jsonObjectUser.getString("avatarOrigin"), jsonObjectUser.getString("avatarStandard"));
            user.setEmail(jsonObjectUser.getString("email"));
            user.setPhone(jsonObjectUser.getString("phone"));
            HashMap<String, String> temp = new HashMap<>();
            String userIdStr = String.valueOf(user.getId());
            temp.put("userId", userIdStr);
            temp.put("userName", user.getUserName());
            temp.put("userEmail", user.getEmail());
            temp.put("userGender", user.getGender());
            temp.put("userFirstName", user.getFirstName());
            temp.put("userLastName", user.getLastName());
            temp.put("userPhone", user.getPhone());
            temp.put("avatarOrigin", user.getAvatarOrigin());
            temp.put("avatarStandard", user.getAvatarStandard());
            temp.put("userPassword", password);
            temp.put(Constants.DEVICEID, deviceId);
            temp.put(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
            temp.put(userIdStr + user.getUserName(), user.getAvatarOrigin());
            Constants.USERID=userIdStr;
            Constants.USERNAME=user.getUserName();
            Constants.USEREMAIL=user.getEmail();
            Constants.USERGENDER=user.getGender();
            Constants.USERFIRSTNAME=user.getFirstName();
            Constants.USERLASTNAME=user.getLastName();
            Constants.USERPHONE=user.getPhone();
            Constants.AVATARORIGIN=user.getAvatarOrigin();
            Constants.AVATARSTANDARD=user.getAvatarStandard();
            Constants.USERPASSWORD=password;
            Constants.PHOTOFILENAME=user.getAvatarOrigin();
            MyAppSharePreference.getInstance().saveMultipleStringValues(temp);
            return user;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getProfile(String userId, String checkId) {
        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("userId", userId);
            parameters.put("checkId", checkId);
            parameters.put(Constants.EQUIPID, Constants.DEVEICEIDVALUE);
            parameters.put(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
            JSONObject jsonObject = jParser.makeRequestForHttp(profileUrl, "POST", parameters);
            if (jsonObject == null || !jsonObject.has("user") || !Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))) {
                return null;
            }
            JSONObject jsonObjectUser = jsonObject.getJSONObject("checkedUser");
            User user = new User(jsonObjectUser.getString("firstName"), jsonObjectUser.getString("gender"), jsonObjectUser.getInt("id"), jsonObjectUser.getString("lastName"), jsonObjectUser.getString("username"), jsonObjectUser.getString("avatarOrigin"), jsonObjectUser.getString("avatarStandard"));
            UserProfile userProfile=new UserProfile(jsonObject.getString("followerNum"), jsonObject.getString("followNum"), user.getId(), jsonObject.getString("joinedEventNum"), jsonObject.getString("postedPhotoNum"), jsonObject.getString("postedReviewNum"), jsonObject.getString("createdEventNum"));
            userProfile.setFollowState(jsonObject.getString("follow"));
            user.setUserProfile(userProfile);
            user.setPhone(jsonObjectUser.getString("phone"));
            user.setEmail(jsonObjectUser.getString("email"));
            LocalDBHelper.getInstance().insertUserProfile(user);
            return user;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList getUserProfileWithType(User myUser,String type,int offset,int limit,String equipId,String signature){
        try {
            HashMap<String,String> parameters=new HashMap<>();
            parameters.put("userId", String.valueOf(myUser.getId()));
            parameters.put("type", type);
            parameters.put("offset", String.valueOf(offset));
            parameters.put("limit", String.valueOf(limit));
            parameters.put(Constants.EQUIPID, equipId);
            parameters.put(Constants.SIGNATURE, signature);
            JSONObject jsonObject=jParser.makeRequestForHttp(profileUrl,"POST",parameters);
            if (jsonObject==null||!Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))) {
                return null;
            }
            if ("followers".equals(type)) {
                JSONArray followers = jsonObject.getJSONArray("followers");
                int total = followers.length();
                ArrayList<User> userArrayList = new ArrayList<>();
                if (total > 0) {
                    for (int i = 0; i < total; i++) {
                        JSONObject user = followers.getJSONObject(i);
                        userArrayList.add(new User(user.getString("firstName"), user.getString("gender"), user.getInt("id"), user.getString("lastName"), user.getString("username"), user.has("avatarOrigin") ? user.getString("avatarOrigin") : "", user.has("avatarStandard") ? user.getString("avatarStandard") : ""));
                    }
                }
                return userArrayList;
            }else if ("follows".equals(type)) {
                JSONArray followers = jsonObject.getJSONArray("follows");
                int total = followers.length();
                ArrayList<User> userArrayList = new ArrayList<>();
                if (total > 0) {
                    for (int i = 0; i < total; i++) {
                        JSONObject user = followers.getJSONObject(i);
                        userArrayList.add(new User(user.getString("firstName"), user.getString("gender"), user.getInt("id"), user.getString("lastName"), user.getString("username"), user.has("avatarOrigin") ? user.getString("avatarOrigin") : "", user.has("avatarStandard") ? user.getString("avatarStandard") : ""));
                    }
                }
                return userArrayList;
            }else if ("joined".equals(type)||"trooped".equals(type)||"reviews".equals(type)||"photos".equals(type)){
                String userName=Tools.isNullString(myUser.getFirstName())||Tools.isNullString(myUser.getLastName())? myUser.getUserName():String.format("%s %s", myUser.getFirstName(), myUser.getLastName());
                JSONArray results;
                boolean postedReviews = false;
                if ("joined".equals(type)){
                    results=jsonObject.getJSONArray("joinedEvents");
                }else if ("trooped".equals(type)){
                    results=jsonObject.getJSONArray("createdEvents");
                }else if ("photos".equals(type)){
                    results=jsonObject.getJSONArray("postedPhotos");
                }else{
                    postedReviews=true;
                    results=jsonObject.getJSONArray("postedReviews");
                }
                int total=results.length();
                ArrayList<ActivityModel> activityModels=new ArrayList<>();
                if (total<1){return activityModels;}
                for (int i = 0; i < total; i++){
                    JSONObject c;
                    JSONObject jsonObjectReview = null;
                    JSONObject postedPhoto=null;
                    if (!postedReviews){
                        if ("photos".equals(type)){
                            postedPhoto=results.getJSONObject(i);
                            c=postedPhoto.has("event")?postedPhoto.getJSONObject("event"):null;
                        }else{
                            c = results.getJSONObject(i);
                        }
                    }else{
                        jsonObjectReview=results.getJSONObject(i);
                        c = jsonObjectReview.getJSONObject("event");
                    }
                    EventModel eventModel;
                    if (c==null){
                        eventModel=new EventModel();
                        eventModel.setDescription(postedPhoto.optString("description",""));// people posted message
                        eventModel.setUser(myUser);
                    }else{
                        String contact=c.isNull("contact")?"":c.getString("contact");
                        int distance = c.isNull("distance")||c.getString("distance").equals("N/A")?0:c.getInt("distance");
                        String categoryName=c.has("categoryName")?c.getString("categoryName"):c.getString("category");
                        String eventStartTime=c.getString("startDate").replaceAll(",","");
                        String eventEndTime=c.getString("endDate").replaceAll(",","");
                        eventModel=new EventModel(c.getString("id"), c.getString("name"), c.getString("description"), eventStartTime, eventEndTime, c.getString("location"), c.getString("imagePath"),
                                categoryName, c.getString("longitude"), c.getString("latitude"),contact,distance,c.optInt("reviewNum"),c.getInt("maxNum"),c.getInt("minNum"),c.getInt("progress"),
                                c.getBoolean("joined"),c.getString("hex"),new User(myUser.getFirstName(),myUser.getGender(),myUser.getId(),myUser.getLastName(),myUser.getUserName(),myUser.getAvatarOrigin(),myUser.getAvatarStandard()),c.getString("createdDate"));
                        eventModel.setCreatedDate(c.getString("createdDate"));
                        eventModel.setColorHex(c.getString("hex"));
                        eventModel.setSmallImageUrl(c.getString("smallImageUrl"));
                        eventModel.setThumbnailImageUrl(c.getString("thumbnailImageUrl"));
                        eventModel.setAbbreviation(c.getString("abbreviation"));
                        eventModel.setShareNum(c.optInt("shareNum"));
                        if (c.has("joiners")){
                            JSONArray joiners=c.getJSONArray("joiners");
                            if (joiners!=null){
                                int len=joiners.length();
                                for (int j=0;j<len;j++){
                                    JSONObject joiner = joiners.getJSONObject(j);
                                    eventModel.addJoiners(new User(joiner.getString("firstName"),joiner.getString("gender"),joiner.getInt("id"),joiner.getString("lastName"),joiner.getString("username"),joiner.has("avatarOrigin")?joiner.getString("avatarOrigin"):"",joiner.has("avatarStandard")?joiner.getString("avatarStandard"):""));
                                }
                            }
                        }
                    }
                    ActivityModel temp;
                    if (!postedReviews){
                        if ("joined".equals(type)){
                            temp=new ActivityModel(userName, c.getString("id"), Constants.JOINEVENT, eventModel, null, null);
                            temp.setCreatedTime(c.getString("createdDate"));
                        }else if ("photos".equals(type)){
                            List<UploadPhoto> uploadPhotos1=null;
                            JSONArray uploadPhotos = postedPhoto.getJSONArray("photos");
                            int ptotal = uploadPhotos.length();
                            if (ptotal > 0) {
                                uploadPhotos1 = new ArrayList<>(ptotal);
                                for (int j = 0; j < ptotal; j++) {
                                    JSONObject photo = uploadPhotos.getJSONObject(j);
                                    uploadPhotos1.add(new UploadPhoto(photo.getString("id"), photo.getString("createdDate"), photo.getString("description"), photo.getString("photoPath"), photo.getString("smallImagePath"), photo.getString("totalLikes")));
                                }
                            }
                            temp=new ActivityModel(userName,postedPhoto.getString("activityId"), Constants.UPLOADPHOTOACT.equals(postedPhoto.getString("type"))?Constants.UPLOADPHOTOACT:Constants.PHOTOACT, eventModel, null, uploadPhotos1);
                            temp.setCreatedTime(postedPhoto.getString("createdDate"));
                        }else{
                            temp=new ActivityModel(userName, c.getString("id"), Constants.CREATEEVENT, eventModel, null, null);
                            temp.setCreatedTime(c.getString("createdDate"));
                        }
                    }else{
                        int totalLikes=jsonObjectReview.getInt("totalLikes");
                        eventModel.setTotalLikes(totalLikes);
                        Review review = new Review(userName, jsonObjectReview.getString("createdDate"), jsonObjectReview.getString("content"), jsonObjectReview.getString("rate"),null);
                        review.setId(jsonObjectReview.getString("reviewId"));
                        temp=new ActivityModel(userName, jsonObjectReview.getString("activityId"), Constants.REVIEWACT, eventModel, review, null);
                        temp.setTotalLikes(totalLikes);
                        temp.setCreatedTime(jsonObjectReview.getString("createdDate"));
                    }
                    activityModels.add(temp);
                }
                return activityModels;
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }


}
