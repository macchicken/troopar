package com.troopar.trooparapp.activity.task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.activity.MultiDrawable;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by barry on 15/07/2016.
 * modification of group information in the local database
 */
public class CreateGroupProfileTask extends AsyncTask<Void,Void,User>{

    private String myUserId;
    private JSONObject payload;
    private String topicStr;
    private MessageModel messageModel;
    private boolean deleted;
    private String chatType;


    public CreateGroupProfileTask(String myUserId,JSONObject payload,String topicStr) {
        this.myUserId = myUserId;
        this.payload = payload;
        this.topicStr = topicStr;
    }

    @Override
    protected User doInBackground(Void... params) {
        long offset= TimeZone.getDefault().getRawOffset();
        JSONArray userIds = null,quitUserIds = null;
        FileOutputStream out=null;
        Response response=null;
        String from,to,groupName = null,groupId,existingUsers,content = null;
        int creatorId = 0,sender;
        User group;
        boolean normalGroup=topicStr.endsWith("group");
        LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
        try {
            chatType = (String) payload.get("type");
            sender = payload.getInt("sender");
            if (chatType.equals("change_group_name")){
                String[] temp=topicStr.split("/");
                groupId = temp[temp.length-2];
                group=localDBHelper.getCacheUser(String.format(normalGroup?"group%s":"event%s",groupId));
                if (normalGroup){
                    groupName=payload.getString("content");
                    if (group==null){
                        group=new User(groupId,null,-5,null,groupName,null,null);
                    }else{
                        group.setUserName(groupName);
                    }
                }else{
                    groupName=payload.getString("name");
                    if (group==null){
                        group=new User(groupId,payload.getJSONArray("joiners").toString(),-3,myUserId,groupName,null,payload.getString("smallImageUrl"));
                    }else{
                        group.setUserName(groupName);
                    }
                }
                from=String.format(normalGroup?"group/%s/%s":"event/%s/%s", groupId,sender);
                to=String.format(normalGroup?"group/%s/%s":"event/%s/%s", groupId,myUserId);
                SimpleDateFormat simpleDateFormatUS=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String dateStr=simpleDateFormatUS.format(new Date(simpleDateFormatUS.parse((String) payload.get("date")).getTime()+offset));// receiving time is gmt time
                messageModel=new MessageModel(groupName,dateStr,java.util.UUID.randomUUID().toString(),from,to, group.getLastName(),!myUserId.equals(String.valueOf(sender)));// creator in eventId
                messageModel.setType(chatType);
                messageModel.setGrouped(true);
                messageModel.setGroupUsers(group.getGender());
                localDBHelper.insertMessage(messageModel, myUserId);
                return group;
            }else{
                Object object=payload.get("content");
                if (object instanceof Integer){
                    groupId= String.valueOf(((Integer) object).intValue());
                }else{
                    groupId = (String) object;
                }
            }
            if (normalGroup){
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000, TimeUnit.MILLISECONDS).writeTimeout(300000, TimeUnit.MILLISECONDS).build();
                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("groupId", groupId)
                        .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                        .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                        .build();
                Request request = new Request.Builder().url(BuildConfig.API_READHOST + "/group/get_group_members.php").post(formBody).build();
                response = client.newCall(request).execute();
                JSONObject jsonObject=new JSONObject(response.body().string());
                JSONArray result = jsonObject.getJSONArray("result");
                creatorId=jsonObject.getInt("ownerId");
                userIds = new JSONArray();
                int userIdTotal = result.length();
                for (int i = 0; i < userIdTotal; i++) {
                    userIds.put(result.getInt(i));
                }
                group=localDBHelper.getCacheUser("group"+groupId);
                existingUsers=userIds.toString();
            }else{
                group=localDBHelper.getCacheUser("event"+groupId);
                if (group==null){
                    group=new User(groupId,payload.getJSONArray("joiners").toString(),-3,myUserId,payload.getString("name"),null,payload.getString("smallImageUrl"));
                }
                existingUsers=group.getGender();
            }
            switch (chatType){
                case "group_invite":
                    JSONArray newUserIds=payload.getJSONArray("userIds");// invited users
                    groupName=payload.getString("name");
                    creatorId =payload.getInt("owner");
                    int length=newUserIds.length();
                    if (normalGroup){
                        for (int i=0;i<length;i++){
                            userIds.put(newUserIds.getInt(i));
                        }
                    }else{
                        StringBuilder stringBuilder=new StringBuilder(existingUsers.substring(0,existingUsers.length()-1));
                        for (int i=0;i<length;i++){
                            stringBuilder.append(",").append(newUserIds.getInt(i));
                        }
                        existingUsers=stringBuilder.append("]").toString();
                    }
                    content=newUserIds.toString();
                    break;
                case "create_group":
                    if (normalGroup){
                        creatorId =sender;
                        groupName="group";
                        if (userIds.length()==0){
                            userIds=payload.getJSONArray("userIds");
                            existingUsers=userIds.toString();
                        }
                    }
                    content=groupName;
                    break;
                case "group_quit":
                    groupName=payload.getString("name");
                    creatorId =payload.getInt("owner");
                    quitUserIds=payload.getJSONArray("userIds");
                    content=quitUserIds.toString();
                    break;
                default:break;
            }
            from=String.format(normalGroup?"group/%s/%s":"event/%s/%s", groupId,sender);
            to=String.format(normalGroup?"group/%s/%s":"event/%s/%s", groupId,myUserId);
            SimpleDateFormat simpleDateFormatUS=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String dateStr=simpleDateFormatUS.format(new Date(simpleDateFormatUS.parse((String) payload.get("date")).getTime()+offset));// receiving time is gmt time
            messageModel=new MessageModel(content,dateStr,java.util.UUID.randomUUID().toString(),from,to, String.valueOf(creatorId),!myUserId.equals(String.valueOf(creatorId)));// creator in eventId
            messageModel.setType(chatType);
            messageModel.setGrouped(true);
            if (quitUserIds!=null){
                for (int i=0;i<quitUserIds.length();i++){
                    if (quitUserIds.getString(i).equals(myUserId)){
                        deleted=true;
                        return group;
                    }else{
                        existingUsers=Tools.replaceCharInStringList(existingUsers,quitUserIds.getString(i));
                    }
                }
            }
            messageModel.setGroupUsers(existingUsers);
            localDBHelper.insertMessage(messageModel, myUserId);
            if (group!=null){
                group.setGender(existingUsers);
            }
            if (normalGroup){
                String[] existingUsersList=existingUsers.substring(1,existingUsers.length()-1).split("/");
                int total=existingUsersList.length>9?9:existingUsersList.length;
                List<Drawable> profilePhotos = new ArrayList<>(total);
                Context context= ApplicationContextStore.getInstance().getContext();
                for (int i=0;i<total;i++) {
                    String userIdStr = String.valueOf(existingUsersList[i]);
                    User user = localDBHelper.getCacheUser(userIdStr);
                    if (user == null) {
                        user = UserService.getInstance().getProfile(myUserId, userIdStr);
                    }
                    FutureTarget<Bitmap> futureTarget = Glide.with(context).load(user.getAvatarStandard()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                    Drawable drawable = new BitmapDrawable(context.getResources(), futureTarget.get());
                    drawable.setBounds(0, 0, 30, 30);
                    profilePhotos.add(drawable);
                }
                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                multiDrawable.setBounds(0, 0, 30, 30);
                Bitmap multiDrawableBitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(multiDrawableBitmap);
                multiDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                multiDrawable.draw(canvas);
                String fileName = Tools.checkAppDirectory("image") + File.separator + java.util.UUID.randomUUID().toString() + ".png";
                out = new FileOutputStream(fileName);
                multiDrawableBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                return new User(groupId,existingUsers,-5, String.valueOf(creatorId),groupName, fileName, fileName);//indicate joining an normal group chat, group users in the gender attribute
            }else{
                return group;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (response!=null){
                response.body().close();
            }
            if (out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(User group) {
        super.onPostExecute(group);
        if (group!=null){
            LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
            if (group.getId()==-3){
                if (deleted){
                    localDBHelper.deleteRecentContact(myUserId,String.format("event%s",group.getFirstName()));
                }else{
                    localDBHelper.insertRecentGroupContact(group,myUserId);
                }
            }else{
                if (deleted){
                    localDBHelper.deleteRecentContact(myUserId,String.format("group%s",group.getFirstName()));
                }else{
                    localDBHelper.insertRecentChatGroupContact(group,myUserId);
                }
            }
            localDBHelper.insertUserProfile(group);
            Intent intent=new Intent("MessageService.local.message.data");
            intent.putExtra("status", Constants.RESULT_OK);
            intent.putExtra("messageModel", messageModel);
            intent.putExtra("group", group);
            intent.putExtra("deleted", deleted);
            LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent);
            if ("group_invite".equals(chatType)||"group_quit".equals(chatType)||"change_group_name".equals(chatType)){
                Intent intent3=new Intent("MessageBox.local.message.data");
                intent3.putExtra("status", Constants.RESULT_OK);
                intent3.putExtra("messageModel", messageModel);
                intent3.putExtra("group", group);
                intent3.putExtra("operationCode", 6);
                LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent3);
            }
        }
    }


}
