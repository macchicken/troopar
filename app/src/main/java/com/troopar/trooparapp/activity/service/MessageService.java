package com.troopar.trooparapp.activity.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.activity.task.CreateGroupProfileTask;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;
import com.troopar.trooparapp.utils.Tools;


import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by Barry on 13/05/2016.
 * singleton service bean for mqtt messaging
 */
public class MessageService {
    private static MessageService ourInstance = new MessageService();
    private CallbackConnection connection;
    private LocalDBHelper localDBHelper;
    private boolean inChat;
    private boolean isConnected;
    private String myUserId,myName;


    public static MessageService getInstance() {
        return ourInstance;
    }

    private MessageService() {
        Log.d("MessageService","MessageService creation");
    }

    public void connect(final String clientId){
        try{
            if (connection==null||!isConnected){
                localDBHelper = LocalDBHelper.getInstance();
                myName= Tools.isNullString(Constants.USERFIRSTNAME)||Tools.isNullString(Constants.USERLASTNAME)?Constants.USERNAME:String.format("%s %s",Constants.USERFIRSTNAME, Constants.USERLASTNAME);
                myUserId=clientId;
                MQTT mqtt = new MQTT();
                mqtt.setClientId(String.format("%s/%s",clientId,Constants.DEVEICEIDVALUE));
                try {
                    mqtt.setHost(BuildConfig.MESSAGE_HOST,BuildConfig.MESSAGE_PORT);
                    mqtt.setUserName(clientId);
                    mqtt.setPassword(clientId);
                    mqtt.setCleanSession(false);
                    mqtt.setKeepAlive(new Short("120"));
                    mqtt.setReconnectDelay(5L);
                    mqtt.setReconnectDelayMax(30L);
                    connection = mqtt.callbackConnection();
                    connection.connect(new Callback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            Log.d("MessageService",String.format("connection success %s",myUserId));
                            setUpListener();
                            isConnected=true;
                        }

                        @Override
                        public void onFailure(Throwable value) {
                            Log.d("MessageService",String.format("MessageService connection fail %s",value.getMessage()));
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.d("MessageService",String.format("MessageService connection exception %s",e.getMessage()));
                }
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    private void setUpListener(){
        connection.listener(new Listener() {
            @Override
            public void onConnected() {
                Log.d("MessageService",String.format("MessageService onConnected %s",myUserId));
            }

            @Override
            public void onDisconnected() {
                Log.d("MessageService",String.format("MessageService onDisconnected %s",myUserId));
            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                try {
                    long offset= TimeZone.getDefault().getRawOffset();
                    String bodyStr=body.utf8().toString();
                    String topicStr=topic.toString();
                    Log.d("MessageService",String.format("connection topic %s receive %s at offset %s\n",topicStr,bodyStr,offset));
                    ack.run();
                    JSONObject payload = new JSONObject(bodyStr);
                    String chatType= (String) payload.get("type");
                    if ("create_group".equals(chatType)||"group_invite".equals(chatType)||"group_quit".equals(chatType)||"change_group_name".equals(chatType)){
                        CreateGroupProfileTask task=new CreateGroupProfileTask(myUserId,payload,topicStr);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            task.execute();
                        }
                        return;
                    }
                    String from,to,content,groupId = null;
                    String[] topics=topicStr.split("/");
                    boolean grouped = topicStr.startsWith("group");
                    if (grouped){// normal and event group chat messages
                        if (payload.getString("equipId").equals(Constants.DEVEICEIDVALUE)){
                            return;
                        }
                        Object object=payload.get("groupId");
                        if (object instanceof Integer){
                            groupId= String.valueOf(((Integer) object).intValue());
                        }else{
                            groupId = (String) object;
                        }
                    }else if (topics.length==3&&topics[2].equals(Constants.DEVEICEIDVALUE)){// private chat
                        return;
                    }else if (topics.length==2&&topics[1].equals(topics[0])){// talking to myself
                        return;
                    }
                    String dateStr=Constants.simpleDateFormatUS.format(new Date(Constants.simpleDateFormatUS.parse((String) payload.get("date")).getTime()+offset));// receiving time is gmt time
                    switch (chatType){
                        case "image":
                            JSONObject jcontent=new JSONObject();
                            jcontent.put("origin",payload.getString("content"));
                            jcontent.put("new",payload.getString("thumbnail"));
                            content=jcontent.toString();
                            break;
                        case "video":
                            JSONObject jcontent2=new JSONObject();
                            jcontent2.put("origin",payload.getString("content"));
                            jcontent2.put("new",payload.getString("thumbnail"));
                            content=jcontent2.toString();
                            break;
                        case "event":
                            content=payload.getJSONObject("content").toString();
                            break;
                        case "comment":
                            MessageModel messageModel=new MessageModel(payload.getJSONObject("content").toString(),dateStr,java.util.UUID.randomUUID().toString(),"admin",myUserId,null,true);
                            messageModel.setType(chatType);
                            localDBHelper.insertMessage(messageModel, myUserId);
                            Intent commentIntent=new Intent("MessageService.local.message.data");
                            commentIntent.putExtra("status", Constants.RESULT_OK);
                            commentIntent.putExtra("messageModel", messageModel);
                            commentIntent.putExtra("setUsers", Constants.ADMINMESSAGE);
                            LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(commentIntent);
                            return;
                        case "like":
                            MessageModel messageModel3=new MessageModel(payload.getJSONObject("content").toString(),dateStr,java.util.UUID.randomUUID().toString(),"admin",myUserId,null,true);
                            messageModel3.setType(chatType);
                            localDBHelper.insertMessage(messageModel3, myUserId);
                            Intent likeIntent=new Intent("MessageService.local.message.data");
                            likeIntent.putExtra("status", Constants.RESULT_OK);
                            likeIntent.putExtra("messageModel", messageModel3);
                            likeIntent.putExtra("setUsers", Constants.ADMINMESSAGE);
                            LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(likeIntent);
                            return;
                        default:
                            content= (String) payload.get("content");
                            break;
                    }
                    boolean isComMeg=true;
                    if (grouped){
                        String sender=payload.getString("sender");
                        String deviceId=payload.getString("equipId");
                        if (sender.equals(myUserId)&&!deviceId.equals(Constants.DEVEICEIDVALUE)){
                            isComMeg=false;// a message from my user login in other devices
                        }
                        localDBHelper.insertUser(sender);
                        from=String.format(topicStr.endsWith("group")?"group/%s/%s":"event/%s/%s",groupId,sender);
                        to=String.format(topicStr.endsWith("group")?"group/%s/%s":"event/%s/%s",groupId,myUserId);
                    }else{
                        if (topics.length==3&&!topics[2].equals(Constants.DEVEICEIDVALUE)){
                            isComMeg=false;// a message from my user login in other devices
                            from=topics[0];// this is my user id
                            to=topics[1];// this is the other user id
                        }else{
                            from=topics[1];
                            to=topics[0];
                        }
                    }
                    MessageModel messageModel=new MessageModel(content,dateStr,java.util.UUID.randomUUID().toString(),from,to,null,isComMeg);
                    messageModel.setType(chatType);
                    messageModel.setGrouped(grouped);
                    localDBHelper.insertMessage(messageModel, myUserId);
                    if (!inChat){
                        Log.d("MessageService","save a receiving message not in chat page");
                        if (!messageModel.isGrouped()){
                            if (localDBHelper.insertRecentContactWithUserId(from,myUserId)==-1){// not the case of chat data being deleted or not chat with before
                                localDBHelper.updateUnreadMessageCount(myUserId,from);
                            }
                        }else{
                            String groupName=payload.getString("name");
                            if (topicStr.endsWith("event")){
                                if (localDBHelper.getCacheUser(String.format("event%s",groupId))==null){// group chat data being deleted locally
                                    User user=new User(groupId,payload.getJSONArray("joiners").toString(),-3,myUserId,groupName,null,payload.getString("smallImageUrl"));//indicate joining an event group chat
                                    localDBHelper.insertRecentGroupContact(user,myUserId);
                                    localDBHelper.insertUserProfile(user);
                                    Log.d("MessageService","not in chat and chat data being deleted,insert a new temp object");
                                    localDBHelper.getEventGroupData(groupId,myUserId);
                                }
                                localDBHelper.updateUnreadMessageCount(myUserId,String.format("event%s",groupId));// unique event id
                            }else{
                                if (localDBHelper.getCacheUser(String.format("group%s",groupId))==null){// group chat data being deleted locally
                                    User user=new User(groupId,null,-5,null,groupName,null,null);
                                    localDBHelper.insertRecentChatGroupContact(user,myUserId);
                                    localDBHelper.insertUserProfile(user);
                                    Log.d("MessageService","not in chat and chat data being deleted,insert a new temp object");
                                    localDBHelper.getNormalGroupData(groupId,groupName);
                                }
                                localDBHelper.updateUnreadMessageCount(myUserId,String.format("group%s",groupId));// unique group id
                            }
                        }
                        Intent intent=new Intent("MessageService.local.message.data");
                        intent.putExtra("status", Constants.RESULT_OK);
                        intent.putExtra("messageModel", messageModel);
                        LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent);
                    }else{
                        Intent intent=new Intent("MessageBox.local.message.data");
                        intent.putExtra("status", Constants.RESULT_OK);
                        intent.putExtra("operationCode", messageModel.isGrouped()?5:3);
                        intent.putExtra("messageModel", messageModel);
                        LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("MessageService",String.format("connection listener onFailure %s",value.getMessage()));
            }
        });
        Topic[] topics = {new Topic(myUserId+"/#", QoS.EXACTLY_ONCE)};
        subscribeTopic(topics);
    }

    private void subscribeTopic(Topic[] topics){
        connection.subscribe(topics,new Callback<byte[]>(){
            @Override
            public void onSuccess(byte[] value) {
                Log.d("MessageService",String.format("connection subscribe %s",myUserId));
            }
            @Override
            public void onFailure(Throwable value) {
                Log.d("MessageService",String.format("MessageService subscribe fail %s",value.getMessage()));
            }
        });
    }

    public void disconnect(final String clientId){
        if (connection!=null&&isConnected){
            connection.disconnect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    Log.d("MessageService",String.format("connection disconnect %s",clientId));
                    isConnected=false;
                }

                @Override
                public void onFailure(Throwable value) {
                    Log.d("MessageService",String.format("connection disconnect fail %s",clientId));
                }
            });
        }
    }

    public void send(final String sendId,final String receiverId,final String message,final String type,final JSONObject remarks){
        if (connection!=null&&isConnected){
            final JSONObject messageObj=new JSONObject();
            try {
                final long offset=TimeZone.getDefault().getRawOffset();
                String dateStr=Constants.simpleDateFormatUS.format(new Date(System.currentTimeMillis()-offset));
                messageObj.put("type",type);
                messageObj.put("date",dateStr);
                messageObj.put("equipId",Constants.DEVEICEIDVALUE);
                if("video".equals(type)||"image".equals(type)){
                    messageObj.put("content",remarks.getString("origin"));
                    messageObj.put("thumbnail",remarks.getString("new"));
                }else{
                    messageObj.put("content",message);
                }
                String topic;
                boolean isEventGroup=receiverId.startsWith("event");
                boolean grouped=receiverId.startsWith("group")||isEventGroup;
                if (grouped){
                    if (type.equals("create_group")||type.equals("group_invite")||type.equals("group_quit")||type.equals("change_group_name")){
                        int[] groupUsers= (int[]) remarks.get("groupUsers");
                        StringBuilder stringBuilder=new StringBuilder("group");
                        for (int userId:groupUsers){// send all participates to create chat group or new invited users
                            stringBuilder.append("/").append(userId);
                        }
                        if (isEventGroup){
                            JSONArray jsonArray=new JSONArray();
                            for (int userId:groupUsers){
                                jsonArray.put(userId);
                            }
                            messageObj.put("smallImageUrl",remarks.getString("smallImageUrl"));
                            messageObj.put("joiners",jsonArray);
                        }
                        stringBuilder.append("/").append(remarks.getString("uid")).append("/").append(remarks.getString("flag"));// can either be event or group id
                        topic=stringBuilder.toString();
                        if (!type.equals("change_group_name")){
                            int[] userIds= (int[]) remarks.get("userIds");
                            JSONArray jsonArray=new JSONArray();
                            for (int uid:userIds){
                                jsonArray.put(uid);
                            }
                            messageObj.put("userIds",jsonArray);
                        }
                        if (!type.equals("create_group")){
                            messageObj.put("owner",Integer.parseInt(remarks.getString("creator")));
                            messageObj.put("name",remarks.getString("groupName"));
                        }
                        messageObj.put("sender",Integer.parseInt(sendId));
                        connection.publish(topic,messageObj.toString().getBytes(),QoS.EXACTLY_ONCE,false,null);
                        return;
                    }else{// normal chat
                        int[] userIds= (int[]) remarks.get("groupUsers");
                        StringBuilder stringBuilder=new StringBuilder("group");
                        for (int userId:userIds){// send all participates to create chat group or new invited users
                            stringBuilder.append("/").append(userId);
                        }
                        stringBuilder.append("/").append(remarks.getString("uid")).append("/").append(remarks.getString("flag"));// can either be event or group id
                        topic=stringBuilder.toString();
                        messageObj.put("name",remarks.getString("groupName"));
                        messageObj.put("sender",Integer.parseInt(sendId));
                        messageObj.put("groupId",remarks.getString("uid"));
                        if (isEventGroup){
                            JSONArray jsonArray=new JSONArray();
                            for (int uid:userIds){
                                jsonArray.put(uid);
                            }
                            messageObj.put("smallImageUrl",remarks.getString("smallImageUrl"));
                            messageObj.put("joiners",jsonArray);
                        }
                    }
                }else{
                    topic=String.format("%s/%s",receiverId,sendId);
                }
                final boolean finalGrouped = grouped;
                final MessageModel messageModel=new MessageModel(message,Constants.simpleDateFormatUS.format(new Date(System.currentTimeMillis())),java.util.UUID.randomUUID().toString(),sendId,receiverId,type.equals("event")?message:"",false);
                messageModel.setMsgType(false);
                messageModel.setType(type);
                messageModel.setGrouped(grouped);
                if (!grouped&&sendId.equals(receiverId)){// the user in his own message chat page
                    localDBHelper.insertMessage(messageModel, sendId);
                    Intent intent=new Intent("MessageBox.local.message.data");
                    intent.putExtra("status", Constants.RESULT_OK);
                    intent.putExtra("operationCode", 1);
                    intent.putExtra("messageModel", messageModel);
                    LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent);
                    connection.publish(String.format("%s/%s/%s",sendId,receiverId,Constants.DEVEICEIDVALUE),messageObj.toString().getBytes(), QoS.EXACTLY_ONCE,false,null);
                }else{
                    connection.publish(topic,messageObj.toString().getBytes(), QoS.EXACTLY_ONCE, false,new Callback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            Log.d("MessageService",String.format("connection send success %s at offset %s",receiverId,offset));
                            if (!finalGrouped){
                                connection.publish(String.format("%s/%s/%s",sendId,receiverId,Constants.DEVEICEIDVALUE),messageObj.toString().getBytes(), QoS.EXACTLY_ONCE,false,null);
                            }
                            localDBHelper.insertMessage(messageModel, sendId);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                new NotificationTask(messageModel.getContent(),myUserId,messageModel.getTo(),finalGrouped?remarks.optString("uid",null):null,finalGrouped).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                new NotificationTask(messageModel.getContent(),myUserId,messageModel.getTo(),finalGrouped?remarks.optString("uid",null):null,finalGrouped).execute();
                            }
                            Intent intent=new Intent("MessageBox.local.message.data");
                            intent.putExtra("status", Constants.RESULT_OK);
                            intent.putExtra("operationCode", 1);
                            intent.putExtra("messageModel", messageModel);
                            LocalBroadcastManager.getInstance(ApplicationContextStore.getInstance().getContext()).sendBroadcast(intent);
                        }

                        @Override
                        public void onFailure(Throwable value) {
                            Log.d("MessageService",String.format("MessageService send message fail %s",value.getMessage()));
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("MessageService",String.format("MessageService send message exception %s\n",e.getMessage()));
            }
        }
    }

    private class NotificationTask extends AsyncTask<Void,Void,JSONObject> {

        private String mUserId;
        private String mSenderId;
        private String mMessage;
        private boolean mIsGroup;
        private String mGroupId;

        public NotificationTask(String message, String senderId, String userId,String groupId,boolean isGroup) {
            mMessage = message;
            mSenderId = senderId;
            mUserId = userId;
            mIsGroup=isGroup;
            mGroupId=groupId;
        }

        @Override
        protected JSONObject doInBackground(java.lang.Void... params) {
            HashMap<String,String> parameters=new HashMap<>();
            if (!mIsGroup){
                parameters.put("userId",mUserId);
            }else{
                parameters.put("type","group");
                parameters.put("groupId",mGroupId);
            }
            parameters.put("senderId",mSenderId);
            parameters.put("title","Troopar Message");
            parameters.put("message",String.format("%s: %s",myName,mMessage));
            parameters.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
            parameters.put(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
            return new JSONParser().makeRequestForHttp(BuildConfig.API_READHOST + "/notification/android.php", "POST", parameters);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            Log.d("MessageService", String.format("android notification: %s",String.valueOf(jsonObject)));
        }
    }

    public void setInChat(boolean inChat) {
        this.inChat = inChat;
    }


}
