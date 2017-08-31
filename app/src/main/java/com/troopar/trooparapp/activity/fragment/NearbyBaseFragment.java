package com.troopar.trooparapp.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.ShareItemOnClickListener;
import com.troopar.trooparapp.activity.service.EventService;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.adapter.ComplexEventInfoAdapter;
import com.troopar.trooparapp.adapter.viewholder.EventViewHolder;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.RecyclerViewDecoration;
import com.troopar.trooparapp.utils.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NearbyBaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyBaseFragment extends Fragment {

    private int viewResId;
    private String mTitle;
    private SwipeRefreshLayout refreshableView;
    private RecyclerView listView;
    private ArrayList<EventModel> events;
    private ComplexEventInfoAdapter eventInfoAdapter;
    private int counter=0;
    private LatLngBounds marker;
    private final int LOAD_MORE_DATA = 1;
    private final int LOAD_MORE_DATA_ERROR = 2;
    private final int NO_MORE_DATA = 3;
    private BroadcastReceiver broadcastReceiver;
    private ShareItemOnClickListener shareItemOnClickListener;
    private String category;
    private String localBroadcastFilterName;
    private ImageView loadingProgress;
    private Button promoteText;


    // newInstance constructor for creating fragment with arguments
    public static NearbyBaseFragment newInstance(int page, String title,int viewResId) {
        NearbyBaseFragment fragmentFirst = new NearbyBaseFragment();
        Bundle args = new Bundle();
        args.putInt("pageNumber", page);
        args.putInt("viewResId", viewResId);
        args.putString("pageTitle", title);
        args.putString("category", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle=getArguments().getString("pageTitle");
        viewResId=getArguments().getInt("viewResId");
        category=getArguments().getString("category");
        localBroadcastFilterName=String.format("NearbyBaseFragment.%s.handler.load.more.data",category);
    }

    private void initView(View view) {
        refreshableView = (SwipeRefreshLayout) view.findViewById(R.id.refreshable_view);
        listView = (RecyclerView) view.findViewById(R.id.listView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        //linearLayoutManager.setMeasuredDimension(Constants.Measuredwidth, RecyclerView.LayoutParams.WRAP_CONTENT);
        listView.setLayoutManager(linearLayoutManager);
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.recyclerview_divider);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerViewDecoration(drawable);
        listView.addItemDecoration(itemDecoration);
        events=new ArrayList<>();
        eventInfoAdapter=new ComplexEventInfoAdapter(events,listView);
        listView.setAdapter(eventInfoAdapter);
        refreshableView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("NearbyBaseFragment", "Nearby activity onRefresh");
                if (Tools.checkNetworkConnected(getActivity().getApplicationContext())) {
                    if (Tools.isNullString(Constants.USERID)){
                        refreshableView.setRefreshing(false);
                        Toast.makeText(getContext(), "login to see events around", Toast.LENGTH_SHORT).show();
                    }else{
                        refreshMapEventFromService();
                    }
                } else {
                    refreshableView.setRefreshing(false);
                    Toast.makeText(getActivity().getApplicationContext(), "not connect to network", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setupLoadMoreListener();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(viewResId, container, false);
        initView(view);
        Context context= getActivity().getApplicationContext();
        boolean networkConnected= Tools.checkNetworkConnected(context);
        if (!networkConnected){
            ImageView imageView= (ImageView) view.findViewById(R.id.network_error_ic);
            imageView.setImageResource(R.drawable.network_error_pic);
            imageView.setVisibility(View.VISIBLE);
        }else{
            view.findViewById(R.id.refreshable_view).setVisibility(View.VISIBLE);
        }
        promoteText= (Button) view.findViewById(R.id.promoteText);
        loadingProgress= (ImageView) view.findViewById(R.id.loadingProgress);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("NearbyBaseFragment", "BroadcastReceiver() {...}.onReceive()");
                try{
                    if (intent!=null){
                        if (Constants.RESULT_CANCELED==intent.getIntExtra("status",-1)){
                            eventInfoAdapter.setNoMore(true);
                            Log.d("NearbyBaseFragment", "get data from service with exception");
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
                                    events.addAll(eventModels);
                                    eventInfoAdapter.notifyItemRangeInserted(counter,total);
                                    counter+=total;
                                    eventInfoAdapter.setLoaded();
                                    Log.d("NearbyBaseFragment", "get data from service");
                                    break;
                                case NO_MORE_DATA:
                                    events.remove(events.size() - 1);
                                    eventInfoAdapter.notifyItemRemoved(events.size());
                                    eventInfoAdapter.setLoaded();
                                    eventInfoAdapter.setNoMore(true);
                                    Log.d("NearbyBaseFragment", "get data from service no more data");
                                    Toast.makeText(context,String.format("no %s activities",category),Toast.LENGTH_SHORT).show();
                                    break;
                                case LOAD_MORE_DATA_ERROR:
                                    Log.d("NearbyBaseFragment", "get data from service with no json object return");
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
                Log.d("NearbyBaseFragment","BroadcastReceiver finish");
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter(localBroadcastFilterName));
        Location location= LocationService.getInstance().getLastKnownLocation();
        if (location==null){
            Toast.makeText(context,"location service not enabled",Toast.LENGTH_LONG).show();
        }else{
            marker = LocationService.getInstance().getLatLngBoundsWithinRange(new LatLng(location.getLatitude(),location.getLongitude()),Constants.RADIUS);
            if (Tools.isNullString(Constants.USERID)){
                Toast.makeText(getContext(), "login to see events around", Toast.LENGTH_SHORT).show();
            }else{
                if (networkConnected){
                    loadingProgress.setVisibility(View.VISIBLE);
                    loadingProgress.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.pulse));
                    refreshMapEventFromService();
                }
            }
        }
        ShareDialog shareDialog = new ShareDialog(getActivity());
        shareItemOnClickListener=new ShareItemOnClickListener(getActivity(),shareDialog);
        return view;
    }

    private void refreshMapEventFromService() {
        Log.d("NearbyBaseFragment", String.format("%s refresh nearby event data remotely",category));
        if (marker==null){
            Location location= LocationService.getInstance().getLastKnownLocation();
            if (location==null) {
                eventInfoAdapter.setLoaded();
                if (refreshableView.isRefreshing()){
                    refreshableView.setRefreshing(false);
                }
                Toast.makeText(getActivity().getApplicationContext(), "location service not enabled", Toast.LENGTH_LONG).show();
                return;
            }else{
                marker=LocationService.getInstance().getLatLngBoundsWithinRange(new LatLng(location.getLatitude(),location.getLongitude()),Constants.RADIUS);
            }
        }
        if (!eventInfoAdapter.setLoading()){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new RefreshTask(marker).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new RefreshTask(marker).execute();
        }
    }

    private class RefreshTask extends AsyncTask<Void, Void, ArrayList>{

        private LatLngBounds mMarker;

        public RefreshTask(LatLngBounds marker) {
            this.mMarker = marker;
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
                ArrayList<EventModel> eventModels= EventService.getInstance().retrieveEvents(mMarker, mMarker.getCenter(), 10, 0, category);
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
                Log.d("NearbyBaseFragment", String.format("%s task executed stopping",category));// stopping swipe refresh
                if (results==null||results.size()<=0){
                    if (counter<1){
                        promoteText.setVisibility(View.VISIBLE);
                        promoteText.setText(String.format("no %s activities",category));
                    }
                }else{
                    if (refreshableView!=null){
                        events.clear();
                        events.addAll(results);
                        eventInfoAdapter.notifyDataSetChanged();
                        counter=results.size();
                        if (promoteText.getVisibility()==View.VISIBLE){
                            promoteText.setVisibility(View.GONE);
                        }
                    }
                }
                if (refreshableView!=null){
                    refreshableView.setRefreshing(false);
                }
                loadingProgress.clearAnimation();
                loadingProgress.setVisibility(View.GONE);
            }catch (Throwable t){
                t.printStackTrace();
            }finally {
                if (eventInfoAdapter!=null){
                    eventInfoAdapter.setLoaded();
                }
            }
        }
    }

    public EventModel getEvent(int position) {
        return events.get(position);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("NearbyBaseFragment", "NearbyBaseFragment on pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("NearbyBaseFragment", "NearbyBaseFragment on stop");
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
        Log.d("NearbyBaseFragment", "NearbyActivity on destroy");
        eventInfoAdapter.setOnLoadMoreListener(null);
        eventInfoAdapter=null;
        RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
        RecyclerView.ViewHolder viewHolder=recycledViewPool.getRecycledView(0);
        if (viewHolder!=null){
            EventViewHolder eventViewHolder= (EventViewHolder) viewHolder;
            eventViewHolder.getListOfJoiners().removeAllViews();
        }
        recycledViewPool.clear();
        ((BitmapDrawable)loadingProgress.getDrawable()).getBitmap().recycle();
        loadingProgress=null;
        events=null;
        marker=null;
        shareItemOnClickListener.setContext(null);
        shareItemOnClickListener.setShareDialog(null);
        shareItemOnClickListener.setEvent(null);
        shareItemOnClickListener.setEventSharePopupWindow(null);
        shareItemOnClickListener=null;
        listView.removeAllViews();
        refreshableView.removeAllViews();
        refreshableView.setOnRefreshListener(null);
        listView=null;
        refreshableView=null;
        promoteText=null;
        broadcastReceiver=null;
        eventInfoAdapter=null;
    }

    private void setupLoadMoreListener() {
        eventInfoAdapter.setOnLoadMoreListener(new ComplexEventInfoAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (marker==null||Tools.isNullString(Constants.USERID)){eventInfoAdapter.setLoaded();return;}
                events.add(null);
                eventInfoAdapter.notifyItemInserted(events.size() - 1);
                new Thread(new GetDataThread()).start();
            }
        });
    }

    private class GetDataThread implements Runnable{

        private GetDataThread(){}

        @Override
        public void run() {
            Intent intent=new Intent(localBroadcastFilterName);
            try {
                ArrayList<EventModel>  eventModels=EventService.getInstance().retrieveEvents(marker, marker.getCenter(), 10, counter, category);
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
                intent.putExtra("status", Constants.RESULT_CANCELED);
            }
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(intent);
        }
    }

    public String getTitle() {
        return mTitle;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}
