package com.troopar.trooparapp.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.troopar.trooparapp.activity.fragment.DisplayDiningsFragment;
import com.troopar.trooparapp.activity.fragment.DisplayEntertainmentFragment;
import com.troopar.trooparapp.activity.fragment.DisplayEventsFragment;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.activity.task.JoinEventTask;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.myview.EventSharePopupWindow;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;


import com.troopar.trooparapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class NearbyActivity extends AppCompatActivity{

	private int EVENT_DETAIL_REQUEST_CODE=623;
	private int MY_PERMISSIONS_REQUEST_CODE=3;
	protected ShareItemOnClickListener shareItemOnClickListener;
	private int currentPosition=0;
	private Dialog confirmDialog;


	public static class MyPagerAdapter extends FragmentPagerAdapter {
		private static int NUM_ITEMS = 3;

		public MyPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		// Returns total number of pages
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		// Returns the fragment to display for that page
		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0: return DisplayEventsFragment.newInstance(0, "Events",R.layout.fragment_display_events);
				case 1: return DisplayDiningsFragment.newInstance(1, "Dining",R.layout.fragment_display_dinings);
				case 2: return DisplayEntertainmentFragment.newInstance(2, "Entertainment",R.layout.fragment_display_entertainment);
				default: return null;
			}
		}

		// Returns the page title for the top indicator
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position){
				case 0: return "Events";
				case 1: return "Dining";
				case 2: return "Entertainment";
				default: return "Page " + position;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("NearbyActivity", "NearbyActivity on create");
		checkPermissionsAtRunTime(NearbyActivity.this);
		setContentView(R.layout.activity_nearby);
		setSupportActionBar((Toolbar) findViewById(R.id.my_nearby_toolbar));
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		confirmDialog = new Dialog(NearbyActivity.this);
		confirmDialog.setContentView(R.layout.comfirm_dialog);
		confirmDialog.setTitle("");
		initView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data==null||resultCode!=RESULT_OK){return;}
		if (requestCode==EVENT_DETAIL_REQUEST_CODE){
			String eventPosition=data.getStringExtra("eventPosition");
			boolean joined=data.getBooleanExtra("joined",false);
			EventModel eventModel;
			switch (currentPosition){
				case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
				case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
				case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
				default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			}
			eventModel.setJoined(joined);
		}else if (requestCode==MY_PERMISSIONS_REQUEST_CODE){
			Log.d("NearbyActivity", "MY_PERMISSIONS_REQUEST_CODE");
		}
	}

	private void initView() {
		ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
		PagerTabStrip pagerTabStrip = (PagerTabStrip) vpPager.findViewById(R.id.pager_header);
		pagerTabStrip.setDrawFullUnderline(false);
		pagerTabStrip.setTabIndicatorColorResource(R.color.appElementFillColor);
		TextView textView=((TextView) pagerTabStrip.getChildAt(vpPager.getCurrentItem()+1));
		textView.setTextColor(getResources().getColor(R.color.appElementFillColor));
		FragmentPagerAdapter adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
		vpPager.setAdapter(adapterViewPager);
		// Attach the page change listener inside the activity
		vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			// This method will be invoked when a new page becomes selected.
			@Override
			public void onPageSelected(int position) {
				currentPosition=position;
			}

			// This method will be invoked when the current page is scrolled
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			// Called when the scroll state changes:
			// SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		ShareDialog shareDialog = new ShareDialog(NearbyActivity.this);
		shareItemOnClickListener=new ShareItemOnClickListener(NearbyActivity.this, shareDialog);
	}

	public void eventShareAction(View view){
		if (Tools.isNullString(Constants.USERID)){
			return;
		}
		EventModel eventModel;
		String eventPosition=view.getContentDescription().toString();
		switch (currentPosition){
			case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
		}
		shareItemOnClickListener.setEvent(eventModel);
		EventSharePopupWindow eventSharePopupWindow = new EventSharePopupWindow(NearbyActivity.this, shareItemOnClickListener);
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		List<ResolveInfo> resInfo = getApplicationContext().getPackageManager().queryIntentActivities(share, 0);
		if (!resInfo.isEmpty()){
			for (ResolveInfo info : resInfo) {
				eventSharePopupWindow.enableShareApp(info.activityInfo.packageName);
			}
		}
		try {
			getApplicationContext().getPackageManager().getApplicationInfo("com.instagram.android", 0);
			eventSharePopupWindow.enableShareApp("com.instagram.android");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		shareItemOnClickListener.setEventSharePopupWindow(eventSharePopupWindow);
		//显示窗口
		eventSharePopupWindow.showAtLocation(NearbyActivity.this.findViewById(R.id.nearbyActView), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
	}

	public void retryCheckNetwork(View v){
		if (Tools.checkNetworkConnected(getApplicationContext())){
			v.setVisibility(View.GONE);
			((ViewGroup)v.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
		}
	}

	public void eventDetailAction(View view){
		if (Tools.isNullString(Constants.USERID)){
			return;
		}
		EventModel eventModel;
		String eventPosition=view.getContentDescription().toString();
		switch (currentPosition){
			case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
		}
		Intent intent=new Intent(NearbyActivity.this, EventDetailActivity.class);
		intent.putExtra("eventModel",eventModel);
		intent.putExtra("eventPosition",eventPosition);
		startActivityForResult(intent,EVENT_DETAIL_REQUEST_CODE);
	}

	public void userImageAction(View view){
		if (Tools.isNullString(Constants.USERID)){
			return;
		}
		EventModel eventModel;
		String eventPosition=view.getContentDescription().toString();
		switch (currentPosition){
			case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
		}
		Intent intent=new Intent(NearbyActivity.this, UserDetailActivity.class);
		intent.putExtra("user",eventModel.getUser());
		startActivity(intent);
	}

	public void cancelAction(View view){
		confirmDialog.hide();
		confirmDialog.findViewById(R.id.dialogButtonOK).setContentDescription("");
	}

	public void okAction(View view){
		confirmDialog.hide();
		if (Tools.isNullString(Constants.USERID)){
			return;
		}
		String eventPosition=view.getContentDescription().toString();
		final EventModel eventModel;
		switch (currentPosition){
			case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
		}
		User eventUser=eventModel.getUser();
		final String creator=String.valueOf(eventUser.getId());
		final String userId=Constants.USERID;
		JoinEventTask task=new JoinEventTask(userId,eventModel.getId());//TODO change to intentService and localbroadcast mechanism
		task.setPostExecuteCallBack(new JoinEventTask.PostExecuteCallBack() {
			@Override
			public void postExecute(JSONObject jsonObject) {
				try {
					if (jsonObject==null){
						Toast.makeText(getApplicationContext(),"fail to join this event",Toast.LENGTH_LONG).show();
						return;
					}
					JSONObject joinJson=jsonObject.getJSONObject("join");//TODO what to do with 'Need approved'
					JSONObject event=jsonObject.getJSONObject("event");
					int[] groupUsers;
					int i;
					ArrayList<User> joiners=eventModel.getJoiners();
					if (eventModel.isJoined()){
						groupUsers=new int[joiners.size()];
						i=0;
					}else{
						groupUsers=new int[joiners.size()+1];
						groupUsers[0]=Integer.parseInt(userId);
						i=1;
					}
					boolean isJoined=event.getBoolean("joined");
					JSONObject remarks=new JSONObject();
					remarks.put("creator",creator);
					remarks.put("uid",eventModel.getId());
					remarks.put("groupName",eventModel.getName());
					remarks.put("flag","event");
					JSONArray jsonArray=new JSONArray();
					for (User joiner:eventModel.getJoiners()){
						jsonArray.put(joiner.getId());
						groupUsers[i]=joiner.getId();
						i++;
					}
					remarks.put("groupUsers",groupUsers);
					remarks.put("joiners",groupUsers);
					remarks.put("smallImageUrl",eventModel.getThumbnailImageUrl());
					int[] users=new int[1];
					users[0]=Integer.parseInt(userId);
					remarks.put("userIds",users);
					if (isJoined){
						User user=new User(eventModel.getId(),jsonArray.toString(),-3,creator,eventModel.getName(),eventModel.getThumbnailImageUrl(),eventModel.getThumbnailImageUrl());//indicate joining an event group chat
						LocalDBHelper.getInstance().insertUserProfile(user);
						MessageService.getInstance().send(userId,String.format("event/%s",userId),eventModel.getId(),"group_invite",remarks);
					}else{
						MessageService.getInstance().send(userId,String.format("event/%s",userId),eventModel.getId(),"group_quit",remarks);
					}
					eventModel.setJoined(isJoined);
					Toast.makeText(getApplicationContext(),joinJson.getString("state")+" to this event",Toast.LENGTH_SHORT).show();
				}catch (Throwable t){
					t.printStackTrace();
				}
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	public void joinEventAction(View view){
		if (Tools.isNullString(Constants.USERID)){
			return;
		}
		EventModel eventModel;
		String eventPosition=view.getContentDescription().toString();
		switch (currentPosition){
			case 0:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 1:eventModel=DisplayDiningsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			case 2:eventModel=DisplayEntertainmentFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
			default:eventModel=DisplayEventsFragment.getInstance().getEvent(Integer.parseInt(eventPosition));break;
		}
		User eventUser=eventModel.getUser();
		if (Constants.USERID.equals(String.valueOf(eventUser.getId()))){
			Toast.makeText(NearbyActivity.this,"you are the creator of this event",Toast.LENGTH_SHORT).show();
			return;
		}
		((TextView)confirmDialog.findViewById(R.id.confirmTitle)).setText(eventModel.isJoined()?"quite this event":"join this event");
		confirmDialog.show();
		confirmDialog.findViewById(R.id.dialogButtonOK).setContentDescription(eventPosition);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_nearby, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_troop:
				if (Tools.isNullString(Constants.USERID)){
					Toast.makeText(NearbyActivity.this, "you should login to use this function", Toast.LENGTH_SHORT).show();
					return true;
				}
				Intent intent3 = new Intent(NearbyActivity.this, TrooparEventActivity.class);
				startActivity(intent3);
				return  true;
			default:return super.onOptionsItemSelected(item);
		}
	}

	private void checkPermissionsAtRunTime(Context ctx){
		if (Tools.shouldAskPermission()){
			if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||
					ContextCompat.checkSelfPermission(ctx, "com.google.android.providers.gsf.permission.READ_GSERVICES")!= PackageManager.PERMISSION_GRANTED){
				ActivityCompat.requestPermissions(NearbyActivity.this,
						new String[]{Manifest.permission.INTERNET, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE,
								Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_CODE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			default:break;
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		Log.d("NearbyActivity","NearbyActivity on post resume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("NearbyActivity", "NearbyActivity on pause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("NearbyActivity", "NearbyActivity on stop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("NearbyActivity", "NearbyActivity on destroy");
		confirmDialog.dismiss();
		confirmDialog=null;
	}


}
