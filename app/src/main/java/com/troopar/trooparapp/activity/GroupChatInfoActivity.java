package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.adapter.NearybyUsersAdapter;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupChatInfoActivity extends AppCompatActivity {

    private int total;
    private ArrayList<User> userArrayList;
    private RecyclerView userList;
    private User group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_info);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userList = (RecyclerView) findViewById(R.id.userList);
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        userList.setLayoutManager(staggeredGridLayoutManager);
        userList.setHasFixedSize(true);
        Intent data=getIntent();
        User user= (User) data.getSerializableExtra("user");
        String myUserId=data.getStringExtra("myUserId");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateNormalGroupChatDetail(user,myUserId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new UpdateNormalGroupChatDetail(user,myUserId).execute();
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

    private class UpdateNormalGroupChatDetail extends AsyncTask<Void,Void,JSONObject> {

        private User mUser;
        private String mMyUserId;
        private ProgressDialog progressDialog;

        public UpdateNormalGroupChatDetail(User user, String myUserId) {
            mUser = user;
            mMyUserId = myUserId;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(GroupChatInfoActivity.this, "loading...", "please wait...", false, false);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("groupId", mUser.getFirstName())
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/get_group_members.php").post(formBody).build();
            Response response = null;
            String responseStr=null;
            try {
                response=client.newCall(request).execute();
                responseStr=response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (response!=null){
                    response.body().close();
                }
            }
            try {
                if (responseStr==null){return null;}
                JSONObject jsonObject=new JSONObject(responseStr);
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    JSONObject taskResult=new JSONObject();
                    JSONArray result=jsonObject.getJSONArray("result");
                    total=result.length();
                    int displayTotal=total>15?15:total;
                    UserService userService=UserService.getInstance();
                    LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
                    User[] users=new User[displayTotal];
                    for (int i=0;i<displayTotal;i++){
                        String userId=String.valueOf(result.getInt(i));
                        User user=localDBHelper.getCacheUser(userId);
                        if (user==null){
                            user=userService.getProfile(Constants.USERID,userId);
                        }
                        if (user!=null){
                            users[i]=user;
                        }
                    }
                    taskResult.put("users",users);
                    taskResult.put("usersArray",result);
                    return taskResult;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject taskResult) {
            super.onPostExecute(taskResult);
            progressDialog.dismiss();
            if (taskResult==null){return;}
            try {
                User[] users= (User[]) taskResult.get("users");
                JSONArray usersArray= taskResult.getJSONArray("usersArray");
                int displayTotal=users.length;
                userArrayList=new ArrayList<>(displayTotal+1);
                Collections.addAll(userArrayList, users);
                userArrayList.add(new User(null,null,-6,null,null,null,null));// add a invite people image
                NearybyUsersAdapter nearbyUsersAdapter=new NearybyUsersAdapter(userArrayList,R.layout.griduser_square_item_view,60,false);
                userList.setAdapter(nearbyUsersAdapter);
                ViewGroup.LayoutParams params=userList.getLayoutParams();
                int rowDipMultiple;
                if (displayTotal<4){
                    rowDipMultiple=1;
                }else if (displayTotal<8){
                    rowDipMultiple=2;
                }else if (displayTotal<12){
                    rowDipMultiple=3;
                }else{
                    rowDipMultiple=4;
                }
                params.height=(int) (100 * rowDipMultiple * Constants.DENSITYSCALE + 0.5f);
                userList.setLayoutParams(params);
                group=new User(mUser.getFirstName(),usersArray.toString(),-5,mUser.getLastName(),mUser.getUserName(),mUser.getAvatarOrigin(),mUser.getAvatarStandard());// update group member
                LocalDBHelper.getInstance().insertUserProfile(group);
                LocalDBHelper.getInstance().insertRecentChatGroupContact(group,mMyUserId);
                initView(mUser);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView(User user){
        ((TextView)findViewById(R.id.displayTitle)).setText(getString(R.string.chatInfo,total));
        ((TextView)findViewById(R.id.groupName)).setText(user.getUserName());
    }

    public void invitePeople(View view){
        Intent intent = new Intent(GroupChatInfoActivity.this,CreateGroupActivity.class);
        intent.putExtra("myUserId",Constants.USERID);
        intent.putExtra("invitePeople",true);
        intent.putExtra("group", group);
        startActivity(intent);
    }

    public void groupNameAction(View view){
        Intent intent=new Intent(GroupChatInfoActivity.this,EditGroupProfileActivity.class);
        intent.putExtra("group",group);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userArrayList=null;
        userList.getRecycledViewPool().clear();
        userList.removeAllViews();
        userList=null;
    }


}
