package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.adapter.UsersCheckBoxViewAdapter;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateGroupActivity extends AppCompatActivity {

    private HashMap<Integer,User> selectedUsers;
    private ArrayList<User> users;
    private int counter;
    private String myUserId;
    private boolean invitePeople;
    private User group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent intent=getIntent();
        myUserId=intent.getStringExtra("myUserId");
        invitePeople = intent.getBooleanExtra("invitePeople", false);
        HashMap<Integer,User> inGroupUsers = null;
        if (invitePeople){
            group= (User) intent.getSerializableExtra("group");
            String[] temp=group.getGender().substring(1,group.getGender().length()-1).split(",");
            inGroupUsers=new HashMap<>();
            for (String userId:temp){
                if (!"".equals(userId)){
                    inGroupUsers.put(Integer.parseInt(userId),null);
                }
            }
        }
        selectedUsers=new HashMap<>();
        executeTask(new GetFriendListTask(myUserId,inGroupUsers));
    }

    public void selectUserAction(View view){
        CheckBox checkBox= (CheckBox) view;
        String position=checkBox.getContentDescription().toString();
        User user=users.get(Integer.parseInt(position));
        if (checkBox.isChecked()){
            ++counter;
            selectedUsers.put(user.getId(),user);
        }else{
            --counter;
            selectedUsers.put(user.getId(),null);
        }
        if (counter>0){
            ((TextView)findViewById(R.id.selectPeopleBtn)).setText(getString(R.string.okCount,counter));
        }else{
            ((TextView)findViewById(R.id.selectPeopleBtn)).setText(getString(R.string.ok));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createGroupAction(View view){
        if (counter<=0){return;}
        try {
            if (invitePeople){
                executeTask(new InviteToGroupTask(selectedUsers,myUserId,group));
            }else{
                executeTask(new CreateGroupTask(myUserId,selectedUsers));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void executeTask(AsyncTask<Void,Void,String> task){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            task.execute();
        }
    }

    private class GetFriendListTask extends AsyncTask<Void,Void,String>{

        private String mMyUserId;
        private HashMap<Integer,User> mInGroupUsers;
        private ProgressDialog progressDialog;

        public GetFriendListTask(String myUserId,HashMap<Integer,User> inGroupUsers) {
            mMyUserId = myUserId;
            mInGroupUsers=inGroupUsers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CreateGroupActivity.this, "loading...", "please wait...", false, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("userId", mMyUserId)
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/user/get_friend_list.php").post(formBody).build();
            Response response = null;
            try {
                response=client.newCall(request).execute();
                return response.body().string();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s!=null){
                try {
                    JSONObject result=new JSONObject(s);
                    if (Constants.TAG_SUCCESS.equals(result.getString("status"))){
                        users=new ArrayList<>();
                        JSONArray friends=result.getJSONArray("friends");
                        int total=friends.length();
                        for (int i=0;i<total;i++){
                            JSONObject t= (JSONObject) friends.get(i);
                            User user=new User(t.getString("firstName"),t.getString("gender"),t.getInt("id"),t.getString("lastName"),t.getString("username"),t.getString("avatarOrigin"),t.getString("avatarStandard"));
                            user.setPhone(t.getString("phone"));
                            user.setEmail("email");
                            users.add(user);
                        }
                        if (total>0){
                            ListView userList= (ListView) findViewById(R.id.userList);
                            UsersCheckBoxViewAdapter viewAdapter=new UsersCheckBoxViewAdapter(CreateGroupActivity.this,users,mInGroupUsers);
                            userList.setAdapter(viewAdapter);
                            viewAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CreateGroupTask extends AsyncTask<Void,Void,String> {

        private ProgressDialog progressDialog;
        private String topic;
        private int[] groupUsers;
        private String mMyUserId;
        private HashMap<Integer,User> mSelectedUsers;

        public CreateGroupTask(String myUserId,HashMap<Integer,User> selectedUsers) {
            mMyUserId = myUserId;
            mSelectedUsers=selectedUsers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CreateGroupActivity.this, "loading...", "Creating a group, please wait...", false, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            topic=java.util.UUID.randomUUID().toString();
            MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("ownerId", mMyUserId)
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
            Iterator<User> iterator=mSelectedUsers.values().iterator();
            groupUsers=new int[counter+1];
            formBody.addFormDataPart("userIds[]",mMyUserId);
            groupUsers[0]=Integer.parseInt(mMyUserId);
            int i=1;
            while(iterator.hasNext()){
                User user=iterator.next();
                if (user!=null){
                    String userIdStr=String.valueOf(user.getId());
                    formBody.addFormDataPart("userIds[]",userIdStr);
                    groupUsers[i]=user.getId();
                    i++;
                }
            }
            formBody.addFormDataPart("name","group");
            formBody.addFormDataPart("groupId",topic);
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/create_group.php").post(formBody.build()).build();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            try{
                if (s==null){
                    Toast.makeText(CreateGroupActivity.this,"server temporary unavailable, try again later",Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject jsonObject=new JSONObject(s);
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    JSONObject remarks=new JSONObject();
                    remarks.put("groupUsers",groupUsers);
                    remarks.put("userIds",groupUsers);
                    remarks.put("uid",topic);
                    remarks.put("flag","group");
                    MessageService.getInstance().send(mMyUserId,String.format("group/%s",mMyUserId),topic,"create_group",remarks);
                    onBackPressed();
                }else{
                    Toast.makeText(CreateGroupActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                }
            }catch (Throwable t){
                t.printStackTrace();
            }
        }

    }

    private class InviteToGroupTask extends AsyncTask<Void,Void,String> {

        private ProgressDialog progressDialog;
        private int[] invitedUsers;
        private HashMap<Integer,User> mSelectedUsers;
        private String mMyUserId;
        private User mGroup;

        public InviteToGroupTask(HashMap<Integer,User> selectedUsers,String myUserId,User group) {
            mSelectedUsers=selectedUsers;
            mMyUserId=myUserId;
            mGroup=group;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CreateGroupActivity.this, "loading...", "please wait...", false, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).
                    addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE).addFormDataPart("userId",mMyUserId);
            Collection<User> collection=mSelectedUsers.values();
            invitedUsers=new int[mSelectedUsers.size()];
            int i=0;
            for (User user : collection) {
                if (user != null) {
                    String userIdStr = String.valueOf(user.getId());
                    formBody.addFormDataPart("userIds[]", userIdStr);
                    invitedUsers[i]=user.getId();
                    i++;
                }
            }
            formBody.addFormDataPart("groupId",mGroup.getFirstName());
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/invite_to_group.php").post(formBody.build()).build();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            try{
                JSONObject jsonObject=new JSONObject(s);
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    JSONObject remarks=new JSONObject();
                    remarks.put("userIds",invitedUsers);
                    MessageService.getInstance().send(mMyUserId,String.format("group/%s",mMyUserId),mGroup.getFirstName(),"group_invite", Tools.buildGroupChatMetaData(mGroup,remarks));
                    onBackPressed();
                }else{
                    Toast.makeText(CreateGroupActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                }
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
    }


}
