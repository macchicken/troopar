package com.troopar.trooparapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.activity.service.EventService;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.myview.MySupportMapFragment;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.troopar.trooparapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyFindMapEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final int MAP_EVENT_DETAIL_REQUEST_CODE = 607;
    private final int MESSAGE_BOX_REQUEST_CODE=617;
    private final int SEARCH_EVENT_REQUEST_CODE=619;
    private final int SEARCH_ACTIVITY_REQUEST_CODE=635;
    private GoogleMap mMap;
    private LatLng myCurrLatLng;
    private int counter;
    private boolean completedInit;
    private MyInfoWindowAdapter myInfoWindowAdapter;
    private MyInfoWindowAdapter myInfoWindowAdapter3;
    private EventService eventService;
    private MySupportMapFragment mapFragment;
    private ProgressBar progressBar;
    private RelativeLayout refreshBtn;
    private View nearbyPeople;
    private MyClusterManager mClusterManager;
    private LocalDBHelper localDBHelper;
    private MyMapMarkerItem displayItem;
    private WeakReference<AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>>> taskWeakReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyFindMapEventActivity", "MyFindMapEventActivity on create");
        setContentView(R.layout.activity_my_find_map_event);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mapFragment = (MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.findMapEvnt);
        mapFragment.getMapAsync(this);
        eventService=EventService.getInstance();
        localDBHelper=LocalDBHelper.getInstance();
        findViewById(R.id.search_field).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tools.isNullString(Constants.USERID)){
                    Toast.makeText(MyFindMapEventActivity.this,"login to search events around",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MyFindMapEventActivity.this, SearchCriteriaActivity.class);
                startActivityForResult(intent, SEARCH_EVENT_REQUEST_CODE);
            }
        });
    }

    private class MyMapEventItemRenderer extends DefaultClusterRenderer<MyMapMarkerItem>{
        private final IconGenerator mIconGenerator = new IconGenerator(MyFindMapEventActivity.this);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(MyFindMapEventActivity.this);
        private final ImageView mImageView;
        private final ImageView mClusterImageView;

        public MyMapEventItemRenderer() {
            super(MyFindMapEventActivity.this, mMap, mClusterManager);
            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            View singleProfile = getLayoutInflater().inflate(R.layout.single_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterIconGenerator.setBackground(getResources().getDrawable(R.drawable.map_item_icon));
            mClusterIconGenerator.setColor(getResources().getColor(R.color.appMainColor));
            mIconGenerator.setContentView(singleProfile);
            mIconGenerator.setBackground(getResources().getDrawable(R.drawable.map_item_icon));
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            mImageView = (ImageView) singleProfile.findViewById(R.id.singleImage);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyMapMarkerItem item, MarkerOptions markerOptions) {
            Bitmap bitmap=item.getBitmap();
            if (bitmap==null||bitmap.isRecycled()){
                mImageView.setImageResource(R.drawable.troopar_logo_red_square_t);
            }else{
                mImageView.setImageBitmap(item.getBitmap());
            }
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
        @Override
        protected void onClusterItemRendered(MyMapMarkerItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyMapMarkerItem> cluster, MarkerOptions markerOptions) {
            mClusterImageView.setImageResource(R.drawable.event_category_icon);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;// Always render clusters.
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Return false so that we don't consume the event and the default behavior still occurs
                // (the camera animates to the user's current position).
                return false;
            }
        });
        Location locationCt = LocationService.getInstance().getLastKnownLocation();
        if (locationCt != null) {
            myCurrLatLng = new LatLng(locationCt.getLatitude(), locationCt.getLongitude());
        }else{
            Toast.makeText(MyFindMapEventActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
        }
        enableMyLocation();
        progressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        refreshBtn= (RelativeLayout) findViewById(R.id.refreshBtn);
        nearbyPeople= findViewById(R.id.nearbyPeople);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventsData(myCurrLatLng);
            }
        });
        nearbyPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Tools.isNullString(Constants.USERID)){
                    return;
                }
                NearestUserTask task=new NearestUserTask();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }
        });
        getEventsData(myCurrLatLng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapevent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null&&myCurrLatLng!=null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    //        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    //        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCurrLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            myInfoWindowAdapter=new MyInfoWindowAdapter();// pre initialise marker info window layout and its elements
            myInfoWindowAdapter3=new MyInfoWindowAdapter();// pre initialise marker info window layout and its elements
            mClusterManager = new MyClusterManager(this, mMap);
            mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
            mClusterManager.setRenderer(new MyMapEventItemRenderer());
            mClusterManager.setOnCameraChangeListener(new MyClusterManager.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (mapFragment.isTouchUp()){
                        mapFragment.setTouchUp(false);
                        Log.d("MyFindMapEventActivity",String.format("display event while moving [%s]",cameraPosition.toString()));
                        myCurrLatLng=cameraPosition.target;
                    }
                }
            });
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyMapMarkerItem>() {
                @Override
                public boolean onClusterClick(Cluster<MyMapMarkerItem> cluster) {
                    // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
                    // inside of bounds, then animate to center of the bounds.
                    // Create the builder to collect all essential cluster items for the bounds.
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    int index=0;
                    for (ClusterItem item : cluster.getItems()) {
                        if (index==0){displayItem=(MyMapMarkerItem) item;}
                        builder.include(item.getPosition());
                        index++;
                    }
                    // Get the LatLngBounds
                    LatLngBounds bounds = builder.build();
                    // Animate camera to the bounds
                    try {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        EventModel mEventModel=displayItem.getEventModel();// display the first item in the cluster
                        Bitmap imageDownloadedBitmap=displayItem.getBitmap();
                        if (imageDownloadedBitmap!=null){
                            myInfoWindowAdapter3.mImageView.setImageBitmap(imageDownloadedBitmap);
                        }else{
                            myInfoWindowAdapter3.mImageView.setImageResource(R.drawable.troopar_logo_red_square_t);
                        }
                        TextView tvTitle = ((TextView) myInfoWindowAdapter3.tvTitle.findViewById(R.id.eventTitle));
                        tvTitle.setText(mEventModel.getName());
                        TextView tvSnippet = ((TextView) myInfoWindowAdapter3.tvSnippet.findViewById(R.id.eventSnippet));
                        TextView tvSnippet2 = ((TextView) myInfoWindowAdapter3.tvSnippet2.findViewById(R.id.eventSnippet2));
                        String eventStartDate =mEventModel.getStartDate();
                        String eventEndDate =mEventModel.getEndDate();
                        String startDateOfWeek = Tools.getDateOfWeekFromDate(eventStartDate);
                        tvSnippet.setText(String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate)));
                        tvSnippet2.setText(Html.fromHtml("<u>" + mEventModel.getLocation() + "</u>"));
                        myInfoWindowAdapter3.eventPeopleJoinedProgress.setMax(mEventModel.getMaxJoinedPeople() == 0 ? 10 : mEventModel.getMaxJoinedPeople());
                        myInfoWindowAdapter3.eventPeopleJoinedProgress.setProgress(mEventModel.getJoinedProgress());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
            mClusterManager.setOnClusterInfoWindowClickListener(new ClusterManager.OnClusterInfoWindowClickListener<MyMapMarkerItem>() {
                @Override
                public void onClusterInfoWindowClick(Cluster<MyMapMarkerItem> cluster) {
                    eventDetailPageAction(displayItem);// display the first item in the cluster
                }
            });
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMapMarkerItem>() {
                @Override
                public boolean onClusterItemClick(MyMapMarkerItem myMapMarkerItem) {
                    Bitmap imageDownloadedBitmap=myMapMarkerItem.getBitmap();
                    EventModel mEventModel=myMapMarkerItem.getEventModel();
                    if (imageDownloadedBitmap!=null){
                        myInfoWindowAdapter.mImageView.setImageBitmap(imageDownloadedBitmap);
                    }else{
                        myInfoWindowAdapter.mImageView.setImageResource(R.drawable.troopar_logo_red_square_t);
                    }
                    TextView tvTitle = ((TextView) myInfoWindowAdapter.tvTitle.findViewById(R.id.eventTitle));
                    tvTitle.setText(mEventModel.getName());
                    TextView tvSnippet = ((TextView) myInfoWindowAdapter.tvSnippet.findViewById(R.id.eventSnippet));
                    TextView tvSnippet2 = ((TextView) myInfoWindowAdapter.tvSnippet2.findViewById(R.id.eventSnippet2));
                    String eventStartDate =mEventModel.getStartDate();
                    String eventEndDate =mEventModel.getEndDate();
                    String startDateOfWeek = Tools.getDateOfWeekFromDate(eventStartDate);
                    tvSnippet.setText(String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate)));
                    tvSnippet2.setText(Html.fromHtml("<u>" + mEventModel.getLocation() + "</u>"));
                    myInfoWindowAdapter.eventPeopleJoinedProgress.setMax(mEventModel.getMaxJoinedPeople() == 0 ? 10 : mEventModel.getMaxJoinedPeople());
                    myInfoWindowAdapter.eventPeopleJoinedProgress.setProgress(mEventModel.getJoinedProgress());
                    return false;
                }
            });
            mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyMapMarkerItem>() {
                @Override
                public void onClusterItemInfoWindowClick(MyMapMarkerItem item) {
                    eventDetailPageAction(item);
                }
            });
            mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(myInfoWindowAdapter3);
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(myInfoWindowAdapter);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_searchList:
                if (Tools.isNullString(Constants.USERID)){
                    return true;
                }
                if (completedInit){
                    Intent intent=new Intent(MyFindMapEventActivity.this,SearchActivity.class);
                    ArrayList<EventModel> eventModels=localDBHelper.getCacheUserEvents(Tools.isNullString(Constants.USERID)?"100abc":Constants.USERID);
                    intent.putExtra("events", eventModels);
                    startActivityForResult(intent,SEARCH_ACTIVITY_REQUEST_CODE);
                }
                return true;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case MAP_EVENT_DETAIL_REQUEST_CODE:
                Log.d("MyFindMapEventActivity","back from MAP_EVENT_DETAIL_REQUEST_CODE");
                break;
            case MESSAGE_BOX_REQUEST_CODE:
                Log.d("MyFindMapEventActivity","back from DisplayUsersActivity");
                break;
            case SEARCH_EVENT_REQUEST_CODE:
                if (Tools.checkNetworkConnected(MyFindMapEventActivity.this)&&resultCode==RESULT_OK) {
                    Serializable tempResultEv=data.getSerializableExtra("searchResult");
                    if (tempResultEv==null){return;}
                    final ArrayList<EventModel> resultEv = (ArrayList) tempResultEv;
                    final int totalEvents=resultEv.size();
                    if (totalEvents<1){return;}
                    Address address = data.getParcelableExtra("address");
                    completedInit=false;
                    refreshBtn.setClickable(false);
                    if (address != null) {
                        int mIndex = address.getMaxAddressLineIndex();
                        if (mIndex > 0) {
                            StringBuilder displayAddress = new StringBuilder();
                            for (int i = 0; i < mIndex; i++) {
                                displayAddress.append(address.getAddressLine(i)).append(" ");
                            }
                            Toast.makeText(MyFindMapEventActivity.this, String.format("found activities at %s", displayAddress.toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                    final Location location = data.getParcelableExtra("location");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                        RotateDrawable rotate = (RotateDrawable) indeterminateDrawable.getDrawable(0);
                        rotate.setToDegrees(360);
                    }
                    localDBHelper.insertUserEvents(Tools.isNullString(Constants.USERID) ? "100abc" : Constants.USERID, resultEv);
                    AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>> task = new AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>>() {
                        @Override
                        protected ArrayList<MyMapMarkerItem> doInBackground(Void... params) {
                            ArrayList<MyMapMarkerItem> results = new ArrayList<>(totalEvents);
                            for (int i = 0; i < totalEvents; i++) {
                                EventModel eventModel = resultEv.get(i);
                                FutureTarget<Bitmap> futureTarget = Glide.with(getApplicationContext()).load(eventModel.getThumbnailImageUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                                MyMapMarkerItem myMapMarkerItem = new MyMapMarkerItem(Double.valueOf(eventModel.getLatitude()), Double.valueOf(eventModel.getLongitude()));
                                myMapMarkerItem.setEventModel(eventModel);
                                try {
                                    myMapMarkerItem.setBitmap(futureTarget.get());
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                results.add(myMapMarkerItem);
                            }
                            return results;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<MyMapMarkerItem> myMapMarkerItems) {
                            super.onPostExecute(myMapMarkerItems);
                            for (MyMapMarkerItem myMapMarkerItem : myMapMarkerItems) {
                                mClusterManager.addItem(myMapMarkerItem);
                            }
                            mClusterManager.cluster();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                                RotateDrawable rotate = (RotateDrawable) indeterminateDrawable.getDrawable(0);
                                rotate.setToDegrees(0);
                            }
                            completedInit = true;
                            refreshBtn.setClickable(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                            taskWeakReference.clear();
                            taskWeakReference=null;
                        }
                    };
                    taskWeakReference= new WeakReference<>(task);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        task.execute();
                    }
                }
                break;
            case SEARCH_ACTIVITY_REQUEST_CODE:
                if (resultCode==RESULT_OK){
                    boolean dataChanged=data.getBooleanExtra("dataChanged",false);
                    if (!dataChanged){return;}
                    Serializable temp=data.getSerializableExtra("events");
                    if (temp==null){return;}
                    final ArrayList<EventModel> eventsTemp= (ArrayList<EventModel>) temp;
                    final int totalEvents=eventsTemp.size();
                    if (totalEvents<1){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                            RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                            rotate.setToDegrees(0);
                        }
                        return;
                    }
                    completedInit=false;
                    refreshBtn.setClickable(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                        RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                        rotate.setToDegrees(360);
                    }
                    localDBHelper.insertUserEvents(Tools.isNullString(Constants.USERID)?"100abc":Constants.USERID,eventsTemp);
                    AsyncTask<Void,Void,ArrayList<MyMapMarkerItem>> task=new AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>>() {
                        @Override
                        protected ArrayList<MyMapMarkerItem> doInBackground(Void... params) {
                            ArrayList<MyMapMarkerItem> results=new ArrayList<>(totalEvents);
                            for (int i = 0; i < totalEvents; i++) {
                                EventModel eventModel = eventsTemp.get(i);
                                FutureTarget<Bitmap> futureTarget=Glide.with(getApplicationContext()).load(eventModel.getThumbnailImageUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
                                MyMapMarkerItem myMapMarkerItem=new MyMapMarkerItem(Double.valueOf(eventModel.getLatitude()),Double.valueOf(eventModel.getLongitude()));
                                myMapMarkerItem.setEventModel(eventModel);
                                try {
                                    myMapMarkerItem.setBitmap(futureTarget.get());
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                results.add(myMapMarkerItem);
                            }
                            return results;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<MyMapMarkerItem> myMapMarkerItems) {
                            super.onPostExecute(myMapMarkerItems);
                            mClusterManager.clearItems();
                            for (MyMapMarkerItem myMapMarkerItem:myMapMarkerItems){
                                mClusterManager.addItem(myMapMarkerItem);
                            }
                            mClusterManager.cluster();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                                RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                                rotate.setToDegrees(0);
                            }
                            completedInit=true;
                            refreshBtn.setClickable(true);
                        }
                    };
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        task.execute();
                    }
                }
                break;
            default:break;
        }
    }

    private void getEventsData(LatLng latLng){
        Location locationCt;
        if (Tools.isNullString(Constants.USERID)){
            Toast.makeText(MyFindMapEventActivity.this, "login to see events around", Toast.LENGTH_SHORT).show();
            if(progressBar.isIndeterminate()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                    RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                    rotate.setToDegrees(0);
                }
            }
            return;
        }else{
            refreshBtn.setClickable(false);
            completedInit=false;
        }
        if (latLng==null){
            locationCt = LocationService.getInstance().getLastKnownLocation();
            if (locationCt==null){
                Toast.makeText(MyFindMapEventActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
                refreshBtn.setClickable(true);
                completedInit=true;
                return;
            }else{
                myCurrLatLng = new LatLng(locationCt.getLatitude(), locationCt.getLongitude());
            }
        }else{
            myCurrLatLng=latLng;
        }
        refreshMapEventFromService(mMap.getProjection().getVisibleRegion().latLngBounds);// use the map visible range for displaying the events
    }

    private class NearestUserTask extends AsyncTask<Void,Void,ArrayList<User>>{

        private String mUserId;
        private String equipId;
        private String signature;

        public NearestUserTask() {
            mUserId = Constants.USERID;
            equipId=Constants.DEVEICEIDVALUE;
            signature=Constants.SIGNATUREVALUE;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nearbyPeople.setClickable(false);
        }

        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new FormBody.Builder()
                    .add("userId", mUserId)
                    .add("myLatitude",Double.toString(myCurrLatLng.latitude))
                    .add("myLongitude", Double.toString(myCurrLatLng.longitude))
                    .add(Constants.EQUIPID, equipId)
                    .add(Constants.SIGNATURE, signature)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/user/nearest_users.php").post(formBody).build();
            ArrayList<User> users=null;
            try {
                response=client.newCall(request).execute();
                JSONObject jsonObject=new JSONObject(response.body().string());
                String status=jsonObject.getString("status");
                if (Constants.TAG_SUCCESS.equals(status)){
                    JSONArray jsonArray=jsonObject.getJSONArray("nearby_users");
                    int total=jsonArray.length();
                    if (total>0){
                        users=new ArrayList<>();
                        for (int i=0;i<total;i++){
                            JSONObject user= (JSONObject) jsonArray.get(i);
                            users.add(new User(user.getString("firstName"),user.getString("gender"),user.getInt("id"),user.getString("lastName"),user.getString("username"),user.getString("avatarOrigin"),user.getString("avatarStandard")));
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (response!=null){
                    response.body().close();
                }
            }
            return users;
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            super.onPostExecute(users);
            nearbyPeople.setClickable(true);
            if (users!=null){
                if (nearbyPeople!=null){
                    Intent intent = new Intent(MyFindMapEventActivity.this, DisplayNearbyUsersActivity.class);
                    intent.putExtra("userArrayList",users);
                    startActivityForResult(intent, MESSAGE_BOX_REQUEST_CODE);
                }
            }else{
                Toast.makeText(MyFindMapEventActivity.this,"do have any users",Toast.LENGTH_LONG).show();
            }
        }
    }
    private void refreshMapEventFromService(final LatLngBounds marker) {
        Log.d("MyFindMapEventActivity", String.format("refresh search event data remotely %s",marker.toString()));
        AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>> task = new AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>>(){
            @Override
            protected void onPreExecute() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                    RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                    rotate.setToDegrees(360);
                }
            }

            @Override
            protected ArrayList<MyMapMarkerItem> doInBackground(Void... params) {
                ArrayList<EventModel> results=eventService.retrieveEvents(marker,marker.getCenter(),50,counter,null);
                if (results==null){return null;}
                int total=results.size();
                if (total==0){return new ArrayList<>();}
                localDBHelper.insertUserEvents(Tools.isNullString(Constants.USERID)?"100abc":Constants.USERID,results);
                ArrayList<MyMapMarkerItem> myMapMarkerItems=new ArrayList<>(total);
                for (EventModel eventModel:results){
                    FutureTarget<Bitmap> futureTarget=Glide.with(getApplicationContext()).load(eventModel.getThumbnailImageUrl()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
                    MyMapMarkerItem myMapMarkerItem=new MyMapMarkerItem(Double.valueOf(eventModel.getLatitude()),Double.valueOf(eventModel.getLongitude()));
                    myMapMarkerItem.setEventModel(eventModel);
                    try {
                        myMapMarkerItem.setBitmap(futureTarget.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    myMapMarkerItems.add(myMapMarkerItem);
                }
                return myMapMarkerItems;
            }

            @Override
            protected void onPostExecute(ArrayList<MyMapMarkerItem> myMapMarkerItems) {
                if (refreshBtn!=null){
                    try {
                        if (myMapMarkerItems==null||myMapMarkerItems.size()==0){Toast.makeText(MyFindMapEventActivity.this,"no activities",Toast.LENGTH_LONG).show();return;}
                        mClusterManager.clearItems();
                        for (MyMapMarkerItem myMapMarkerItem:myMapMarkerItems){
                            mClusterManager.addItem(myMapMarkerItem);
                        }
                        mClusterManager.cluster();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }finally {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            LayerDrawable indeterminateDrawable = (LayerDrawable) progressBar.getIndeterminateDrawable();
                            RotateDrawable rotate= (RotateDrawable) indeterminateDrawable.getDrawable(0);
                            rotate.setToDegrees(0);
                        }
                        refreshBtn.setClickable(true);
                        completedInit=true;
                    }
                }else{
                    completedInit=true;
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View myContentsView;
        private ImageView mImageView;
        private TextView tvTitle;
        private TextView tvSnippet;
        private TextView tvSnippet2;
        private ProgressBar eventPeopleJoinedProgress;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.event_brief_info, null);
            mImageView = (ImageView) myContentsView.findViewById(R.id.eventImageView);
            tvTitle = ((TextView) myContentsView.findViewById(R.id.eventTitle));
            tvSnippet = ((TextView) myContentsView.findViewById(R.id.eventSnippet));
            tvSnippet2 = ((TextView) myContentsView.findViewById(R.id.eventSnippet2));
            eventPeopleJoinedProgress = (ProgressBar) myContentsView.findViewById(R.id.eventPeopleJoinedProgress);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return myContentsView;
        }
    }

    private void eventDetailPageAction(MyMapMarkerItem myMapMarkerItem){
        if (Tools.isNullString(Constants.USERID)){return;}
        Intent intent = new Intent(MyFindMapEventActivity.this, EventDetailActivity.class);
        intent.putExtra("eventModel",myMapMarkerItem.getEventModel());
        startActivityForResult(intent, MAP_EVENT_DETAIL_REQUEST_CODE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MyFindMapEventActivity", "MyFindMapEventActivity on stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MyFindMapEventActivity", "MyFindMapEventActivity on destroy");
        try{
            if (taskWeakReference!=null) {
                AsyncTask<Void, Void, ArrayList<MyMapMarkerItem>> task = taskWeakReference.get();
                if (task != null && !task.isCancelled()) {
                    Log.d("MyFindMapEventActivity", "MyFindMapEventActivity on destroy cancel search task");
                    task.cancel(true);
                }
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        myCurrLatLng=null;
        displayItem=null;
        myInfoWindowAdapter=null;
        myInfoWindowAdapter3=null;
        refreshBtn.removeAllViews();
        refreshBtn=null;
        nearbyPeople=null;
        mClusterManager.clearItems();
        mClusterManager.setOnCameraChangeListener(null);
        mClusterManager.setOnClusterItemInfoWindowClickListener(null);
        mClusterManager.setOnClusterItemClickListener(null);
        mClusterManager.setOnClusterInfoWindowClickListener(null);
        mClusterManager.setOnClusterClickListener(null);
        mClusterManager=null;
        mMap.setOnInfoWindowClickListener(null);
        mMap.setOnMapClickListener(null);
        mMap.setOnMapLongClickListener(null);
        mMap.setInfoWindowAdapter(null);
        mMap.setOnMarkerClickListener(null);
        mMap.setOnCameraChangeListener(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MyFindMapEventActivity", "MyFindMapEventActivity on pause");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("MyFindMapEventActivity","MyFindMapEventActivity on post resume");
    }

    @Override
    public void onBackPressed() {
        Log.d("MyFindMapEventActivity","MyFindMapEventActivity on back press");
        finish();
        super.onBackPressed();
    }


}
