package com.troopar.trooparapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.model.UserProfile;
import com.troopar.trooparapp.utils.Draws;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.Tools;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.MyAppSharePreference;


public class MoreActivity extends AppCompatActivity {

	private final int USER_REG_REQUEST_CODE=602;
	private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=609;
	private final int PICK_PROFILE_BACKGROUND_CODE_REQUEST_CODE=616;
	private TelephonyManager tm;
	private String photoFileName;
	private Dialog userDialog;
	private boolean userImageChanged;
	private User myUser;
	private UserService userService;
	private Toolbar toolbar;
	private AtomicBoolean isPageLoading=new AtomicBoolean(false);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("MoreActivity","MoreActivity on create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		userService=UserService.getInstance();
		initView();
	}

	private void initView() {
		toolbar=(Toolbar) findViewById(R.id.my_more_toolbar);
		setSupportActionBar(toolbar);
		View userOperations=findViewById(R.id.userOperations);
		if (userOperations.getVisibility()==View.VISIBLE){
			userOperations.setVisibility(View.GONE);
		}
		FloatingActionButton refreshBtn= (FloatingActionButton) findViewById(R.id.refresh_btn);
		FloatingActionButton signUpBtn= (FloatingActionButton) findViewById(R.id.signUp_btn);
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setUserProfileInfo();
			}
		});
		signUpBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("MoreActivity","to registration page");
				Intent intent4 = new Intent(MoreActivity.this, UsersRegActivity.class);
				startActivityForResult(intent4, USER_REG_REQUEST_CODE);
			}
		});
		View userHeaderInfo=findViewById(R.id.user_header_info);
		ViewGroup.LayoutParams params=userHeaderInfo.getLayoutParams();
		params.height=(int) (200 * Constants.DENSITYSCALE + 0.5f);
		userHeaderInfo.setLayoutParams(params);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		String myUserId = Constants.USERID;
		if (!Tools.isNullString(myUserId)) {
			myUser=new User(Constants.USERFIRSTNAME, Constants.USERGENDER,Integer.parseInt(myUserId), Constants.USERLASTNAME,Constants.USERNAME,Constants.AVATARORIGIN,Constants.AVATARSTANDARD);
			myUser.setEmail(Constants.USEREMAIL);
			myUser.setPhone(Constants.USERPHONE);
			photoFileName=Constants.PHOTOFILENAME;
			View userActionBtnContainer3=findViewById(R.id.userActionBtnContainer3);
			if (userActionBtnContainer3.getVisibility()==View.VISIBLE){
				userActionBtnContainer3.setVisibility(View.GONE);
			}
		}else{
			toolbar.setVisibility(View.GONE);
		}
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		userDialog = new Dialog(MoreActivity.this);
		userDialog.setContentView(R.layout.user_login_dialog);
		userDialog.setTitle("");
		userDialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tm==null){return;}
				try{
					String deviceId = tm.getDeviceId();
					String userPassword = ((EditText) userDialog.findViewById(R.id.userLoginPass)).getText().toString();
					String userEmail = ((EditText) userDialog.findViewById(R.id.userLoginEmail)).getText().toString();
					loginAction(deviceId,userEmail, userPassword);
				}catch(SecurityException se){
					Toast.makeText(MoreActivity.this, "enable access phone to register with your device", Toast.LENGTH_SHORT).show();
				}
			}
		});
		userDialog.findViewById(R.id.dialogButtonCancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				userDialog.hide();
			}
		});
		if (myUser!=null){
			setUserProfileInfo();
		}
	}

	public void userAction(View v) {
		((EditText) userDialog.findViewById(R.id.userLoginPass)).setText("");
		userDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_more, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void openOptionsMenu() {
		super.openOptionsMenu();
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}


	private void loginAction(String deviceId, String loginEmail, String userPassword){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new UserLoginTask(deviceId,loginEmail,userPassword).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new UserLoginTask(deviceId,loginEmail,userPassword).execute();
		}
	}

	private class UserLoginTask extends AsyncTask<Void, Void, User>{

		private ProgressDialog uploadProgressDialog;
		private String mLoginEmail;
		private String mUserPassword;
		private String mDeviceId;

		public UserLoginTask(String deviceId, String loginEmail, String userPassword) {
			mDeviceId = deviceId;
			mLoginEmail = loginEmail;
			mUserPassword = userPassword;
		}

		@Override
		protected void onPreExecute() {
			uploadProgressDialog = ProgressDialog.show(MoreActivity.this, "login...", "please wait...", false, false);
		}

		@Override
		protected User doInBackground(Void... params) {
			String token = null;
			try {
				token=InstanceID.getInstance(getApplicationContext()).getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return userService.login(mLoginEmail,mUserPassword,mDeviceId,token);
		}

		@Override
		protected void onPostExecute(User user) {
			uploadProgressDialog.dismiss();
			super.onPostExecute(user);
			try {
				if (user!=null){
					String remark=user.getRemark();
					if (remark!=null){
						Toast.makeText(MoreActivity.this, remark, Toast.LENGTH_SHORT).show();
					}else{
						String userId=String.valueOf(user.getId());
						if (LocalDBHelper.getInstance()!=null){
							LocalDBHelper.getInstance().releaseResource();
						}
						LocalDBHelper.getUserInstance(getApplicationContext(),userId).initDatabase();
						MessageService.getInstance().connect(userId);
						LocalDBHelper.getInstance().initTotalUnreadCount(userId);
						LocalDBHelper.getInstance().insertUserProfile(user);
						myUser=user;
						photoFileName=user.getAvatarOrigin();
						userDialog.hide();
						if (toolbar.getVisibility()==View.GONE){
							toolbar.setVisibility(View.VISIBLE);
						}
						View userActionBtnContainer3=findViewById(R.id.userActionBtnContainer3);
						if (userActionBtnContainer3.getVisibility()==View.VISIBLE){
							userActionBtnContainer3.setVisibility(View.GONE);
						}
						setUserProfileInfo();
						Toast.makeText(MoreActivity.this, "user login success", Toast.LENGTH_SHORT).show();
						Intent intent=new Intent("MessageService.local.message.data");
						intent.putExtra("status", RESULT_OK);
						intent.putExtra("setUsers", Constants.RESETUSER);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
						Intent initUnread=new Intent("TrooparGcmListenerService.local.message.data");
						initUnread.putExtra("status", RESULT_OK);
						initUnread.putExtra("initUnreadCount",true);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(initUnread);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void runListAsyncTask(AsyncTask<Void, Void, ArrayList> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_userprofile:
				if (myUser==null){return true;}
				Intent intent3 = new Intent(MoreActivity.this,UserProfileActivity.class);
				intent3.putExtra("userImageChanged", userImageChanged);
				intent3.putExtra("userImageFile",Constants.PHOTOFILENAME);
				startActivity(intent3);
				return true;
			case R.id.action_logout:
				LocalDBHelper.getInstance().releaseResource();
				LocalDBHelper.getInstance(getApplicationContext()).initDatabase();
				MessageService.getInstance().disconnect(String.valueOf(myUser.getId()));
				MyAppSharePreference sharedPreferences = MyAppSharePreference.getInstance();
				HashMap<String,String> temp = new HashMap<>();
				temp.put("userId", "");
				temp.put("userName", "");
				temp.put("userEmail", "");
				temp.put("userGender", "");
				temp.put("userFirstName", "");
				temp.put("userLastName", "");
				temp.put("userPhone", "");
				temp.put("userPassword", "");
				Constants.USERID=null;
				Constants.USERNAME="";
				Constants.USEREMAIL="";
				Constants.USERGENDER="";
				Constants.USERFIRSTNAME="";
				Constants.USERLASTNAME="";
				Constants.USERPHONE="";
				Constants.AVATARORIGIN="";
				Constants.AVATARSTANDARD="";
				Constants.USERPASSWORD="";
				Constants.PHOTOFILENAME="";
				myUser = null;
				sharedPreferences.saveMultipleStringValues(temp);
				LinearLayout userOperations= (LinearLayout) findViewById(R.id.userOperations);
				((TextView)findViewById(R.id.userName)).setText(R.string.pleaseLogin);
				View userActionBtnContainer3=findViewById(R.id.userActionBtnContainer3);
				if (userActionBtnContainer3.getVisibility()==View.GONE){
					userActionBtnContainer3.setVisibility(View.VISIBLE);
				}
				ImageView userImage=((ImageView)findViewById(R.id.userImage));
				userImage.setImageResource(R.drawable.user_image);
				if (userOperations.getVisibility()==View.VISIBLE){
					userOperations.setVisibility(View.GONE);
				}
				((TextView)findViewById(R.id.userFollows)).setText("0");
				((TextView)findViewById(R.id.userFollowers)).setText("0");
				((TextView)findViewById(R.id.userReviews)).setText("0");
				((TextView)findViewById(R.id.userPhotoNumbers)).setText("0");
				((TextView)findViewById(R.id.userTroopNumbers)).setText("0");
				((TextView)findViewById(R.id.userJoinedNumbers)).setText("0");
				Intent intent=new Intent("MessageService.local.message.data");
				intent.putExtra("status", RESULT_OK);
				intent.putExtra("setUsers", Constants.CLEARUSER);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
				Intent intent7=new Intent("TrooparGcmListenerService.local.message.data");
				intent7.putExtra("status", RESULT_OK);
				intent7.putExtra("increment", false);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent7);
				toolbar.setVisibility(View.GONE);
				return true;
			default:break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK){return;}
		switch (requestCode){
			case USER_REG_REQUEST_CODE:
				Toast.makeText(MoreActivity.this,"registration success",Toast.LENGTH_SHORT).show();
				break;
			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
				Constants.PHOTOFILENAME=Tools.checkAppImageDirectory() + File.separator + photoFileName;
				userImageChanged=true;
				break;
			case PICK_PROFILE_BACKGROUND_CODE_REQUEST_CODE:
				if (data!=null) {
					try {
						Uri photoUri = data.getData();
						String[] projection = {MediaStore.Images.Media.DATA};
						Cursor cursor = getContentResolver().query(photoUri, projection, null, null, null);
						if (cursor != null) {
							cursor.moveToFirst();
							int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
							String path = cursor.getString(column_index);
							cursor.close();
							String[] pathList = path.split(File.separator);
							String profileBackgroundFileName = "background_"+pathList[pathList.length - 1];
							BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
							sampleOptions.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(path, sampleOptions);
							int inSampleSize = Tools.calculateInSampleSize(sampleOptions, Constants.Measuredwidth, 260);
							sampleOptions.inJustDecodeBounds = false;
							sampleOptions.inSampleSize = inSampleSize;
							Bitmap takenImage = BitmapFactory.decodeFile(path, sampleOptions);
							writeImageInfo(takenImage, profileBackgroundFileName);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:break;
		}
	}

	@Override
	protected void onPostResume() {
		Log.d("MoreActivity", "MoreActivity on post resume");
		super.onPostResume();
	}

	@Override
	protected void onPause() {
		Log.d("MoreActivity","MoreActivity on pause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("MoreActivity", "MoreActivity on stop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("MoreActivity", "MoreActivity on destroy");
		super.onDestroy();
		userDialog.cancel();
		userDialog = null;
		toolbar.removeAllViews();
		toolbar=null;
	}

	public void captureUserImage(View v){
		if (myUser==null){
			return;
		}
		Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		photoFileName = UUID.randomUUID().toString() + ".png";
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Draws.getAppImageFileUri(photoFileName));
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private void writeImageInfo(Bitmap takenImage,String photoFileName){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new WriteImageInfoTask(photoFileName,takenImage).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new WriteImageInfoTask(photoFileName,takenImage).execute();
		}
	}

	private class WriteImageInfoTask extends AsyncTask<Void, Void, JSONObject>{
		private Bitmap mTakenImage;
		private String mPhotoFileName;

		public WriteImageInfoTask(String photoFileName, Bitmap takenImage) {
			mPhotoFileName = photoFileName;
			mTakenImage = takenImage;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			FileOutputStream out=null;
			try {
				out = new FileOutputStream(Tools.checkAppImageDirectory()+File.separator+mPhotoFileName);
				mTakenImage.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e){
				e.printStackTrace();
			}finally {
				if(out!=null){
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
		protected void onPostExecute(JSONObject jsonObject) {
			super.onPostExecute(jsonObject);
			MyAppSharePreference sharedPreferences = MyAppSharePreference.getInstance();
			String userId=sharedPreferences.getStringValue("userId");
			String userNameText=sharedPreferences.getStringValue("userName");
			Constants.BACKGROUNDPHOTOFILENAME=mPhotoFileName;
			sharedPreferences.saveStringValue(userId + userNameText + "background", mPhotoFileName);
			findViewById(R.id.user_header_info).setBackground(new BitmapDrawable(Tools.checkAppImageDirectory()+File.separator+mPhotoFileName));
		}
	}

	public void inviteAction(View view){
		Log.d("MoreActivity", "MoreActivity inviteAction");
	}

	public void messageAction(View view){
		Log.d("MoreActivity","MoreActivity messageAction to message box");
	}

	public void followersAction(View view){
		Log.d("MoreActivity", "MoreActivity to followers");
		if (myUser==null||isPageLoading.get()){
			return;
		}
		runListAsyncTask(new GetUserProfileWithType(Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE, "followers",0,10));
	}

	public void followAction(View view){
		Log.d("MoreActivity", "MoreActivity followAction");
	}

	public void followsAction(View view){
		Log.d("MoreActivity", "MoreActivity followsAction");
		if (myUser==null){
			return;
		}
		runListAsyncTask(new GetUserProfileWithType(Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE, "follows",0,10));
	}

	public void displayJoinedActAction(View view){
		Log.d("MoreActivity", "MoreActivity displayJoinedActAction");
		if (myUser==null||isPageLoading.get()){
			return;
		}
		Intent intent=new Intent(MoreActivity.this,DisplayActivitiesActivity.class);
		intent.putExtra("user",myUser);
		intent.putExtra("profileType","joined");
		startActivity(intent);
	}

	public void viewTroopedAction(View view){
		Log.d("MoreActivity", "MoreActivity viewTroopedAction");
		if (myUser==null||isPageLoading.get()){
			return;
		}
		Intent intent=new Intent(MoreActivity.this,DisplayActivitiesActivity.class);
		intent.putExtra("user",myUser);
		intent.putExtra("profileType","trooped");
		startActivity(intent);
	}

	public void viewReviewsAction(View view){
		Log.d("MoreActivity", "MoreActivity viewReviewsAction");
		if (myUser==null||isPageLoading.get()){
			return;
		}
		Intent intent=new Intent(MoreActivity.this,DisplayActivitiesActivity.class);
		intent.putExtra("user",myUser);
		intent.putExtra("profileType","reviews");
		startActivity(intent);
	}

	public void viewPhotosAction(View view){
		Log.d("MoreActivity", "MoreActivity viewPhotosAction");
		if (myUser==null||isPageLoading.get()){
			return;
		}
		Intent intent=new Intent(MoreActivity.this,DisplayActivitiesActivity.class);
		intent.putExtra("user",myUser);
		intent.putExtra("profileType","photos");
		startActivity(intent);
	}

	private class GetUserProfileWithType extends AsyncTask<Void,Void,ArrayList>{

		private String mEquipId;
		private String mSignature;
		private String mType;
		private ProgressDialog progressDialog;
		private int mOffset;
		private int mLimit;

		GetUserProfileWithType(String deviceId,String signature,String type,int offset,int limit){
			mEquipId=deviceId;
			mSignature=signature;
			mType=type;
			mOffset=offset;
			mLimit=limit;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MoreActivity.this, "loading...", "please wait...", false, false);
		}

		@Override
		protected ArrayList doInBackground(Void... params) {
			return userService.getUserProfileWithType(myUser,mType,mOffset,mLimit,mEquipId,mSignature);
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
						Toast.makeText(MoreActivity.this, "you do not have any followers", Toast.LENGTH_SHORT).show();
					}else{
						int userFollowersTotal=Integer.parseInt(((TextView)findViewById(R.id.userFollowers)).getText().toString());
						Intent intent = new Intent(MoreActivity.this, DisplayUsersActivity.class);
						intent.putExtra("userArrayList", result);
						intent.putExtra("myUser", myUser);
						intent.putExtra("profileType", "followers");
						intent.putExtra("total", userFollowersTotal);
						startActivity(intent);
					}
				}else if ("follows".equals(mType)) {
					int total=result.size();
					if (total==0){
						Toast.makeText(MoreActivity.this, "you have not followed any one", Toast.LENGTH_SHORT).show();
					}else{
						int userFollowsTotal=Integer.parseInt(((TextView)findViewById(R.id.userFollows)).getText().toString());
						Intent intent = new Intent(MoreActivity.this, DisplayUsersActivity.class);
						intent.putExtra("userArrayList", result);
						intent.putExtra("myUser", myUser);
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

	private void setUserProfileInfo(){
		if (findViewById(R.id.userOperations).getVisibility()==View.VISIBLE){
			findViewById(R.id.userOperations).setVisibility(View.GONE);
		}
		if (myUser==null){
			return;
		}
		if (!isPageLoading.compareAndSet(false,true)){return;}
		ImageView loadingProgress= (ImageView) findViewById(R.id.loadingProgress);
		loadingProgress.setVisibility(View.VISIBLE);
		loadingProgress.startAnimation(AnimationUtils.loadAnimation(MoreActivity.this, R.anim.pulse));
		Log.d("MoreActivity","setUserProfileInfo");
		String myUserId=String.valueOf(myUser.getId());
		((TextView)findViewById(R.id.userName)).setText(Tools.isNullString(myUser.getFirstName()) || Tools.isNullString(myUser.getLastName())? "User name" : String.format("%s %s",myUser.getFirstName(), myUser.getLastName()));
		if (!"".equals(Constants.BACKGROUNDPHOTOFILENAME)) {
			File bgFile=new File(Tools.checkAppImageDirectory(),Constants.BACKGROUNDPHOTOFILENAME);
			findViewById(R.id.user_header_info).setBackground(new BitmapDrawable(bgFile.getPath()));
		}
		User user=LocalDBHelper.getInstance().getCacheUser(myUserId);// get user from local cache
		if (user==null||user.getUserProfile()==null){
			GetUserProfile task=new GetUserProfile(Constants.USERID, Constants.PHOTOFILENAME);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		}else{// display cache user and asynchronous update data and UI
			UserProfile userProfile=user.getUserProfile();
			((TextView)findViewById(R.id.userFollows)).setText(userProfile.getFollowNum());
			((TextView)findViewById(R.id.userFollowers)).setText(userProfile.getFollowerNum());
			((TextView)findViewById(R.id.userReviews)).setText(userProfile.getPostedReviewNum());
			((TextView)findViewById(R.id.userPhotoNumbers)).setText(userProfile.getPostedPhotoNum());
			((TextView)findViewById(R.id.userTroopNumbers)).setText(userProfile.getCreatedEventNum());
			((TextView)findViewById(R.id.userJoinedNumbers)).setText(userProfile.getJoinedEventNum());
			String avatarOrigin=user.getAvatarOrigin();
			ImageView imageView= (ImageView) findViewById(R.id.userImage);
			if (!Tools.isNullString(avatarOrigin)) {
				ImageDownloader.getInstance().getRequestManager().load(avatarOrigin).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
			}
			else if (!Tools.isNullString(photoFileName)) {
				imageView.setImageBitmap(Draws.loadImage(photoFileName));
			}else{
				imageView.setImageResource(R.drawable.user_image);
			}
			GetUserProfile task=new GetUserProfile(myUserId, photoFileName);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		}
	}

	private class GetUserProfile extends AsyncTask<Void,Void,User> {

		private String mUserId;
		private String mPhotoFileName;

		public GetUserProfile(String userId, String photoFileName) {
			mUserId=userId;
			mPhotoFileName=photoFileName;
		}

		@Override
		protected User doInBackground(Void... params) {
			Log.d("MoreActivity","GetUserProfile");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return UserService.getInstance().getProfile(mUserId,mUserId);
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
					String avatarOrigin=user.getAvatarOrigin();
					ImageView imageView= (ImageView) findViewById(R.id.userImage);
					if (!Tools.isNullString(avatarOrigin)) {
						ImageDownloader.getInstance().getRequestManager().load(avatarOrigin).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
					}
					else if (!Tools.isNullString(mPhotoFileName)) {
						imageView.setImageBitmap(Draws.loadImage(mPhotoFileName));
					}else{
						imageView.setImageResource(R.drawable.user_image);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}finally {
				ImageView imageView= (ImageView) findViewById(R.id.loadingProgress);
				imageView.clearAnimation();
				imageView.setVisibility(View.GONE);
				isPageLoading.set(false);
			}
		}
	}

	public void recentActivitiesAction(View view){
		if (myUser==null||isPageLoading.get()){
			return;
		}
		Intent intent=new Intent(MoreActivity.this,DisplayRecentActivitiesActivity.class);
		intent.putExtra("user",myUser);
		startActivity(intent);
	}


}
