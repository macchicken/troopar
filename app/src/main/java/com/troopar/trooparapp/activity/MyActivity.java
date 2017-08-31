package com.troopar.trooparapp.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.troopar.trooparapp.activity.service.ActivitiesService;
import com.troopar.trooparapp.activity.service.LikeActivityIntentService;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.activity.task.JoinEventTask;
import com.troopar.trooparapp.adapter.MyActivityInfoAdapter;
import com.troopar.trooparapp.model.ActivityModel;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.myview.EventSharePopupWindow;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyActivity extends AppCompatActivity{

	private int counter;
	private ArrayList<ActivityModel> activityModels;
	private MyActivityInfoAdapter activityInfoAdapter;
	private SwipeRefreshLayout refreshableView;
	private RecyclerView listView;
	private final int EVENT_DETAIL_REQUEST_CODE = 610;
	private final int POST_ACTIVITY_REQUEST_CODE = 640;
	private final int LOAD_MORE_DATA = 1;
	private final int NO_MORE_DATA = -1;
	private BroadcastReceiver broadcastReceiver;
	private ActivitiesService activitiesService;
	private SwipeRefreshLayout.OnRefreshListener refreshListener;
	private ShareItemOnClickListener itemOnClickListener;
	private Dialog confirmDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MyActivity", "MyActivity on create");
		setContentView(R.layout.activity_my);
		confirmDialog = new Dialog(MyActivity.this);
		confirmDialog.setContentView(R.layout.comfirm_dialog);
		confirmDialog.setTitle("");
		refreshableView = (SwipeRefreshLayout) findViewById(R.id.myactrefreshable_view);
		listView = (RecyclerView) findViewById(R.id.myactlistView);
		setSupportActionBar((Toolbar) findViewById(R.id.my_myacttoolbar));
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		activityModels=new ArrayList<>();
		listView.setLayoutManager(new LinearLayoutManager(MyActivity.this));
		activityInfoAdapter=new MyActivityInfoAdapter(activityModels,listView);
		listView.setAdapter(activityInfoAdapter);
		refreshListener=new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (Tools.checkNetworkConnected(MyActivity.this)) {
					if (Tools.isNullString(Constants.USERID)){
						refreshableView.setRefreshing(false);
						Toast.makeText(MyActivity.this, "login to activities between you and friends", Toast.LENGTH_SHORT).show();
					}else{
						refreshMyActivities();
					}
				} else {
					refreshableView.setRefreshing(false);
					Toast.makeText(MyActivity.this, "not connect to network", Toast.LENGTH_SHORT).show();
				}
			}
		};
		refreshableView.setOnRefreshListener(refreshListener);
		setupLoadMoreListener();
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try{
					Log.d("MyActivity", "BroadcastReceiver() {...}.onReceive()");
					String operationCode=intent.getStringExtra("operationCode");
					if ("likeActivity".equals(operationCode)){
						String status=intent.getStringExtra("status");
						if (Constants.TAG_SUCCESS.equals(status)){
							int position=Integer.parseInt(intent.getStringExtra("position"));
							int totalLikes=intent.getIntExtra("totalLikes",0);
							ActivityModel activityModel=activityModels.get(position);
							activityModel.setTotalLikes(totalLikes);
							activityModels.set(position,activityModel);
							activityInfoAdapter.notifyItemChanged(position);
						}else{
							Toast.makeText(MyActivity.this, "server error", Toast.LENGTH_SHORT).show();
						}
					}else{
						activityModels.remove(activityModels.size() - 1);
						activityInfoAdapter.notifyItemRemoved(activityModels.size());
						if (intent.getIntExtra("status",RESULT_CANCELED)==(RESULT_OK)){
							int statusCode=intent.getIntExtra("what",NO_MORE_DATA);
							if (statusCode==LOAD_MORE_DATA){
								ArrayList<ActivityModel> result= (ArrayList<ActivityModel>) intent.getSerializableExtra("activityModels");
								int total=result.size();
								activityModels.addAll(result);
								activityInfoAdapter.notifyItemRangeInserted(counter,total);
								counter+=total;
							}else{
								activityModels.add(new ActivityModel(Constants.NOMOREDATA));
								activityInfoAdapter.setNoMore(true);
								activityInfoAdapter.notifyItemInserted(activityModels.size()-1);
								Toast.makeText(MyActivity.this, "refresh to see other activities", Toast.LENGTH_SHORT).show();
							}
						}
					}
					Log.d("MyActivity", "BroadcastReceiver finish");
				}catch (Throwable t){
					t.printStackTrace();
				}finally {
					activityInfoAdapter.setLoaded();
				}
			}
		};
		LocalBroadcastManager.getInstance(MyActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("MyActivity.handler.load.more.data"));
		activitiesService=ActivitiesService.getInstance();
		if (Tools.checkNetworkConnected(MyActivity.this)){
			if (Tools.isNullString(Constants.USERID)){
				Toast.makeText(MyActivity.this,"login to activities between you and friends",Toast.LENGTH_SHORT).show();
			}else{
				ArrayList<ActivityModel> results=LocalDBHelper.getInstance().getActivities("100abc");
				if (results!=null){// load cache data in the first
					counter=results.size();
					activityModels.addAll(results);
					activityInfoAdapter.notifyDataSetChanged();
				}else{
					refreshMyActivities();
				}
			}
		}else{
			findViewById(R.id.myactrefreshable_view).setVisibility(View.GONE);
			ImageView imageView= (ImageView) findViewById(R.id.network_error_ic);
			imageView.setImageResource(R.drawable.network_error_pic);
			imageView.setVisibility(View.VISIBLE);
			Toast.makeText(MyActivity.this,"not connect to network",Toast.LENGTH_SHORT).show();
		}
		ShareDialog shareDialog = new ShareDialog(MyActivity.this);
		itemOnClickListener=new ShareItemOnClickListener(MyActivity.this,shareDialog);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_myact, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_post:
				if (Tools.isNullString(Constants.USERID)){Toast.makeText(MyActivity.this, "login to post your findings", Toast.LENGTH_SHORT).show();return true;}
				Intent intent=new Intent(MyActivity.this,PostActivityActivity.class);
				startActivityForResult(intent,POST_ACTIVITY_REQUEST_CODE);
				break;
			default:break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshMyActivities(){
		final Location myLocation= LocationService.getInstance().getLastKnownLocation();
		if (myLocation==null){
			Toast.makeText(MyActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
			if (refreshableView.isRefreshing()){
				refreshableView.setRefreshing(false);
			}
			return;
		}
		if (!activityInfoAdapter.setLoading()){
			return;
		}
		AsyncTask<Void, Void, ArrayList<ActivityModel>> task = new AsyncTask<Void, Void, ArrayList<ActivityModel>>(){

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				Log.d("MyActivity", "update activity data");
				if (!refreshableView.isRefreshing()){
					refreshableView.setRefreshing(true);
				}
				activityInfoAdapter.setNoMore(false);
			}

			@Override
			protected ArrayList<ActivityModel> doInBackground(Void... params) {
				return activitiesService.retrieveActivities(myLocation,0,10,null);
			}

			@Override
			protected void onPostExecute(ArrayList<ActivityModel> results) {
				try{
					super.onPostExecute(results);
					Log.d("MyActivity", "updateData task executed stopping");// stopping swipe refresh
					if (results == null||results.size()<=0) {
						Toast.makeText(MyActivity.this,"no more activities",Toast.LENGTH_SHORT).show();
					}else{
						if (refreshableView!=null){
							activityModels.clear();
							activityModels.addAll(results);
							counter = activityModels.size();
							activityInfoAdapter.notifyDataSetChanged();
						}
					}
					if (refreshableView!=null){
						refreshableView.setRefreshing(false);
					}
				}catch (Throwable t){
					t.printStackTrace();
				}finally {
					if (activityInfoAdapter!=null){
						activityInfoAdapter.setLoaded();
					}
				}
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	private void setupLoadMoreListener() {
		activityInfoAdapter.setOnLoadMoreListener(new MyActivityInfoAdapter.OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if (Tools.isNullString(Constants.USERID)){activityInfoAdapter.setLoaded();return;}
				activityModels.add(null);
				activityInfoAdapter.notifyItemInserted(activityModels.size() - 1);
				new Thread(new GetDataThread()).start();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {return;}
		switch (requestCode){
			case EVENT_DETAIL_REQUEST_CODE:
				if (data!=null){
					String eventPosition=data.getStringExtra("eventPosition");
					boolean joined=data.getBooleanExtra("joined",false);
					activityModels.get(Integer.parseInt(eventPosition)).getEvent().setJoined(joined);
				}
				break;
			case POST_ACTIVITY_REQUEST_CODE:
				if (data!=null){
					try {
						JSONObject postResult=new JSONObject(data.getStringExtra("postResult"));
						EventModel eventModel=new EventModel();
						JSONObject activity=postResult.getJSONObject("activity");
						String actType=activity.getString("type");
						if (Constants.POSTACT.equals(actType)){
							JSONObject user=postResult.getJSONObject("user");
							eventModel.setDescription(activity.getString("description"));// people posted message
							String userName=user.getString("username");
							eventModel.setUser(new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),userName,user.has("avatarOrigin")?user.getString("avatarOrigin"):"",user.has("avatarStandard")?user.getString("avatarStandard"):""));
							ActivityModel activityModel=new ActivityModel(userName,activity.getString("id"), actType, eventModel, null, null);
							activityModel.setCreatedTime(activity.getString("createdDate"));
							activityModels.add(0,activityModel);
							activityInfoAdapter.notifyDataSetChanged();
							++counter;
							listView.smoothScrollToPosition(0);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			default:break;
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		Log.d("MyActivity", "MyActivity on post resume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("MyActivity", "MyActivity on stop");
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(MyActivity.this).unregisterReceiver(broadcastReceiver);
		Log.d("MyActivity", "MyActivity on destroy");
		super.onDestroy();
		confirmDialog.dismiss();
		confirmDialog=null;
		activityInfoAdapter.setOnLoadMoreListener(null);
		RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
		recycledViewPool.clear();
		activityModels=null;
		activityInfoAdapter=null;
		itemOnClickListener.setContext(null);
		itemOnClickListener.setShareDialog(null);
		itemOnClickListener.setEvent(null);
		itemOnClickListener.setEventSharePopupWindow(null);
		itemOnClickListener=null;
		listView.removeAllViews();
		refreshableView.removeAllViews();
		refreshableView.setOnRefreshListener(null);
		refreshListener=null;
		refreshableView=null;
		broadcastReceiver = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MyActivity", "MyActivity on pause");
	}

	private class GetDataThread implements Runnable{

		private GetDataThread(){}

		@Override
		public void run() {
			try {
				Location lastKnownLocation=LocationService.getInstance().getLastKnownLocation();
				if (lastKnownLocation==null){
					return;
				}
				ArrayList<ActivityModel> result=activitiesService.retrieveActivities(lastKnownLocation, counter,10, null);
				Intent intent=new Intent("MyActivity.handler.load.more.data");
				if (result!=null&&result.size()>0){
					intent.putExtra("activityModels",result);
					intent.putExtra("what", LOAD_MORE_DATA);
					intent.putExtra("status", RESULT_OK);
				}else{
					intent.putExtra("what", NO_MORE_DATA);
					intent.putExtra("status", RESULT_OK);
				}
				LocalBroadcastManager.getInstance(MyActivity.this).sendBroadcast(intent);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void retryCheckNetwork(View v){
		if (Tools.checkNetworkConnected(MyActivity.this)){
			findViewById(R.id.network_error_ic).setVisibility(View.GONE);
			findViewById(R.id.myactrefreshable_view).setVisibility(View.VISIBLE);
		}
	}

	public void loadMore(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		activityInfoAdapter.setNoMore(false);
		activityInfoAdapter.setLoading();
		activityModels.remove(activityModels.size() - 1);
		activityInfoAdapter.notifyItemRemoved(activityModels.size());
		activityModels.add(null);
		activityInfoAdapter.notifyItemInserted(activityModels.size() - 1);
		new Thread(new GetDataThread()).start();
	}

	public void eventShareAction(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		String eventPosition=view.getContentDescription().toString();
		itemOnClickListener.setEvent(activityModels.get(Integer.parseInt(eventPosition)).getEvent());
		EventSharePopupWindow eventSharePopupWindow = new EventSharePopupWindow(MyActivity.this, itemOnClickListener);
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
		if (!resInfo.isEmpty()){
			for (ResolveInfo info : resInfo) {
				eventSharePopupWindow.enableShareApp(info.activityInfo.packageName);
			}
		}
		try {
			getPackageManager().getApplicationInfo("com.instagram.android", 0);
			eventSharePopupWindow.enableShareApp("com.instagram.android");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		itemOnClickListener.setEventSharePopupWindow(eventSharePopupWindow);
		//显示窗口
		eventSharePopupWindow.showAtLocation(MyActivity.this.findViewById(R.id.myactrefreshable_view), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
	}

	public void eventDetailAction(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		String eventPosition=view.getContentDescription().toString();
		ActivityModel activityModel=activityModels.get(Integer.parseInt(eventPosition));
		if (activityModel.getType().equals("post")||activityModel.getType().equals("upload_photo")){
			return;
		}
		Intent intent=new Intent(MyActivity.this,EventDetailActivity.class);
		intent.putExtra("eventModel",activityModel.getEvent());
		intent.putExtra("eventPosition",eventPosition);
		startActivityForResult(intent,EVENT_DETAIL_REQUEST_CODE);
	}

	public void userImageAction(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		String eventPosition=view.getContentDescription().toString();
		Intent intent=new Intent(MyActivity.this,UserDetailActivity.class);
		intent.putExtra("user",activityModels.get(Integer.parseInt(eventPosition)).getEvent().getUser());
		startActivity(intent);
	}

	public void cancelAction(View view){
		confirmDialog.hide();
		confirmDialog.findViewById(R.id.dialogButtonOK).setContentDescription("");
	}

	public void okAction(View view){
		confirmDialog.hide();
		String eventPosition=view.getContentDescription().toString();
		final EventModel eventModel=activityModels.get(Integer.parseInt(eventPosition)).getEvent();
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
					JSONObject joinJson=jsonObject.getJSONObject("join");//TODO what to do with 'Need approved'
					JSONObject event=jsonObject.getJSONObject("event");
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
						User user=new User(eventModel.getId(),jsonArray.toString(),-3,userId,eventModel.getName(),eventModel.getThumbnailImageUrl(),eventModel.getThumbnailImageUrl());//indicate joining an event group chat
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
		if (Tools.isNullString(Constants.USERID)){return;}
		String eventPosition=view.getContentDescription().toString();
		EventModel eventModel=activityModels.get(Integer.parseInt(eventPosition)).getEvent();
		User eventUser=eventModel.getUser();
		String creator=String.valueOf(eventUser.getId());
		if (Constants.USERID.equals(creator)){
			Toast.makeText(MyActivity.this,"you are the creator of this event",Toast.LENGTH_SHORT).show();
			return;
		}
		((TextView)confirmDialog.findViewById(R.id.confirmTitle)).setText(eventModel.isJoined()?"quite this event":"join this event");
		confirmDialog.show();
		confirmDialog.findViewById(R.id.dialogButtonOK).setContentDescription(eventPosition);
	}

	public void userCommentsAction(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		String position=view.getContentDescription().toString();
		ActivityModel activityModel=activityModels.get(Integer.parseInt(position));
		Intent intent=new Intent(MyActivity.this,CommentsActivity.class);
		intent.putExtra("activityModel",activityModel);
		intent.putExtra("actPosition",position);
		startActivity(intent);
	}

	public void likeCommentAction(View view){
		if (Tools.isNullString(Constants.USERID)){return;}
		String position=view.getContentDescription().toString();
		ActivityModel activityModel=activityModels.get(Integer.parseInt(position));
		Intent serviceIntent=new Intent(MyActivity.this, LikeActivityIntentService.class);
		String type=activityModel.getType();
		serviceIntent.putExtra("itemId","review".equals(type)?activityModel.getReview().getId():activityModel.getId());
		serviceIntent.putExtra("type",type);
		serviceIntent.putExtra("myUserId",Constants.USERID);
		serviceIntent.putExtra("position",position);
		serviceIntent.putExtra("pageCode","MyActivity");
		MyActivity.this.startService(serviceIntent);
	}


}
