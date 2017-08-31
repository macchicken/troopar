package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.model.UserProfile;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class UserDetailActivity extends AppCompatActivity {

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        View userActionBtnContainer3=findViewById(R.id.userActionBtnContainer3);
        if (userActionBtnContainer3.getVisibility()==View.VISIBLE){
            userActionBtnContainer3.setVisibility(View.GONE);
        }
        View userHeaderInfo=findViewById(R.id.user_header_info);
        ViewGroup.LayoutParams params=userHeaderInfo.getLayoutParams();
        params.height=(int) (200 * Constants.DENSITYSCALE + 0.5f);
        userHeaderInfo.setLayoutParams(params);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_more_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent data=getIntent();
        user= (User) data.getSerializableExtra("user");
        setUserProfileInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void inviteAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity inviteAction");
    }

    public void messageAction(View view){
        Log.d("UserDetailActivity","UserDetailActivity messageAction to message box");
        Intent intent = new Intent(UserDetailActivity.this,MessageBoxActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    public void followAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity followAction");
        try {
            findViewById(R.id.followBtn).setClickable(false);
            String userPassword= Tools.sha1OfStr(Constants.USERPASSWORD);
            String followeeId=String.valueOf(user.getId());
            FollowTask task=new FollowTask(Constants.USEREMAIL,Constants.DEVEICEIDVALUE,followeeId,Constants.SIGNATUREVALUE,userPassword);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void followersAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity messageAction to followers");
        GetUserProfileWithType task=new GetUserProfileWithType(Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE, "followers",0,10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void followsAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity followsAction");
        GetUserProfileWithType task=new GetUserProfileWithType(Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE, "follows",0,10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void displayJoinedActAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity displayJoinedActAction");
        Intent intent=new Intent(UserDetailActivity.this,DisplayActivitiesActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("profileType","joined");
        startActivity(intent);
    }

    public void viewTroopedAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity viewTroopedAction");
        Intent intent=new Intent(UserDetailActivity.this,DisplayActivitiesActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("profileType","trooped");
        startActivity(intent);
    }

    public void viewReviewsAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity viewReviewsAction");
        Intent intent=new Intent(UserDetailActivity.this,DisplayActivitiesActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("profileType","reviews");
        startActivity(intent);
    }

    public void viewPhotosAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity viewPhotosAction");
        Intent intent=new Intent(UserDetailActivity.this,DisplayActivitiesActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("profileType","photos");
        startActivity(intent);
    }

    private class FollowTask extends AsyncTask<Void,Void,String>{

        private String mEmail;
        private String mUserPassword;
        private String mEquipId;
        private String mSignature;
        private String mFolloweeId;

        private FollowTask(String email, String equipId, String followeeId, String signature, String userPassword) {
            mEmail = email;
            mEquipId = equipId;
            mFolloweeId = followeeId;
            mSignature = signature;
            mUserPassword = userPassword;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("email",mEmail).addFormDataPart("password",mUserPassword).addFormDataPart("followee",mFolloweeId)
                    .addFormDataPart(Constants.EQUIPID,mEquipId).addFormDataPart(Constants.SIGNATURE,mSignature).build();
            Request request=new Request.Builder().url(String.format("%s/%s", BuildConfig.API_READHOST,"user/follow.php")).post(formBody).build();
            ResponseBody response = null;
            try {
                response=client.newCall(request).execute().body();
                return response.string();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (response!=null){
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                TextView followBtn= (TextView) findViewById(R.id.followBtn);
                if (s!=null){
                    JSONObject result=new JSONObject(s);
                    if (result.getString("status").equals(Constants.TAG_SUCCESS)){
                        if (result.getBoolean("addFollow")){
                            if (followBtn!=null){
                                followBtn.setText(R.string.unFollow);
                            }
                        }else{
                            if (followBtn!=null){
                                followBtn.setText(R.string.follow);
                            }
                        }
                    }
                }
                if (followBtn!=null){
                    followBtn.setClickable(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class GetUserProfileWithType extends AsyncTask<Void,Void,ArrayList>{

        private String mEquipId;
        private String mSignature;
        private String mType;
        private ProgressDialog progressDialog;
        private int mOffset;
        private int mLimit;

        private GetUserProfileWithType(String deviceId,String signature,String type,int offset,int limit){
            mEquipId=deviceId;
            mSignature=signature;
            mType=type;
            mOffset=offset;
            mLimit=limit;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(UserDetailActivity.this, "loading...", "please wait...", false, false);
        }

        @Override
        protected ArrayList doInBackground(Void... params) {
            return UserService.getInstance().getUserProfileWithType(user,mType,mOffset,mLimit,mEquipId,mSignature);
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            progressDialog.dismiss();
            try {
                if (result==null){
                    return;
                }
                if ("followers".equals(mType)) {
                    int total=result.size();
                    if (total==0){
                        Toast.makeText(UserDetailActivity.this, "you do not have any followers", Toast.LENGTH_SHORT).show();
                    }else{
                        int userFollowersTotal=Integer.parseInt(((TextView)findViewById(R.id.userFollowers)).getText().toString());
                        Intent intent = new Intent(UserDetailActivity.this, DisplayUsersActivity.class);
                        intent.putExtra("userArrayList", result);
                        intent.putExtra("myUser", user);
                        intent.putExtra("profileType", "followers");
                        intent.putExtra("total", userFollowersTotal);
                        startActivity(intent);
                    }
                }else if ("follows".equals(mType)) {
                    int total=result.size();
                    if (total==0){
                        Toast.makeText(UserDetailActivity.this, "you have not followed any one", Toast.LENGTH_SHORT).show();
                    }else{
                        int userFollowsTotal=Integer.parseInt(((TextView)findViewById(R.id.userFollows)).getText().toString());
                        Intent intent = new Intent(UserDetailActivity.this, DisplayUsersActivity.class);
                        intent.putExtra("userArrayList", result);
                        intent.putExtra("myUser", user);
                        intent.putExtra("profileType", "follows");
                        intent.putExtra("total", userFollowsTotal);
                        startActivity(intent);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMore(View view){
        Log.d("UserDetailActivity", "UserDetailActivity loadMore");
    }

    public void userAction(View view){
        Log.d("UserDetailActivity", "UserDetailActivity userAction");
    }

    public void captureUserImage(View view){
        Log.d("UserDetailActivity", "UserDetailActivity userAction");
    }

    @Override
    protected void onDestroy() {
        Log.d("UserDetailActivity", "UserDetailActivity onDestroy");
        super.onDestroy();
        user=null;
    }

    private void setUserProfileInfo(){
        ((TextView)findViewById(R.id.userName)).setText(Tools.isNullString(user.getFirstName()) || Tools.isNullString(user.getLastName())? "User name" : String.format("%s %s",user.getFirstName(), user.getLastName()));
        ImageView userImage= (ImageView) findViewById(R.id.userImage);
        if (Tools.isNullString(user.getAvatarOrigin())){
            userImage.setImageResource(R.drawable.user_image);
        }else{
            ImageDownloader.getInstance().getRequestManager().load(user.getAvatarOrigin()).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(userImage);
        }
        GetUserProfile task=new GetUserProfile(String.valueOf(user.getId()),Constants.USERID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private class GetUserProfile extends AsyncTask<Void,Void,User> {

        private String mUserId;
        private String mMyUserId;

        GetUserProfile(String userId,String myUserId) {
            mUserId=userId;
            mMyUserId=myUserId;
        }

        @Override
        protected User doInBackground(Void... params) {
            return UserService.getInstance().getProfile(mMyUserId,mUserId);
        }

        @Override
        protected void onPostExecute(User user) {
            try {
                if (user!=null){
                    UserProfile userProfile=user.getUserProfile();
                    ((TextView)findViewById(R.id.userFollows)).setText(userProfile.getFollowNum());
                    ((TextView)findViewById(R.id.userFollowers)).setText(userProfile.getFollowerNum());
                    ((TextView)findViewById(R.id.userReviews)).setText(userProfile.getPostedReviewNum());
                    ((TextView)findViewById(R.id.userPhotoNumbers)).setText(userProfile.getPostedPhotoNum());
                    ((TextView)findViewById(R.id.userTroopNumbers)).setText(userProfile.getCreatedEventNum());
                    ((TextView)findViewById(R.id.userJoinedNumbers)).setText(userProfile.getJoinedEventNum());
                    ((TextView)findViewById(R.id.followBtn)).setText(userProfile.getFollowState().equals("Following")||userProfile.getFollowState().equals("Friend")?" UnFollow":" Follow");
                    String avatarOrigin=user.getAvatarOrigin();
                    ImageView userImage=(ImageView) findViewById(R.id.userImage);
                    if (Tools.isNullString(avatarOrigin)) {
                        userImage.setImageResource(R.drawable.user_image);
                    }else{
                        ImageDownloader.getInstance().getRequestManager().load(avatarOrigin).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into((ImageView) findViewById(R.id.userImage));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void recentActivitiesAction(View view){
        Intent intent=new Intent(UserDetailActivity.this,DisplayRecentActivitiesActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
    }


}
