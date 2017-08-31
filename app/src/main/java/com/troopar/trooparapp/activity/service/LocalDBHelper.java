package com.troopar.trooparapp.activity.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.activity.MultiDrawable;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Barry on 29/02/2016.
 * local Sqlite service
 */
public class LocalDBHelper extends SQLiteOpenHelper {

    private final String TABLE_NAME = "TroopMessages";
    private final String COLUMN_ID = "ID";
    private final String COLUMN_MESSAGE_ID = "MID";
    private final String COLUMN_FROM = "SENDER";
    private final String COLUMN_TO = "RECIPIENT";
    private final String COLUMN_TIME = "TIME";
    private final String COLUMN_CONTENT = "CONTENT";
    private final String COLUMN_EVENTID = "EVENTID";
    private final String COLUMN_MESSAGE_TYPE = "MTYPE";
    private final String COLUMN_CHAT_TYPE = "CTYPE";
    private final String COLUMN_OWNER_ID = "MOWNERID";
    private final String COLUMN_GROUPUSERS = "GROUPUSERIDS";
    private final String COLUMN_GROUPFLAG = "GROUPFLAG";
    private final String TABLE_RECENT_CONTACT = "RecentContacts";
    private final String COLUMN_R_F = "USER";
    private final String COLUMN_R_MYID = "MYID";
    private final String COLUMN_R_FID = "FID";
    private final String COLUMN_R_UNREAD = "UNRCOUNT";
    private final String TABLE_ACTIVITIES = "TrooparActivities";
    private final String COLUMN_ACTS="ACTS";
    private final String COLUMN_USERID_ACT="USERID";
    private final String TABLE_USERPROFILE="UserProfile";
    private final String COLOUMN_USERID="USERID";
    private final String COLOUMN_USER_PROFILE="UserprofileView";
    private final String TABLE_EVENTS = "TrooparEvents";
    private final String COLOUMN_EVENTS="EVENTS";
    private final String TABLE_UNREAD = "TrooparUnread";
    private final String COLOUMN_COUNT="MESSCOUNT";
    private final String COLOUMN_UNUSER="USERID";
    private static LocalDBHelper localDBHelper;


    public static LocalDBHelper getInstance(Context context) {// general local database for no user specific
        if (localDBHelper == null){
            localDBHelper = new LocalDBHelper(context,null);
        }
        return localDBHelper;
    }

    public static LocalDBHelper getUserInstance(Context context,String userId) {
        if (localDBHelper == null){
            localDBHelper = new LocalDBHelper(context,userId);
        }
        return localDBHelper;
    }

    public static LocalDBHelper getInstance() {
        return localDBHelper;
    }

    private LocalDBHelper(Context context,String userId)
    {
        this(context, userId==null?"TroopLocalDB.db":"TroopLocalDB"+userId+".db", null, 41);
        Log.d("LocalDBHelper", "LocalDBHelper creation");
    }

