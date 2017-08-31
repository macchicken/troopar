package com.troopar.trooparapp.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.activity.task.JoinEventTask;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.model.UploadPhoto;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.myview.EventSharePopupWindow;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;
import com.troopar.trooparapp.utils.MultipartUtility;
import com.troopar.trooparapp.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;


public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback{


    private JSONParser jParser = new JSONParser();
    private static final int MORE_REVIEWS_REQUEST_CODE = 601;
    private static final int WRITE_REVIEWS_REQUEST_CODE = 603;
    private static final int ORI_IMAGE_REQUEST_CODE = 608;
    private static final int VIEW_ALL_EVENT_PHOTOS_REQUEST_CODE = 613;
    private final static int PICK_PHOTO_CODE = 1046;
    private String latitude;
    private String longitude;
    private String eventId;
    private String eventName;
    private String eventDescriptionText;
    private String eventPlaceText;
    private String eventStartTime;
    private String eventEndTime;
    private String contactNumText;
    private String imageUrl;
    private String userId;
    private String userImagePath;
    private String eventCreatedTime;
    private int reviewNum;
    private int maxJoinedPeople;
    private int joinedProgress;
    private String userNameText;
    private MapFragment mapFragment;
    private JSONArray reviewResult;
    private UploadPhoto[] eventGallery;
    private static int scrollX = 0;
    private static int scrollY = -1;
    private ScrollView scrollView;
    private Intent UploadPhotoData;
    private BroadcastReceiver broadcastReceiver;
    private LoadViewTask loadViewTask;
    private boolean completedInit;
    private EventModel eventModel;
    private String eventPosition;
    private LinearLayout listOfPictures;
    private LinearLayout listOfJoiners;
    private int savedHeight;
    private boolean expanded;
    private ShareItemOnClickListener shareItemOnClickListener;
    private Dialog confirmDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("EventDetailActivity", "EventDetailActivity onCreate");
        setContentView(R.layout.activity_event_detail);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.eventPhotosBroadcastId)) {
                    if (eventGallery != null && eventGallery.length > 0) {
                        int arrIndex = intent.getIntExtra("arrIndex", 0);
                        boolean isLiked = intent.getBooleanExtra("isLiked", false);
                        Log.i("EventDetailActivity",arrIndex+" photo index set liked "+isLiked);
                        eventGallery[arrIndex].setLiked(isLiked);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.eventPhotosBroadcastId));
        confirmDialog = new Dialog(EventDetailActivity.this);
        confirmDialog.setContentView(R.layout.comfirm_dialog);
        confirmDialog.setTitle("");
        prepareDataFromIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            (loadViewTask=new LoadViewTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            (loadViewTask=new LoadViewTask()).execute();//Initialize a LoadViewTask object and call the execute() method
        }
    }

    private View addImage(Bitmap bitmap,final String originalImageUrl,final String imageId,final int arrIndex){
        LinearLayout layout = new LinearLayout(EventDetailActivity.this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);
        ImageView imageView = new ImageView(EventDetailActivity.this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetailActivity.this, OriginalImageActivity.class);
                scrollX = scrollView.getScrollX();
                scrollY = scrollView.getScrollY();
                intent.putExtra("originalImageUrl", originalImageUrl);
                intent.putExtra("imageId", imageId);
                intent.putExtra("isLiked", eventGallery[arrIndex].isLiked());
                intent.putExtra("arrIndex", arrIndex);
                startActivityForResult(intent, ORI_IMAGE_REQUEST_CODE);
            }
        });
        layout.addView(imageView);
        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng temp=new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 13f));
        ApplicationContextStore applicationContextStore=ApplicationContextStore.getInstance();
        if (applicationContextStore.getMAPLOCATIONICONBITMAP()==null){
            int pixels = (int) (30 * Constants.DENSITYSCALE + 0.5f);
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("message_location_icon", "drawable", getPackageName()));
            applicationContextStore.setMAPLOCATIONICONBITMAP(Bitmap.createScaledBitmap(imageBitmap, pixels, pixels, false));
        }
        if (applicationContextStore.getMAPLOCATIONICON()==null){
            applicationContextStore.setMAPLOCATIONICON(BitmapDescriptorFactory.fromBitmap(applicationContextStore.getMAPLOCATIONICONBITMAP()));
        }
        googleMap.addMarker(new MarkerOptions().position(temp).title(this.eventName).icon(applicationContextStore.getMAPLOCATIONICON())).showInfoWindow();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void setUpMapIfNeeded() {
        if (mapFragment==null){
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.eventMap);
            mapFragment.getMapAsync(this);
            TextView eventDistance= (TextView) findViewById(R.id.eventDistance);
            if (eventDistance!=null){
                String eDistance=String.format("%.2f", ((float) eventModel.getDistance()) / 1000);
                eventDistance.setText(String.format("%s km",eDistance));
            }
        }
    }

    public void shareAction(View v){
        EventSharePopupWindow eventSharePopupWindow = new EventSharePopupWindow(EventDetailActivity.this, shareItemOnClickListener);
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
        shareItemOnClickListener.setEventSharePopupWindow(eventSharePopupWindow);
        //显示窗口
        eventSharePopupWindow.showAtLocation(findViewById(R.id.scrollView2), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    public void viewAllEventPhotosAction(View v){
        Intent intent = new Intent(EventDetailActivity.this, EventPhotosActivity.class);
        scrollX = scrollView.getScrollX();
        scrollY = scrollView.getScrollY();
        intent.putExtra("images", eventGallery);
        startActivityForResult(intent, VIEW_ALL_EVENT_PHOTOS_REQUEST_CODE);
    }

    public void writeReviewAction(View v){
        Intent intent = new Intent(EventDetailActivity.this, WriteReviewActivity.class);
        scrollX = scrollView.getScrollX();
        scrollY = scrollView.getScrollY();
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventModel", eventModel);
        startActivityForResult(intent, WRITE_REVIEWS_REQUEST_CODE);
    }

    public void loadMoreReviews(View v){
        Intent intent = new Intent(EventDetailActivity.this, MoreReviewsActivity.class);
        scrollX = scrollView.getScrollX();
        scrollY = scrollView.getScrollY();
        intent.putExtra("eventId",eventId);
        startActivityForResult(intent, MORE_REVIEWS_REQUEST_CODE);
    }

    public void expandCollapsedByMaxLines(View view) {
        TextView text= (TextView) findViewById(R.id.eventDescription);
        int height = text.getMeasuredHeight();
        text.setHeight(height);
        int newHeight;
        if (!expanded){
            ((TextView)findViewById(R.id.ExpandBtn)).setText(R.string.collapse);
            savedHeight=height;
            text.setMaxLines(Integer.MAX_VALUE); //expand fully
            text.measure(View.MeasureSpec.makeMeasureSpec(text.getMeasuredWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(LinearLayout.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED));
            newHeight=text.getMeasuredHeight();
        }else{
            text.setMaxLines(4);
            newHeight=savedHeight;
            ((TextView)findViewById(R.id.ExpandBtn)).setText(R.string.fullText);
        }
        expanded=!expanded;
        ObjectAnimator animation = ObjectAnimator.ofInt(text, "height", height, newHeight);
        animation.setDuration(250).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case MORE_REVIEWS_REQUEST_CODE:
                Log.d("EventDetailActivity","back from more event reviews");
                break;
            case WRITE_REVIEWS_REQUEST_CODE:
                Log.d("EventDetailActivity", "back from writing event reviews");
                if (data!=null&&resultCode==RESULT_OK){
                    Toast.makeText(EventDetailActivity.this,"review posted",Toast.LENGTH_LONG).show();
                }
                break;
            case PICK_PHOTO_CODE:
                Log.d("EventDetailActivity", "back from upload event photo");
                UploadPhotoData=data;
                break;
            case ORI_IMAGE_REQUEST_CODE:
                Log.d("EventDetailActivity", "back from original image");
                break;
            case VIEW_ALL_EVENT_PHOTOS_REQUEST_CODE:
                Log.d("EventDetailActivity", "back from view all photos image");
                break;
            default:break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
                // Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Bring up gallery to select a photo
                startActivityForResult(intent, PICK_PHOTO_CODE);
                return true;
            case android.R.id.home:
                onBackPressed();
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eventdetail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
        //Before running code in separate thread
        @Override
        protected void onPreExecute()
        {
            ImageView imageView= (ImageView) findViewById(R.id.loadingProgress);
            imageView.startAnimation(AnimationUtils.loadAnimation(EventDetailActivity.this, R.anim.pulse));
        }
        //The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                HashMap<String,String> parameters3=new HashMap<>();
                parameters3.put("eventIds[]",eventId);
                parameters3.put("userId",Constants.USERID);
                parameters3.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
                parameters3.put(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
                JSONObject obj=jParser.makeRequestForHttp(BuildConfig.API_READHOST + "/event/get_event_list.php", "POST", parameters3);
                if (obj!=null){
                    String status = (String) obj.get("status");
                    if (Constants.TAG_SUCCESS.equals(status)){
                        JSONObject eventDetailObj=obj.getJSONArray("events").getJSONObject(0);
                        eventName=eventDetailObj.getString("name");
                        eventDescriptionText=eventDetailObj.getString("description");
                        eventPlaceText=eventDetailObj.getString("location");
                        eventStartTime=eventDetailObj.getString("startDate").replaceAll(",","");
                        eventEndTime=eventDetailObj.getString("endDate").replaceAll(",","");
                        latitude=eventDetailObj.getString("latitude");
                        longitude=eventDetailObj.getString("longitude");
                        contactNumText=eventDetailObj.getString("contact");
                        eventModel.setJoined(eventDetailObj.optBoolean("joined"));
                        JSONArray jsonArray=eventDetailObj.getJSONArray("joiners");
                        int total=jsonArray.length();
                        eventModel.resetJoiners(total);
                        for (int i=0;i<total;i++){
                            JSONObject joiner=jsonArray.getJSONObject(i);
                            eventModel.addJoiners(new User(joiner.getString("firstName"),joiner.getString("gender"),joiner.getInt("id"),joiner.getString("lastName"),joiner.getString("username"),joiner.has("avatarOrigin")?joiner.getString("avatarOrigin"):"",joiner.has("avatarStandard")?joiner.getString("avatarStandard"):""));
                        }
                    }
                }
                HashMap<String,String> parameters=new HashMap<>();
                parameters.put("eventId",eventId);
                parameters.put("offSet","0");
                parameters.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
                parameters.put(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
                JSONObject obj2=jParser.makeRequestForHttp(BuildConfig.API_READHOST + "/event_reviews.php", "POST", parameters);
                if (obj2!=null){
                    String status = obj2.getString("status");
                    if (Constants.TAG_SUCCESS.equals(status)){
                        reviewResult=obj2.getJSONArray("result");
                    }
                }
                HashMap<String,String> parameters2=new HashMap<>();
                parameters2.put("eventId",eventId);
                parameters2.put("userId",userId);
                parameters2.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
                parameters2.put(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
                JSONObject obj3=jParser.makeRequestForHttp(BuildConfig.API_READHOST + "/photo/get_event_photo.php", "POST", parameters2);
                if (obj3!=null){
                    String status2 = obj3.getString("status");
                    if (Constants.TAG_SUCCESS.equals(status2)){
                        if (obj3.has("photos")){
                            JSONArray photos=obj3.getJSONArray("photos");
                            int total=photos.length();
                            if (total>0){
                                eventGallery=new UploadPhoto[total];
                                for (int i=0;i<total;i++){
                                    JSONObject temp=photos.getJSONObject(i);
                                    UploadPhoto t=new UploadPhoto(temp.getString("id"),temp.getString("createdDate"),temp.getString("description"),temp.getString("photoPath"),temp.getString("smallImagePath"),temp.getString("totalLikes"));
                                    t.setLiked(temp.getBoolean("liked"));
                                    eventGallery[i]=t;
                                }
                            }else{
                                eventGallery=new UploadPhoto[0];
                            }
                        }
                    }else{
                        eventGallery=new UploadPhoto[0];
                    }
                }else{
                    eventGallery=new UploadPhoto[0];
                }
                User user=eventModel.getUser();
                String strUserId=String.valueOf(user.getId());
                LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
                User cacheUser=localDBHelper.getCacheUser(strUserId);
                if (cacheUser==null||cacheUser.getUserProfile()==null){
                    cacheUser=UserService.getInstance().getProfile(userId,strUserId);
                    localDBHelper.insertUserProfile(cacheUser);
                }
                eventModel.setUser(cacheUser);
            } catch (Throwable e){
                e.printStackTrace();
            }
            return null;
        }

        //Update the progress
        //set the current progress of the progress dialog if necessary
        @Override
        protected void onProgressUpdate(Integer... values)
        {
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result)
        {
            ImageView imageView= (ImageView) findViewById(R.id.loadingProgress);
            imageView.clearAnimation();
            imageView.setVisibility(View.GONE);
            findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
            initView();//initialize the View
        }
    }

    private void prepareDataFromIntent(){
        userId = Constants.USERID;
        Intent tintent = getIntent();
        if (tintent != null) {
            eventModel = (EventModel) tintent.getSerializableExtra("eventModel");
            eventPosition= tintent.getStringExtra("eventPosition");
            if (eventModel == null) {
                return;
            }
            eventId = eventModel.getId();
            eventCreatedTime = eventModel.getCreatedDate();
            User user = eventModel.getUser();
            userImagePath = Tools.isNullString(user.getAvatarOrigin()) ? "" : user.getAvatarOrigin();
            userNameText = Tools.isNullString(user.getLastName())||Tools.isNullString(user.getFirstName())?user.getUserName():String.format("%s %s", user.getFirstName(), user.getLastName());
            imageUrl = eventModel.getMediumImageUrl();// download medium image in the detail page
            eventName = eventModel.getName();
            eventDescriptionText = eventModel.getDescription();
            eventPlaceText = eventModel.getLocation();
            eventStartTime = eventModel.getStartDate();
            eventEndTime = eventModel.getEndDate();
            latitude = eventModel.getLatitude();
            longitude = eventModel.getLongitude();
            contactNumText = Tools.isNullString(eventModel.getContact())?"":eventModel.getContact();
            reviewNum = eventModel.getReviewNum();
            maxJoinedPeople = eventModel.getMaxJoinedPeople();
            joinedProgress = eventModel.getJoinedProgress();
        }
    }

    private void initView() {
        scrollView= (ScrollView) findViewById(R.id.scrollView2);
        scrollView.setVisibility(View.VISIBLE);
        ImageView eventImageView= (ImageView) findViewById(R.id.eventImageView);
        ImageView userImage= (ImageView) findViewById(R.id.userImage);
        if (!Tools.isNullString(userImagePath)) {
            Glide.with(EventDetailActivity.this).load(userImagePath).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(userImage);
        }else{
            userImage.setImageResource(R.drawable.user_image);
        }
        if (!Tools.isNullString(imageUrl)){
            Glide.with(EventDetailActivity.this).load(imageUrl).override(128,128).diskCacheStrategy(DiskCacheStrategy.ALL).into(eventImageView);
        }else{
            eventImageView.setImageResource(R.drawable.troopar_logo_red_square_t);
        }
        ((TextView)findViewById(R.id.userName)).setText(userNameText);
        ((TextView)findViewById(R.id.userFollowers)).setText(getString(R.string.followersCount,eventModel.getUser().getUserProfile().getFollowerNum()));
        TextView eventNameView = (TextView) findViewById(R.id.eventName);
        eventNameView.setTypeface(null, Typeface.BOLD);
        eventNameView.setText(eventName);
        TextView eventPublisher = (TextView) findViewById(R.id.eventPublisher);
        eventPublisher.setPaintFlags(eventPublisher.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        eventPublisher.setText(String.format("by %s", this.eventName));
        ((TextView)findViewById(R.id.eventCreatedTime)).setText(Tools.formatTime(eventCreatedTime));
        TextView eventPlace = (TextView) findViewById(R.id.eventPlace);
        eventPlace.setPaintFlags(eventPlace.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        eventPlace.setText(eventPlaceText);
        String startDateOfWeek= Tools.getDateOfWeekFromDate(eventStartTime);
        String endDateOfWeek= Tools.getDateOfWeekFromDate(eventEndTime);
        ((TextView) findViewById(R.id.eventTime)).setText(String.format("%s %s to %s %s",startDateOfWeek,Tools.formatTime(eventStartTime),endDateOfWeek,Tools.formatTime(eventEndTime)));
        ProgressBar eventPeopleJoinedProgress= (ProgressBar) findViewById(R.id.eventPeopleJoinedProgress);
        eventPeopleJoinedProgress.setMax(maxJoinedPeople==0?10:maxJoinedPeople);
        eventPeopleJoinedProgress.setProgress(joinedProgress);
        ((TextView) findViewById(R.id.eventJoinedPeople)).setText(String.format("%d/%d",joinedProgress,maxJoinedPeople));
        ((TextView) findViewById(R.id.eventFee)).setText(String.format("%s","Free"));
        Calendar now= Calendar.getInstance();
        TextView daysToGo= (TextView) findViewById(R.id.eventDayToGo);
        StringBuilder temp=new StringBuilder(String.valueOf(now.get(Calendar.YEAR)));
        temp.append("-");
        int month=now.get(Calendar.MONTH)+1;
        String strMonth= String.valueOf(month);
        if (month<10){
            strMonth="0"+strMonth;
        }
        temp.append(strMonth);
        temp.append("-");
        temp.append(now.get(Calendar.DAY_OF_MONTH)<10?"0"+String.valueOf(now.get(Calendar.DAY_OF_MONTH)):String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
        temp.append(" 00:00:00");
        daysToGo.setText(String.valueOf(Tools.calculateTimeDiffByDay(eventStartTime,temp.toString())));
        setUpMapIfNeeded();
        TextView numberOfReviewers= (TextView) findViewById(R.id.numberOfReviewers);
        try{
            if (reviewResult!=null){
                int reviewTotal=reviewResult.length();
                if (reviewTotal>0){
                    reviewNum=reviewTotal;
                    reviewTotal=reviewTotal>3?3:reviewTotal;
                    for (int i = 0; i < reviewTotal; i++){
                        JSONObject c = reviewResult.getJSONObject(i);
                        JSONObject user = c.getJSONObject("user");
                        String displayName=Tools.isNullString(user.getString("firstName"))&&Tools.isNullString(user.getString("lastName"))?user.getString("username"):String.format("%s %s", user.getString("firstName"), user.getString("lastName"));
                        if (i==0){
                            ((TextView)findViewById(R.id.eventReviewPosterOne)).setText(displayName);
                            ((TextView)findViewById(R.id.eventReviewContentOne)).setText(c.getString("content"));
                            ((TextView)findViewById(R.id.eventReviewPostTimeOne)).setText(Tools.calculateTimeElapsed(c.getString("createdDate")));
                            findViewById(R.id.eventReviewOne).setVisibility(View.VISIBLE);
                            ImageView userImageOne=(ImageView) findViewById(R.id.userImageOne);
                            if (!Tools.isNullString(user.getString("avatarOrigin"))){
                                Glide.with(EventDetailActivity.this).load(user.getString("avatarOrigin")).override(100,100).into((ImageView) findViewById(R.id.userImageOne));
                            }else{
                                userImageOne.setImageResource(R.drawable.user_image);
                            }
                        }else if (i==1){
                            ((TextView)findViewById(R.id.eventReviewPosterTwo)).setText(displayName);
                            ((TextView)findViewById(R.id.eventReviewContentTwo)).setText(c.getString("content"));
                            ((TextView)findViewById(R.id.eventReviewPostTimeTwo)).setText(Tools.calculateTimeElapsed(c.getString("createdDate")));
                            findViewById(R.id.eventReviewTwo).setVisibility(View.VISIBLE);
                            ImageView userImageTwo=(ImageView) findViewById(R.id.userImageTwo);
                            if (!Tools.isNullString(user.getString("avatarOrigin"))){
                                Glide.with(EventDetailActivity.this).load(user.getString("avatarOrigin")).override(100, 100).into(userImageTwo);
                            }else{
                                userImageTwo.setImageResource(R.drawable.user_image);
                            }
                        }else if (i==2){
                            ((TextView)findViewById(R.id.eventReviewPosterThree)).setText(displayName);
                            ((TextView)findViewById(R.id.eventReviewContentThree)).setText(c.getString("content"));
                            ((TextView)findViewById(R.id.eventReviewPostTimeThree)).setText(Tools.calculateTimeElapsed(c.getString("createdDate")));
                            findViewById(R.id.eventReviewThree).setVisibility(View.VISIBLE);
                            ImageView userImageThree=(ImageView) findViewById(R.id.userImageThree);
                            if (!Tools.isNullString(user.getString("avatarOrigin"))){
                                Glide.with(EventDetailActivity.this).load(user.getString("avatarOrigin")).override(100, 100).into((ImageView) findViewById(R.id.userImageThree));
                            }else{
                                userImageThree.setImageResource(R.drawable.user_image);
                            }
                        }
                    }
                }
                if (reviewNum<4){
                    findViewById(R.id.moreEvReviewsBtn).setClickable(false);
                }
                numberOfReviewers.setText(String.format("%s Reviews",reviewNum));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        listOfPictures= (LinearLayout) findViewById(R.id.listOfPictures);
        int maxTotal=eventGallery.length<7?eventGallery.length:6;
        for (int i=0;i<maxTotal;i++){
            final int j=i;
            Glide.with(EventDetailActivity.this).load(eventGallery[i].getSmallImagePath()).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    listOfPictures.addView(addImage(bitmap, eventGallery[j].getPhotoPath(), eventGallery[j].getId(), j));
                }
            });
        }
        ArrayList<User> joiners=eventModel.getJoiners();
        if (joiners==null){completedInit=true;return;}
        ViewGroup.MarginLayoutParams marginLayoutParams=new ViewGroup.MarginLayoutParams(170, 170);
        final ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(150, 150);
        marginLayoutParams.setMargins(6, 6, 6, 6);
        listOfJoiners= (LinearLayout) findViewById(R.id.listOfJoiners);
        for (User joiner:joiners){
            final LinearLayout layout = new LinearLayout(EventDetailActivity.this);
            layout.setLayoutParams(marginLayoutParams);
            if (Tools.isNullString(joiner.getAvatarStandard())){
                CircleImageView imageView = new CircleImageView(EventDetailActivity.this);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageResource(R.drawable.user_image);
                layout.addView(imageView);
                listOfJoiners.addView(layout);
                continue;
            }
            Glide.with(EventDetailActivity.this).load(joiner.getAvatarStandard()).asBitmap().override(70,70).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    CircleImageView imageView = new CircleImageView(EventDetailActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(resource);
                    layout.addView(imageView);
                    listOfJoiners.addView(layout);
                }
            });
        }
        ShareDialog shareDialog=new ShareDialog(EventDetailActivity.this);
        shareItemOnClickListener=new ShareItemOnClickListener(EventDetailActivity.this,shareDialog);
        shareItemOnClickListener.setEvent(eventModel);
        final TextView textView=((TextView) findViewById(R.id.eventDescription));
        textView.setText(eventDescriptionText);
        textView.post(new Runnable() {
            @Override
            public void run() {
                if (textView.getLineCount()>4){
                    textView.setMaxLines(4);
                }else{
                    findViewById(R.id.ExpandBtn).setVisibility(View.GONE);
                }
            }
        });
        if (eventModel.isJoined()){
            findViewById(R.id.joinBtn).setBackgroundResource(R.drawable.bg_oval_for_event_joined_with_gradient);
        }else{
            findViewById(R.id.joinBtn).setBackgroundResource(R.drawable.bg_oval_for_event_with_gradient);
        }
        completedInit=true;
    }

    private class UploadPhotoView extends AsyncTask<Void, Integer, Boolean>{

        private ProgressDialog uploadProgressDialog;
        private String mImagePath;
        private String mEventId;
        private String mUserId;

        UploadPhotoView(String imagePath,String eventId,String userId){
            super();
            mImagePath=imagePath;
            mEventId=eventId;
            mUserId=userId;
        }

        @Override
        protected void onPreExecute()
        {
            uploadProgressDialog = ProgressDialog.show(EventDetailActivity.this, "Uploading...", "Uploading a photo, please wait...", false, false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            uploadProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean submitSucceed) {
            if (submitSucceed){
                Toast.makeText(EventDetailActivity.this,"successfully uploaded",Toast.LENGTH_SHORT).show();
            }
            uploadProgressDialog.dismiss();//close the progress dialog
        }

        @Override
        protected Boolean doInBackground(Void... params) {
                try {
                    MultipartUtility multipart = new MultipartUtility(BuildConfig.API_WRITEHOST+"/photo/upload_photo.php", "UTF-8","POST");
                    multipart.addFormField("eventId",mEventId);
                    multipart.addFormField("userId",mUserId);
                    multipart.addFormField("type","photo");
                    multipart.addFormField("description","");
                    multipart.addFormField(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
                    multipart.addFormField(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
                    multipart.addFilePart("photos", new File(mImagePath));
                    String response = multipart.finish(); // response from server.
                    Log.i("EventDetailActivity", String.format("Response: %s",response));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return false;
        }
    }

    public void cancelAction(View view){
        confirmDialog.hide();
    }

    public void okAction(View view){
        confirmDialog.hide();
        User eventUser=eventModel.getUser();
        final String creator=String.valueOf(eventUser.getId());
        if (Constants.USERID.equals(creator)){
            Toast.makeText(EventDetailActivity.this,"you are the creator of this event",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i("EventDetailActivity","join this event");
        JoinEventTask task=new JoinEventTask(userId,eventId);//TODO change to intentService and localbroadcast mechanism
        task.setPostExecuteCallBack(new JoinEventTask.PostExecuteCallBack() {
            @Override
            public void postExecute(JSONObject jsonObject) {
                try {
                    if (jsonObject==null){
                        Toast.makeText(getApplicationContext(),"fail to join this event",Toast.LENGTH_LONG).show();
                        return;
                    }
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
                    JSONObject remarks=new JSONObject();
                    remarks.put("creator",creator);
                    remarks.put("uid",eventModel.getId());
                    remarks.put("groupName",eventModel.getName());
                    remarks.put("flag","event");
                    JSONArray jsonArray=new JSONArray();
                    for (User joiner:joiners){
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
                    if (event.getBoolean("joined")){
                        Toast.makeText(getApplicationContext(),"you joined this event",Toast.LENGTH_LONG).show();
                        User user=new User(eventModel.getId(),jsonArray.toString(),-3,userId,eventName,eventModel.getThumbnailImageUrl(),eventModel.getThumbnailImageUrl());//indicate joining an event group chat
                        LocalDBHelper.getInstance().insertUserProfile(user);
                        eventModel.setJoined(true);
                        findViewById(R.id.joinBtn).setBackgroundResource(R.drawable.bg_oval_for_event_joined_with_gradient);
                        MessageService.getInstance().send(userId,String.format("event/%s",userId),eventModel.getId(),"group_invite",remarks);
                    }else{
                        Toast.makeText(getApplicationContext(),"you quit this event",Toast.LENGTH_LONG).show();
                        eventModel.setJoined(false);
                        findViewById(R.id.joinBtn).setBackgroundResource(R.drawable.bg_oval_for_event_with_gradient);
                        MessageService.getInstance().send(userId,String.format("event/%s",userId),eventModel.getId(),"group_quit",remarks);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void joinEventAction(View v){
        ((TextView)confirmDialog.findViewById(R.id.confirmTitle)).setText(eventModel.isJoined()?"quite this event":"join this event");
        confirmDialog.show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("EventDetailActivity", "event detail activity on post resume");
        if (scrollView!=null){
            Log.i("EventDetailActivity", String.format("event detail activity on post resume scroll to %d %d\n", scrollX, scrollY));
            scrollView.scrollTo(scrollX, scrollY);
        }
        String uploadImagePath=null;
        if (UploadPhotoData!=null) {
            try {
                Uri photoUri = UploadPhotoData.getData();
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(photoUri, projection, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    uploadImagePath = cursor.getString(column_index);
                    cursor.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        UploadPhotoData=null;
        if (uploadImagePath!=null){
            new UploadPhotoView(uploadImagePath,eventId,userId).execute();//no need to parallel execution since only one task once a time
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("EventDetailActivity","EventDetailActivity on stop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("EventDetailActivity", "EventDetailActivity on pause");
    }

    @Override
    protected void  onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
        Log.d("EventDetailActivity", "EventDetailActivity on destroy");
        confirmDialog.dismiss();
        confirmDialog=null;
        eventModel=null;
        broadcastReceiver=null;
        shareItemOnClickListener.setContext(null);
        shareItemOnClickListener.setShareDialog(null);
        shareItemOnClickListener.setEvent(null);
        shareItemOnClickListener.setEventSharePopupWindow(null);
        shareItemOnClickListener=null;
        if (scrollView!=null){
            scrollView.removeAllViews();
            scrollView=null;
        }

        if (listOfPictures!=null){
            listOfPictures.removeAllViews();
            listOfPictures=null;
        }

        if (listOfJoiners!=null){
            listOfJoiners.removeAllViews();
            listOfJoiners=null;
        }
    }

    @Override
    public void onBackPressed() {
        if (!completedInit){
            loadViewTask.cancel(true);
            Log.d("EventDetailActivity", "EventDetailActivity cancel load view task");
        }
        Intent intent=getIntent();
        intent.putExtra("eventPosition",eventPosition);
        intent.putExtra("joined",eventModel.isJoined());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        User user=eventModel.getUser();
        outState.putString("eventId", eventModel.getId());
        outState.putString("eventName", eventModel.getName());
        outState.putString("eventDescription", eventModel.getDescription());
        outState.putString("eventPlace", eventModel.getLocation());
        outState.putString("eventStartTime", eventModel.getStartDate());
        outState.putString("eventEndTime", eventModel.getEndDate());
        outState.putString("eventCategory", eventModel.getCategory());
        outState.putString("latitude", eventModel.getLatitude());
        outState.putString("longitude", eventModel.getLongitude());
        outState.putString("contactNum", eventModel.getContact());
        outState.putInt("reviewNum", eventModel.getReviewNum());
        outState.putInt("maxJoinedPeople", eventModel.getMaxJoinedPeople());
        outState.putInt("joinedProgress", eventModel.getJoinedProgress());
        outState.putBoolean("joined", eventModel.isJoined());
        outState.putString("colorHex", eventModel.getColorHex());
        outState.putString("userImagePath", Tools.isNullString(user.getAvatarOrigin())?"":user.getAvatarOrigin());
        outState.putString("userNameText", Tools.isNullString(user.getLastName())||Tools.isNullString(user.getFirstName())?user.getUserName():String.format("%s %s", user.getFirstName(), user.getLastName()));
        outState.putSerializable("joiners", eventModel.getJoiners());
        outState.putSerializable("eventModel",eventModel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventId=savedInstanceState.getString("eventId");
        eventName=savedInstanceState.getString("eventName");
        eventDescriptionText=savedInstanceState.getString("eventDescription");
        eventPlaceText=savedInstanceState.getString("eventPlace");
        eventStartTime=savedInstanceState.getString("eventStartTime");
        eventEndTime=savedInstanceState.getString("eventEndTime");
        latitude=savedInstanceState.getString("latitude");
        longitude=savedInstanceState.getString("longitude");
        contactNumText=savedInstanceState.getString("contactNum");
        reviewNum=savedInstanceState.getInt("reviewNum");
        maxJoinedPeople=savedInstanceState.getInt("maxJoinedPeople");
        joinedProgress=savedInstanceState.getInt("joinedProgress");
        userImagePath=savedInstanceState.getString("userImagePath");
        userNameText=savedInstanceState.getString("userNameText");
        eventModel= (EventModel) savedInstanceState.getSerializable("eventModel");
    }


}
