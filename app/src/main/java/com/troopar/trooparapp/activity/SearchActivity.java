package com.troopar.trooparapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.troopar.trooparapp.activity.service.EventService;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.model.EventModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.ComplexSearchedEventInfoAdapter;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;


public class SearchActivity extends AppCompatActivity {

	private final int SEARCH_EVENT_REQUEST_CODE=619;
	private final int LOAD_MORE_DATA = 1;
	private final int LOAD_MORE_DATA_ERROR = 2;
	private final int NO_MORE_DATA = 3;
	private int current_request_code=-1;
	private LatLngBounds marker;
	private Location location;
	private String searchKeyWords;
	private String sortKw;
	private int counter;
	private EventModel[] currentEvents;
	private SwipeRefreshLayout refreshableView;
	private ArrayList<EventModel> events;
	private ComplexSearchedEventInfoAdapter eventInfoAdapter;
	private RecyclerView listView;
	private BroadcastReceiver broadcastReceiver;
	private boolean dataChanged;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("SearchActivity","SearchActivity on create");
		setContentView(R.layout.activity_search);
		Toolbar myToolbar=(Toolbar) findViewById(R.id.my_searchtoolbar);
		myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		initView();
		location= LocationService.getInstance().getLastKnownLocation();
		if (location==null) {
			Toast.makeText(SearchActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
		}else{
			marker = LocationService.getInstance().getLatLngBoundsWithinRange(new LatLng(location.getLatitude(),location.getLongitude()), Constants.RADIUS);
			Serializable temp=getIntent().getSerializableExtra("events");
			if (temp!=null){
				events.addAll((ArrayList<EventModel>)temp);
				eventInfoAdapter.notifyDataSetChanged();
			}
		}
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("SearchActivity", "BroadcastReceiver() {...}.onReceive()");
				try{
					if (intent!=null){
						if (RESULT_CANCELED==intent.getIntExtra("status",-1)){
							eventInfoAdapter.setNoMore(true);
							Log.d("SearchActivity", "get data from service with exception");
							events.remove(events.size() - 1);
							eventInfoAdapter.notifyItemRemoved(events.size());
							eventInfoAdapter.setLoaded();
						}else{
							switch (intent.getIntExtra("what",-1)){
								case LOAD_MORE_DATA:
									events.remove(events.size()-1);
									eventInfoAdapter.notifyItemRemoved(events.size());
									ArrayList<EventModel> eventModels= (ArrayList<EventModel>) intent.getSerializableExtra("eventModels");
									int total=eventModels.size();
									Log.d("SearchActivity", "get data from service");
									events.addAll(eventModels);
									eventInfoAdapter.notifyItemRangeInserted(counter,total);
									counter+=total;
									dataChanged=true;
									eventInfoAdapter.setLoaded();
									break;
								case NO_MORE_DATA:
									events.remove(events.size() - 1);
									eventInfoAdapter.notifyItemRemoved(events.size());
									eventInfoAdapter.setLoaded();
									eventInfoAdapter.setNoMore(true);
									Log.d("SearchActivity", "get data from service no more data");
									Toast.makeText(SearchActivity.this,"no more activities",Toast.LENGTH_SHORT).show();
									break;
								case LOAD_MORE_DATA_ERROR:
									Log.d("SearchActivity", "get data from service with no json object return");
									eventInfoAdapter.setNoMore(true);
									events.remove(events.size() - 1);
									eventInfoAdapter.notifyItemRemoved(events.size());
									eventInfoAdapter.setLoaded();
									break;
								default:break;
							}
						}
					}
				}catch (Throwable throwable){
					throwable.printStackTrace();
				}
				Log.d("SearchActivity","BroadcastReceiver finish");
			}
		};
		LocalBroadcastManager.getInstance(SearchActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("SearchActivity.handler.load.more.data"));
	}

	private void refreshMapEventFromService() {
		if (marker==null&&searchKeyWords==null){
			eventInfoAdapter.setLoaded();
			if (refreshableView.isRefreshing()){
				refreshableView.setRefreshing(false);
			}
			return;
		}
		if (!eventInfoAdapter.setLoading()){
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new RefreshTask(location,marker,searchKeyWords,sortKw).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new RefreshTask(location,marker,searchKeyWords,sortKw).execute();
		}
	}

	private class RefreshTask extends AsyncTask<Void,Void,ArrayList>{

		private String mSearchKeyWords;
		private LatLngBounds mMarker;
		private Location mLocation;
		private String mSortKw;

		public RefreshTask(Location location, LatLngBounds marker, String searchKeyWords, String sortKw) {
			this.mLocation = location;
			this.mMarker = marker;
			this.mSearchKeyWords = searchKeyWords;
			this.mSortKw = sortKw;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!refreshableView.isRefreshing()){
				refreshableView.setRefreshing(true);
			}
			eventInfoAdapter.setNoMore(false);
		}

		@Override
		protected ArrayList doInBackground(Void... params) {
			try {
				ArrayList<EventModel> eventModels;
				if (mSearchKeyWords==null){
					eventModels=EventService.getInstance().retrieveEvents(mMarker, mMarker.getCenter(), 10, 0, null);
				}else{
					eventModels=EventService.getInstance().searchEvents(mLocation,mSearchKeyWords,mSortKw,0,false);
				}
				if (eventModels==null){return null;}
				int total=eventModels.size();
				if (total<=0){
					return new ArrayList();
				}
				return eventModels;
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList results) {
			try{
				if (results==null||results.size()<=0){
					Toast.makeText(SearchActivity.this, "no more activities", Toast.LENGTH_SHORT).show();
				}else{
					if (refreshableView!=null){
						events.clear();
						events.addAll(results);
						eventInfoAdapter.notifyDataSetChanged();
						counter=results.size();
						dataChanged=true;
					}
				}
				if (refreshableView!=null){
					refreshableView.setRefreshing(false);
				}
			}catch (Throwable t){
				t.printStackTrace();
			}finally {
				if (eventInfoAdapter!=null){
					eventInfoAdapter.setLoaded();
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data==null||resultCode!=RESULT_OK){return;}
		switch (requestCode){
			case SEARCH_EVENT_REQUEST_CODE:
				if (Tools.checkNetworkConnected(SearchActivity.this)){
					Address address=data.getParcelableExtra("address");
					if (address!=null){
						int mIndex=address.getMaxAddressLineIndex();
						if (mIndex>0){
							StringBuilder displayAddress=new StringBuilder();
							for (int i=0;i<mIndex;i++){
								displayAddress.append(address.getAddressLine(i)).append(" ");
							}
							Toast.makeText(SearchActivity.this,String.format("found activities at %s",displayAddress.toString()),Toast.LENGTH_LONG).show();
						}
					}
					List<EventModel> resultEv= (ArrayList) data.getSerializableExtra("searchResult");
					location=data.getParcelableExtra("location");
					searchKeyWords=data.getStringExtra("searchKeyWords");
					sortKw=data.getStringExtra("sortKw");
					int total=resultEv.size();
					EventModel[] temp3=new EventModel[total];
					for (int i=0;i<total;i++){
						temp3[i]=resultEv.get(i);
					}
					currentEvents=temp3;
					current_request_code=SEARCH_EVENT_REQUEST_CODE;
					if (total>0){dataChanged=true;}
				}
				break;
			default:break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_findevent:
				Intent data = new Intent();
				data.putExtra("events",events);
				data.putExtra("dataChanged",dataChanged);
				setResult(RESULT_OK,data);
				onBackPressed();
				return true;
			case android.R.id.home:
				onBackPressed();
				return true;
			default:return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_search, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void initView(){
		refreshableView = (SwipeRefreshLayout) findViewById(R.id.refreshable_view);
		listView = (RecyclerView) findViewById(R.id.listView);
		StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		listView.setLayoutManager(staggeredGridLayoutManager);
		events=new ArrayList<>();
		eventInfoAdapter=new ComplexSearchedEventInfoAdapter(events,listView);
		listView.setAdapter(eventInfoAdapter);
//		listView.addItemDecoration(new SearchedEventItemDecoration(getApplicationContext().getResources().getDrawable(R.drawable.stroke_line),  LinearLayoutManager.VERTICAL));
		eventInfoAdapter.setOnLoadMoreListener(new ComplexSearchedEventInfoAdapter.OnLoadMoreListener(){
			@Override
			public void onLoadMore() {
				if (marker==null&&searchKeyWords==null){eventInfoAdapter.setLoaded();return;}
				events.add(null);
				eventInfoAdapter.notifyItemInserted(events.size() - 1);
				new Thread(new GetDataThread()).start();
			}
		});
		refreshableView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (Tools.checkNetworkConnected(SearchActivity.this)) {
					refreshMapEventFromService();
				}else {
					refreshableView.setRefreshing(false);
					Toast.makeText(SearchActivity.this, "not connect to network", Toast.LENGTH_SHORT).show();
				}
			}
		});
		findViewById(R.id.search_field).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchActivity.this, SearchCriteriaActivity.class);
				startActivityForResult(intent, SEARCH_EVENT_REQUEST_CODE);
			}
		});
	}

	public void eventDetailAction(View view){
		String eventPosition=view.getContentDescription().toString();
		Intent intent=new Intent(SearchActivity.this,EventDetailActivity.class);
		intent.putExtra("eventModel",events.get(Integer.parseInt(eventPosition)));
		intent.putExtra("eventPosition",eventPosition);
		startActivity(intent);
	}

	private class GetDataThread implements Runnable{

		private GetDataThread(){}

		@Override
		public void run() {
			Intent intent=new Intent("SearchActivity.handler.load.more.data");
			try {
				ArrayList<EventModel> eventModels;
				if (searchKeyWords==null){
					eventModels=EventService.getInstance().retrieveEvents(marker, marker.getCenter(), 10, counter,null);
				}else{
					eventModels=EventService.getInstance().searchEvents(location,searchKeyWords,sortKw,counter,true);
				}
				if (eventModels==null){
					intent.putExtra("what",LOAD_MORE_DATA_ERROR);
				}else{
					int total=eventModels.size();
					if (total<1){
						intent.putExtra("what",NO_MORE_DATA);
					}else{
						intent.putExtra("eventModels",eventModels);
						intent.putExtra("what", LOAD_MORE_DATA);
					}
				}
			} catch (Throwable e){
				e.printStackTrace();
				intent.putExtra("status", RESULT_CANCELED);
			}
			LocalBroadcastManager.getInstance(SearchActivity.this).sendBroadcast(intent);
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		Log.d("SearchActivity","SearchActivity on post resume");
		if (current_request_code!=-1&&currentEvents!=null){
			Log.d("SearchActivity","SearchActivity on post resume with returning activities");
			events.clear();
			Collections.addAll(events, currentEvents);
			eventInfoAdapter.notifyDataSetChanged();
			current_request_code=-1;
		}
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(SearchActivity.this).unregisterReceiver(broadcastReceiver);
		super.onDestroy();
		Log.d("SearchActivity", "SearchActivity on destroy");
		eventInfoAdapter.setOnLoadMoreListener(null);
		RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
		recycledViewPool.clear();
		currentEvents=null;
		events=null;
		eventInfoAdapter=null;
		marker=null;
		eventInfoAdapter=null;
		listView.removeAllViews();
		refreshableView.removeAllViews();
		listView=null;
		refreshableView=null;
		broadcastReceiver=null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("SearchActivity", "SearchActivity on pause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("SearchActivity", "SearchActivity on stop");
	}


}