    private LocalDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d("LocalDBHelper", String.format("LocalDBHelper creation %s with version %d",name,version));
    }

    private LocalDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        Log.d("LocalDBHelper", String.format("LocalDBHelper creation with version %d and DatabaseErrorHandler",version));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("LocalDBHelper", "LocalDBHelper onCreate");
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s INTEGER,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s INTEGER) ",
                TABLE_NAME, COLUMN_ID, COLUMN_MESSAGE_ID, COLUMN_FROM, COLUMN_TO, COLUMN_TIME, COLUMN_CONTENT, COLUMN_EVENTID, COLUMN_MESSAGE_TYPE,COLUMN_CHAT_TYPE,COLUMN_OWNER_ID,COLUMN_GROUPUSERS,COLUMN_GROUPFLAG));
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR,%s BLOB,%s VARCHAR,%s INTEGER DEFAULT 0,PRIMARY KEY (%s, %s)) ",
                TABLE_RECENT_CONTACT, COLUMN_R_MYID, COLUMN_R_F, COLUMN_R_FID, COLUMN_R_UNREAD, COLUMN_R_MYID, COLUMN_R_FID));
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR PRIMARY KEY,%s BLOB) ",TABLE_ACTIVITIES,COLUMN_USERID_ACT,COLUMN_ACTS));
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR PRIMARY KEY,%s BLOB) ",TABLE_USERPROFILE,COLOUMN_USERID,COLOUMN_USER_PROFILE));
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR PRIMARY KEY,%s BLOB) ",TABLE_EVENTS,COLOUMN_USERID,COLOUMN_EVENTS));
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s VARCHAR PRIMARY KEY,%s INTEGER) ",TABLE_UNREAD,COLOUMN_UNUSER,COLOUMN_COUNT));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("LocalDBHelper",String.format("LocalDBHelper onUpgrade from %d to %d",oldVersion,newVersion));
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_RECENT_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USERPROFILE);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_UNREAD);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_EVENTS);
        onCreate(db);
    }

    public long insertMessage(MessageModel messageModel,String myUserId)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MESSAGE_ID, messageModel.getId());
        contentValues.put(COLUMN_FROM, messageModel.getFrom());
        contentValues.put(COLUMN_TO, messageModel.getTo());
        contentValues.put(COLUMN_TIME, messageModel.getCreatedTime());
        contentValues.put(COLUMN_CONTENT, messageModel.getContent());
        contentValues.put(COLUMN_EVENTID, messageModel.getEventId());
        contentValues.put(COLUMN_MESSAGE_TYPE, messageModel.getMsgType()?1:0);// coming message or sending message
        contentValues.put(COLUMN_CHAT_TYPE, messageModel.getType());
        contentValues.put(COLUMN_OWNER_ID, myUserId);
        contentValues.put(COLUMN_GROUPUSERS, messageModel.getGroupUsers());
        contentValues.put(COLUMN_GROUPFLAG, messageModel.isGrouped()?1:0);
        return db.insert(TABLE_NAME, null, contentValues);
    }

    public void updateUnreadMessageCount(String userId,String fUserId){
        SQLiteDatabase db = getWritableDatabase();
        String updateSql=String.format("UPDATE %s set %s = %s + 1 WHERE %s = ? and %s = ?",TABLE_RECENT_CONTACT,COLUMN_R_UNREAD,COLUMN_R_UNREAD,COLUMN_R_MYID,COLUMN_R_FID);
        db.beginTransaction();
        try {
            db.execSQL(updateSql,new String[]{userId,fUserId});
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    public int resetUnreadMessageCount(String userId,String fUserId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_R_UNREAD,0);
        return db.update(TABLE_RECENT_CONTACT,contentValues,String.format("%s = ? and %s = ?",COLUMN_R_MYID,COLUMN_R_FID),new String[]{userId,fUserId});
    }

    public long initTotalUnreadCount(String userID){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOUMN_COUNT,0);
        contentValues.put(COLOUMN_UNUSER,userID);
        return db.insertWithOnConflict(TABLE_UNREAD, null, contentValues,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int resetTotalUnreadMessageCount(String userId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOUMN_COUNT,0);
        return db.update(TABLE_UNREAD,contentValues,String.format("%s = ?",COLOUMN_UNUSER),new String[]{userId});
    }

    public void updateTotalUnreadMessageCount(String userId){
        SQLiteDatabase db = getWritableDatabase();
        String updateSql=String.format("UPDATE %s set %s = %s + 1 where %s = ?",TABLE_UNREAD,COLOUMN_COUNT,COLOUMN_COUNT,COLOUMN_UNUSER);
        db.beginTransaction();
        try {
            db.execSQL(updateSql,new String[]{userId});
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    public int readUnreadTotal(String userId){
        SQLiteDatabase db = getReadableDatabase();
        int totalUnread=0;
        Cursor res = db.rawQuery(String.format("select * from %s where %s = %s", TABLE_UNREAD, COLOUMN_UNUSER, userId), null);
        if (res.getCount()>0){
            res.moveToNext();
            totalUnread=res.getInt(res.getColumnIndex(COLOUMN_COUNT));
        }
        res.close();
        return totalUnread;
    }

    public long insertRecentGroupContact(User fUser,String myId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_R_MYID, myId);
        contentValues.put(COLUMN_R_F, getSerializedObject(fUser));
        contentValues.put(COLUMN_R_FID, String.format("event%s",fUser.getFirstName()));
        return db.insertWithOnConflict(TABLE_RECENT_CONTACT, null, contentValues,fUser.getAvatarOrigin()==null?SQLiteDatabase.CONFLICT_IGNORE:SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long insertRecentChatGroupContact(User fUser,String myId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_R_MYID, myId);
        contentValues.put(COLUMN_R_F, getSerializedObject(fUser));
        contentValues.put(COLUMN_R_FID, String.format("group%s",fUser.getFirstName()));
        return db.insertWithOnConflict(TABLE_RECENT_CONTACT, null, contentValues,fUser.getAvatarOrigin()==null?SQLiteDatabase.CONFLICT_IGNORE:SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int deleteRecentContact(String myId,String fUserId){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_RECENT_CONTACT,String.format("%s=? and %s=?",COLUMN_R_MYID,COLUMN_R_FID),new String[]{myId,fUserId});
    }

    public boolean updateRecentContact(User fUser, String myId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_R_MYID, myId);
        contentValues.put(COLUMN_R_F, getSerializedObject(fUser));
        contentValues.put(COLUMN_R_FID, String.valueOf(fUser.getId()));
        db.insertWithOnConflict(TABLE_RECENT_CONTACT, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    public void insertRecentContact(String myId,User user){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_R_MYID, myId);
        contentValues.put(COLUMN_R_F, getSerializedObject(user));
        contentValues.put(COLUMN_R_FID, String.valueOf(user.getId()));
        contentValues.put(COLUMN_R_UNREAD,1);
        db.insertWithOnConflict(TABLE_RECENT_CONTACT, null, contentValues,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long insertRecentContactWithUserId(String fUserId, String myId) {
        synchronized (this){
            if (checkUser(fUserId)>0){return -1;}
            insertUserProfile(new User(null,null,Integer.parseInt(fUserId),null,null,null,null));
        }
        runAsyncTask(new InsertRecentContactTask(myId,fUserId));
        return 0;
    }

    private class InsertRecentContactTask extends AsyncTask<Void,Void,User>{

        private String mMyid;
        private String mFUserId;

        public InsertRecentContactTask(String myid,String fUserId) {
            this.mFUserId = fUserId;
            this.mMyid = myid;
        }

        @Override
        protected User doInBackground(Void... params) {
            return UserService.getInstance().getProfile(mMyid,mFUserId);
        }

        @Override
        protected void onPostExecute(User user) {
            if (user!=null){
                insertRecentContact(mMyid,user);
                insertUserProfile(user);
            }
        }
    }

    private void runAsyncTask(AsyncTask<Void, Void, User> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private class SaveUserTask extends AsyncTask<Void, Void, User>{
        private String mUserId;

        private SaveUserTask(String userId){
            mUserId=userId;
        }
        @Override
        protected User doInBackground(Void... params) {
            return UserService.getInstance().getProfile(Constants.USERID,mUserId);
        }

        @Override
        protected void onPostExecute(User user) {
            if (user!=null){
                insertUserProfile(user);
            }
        }
    }

    public long insertUser(String userId) {
        synchronized (this){
            if (checkUser(userId)>0){return -1;}
            insertUserProfile(new User(null,null,Integer.parseInt(userId),null,null,null,null));
        }
        runAsyncTask(new SaveUserTask(userId));
        return 0;
    }

    public ArrayList<User> getRecentContacts(String myUserId){
        ArrayList<User> recent=new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor res =  db.rawQuery(String.format("select * from %s where %s=%s",TABLE_RECENT_CONTACT,COLUMN_R_MYID,myUserId),null);
        if (res.getCount()>0){
            res.moveToNext();
            while(!res.isAfterLast()){
                User user=(User) readSerializedObject(res.getBlob(res.getColumnIndex(COLUMN_R_F)));
                if (user!=null){
                    user.setUnreadMessageCount(res.getInt(res.getColumnIndex(COLUMN_R_UNREAD)));
                    recent.add(user);
                }
                res.moveToNext();
            }
        }
        res.close();
        return recent;
    }

    public ArrayList<MessageModel> getMessagesWithUser(String userId,String otherUserId,long limit,long lastMessagePosition)
    {
        ArrayList<MessageModel> array_list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Log.d("LocalDBHelper",String.format("getMessagesWithUser %s lastMessagePosition %s",userId,lastMessagePosition));
        Cursor res = db.rawQuery(String.format("select * from (select * from %s where %s='%s' and (%s='%s' or %s='%s') order by %s) LIMIT %d OFFSET %d",TABLE_NAME,COLUMN_OWNER_ID,userId,COLUMN_FROM,otherUserId,COLUMN_TO,otherUserId,COLUMN_ID,limit,lastMessagePosition),null);
        if (res.getCount()>0){
            res.moveToNext();
            while(!res.isAfterLast()){
                MessageModel temp=new MessageModel(res.getString(res.getColumnIndex(COLUMN_CONTENT)),res.getString(res.getColumnIndex(COLUMN_TIME)),res.getString(res.getColumnIndex(COLUMN_MESSAGE_ID)),
                        res.getString(res.getColumnIndex(COLUMN_FROM)), res.getString(res.getColumnIndex(COLUMN_TO)),res.getString(res.getColumnIndex(COLUMN_EVENTID)), res.getInt(res.getColumnIndex(COLUMN_MESSAGE_TYPE)) == 1);
                temp.setType(res.getString(res.getColumnIndex(COLUMN_CHAT_TYPE)));
                temp.setGrouped(res.getInt(res.getColumnIndex(COLUMN_GROUPFLAG)) == 1);
                temp.setRowId(res.getInt(res.getColumnIndex(COLUMN_ID)));
                temp.setGroupUsers(res.getString(res.getColumnIndex(COLUMN_GROUPUSERS)));
                array_list.add(temp);
                res.moveToNext();
            }
        }
        res.close();
        return array_list;
    }

    public ArrayList<MessageModel> getAdminMessagesWithUser(String userId,String messageType,long limit,long lastMessagePosition)
    {
        ArrayList<MessageModel> array_list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Log.d("LocalDBHelper",String.format("getMessagesWithUser %s lastMessagePosition %s",userId,lastMessagePosition));
        Cursor res = db.rawQuery(String.format("select * from (select * from %s where %s='%s' and %s='%s' and %s='%s'order by %s) LIMIT %d OFFSET %d",TABLE_NAME,COLUMN_OWNER_ID,userId,COLUMN_FROM,"admin",COLUMN_CHAT_TYPE,messageType,COLUMN_ID,limit,lastMessagePosition),null);
        if (res.getCount()>0){
            res.moveToNext();
            while(!res.isAfterLast()){
                MessageModel temp=new MessageModel(res.getString(res.getColumnIndex(COLUMN_CONTENT)),res.getString(res.getColumnIndex(COLUMN_TIME)),res.getString(res.getColumnIndex(COLUMN_MESSAGE_ID)),
                        res.getString(res.getColumnIndex(COLUMN_FROM)), res.getString(res.getColumnIndex(COLUMN_TO)),res.getString(res.getColumnIndex(COLUMN_EVENTID)), res.getInt(res.getColumnIndex(COLUMN_MESSAGE_TYPE)) == 1);
                temp.setType(res.getString(res.getColumnIndex(COLUMN_CHAT_TYPE)));
                temp.setGrouped(res.getInt(res.getColumnIndex(COLUMN_GROUPFLAG)) == 1);
                temp.setRowId(res.getInt(res.getColumnIndex(COLUMN_ID)));
                temp.setGroupUsers(res.getString(res.getColumnIndex(COLUMN_GROUPUSERS)));
                array_list.add(temp);
                res.moveToNext();
            }
        }
        res.close();
        return array_list;
    }

    public long getMessageCountWithUser(String userId,String otherUserId){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res=db.rawQuery(String.format("select count(1) from %s where %s='%s' and (%s='%s' or %s='%s') ",TABLE_NAME,COLUMN_OWNER_ID,userId,COLUMN_FROM,otherUserId,COLUMN_TO,otherUserId),null);
        if (res.getCount()>0){
            res.moveToNext();
            return res.getLong(0);
        }
        res.close();
        return 0;
    }

    public long getAdminMessagesCountWithUser(String userId,String messageType){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res=db.rawQuery(String.format("select count(1) from %s where %s='%s' and %s='%s' and %s='%s'",TABLE_NAME,COLUMN_OWNER_ID,userId,COLUMN_FROM,"admin",COLUMN_CHAT_TYPE,messageType),null);
        if (res.getCount()>0){
            res.moveToNext();
            return res.getLong(0);
        }
        res.close();
        return 0;
    }

    private byte[] getSerializedObject(Serializable s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (oos != null){
                    oos.close();
                }
                baos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        return baos.toByteArray();
    }

    private Object readSerializedObject(byte[] in) {
        Object result = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            result = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            try {
                assert ois != null;
                ois.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public long insertActivity(String userId,ArrayList<ActivityModel> activityModels) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERID_ACT,userId);
        contentValues.put(COLUMN_ACTS, getSerializedObject(activityModels));
        return db.insertWithOnConflict(TABLE_ACTIVITIES, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<ActivityModel> getActivities(String userId){
        Cursor res=null;
        ArrayList<ActivityModel> results = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            res =  db.rawQuery(String.format("select * from %s where %s='%s'", TABLE_ACTIVITIES, COLUMN_USERID_ACT, userId), null);
            if (res.getCount()>0){
                res.moveToNext();
                results= (ArrayList<ActivityModel>) readSerializedObject(res.getBlob(res.getColumnIndex(COLUMN_ACTS)));
                Log.d("LocalDBHelper",String.format("%s getActivities from local db %d",userId,results==null?0:results.size()));
            }
        }catch (Throwable t){
            t.printStackTrace();
        }finally {
            if (res != null) {
                res.close();
            }
        }
        return results;
    }

    public long insertUserProfile(User user){
        Log.d("LocalDBHelper",String.format("save user %d profile to local db",user.getId()));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        switch (user.getId()){// group chat, unique id in the first name attribute
            case -5:
                contentValues.put(COLOUMN_USERID,"group"+user.getFirstName());
                break;
            case -3:
                contentValues.put(COLOUMN_USERID,"event"+user.getFirstName());
                break;
            default:
                contentValues.put(COLOUMN_USERID,String.valueOf(user.getId()));
                break;
        }
        contentValues.put(COLOUMN_USER_PROFILE, getSerializedObject(user));
        return db.insertWithOnConflict(TABLE_USERPROFILE, null, contentValues,user.getAvatarOrigin()==null?SQLiteDatabase.CONFLICT_IGNORE:SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long insertUserEvents(String userId, ArrayList<EventModel> eventModels){
        Log.d("LocalDBHelper",String.format("%s save user events to local db",userId));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOUMN_USERID,userId);
        contentValues.put(COLOUMN_EVENTS, getSerializedObject(eventModels));
        return db.insertWithOnConflict(TABLE_EVENTS, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public User getCacheUser(String userId){
        Cursor res=null;
        User user = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            res =  db.rawQuery(String.format("select * from %s where %s='%s'", TABLE_USERPROFILE, COLOUMN_USERID, userId), null);
            if (res.getCount()>0){
                res.moveToNext();
                Log.d("LocalDBHelper",String.format("%s get user profile from local db",userId));
                user=(User) readSerializedObject(res.getBlob(res.getColumnIndex(COLOUMN_USER_PROFILE)));
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            if (res!=null){
                res.close();
            }
        }
        return user;
    }

    private long checkUser(String userId){
        Cursor res=null;
        try {
            Log.d("LocalDBHelper",String.format("%s check user from local db",userId));
            SQLiteDatabase db = getReadableDatabase();
            res =  db.rawQuery(String.format("select count(1) from %s where %s='%s'", TABLE_USERPROFILE, COLOUMN_USERID, userId), null);
            if (res.getCount()>0){
                res.moveToNext();
                return res.getLong(0);
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            if (res!=null){
                res.close();
            }
        }
        return 0;
    }

    public ArrayList<EventModel> getCacheUserEvents(String userId){
        Cursor res=null;
        ArrayList<EventModel> eventModels = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            res =  db.rawQuery(String.format("select * from %s where %s='%s'", TABLE_EVENTS, COLOUMN_USERID, userId), null);
            if (res.getCount()>0){
                res.moveToNext();
                Log.d("LocalDBHelper",String.format("%s get user events from local db",userId));
                eventModels=(ArrayList<EventModel>) readSerializedObject(res.getBlob(res.getColumnIndex(COLOUMN_EVENTS)));
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            if (res!=null){
                res.close();
            }
        }
        return eventModels;
    }

    private static class UpdateGroupChatDetail extends AsyncTask<Void, Void, String>{

        private String eventId;
        private String myUserId;

        public UpdateGroupChatDetail(String eventId,String myUserId) {
            this.eventId = eventId;
            this.myUserId = myUserId;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("eventIds[]", eventId)
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/event/get_event_list.php").post(formBody).build();
            Response response = null;
            try {
                response=client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (response!=null){
                    response.body().close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseStr) {
            super.onPostExecute(responseStr);
            if (responseStr!=null){
                try {
                    JSONObject result=new JSONObject(responseStr);
                    if (Constants.TAG_SUCCESS.equals(result.getString("status"))){
                        JSONObject event=result.getJSONArray("events").getJSONObject(0);
                        JSONArray jsonArray=new JSONArray();
                        JSONArray joiners=event.getJSONArray("joiners");
                        int totalJoiners=joiners.length();
                        for (int i=0;i<totalJoiners;i++){
                            jsonArray.put(joiners.getJSONObject(i).getInt("id"));
                        }
                        User group=new User(event.getString("id"),jsonArray.toString(),-3,event.getString("userId"),event.getString("name"),event.getString("imagePath"),event.getString("thumbnailImageUrl"));
                        LocalDBHelper.getInstance().insertRecentGroupContact(group,myUserId);
                        LocalDBHelper.getInstance().insertUserProfile(group);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getEventGroupData(String eventId,String myUserId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateGroupChatDetail(eventId, myUserId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new UpdateGroupChatDetail(eventId, myUserId).execute();
        }
    }

    public void getNormalGroupData(String groupId,String groupName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateNormalGroupChatDetail(groupId, groupName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new UpdateNormalGroupChatDetail(groupId, groupName).execute();
        }
    }

    private static class UpdateNormalGroupChatDetail extends AsyncTask<Void,Void,List<Drawable>>{

        private String groupId;
        private String groupName;
        private JSONArray result;
        private String ownerId;

        public UpdateNormalGroupChatDetail(String groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }

        @Override
        protected List<Drawable> doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("groupId", groupId)
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/get_group_members.php").post(formBody).build();
            Response response = null;
            try {
                response=client.newCall(request).execute();
                JSONObject jsonObject= new JSONObject(response.body().string());
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    result=jsonObject.getJSONArray("result");
                    ownerId=jsonObject.getString("ownerId");
                    int total=result.length()>9?9:result.length();
                    List<Drawable> profilePhotos = new ArrayList<>(total);
                    Context context= ApplicationContextStore.getInstance().getContext();
                    for (int i=0;i<total;i++) {
                        String userIdStr = String.valueOf(result.get(i));
                        User user = localDBHelper.getCacheUser(userIdStr);
                        if (user == null) {
                            user = UserService.getInstance().getProfile(Constants.USERID, userIdStr);
                        }
                        FutureTarget<Bitmap> futureTarget = Glide.with(context).load(user.getAvatarStandard()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                        Drawable drawable = new BitmapDrawable(context.getResources(), futureTarget.get());
                        drawable.setBounds(0, 0, 30, 30);
                        profilePhotos.add(drawable);
                    }
                    return profilePhotos;
                }
            } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                if (response!=null){
                    response.body().close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Drawable> profilePhotos) {
            super.onPostExecute(profilePhotos);
            if (profilePhotos==null){return;}
            FileOutputStream out=null;
            try {
                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                multiDrawable.setBounds(0, 0, 30, 30);
                Bitmap multiDrawableBitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(multiDrawableBitmap);
                multiDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                multiDrawable.draw(canvas);
                String fileName = Tools.checkAppDirectory("image") + File.separator + java.util.UUID.randomUUID().toString() + ".png";
                out = new FileOutputStream(fileName);
                multiDrawableBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                User group=new User(groupId,result.toString(),-5,ownerId,groupName,fileName,fileName);// update group member
                LocalDBHelper.getInstance().insertRecentChatGroupContact(group,Constants.USERID);
                LocalDBHelper.getInstance().insertUserProfile(group);
            } catch (Throwable e) {
                e.printStackTrace();
            }finally {
                if (out!=null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void initDatabase(){
        Log.d("LocalDBHelper","initDatabase with getReadableDatabase");
        getReadableDatabase();
    }

    public void releaseResource(){
        localDBHelper.close();
        localDBHelper = null;
    }


}
