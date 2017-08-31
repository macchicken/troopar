package com.troopar.trooparapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaCodecInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.activity.task.MediaProgressListener;
import com.troopar.trooparapp.activity.task.SendVideoTask;
import com.troopar.trooparapp.adapter.ChatMsgViewAdapter;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.MyClipboardManager;
import com.troopar.trooparapp.utils.Tools;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageBoxActivity extends AppCompatActivity {

    private ArrayList<MessageModel> incomeMessages;
    private ChatMsgViewAdapter chatMsgViewAdapter;
    private ListView incomeMessagesListView;
    private EditText etSendmessage;
    private String myUserId;
    private String receiverUserId;
    private String mDeviceId;
    private String mSignature;
    private User fUser;
    private User mUser;
    private MessageService messageService;
    private MediaRecorder mediaRecorder;
    private String audioFile;
    private ImageView btnSelectOther;
    private Button btnSend;
    private final int PICK_PICTURE_CODE_REQUEST_CODE=616;
    private final int PICK_VIDEO_CODE_REQUEST_CODE=618;
    private int position;
    private boolean loadingMore,theEnd;
    private long lastMessagePosition,limit=10;
    private PowerManager.WakeLock wakeLock;
    private View loadMoreView;
    private MediaPlayer mediaPlayer;
    private AtomicBoolean isRecording;
    private String prevPosition;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MessageBoxActivity", "MessageBoxActivity on create");
        super.onCreate(savedInstanceState);
        isRecording=new AtomicBoolean(false);
        messageService=MessageService.getInstance();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (fUser==null){return;}
                if (intent.getIntExtra("status",0)!=RESULT_OK){return;}
                MessageModel messageModel= (MessageModel) intent.getSerializableExtra("messageModel");
                switch (intent.getIntExtra("operationCode",-1)){
                    case 1:
                        if (etSendmessage!=null) {// still in the page, send message action
                            etSendmessage.setText("");
                            incomeMessages.add(messageModel);
                            chatMsgViewAdapter.notifyDataSetChanged();
                            incomeMessagesListView.smoothScrollToPosition(chatMsgViewAdapter.getCount()-1);
                        }
                        break;
                    case 3://private receive message action
                        if (mUser.getId()>-1&&messageModel.getFrom().equals(String.valueOf(fUser.getId()))){
                            if (incomeMessages!=null){
                                incomeMessages.add(messageModel);
                                chatMsgViewAdapter.notifyDataSetChanged();
                                incomeMessagesListView.smoothScrollToPosition(chatMsgViewAdapter.getCount()-1);
                            }
                        }else{
                            if (LocalDBHelper.getInstance().insertRecentContactWithUserId(messageModel.getFrom(),myUserId)==-1){// not the case of chat data being deleted or not chat with before
                                LocalDBHelper.getInstance().updateUnreadMessageCount(myUserId,messageModel.getFrom());
                                Intent intent3=new Intent("MessageService.local.message.data");
                                intent3.putExtra("status", RESULT_OK);
                                intent3.putExtra("messageModel", messageModel);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent3);
                            }
                        }
                        break;
                    case 5://group receive message action
                        String[] temp=messageModel.getFrom().split("/");
                        if ((mUser.getId()==-3||mUser.getId()==-5)&&temp[1].equals(mUser.getFirstName())){
                            if (incomeMessages!=null){
                                incomeMessages.add(messageModel);
                                chatMsgViewAdapter.notifyDataSetChanged();
                                incomeMessagesListView.smoothScrollToPosition(chatMsgViewAdapter.getCount()-1);
                            }
                        }else{
                            LocalDBHelper.getInstance().updateUnreadMessageCount(myUserId,String.format("%s%s",temp[0],temp[1]));
                            Intent intent5=new Intent("MessageService.local.message.data");
                            intent5.putExtra("status", RESULT_OK);
                            intent5.putExtra("messageModel", messageModel);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent5);
                        }
                        break;
                    case 6:// group information change
                        User group=(User) intent.getSerializableExtra("group");
                        mUser=group;
                        fUser=group;
                        String displayName=String.format("%s (%d)",mUser.getUserName(),mUser.getGender().split(",").length);// group name with user number
                        ((TextView) findViewById(R.id.userName)).setText(displayName);
                        if (incomeMessages!=null){
                            incomeMessages.add(messageModel);
                            chatMsgViewAdapter.notifyDataSetChanged();
                            incomeMessagesListView.smoothScrollToPosition(chatMsgViewAdapter.getCount()-1);
                        }
                        break;
                    default:break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver,new IntentFilter("MessageBox.local.message.data"));
        mDeviceId=Constants.DEVEICEIDVALUE;
        mSignature=Constants.SIGNATUREVALUE;
        myUserId=Constants.USERID;
        setContentView(R.layout.activity_message_box);
        initView();
        messageService.setInChat(true);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "demo");
    }

    private void initView(){
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        loadMoreView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.refreshable_listview_footer, null, false);
        incomeMessagesListView= (ListView) findViewById(R.id.incomeMessagesListView);
        incomeMessages=new ArrayList<>();
        chatMsgViewAdapter=new ChatMsgViewAdapter(MessageBoxActivity.this,incomeMessages);
        incomeMessagesListView.setAdapter(chatMsgViewAdapter);
        incomeMessagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(!theEnd && !loadingMore && view.getFirstVisiblePosition() == 0 && scrollState == SCROLL_STATE_IDLE){
                    loadingMore=true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new LoadMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new LoadMessageTask().execute();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        etSendmessage= (EditText) findViewById(R.id.et_sendmessage);
        btnSelectOther= (ImageView) findViewById(R.id.btn_selectOther);
        btnSend= (Button) findViewById(R.id.btn_send);
        String audioDirectory=Tools.checkAppDirectory("audio");
        if (audioDirectory!=null){
            audioFile=audioDirectory+File.separator+"temp.acc";
        }
        findViewById(R.id.recordBtn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        onCaptureAudio(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        onStopRecording();
                        return true;
                    default:return false;
                }
            }
        });
        etSendmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start==0&&count>0){
                    btnSelectOther.setVisibility(View.GONE);
                    btnSend.setVisibility(View.VISIBLE);
                }else if (start==0&&count==0){
                    btnSend.setVisibility(View.GONE);
                    btnSelectOther.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        GridLayout gridLayout= (GridLayout) findViewById(R.id.chatOptionalFunctions);
        ApplicationContextStore applicationContextStore=ApplicationContextStore.getInstance();
        ImageView selectPhotosBtn= (ImageView) ((ViewGroup) gridLayout.getChildAt(0)).getChildAt(0);
        selectPhotosBtn.setImageBitmap(applicationContextStore.getMESSAGECHOOSEPHOTOICON());
        ImageView selectVideosBtn= (ImageView) ((ViewGroup) gridLayout.getChildAt(1)).getChildAt(0);
        selectVideosBtn.setImageBitmap(applicationContextStore.getMESSAGECAMERAICON());
        Intent data=getIntent();
        mUser=(User) data.getSerializableExtra("user");
        position=data.getIntExtra("position",-1);
        switch (mUser.getId()){
            case -3:// event group
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    (new UpdateGroupChatDetail()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    (new UpdateGroupChatDetail()).execute();
                }
                break;
            case -5:// normal group
                ImageView chatPeopleInfo= (ImageView) findViewById(R.id.chat_people_info);
                chatPeopleInfo.setImageBitmap(ApplicationContextStore.getInstance().getCHATPEOPLEINFOICON());
                chatPeopleInfo.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    (new UpdateNormalGroupChatDetail()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    (new UpdateNormalGroupChatDetail()).execute();
                }
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    (new GetUserProfileTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    (new GetUserProfileTask()).execute();
                }
                break;
        }
    }

    private void initData(){
        if (fUser!=null&&incomeMessagesListView!=null){// check still in the page
            LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
            String displayName;
            if (mUser.getId()==-3){
                receiverUserId=String.format("event/%s/%s",mUser.getFirstName(),myUserId);//event id in firstname, current login userid in lastname
                displayName=mUser.getUserName();// event name as group name
                localDBHelper.resetUnreadMessageCount(myUserId,String.format("event%s",mUser.getFirstName()));
                chatMsgViewAdapter.setGroupFlag(true);
            }else if(mUser.getId()==-5){
                receiverUserId=String.format("group/%s/%s",mUser.getFirstName(),myUserId);//group id in firstname, current login userid in lastname
                displayName=String.format("%s (%d)",mUser.getUserName(),mUser.getGender().split(",").length);// group name with user number
                localDBHelper.resetUnreadMessageCount(myUserId,String.format("group%s",mUser.getFirstName()));
                chatMsgViewAdapter.setGroupFlag(true);
            }else{
                receiverUserId=String.valueOf(fUser.getId());
                displayName=Tools.isNullString(fUser.getFirstName())||Tools.isNullString(fUser.getLastName())?fUser.getUserName():String.format("%s %s", fUser.getFirstName(), fUser.getLastName());
                localDBHelper.resetUnreadMessageCount(myUserId,receiverUserId);
            }
            Log.d("MessageBoxActivity",String.format("myUserId %s receiverUserId %s displayName %s",myUserId,receiverUserId,displayName));
            ((TextView) findViewById(R.id.userName)).setText(displayName);
            lastMessagePosition=localDBHelper.getMessageCountWithUser(myUserId,receiverUserId);
            if (lastMessagePosition<limit){
                limit=lastMessagePosition;
                lastMessagePosition=0;
                theEnd=true;
            }else{
                lastMessagePosition-=limit;
            }
            incomeMessages.addAll(localDBHelper.getMessagesWithUser(myUserId, receiverUserId, limit, lastMessagePosition));
            chatMsgViewAdapter.notifyDataSetChanged();
            incomeMessagesListView.setSelection(incomeMessages.size()-1);
        }
    }

    private class LoadMessageTask extends AsyncTask<Void,Void,ArrayList<MessageModel>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            incomeMessagesListView.addHeaderView(loadMoreView);
        }

        @Override
        protected ArrayList<MessageModel> doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (lastMessagePosition<limit){
                limit=lastMessagePosition;
                lastMessagePosition=0;
                theEnd=true;
            }else{
                lastMessagePosition-=limit;
            }
            return LocalDBHelper.getInstance().getMessagesWithUser(myUserId, receiverUserId, limit, lastMessagePosition);
        }

        @Override
        protected void onPostExecute(ArrayList<MessageModel> messageModels) {
            super.onPostExecute(messageModels);
            incomeMessagesListView.removeHeaderView(loadMoreView);
            incomeMessages.addAll(0,messageModels);
            chatMsgViewAdapter.notifyDataSetChanged();
            incomeMessagesListView.setSelection(messageModels.size()-1);
            loadingMore=false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_messagebox, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MessageBoxActivity", "MessageBoxActivity on pause");
        messageService.setInChat(false);
        if (wakeLock.isHeld()){
            wakeLock.release();
        }
        try{
            if (mediaRecorder!=null){
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder=null;
            }
            isRecording.set(false);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MessageBoxActivity", "MessageBoxActivity on stop");
        messageService.setInChat(false);
        if (wakeLock.isHeld()){
            wakeLock.release();
        }
        try{
            if (mediaRecorder!=null){
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder=null;
            }
            isRecording.set(false);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        broadcastReceiver=null;
        super.onDestroy();
        Log.d("MessageBoxActivity", "MessageBoxActivity on destroy");
        messageService.setInChat(false);
        etSendmessage=null;
        chatMsgViewAdapter.releaseResource();
        loadMoreView=null;
        incomeMessages=null;
        chatMsgViewAdapter=null;
        incomeMessagesListView=null;
        loadMoreView=null;
        if (mediaRecorder!=null){
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder=null;
        }
        btnSelectOther=null;
        if (btnSend.getVisibility()==View.VISIBLE){
            btnSend.setVisibility(View.GONE);
        }
        btnSend=null;
        if (wakeLock.isHeld()){
            wakeLock.release();
        }
        if (mediaPlayer!=null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer=null;
        }
        wakeLock=null;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("MessageBoxActivity", "MessageBoxActivity on post resume");
        messageService.setInChat(true);
    }

    public void sendMessage(View view){
        if (Tools.isNullString(receiverUserId)){etSendmessage.setText("");return;}
        String contString = etSendmessage.getText().toString();
        if (contString.length() > 0) {
            try {
                messageService.send(myUserId,receiverUserId,contString,"chat",Tools.buildGroupChatMetaData(mUser,null));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private class SendImageTask extends  AsyncTask<Void, Void, JSONObject>{

        private ProgressDialog progressDialog;
        private Uri mPhotoUri;
        private String mMyUserId;
        private String mReceiverUserId;
        private User user;

        public SendImageTask(Uri photoUri,String myUserId,String receiverUserId,User mUser) {
            mPhotoUri = photoUri;
            mMyUserId = myUserId;
            mReceiverUserId = receiverUserId;
            user = mUser;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MessageBoxActivity.this, "sending...", "...", false, false);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            FileOutputStream out=null;
            Response response=null;
            try{
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(mPhotoUri, projection, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(column_index);
                    cursor.close();
                    BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
                    sampleOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, sampleOptions);
                    int inSampleSize = Tools.calculateInSampleSize(sampleOptions, Constants.Measuredwidth, 300);
                    sampleOptions.inJustDecodeBounds = false;
                    sampleOptions.inSampleSize = inSampleSize;
                    Bitmap takenImage = BitmapFactory.decodeFile(path, sampleOptions);
                    String tempImageFile=Tools.checkAppImageDirectory()+File.separator+"temp.png";
                    out = new FileOutputStream(tempImageFile);
                    takenImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.close();
                    takenImage.recycle();
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000,TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
                    RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", tempImageFile, RequestBody.create(MediaType.parse("text/plain"), new File(tempImageFile)))
                            .addFormDataPart("userId", mMyUserId)
                            .addFormDataPart(Constants.EQUIPID, mDeviceId)
                            .addFormDataPart(Constants.SIGNATURE, mSignature)
                            .build();
                    Request request=new Request.Builder().url(BuildConfig.API_WRITEHOST + "/photo/upload_file.php").post(formBody).build();
                    response=client.newCall(request).execute();
                    if (response.isSuccessful()){
                        return new JSONObject(response.body().string());
                    }
                }
            }catch(Throwable t){
                t.printStackTrace();
            }finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (response!=null){
                        response.body().close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            progressDialog.cancel();
            try{
                if (response==null){return;}
                if (!Constants.TAG_SUCCESS.equals(response.getString("status"))){
                    Toast.makeText(getApplicationContext(),"upload image fail",Toast.LENGTH_LONG).show();
                }else{
                    JSONObject fileUrls=response.getJSONObject("file");
                    JSONObject imageFiles=new JSONObject();
                    imageFiles.put("origin",fileUrls.getString("origin"));// in case of key changing from API
                    imageFiles.put("new",fileUrls.getString("new"));
                    MessageService.getInstance().send(mMyUserId,mReceiverUserId,imageFiles.toString(),"image",Tools.buildGroupChatMetaData(user,imageFiles));
                }
            }catch (Throwable t){
                t.printStackTrace();
                Toast.makeText(getApplicationContext(),"upload image fail",Toast.LENGTH_LONG).show();
            }
        }

    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode){
            case PICK_PICTURE_CODE_REQUEST_CODE:
                if (data!=null&&resultCode==RESULT_OK){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new SendImageTask(data.getData(),myUserId,receiverUserId,mUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new SendImageTask(data.getData(),myUserId,receiverUserId,mUser).execute();
                    }
                }
                break;
            case PICK_VIDEO_CODE_REQUEST_CODE:
                if (resultCode==RESULT_OK&&data!=null) {
                    String[] projection = {MediaStore.Video.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int column_index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        String videoPath = cursor.getString(column_index);
                        cursor.close();
                        String imageTmpDir=Tools.checkAppDirectory("image");
                        String videoTmpDir=Tools.checkAppDirectory("video");
                        if (imageTmpDir==null||videoPath==null){
                            Toast.makeText(MessageBoxActivity.this,"cannot get video information",Toast.LENGTH_LONG).show();
                            break;
                        }
                        Log.d("MessageBoxActivity",String.format("videoPath %s",videoPath));
                        String[] temp=videoPath.split("/");
                        String fileName=temp[temp.length-1].split("\\.")[0];
                        String output=videoTmpDir+File.separator+fileName+".mp4";
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                        OutputStream outStream = null;
                        File file = new File(imageTmpDir,fileName + ".jpg");
                        try {
                            outStream = new FileOutputStream(file);
                            thumb.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                            outStream.flush();
                            String thumbFileName=file.getAbsolutePath();
                            transCode(videoPath,output,thumbFileName);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }finally {
                            if (outStream!=null){
                                try {
                                    outStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class GetUserProfileTask extends AsyncTask<Void,Void,User>{

        @Override
        protected User doInBackground(Void... params) {
                if (mUser==null){return null;}
                Log.d("MessageBoxActivity","get user profile in background of MessageBoxActivity");
                return UserService.getInstance().getProfile(Constants.USERID,String.valueOf(mUser.getId()));
            }

        @Override
        protected void onPostExecute(User user) {
            try {
                if (user!=null){
                    LocalDBHelper.getInstance().updateRecentContact(user,myUserId);
                    LocalDBHelper.getInstance().insertUserProfile(user);
                    fUser=user;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (fUser==null){fUser=mUser;}
            initData();
        }
    }

    private class UpdateGroupChatDetail extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("eventIds[]", mUser.getFirstName())
                    .addFormDataPart(Constants.EQUIPID, mDeviceId)
                    .addFormDataPart(Constants.SIGNATURE, mSignature)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/event/get_event_list.php").post(formBody).build();
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
        protected void onPostExecute(String responseStr) {
            super.onPostExecute(responseStr);
            if (responseStr!=null){
                try {
                    JSONObject result=new JSONObject(responseStr);
                    if (Constants.TAG_SUCCESS.equals(result.getString("status"))){
                        JSONObject event=result.getJSONArray("events").getJSONObject(0);
                        JSONArray jsonArray=new JSONArray();
                        JSONArray joiners=event.getJSONArray("joiners");
                        int totalJoiners=joiners.length();
                        for (int i=0;i<totalJoiners;i++){
                            jsonArray.put(joiners.getJSONObject(i).getInt("id"));
                        }
                        User group=new User(event.getString("id"),jsonArray.toString(),-3,event.getString("userId"),event.getString("name"),event.getString("imagePath"),event.getString("thumbnailImageUrl"));
                        LocalDBHelper.getInstance().insertRecentGroupContact(group,myUserId);
                        LocalDBHelper.getInstance().insertUserProfile(group);
                        mUser=group;
                        fUser=group;
                        initData();
                    }else{
                        btnSelectOther.setEnabled(false);
                        btnSend.setEnabled(false);
                        findViewById(R.id.recordBtn).setEnabled(false);
                        findViewById(R.id.chat_people_info).setEnabled(false);
                        findViewById(R.id.btn_voiceSend).setEnabled(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UpdateNormalGroupChatDetail extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("groupId", mUser.getFirstName())
                    .addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE, Constants.SIGNATUREVALUE)
                    .build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/get_group_members.php").post(formBody).build();
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
        protected void onPostExecute(String responseStr) {
            super.onPostExecute(responseStr);
            if (responseStr==null){return;}
            try {
                JSONObject jsonObject=new JSONObject(responseStr);
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    JSONArray result=jsonObject.getJSONArray("result");
                    String ownerId=jsonObject.getString("ownerId");
                    User group=new User(mUser.getFirstName(),result.toString(),-5,ownerId,mUser.getUserName(),mUser.getAvatarOrigin(),mUser.getAvatarStandard());// update group member
                    LocalDBHelper.getInstance().insertUserProfile(group);
                    LocalDBHelper.getInstance().insertRecentChatGroupContact(group,myUserId);
                    mUser=group;
                    fUser=group;
                    initData();
                }else{
                    btnSelectOther.setEnabled(false);
                    btnSend.setEnabled(false);
                    findViewById(R.id.recordBtn).setEnabled(false);
                    findViewById(R.id.chat_people_info).setEnabled(false);
                    findViewById(R.id.btn_voiceSend).setEnabled(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * start record audio
     */
    private void onCaptureAudio(View view){
        if (audioFile==null){
            view.setPressed(false);
            Toast.makeText(MessageBoxActivity.this,"can not access audio",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)&&isRecording.compareAndSet(false,true)) {
                wakeLock.acquire();
                findViewById(R.id.btn_chatMessage).setEnabled(false);
//                Toast.makeText(MessageBoxActivity.this,"start recording audio",Toast.LENGTH_SHORT).show();
                mediaRecorder = new MediaRecorder();// Set the audio format and encoder
                mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                            Log.v("MessageBoxActivity","Maximum recording audio Duration Reached");
                            onStopRecording();
                        }
                    }
                });
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                // Setup the output location
                mediaRecorder.setOutputFile(audioFile);
                mediaRecorder.setMaxDuration(60000);
                // Start the recording
                mediaRecorder.prepare();
                mediaRecorder.start();
            } else { // no mic on device
                view.setPressed(false);
                Toast.makeText(this, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            isRecording.set(false);
            Toast.makeText(MessageBoxActivity.this,"can not access microphone",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            try{
                if (mediaRecorder!=null){
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder=null;
                }
            }catch (Throwable t){
                t.printStackTrace();
            }
            if (wakeLock.isHeld()){
                wakeLock.release();
            }
            view.setPressed(false);
            findViewById(R.id.btn_chatMessage).setEnabled(true);
        }
    }

    /**
     * Stop the recording of the audio
     */
    private void onStopRecording(){
        if (mediaRecorder!=null&&isRecording.compareAndSet(true,false)){
            if (wakeLock.isHeld()){
                wakeLock.release();
            }
//            Toast.makeText(MessageBoxActivity.this,"finish recording audio",Toast.LENGTH_SHORT).show();
            InputStream ios=null;
            String contentStr="";
            boolean tooShort=false;
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder=null;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(audioFile);
                int duration=Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                Log.d("MessageBoxActivity",String.format("record length %d",duration));
                if (duration<1100){
                    Toast.makeText(MessageBoxActivity.this,"too short for recording",Toast.LENGTH_SHORT).show();
                    tooShort=true;
                }else{
                    File temp=new File(audioFile);
                    byte[] buffer = new byte[(int) temp.length()];
                    ios = new FileInputStream(temp);
                    ios.read(buffer);
                    contentStr=Base64.encodeToString(buffer,Base64.DEFAULT);
                }
            }catch (Throwable t){
                tooShort=true;
                t.printStackTrace();
            }finally {
                try {
                    if (ios != null){
                        ios.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            findViewById(R.id.btn_chatMessage).setEnabled(true);
            if (!tooShort){
                try {
                    MessageService.getInstance().send(myUserId,receiverUserId,contentStr,"audio",Tools.buildGroupChatMetaData(mUser,null));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void transCode(String sourceFile,String outputFile,String thumbFileName){
        try{
            AndroidMediaObjectFactory factory = new AndroidMediaObjectFactory(getApplicationContext());
            org.m4m.MediaFileInfo mediaFileInfo=new org.m4m.MediaFileInfo(factory);
            mediaFileInfo.setUri(new org.m4m.Uri(sourceFile));
            org.m4m.AudioFormat audioFormat = (org.m4m.AudioFormat) mediaFileInfo.getAudioFormat();
            MediaProgressListener progressListener=new MediaProgressListener();
            org.m4m.MediaComposer mediaComposer = new org.m4m.MediaComposer(factory, progressListener);
            progressListener.setMediaComposer(mediaComposer);
            SendVideoTask sendVideoTask=new SendVideoTask(mUser,myUserId,outputFile,receiverUserId,thumbFileName);
            progressListener.setSendVideoTask(sendVideoTask);
            mediaComposer.addSourceFile(sourceFile);
            mediaComposer.setTargetFile(outputFile);
            VideoFormatAndroid videoFormat = new VideoFormatAndroid("video/avc", 480, 320);
            videoFormat.setVideoBitRateInKBytes(1000);
            videoFormat.setVideoFrameRate(30);
            videoFormat.setVideoIFrameInterval(1);
            mediaComposer.setTargetVideoFormat(videoFormat);
            AudioFormatAndroid aFormat = new AudioFormatAndroid("audio/mp4a-latm", audioFormat.getAudioSampleRateInHz(), audioFormat.getAudioChannelCount());
            aFormat.setAudioBitrateInBytes(98304);//96 * 1024
            aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mediaComposer.setTargetAudioFormat(aFormat);
            mediaComposer.start();
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    public void contentAction(View view){// item position also in content description
        CharSequence charSequence=view.getContentDescription();
        if (charSequence==null){return;}
        String currentPosition=charSequence.toString();
        MessageModel messageModel=incomeMessages.get(Integer.parseInt(currentPosition));
        if (messageModel==null){return;}
        String type=messageModel.getType();
        switch (type){
            case "audio":
                if (prevPosition!=null&&mediaPlayer!=null&&mediaPlayer.isPlaying()) {// click again would stop playing
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                if (currentPosition.equals(prevPosition)){
                    prevPosition=null;
                }else{
                    FileOutputStream stream = null;
                    prevPosition=currentPosition;
                    try {
                        String audioDirectory = Tools.checkAppDirectory("audio");
                        if (audioDirectory != null) {
                            String content=messageModel.getContent();
                            String audioFile;
                            if (content.startsWith("http")){
                                audioFile=content;
                            }else{
                                audioFile = audioDirectory + File.separator + "temp.acc";
                                stream = new FileOutputStream(audioFile);
                                stream.write(Base64.decode(content.getBytes(), Base64.DEFAULT));
                                stream.close();
                            }
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(audioFile);
                            mediaPlayer.prepare(); // must call prepare first
                            Log.d("MessageBoxActivity", String.format("chat audio length %d", mediaPlayer.getDuration()));
                            mediaPlayer.start(); // then start
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                    mediaPlayer=null;
                                    prevPosition=null;
                                }
                            });
                        } else {
                            Toast.makeText(MessageBoxActivity.this, "can not access audio", Toast.LENGTH_LONG).show();
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case "image":
                try {
                    Intent intent = new Intent(MessageBoxActivity.this, ShowBigImageActivity.class);
                    intent.putExtra("original", new JSONObject(messageModel.getContent()).getString("origin"));
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "chat":
                if (MyClipboardManager.copyToClipboard(MessageBoxActivity.this,((TextView)view).getText().toString())){
                    Toast.makeText(MessageBoxActivity.this,"copy To Clipboard",Toast.LENGTH_SHORT).show();
                }
                break;
            case "create_group":
                if (MyClipboardManager.copyToClipboard(MessageBoxActivity.this,((TextView)view).getText().toString())){
                    Toast.makeText(MessageBoxActivity.this,"copy To Clipboard",Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }


    public void selectOther(View view){
        View recordBtn=findViewById(R.id.recordBtn);
        if (recordBtn.getVisibility()==View.VISIBLE){
            recordBtn.setVisibility(View.GONE);
            findViewById(R.id.btn_chatMessage).setVisibility(View.GONE);
            findViewById(R.id.et_sendmessage).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_voiceSend).setVisibility(View.VISIBLE);
        }
        final GridLayout gridLayout= (GridLayout) findViewById(R.id.chatOptionalFunctions);
        if (gridLayout.getVisibility()==View.GONE){
            gridLayout.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            gridLayout.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public void selectPhotos(View view){
        Intent intent5 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// Create intent for picking a photo from the gallery
        startActivityForResult(intent5, PICK_PICTURE_CODE_REQUEST_CODE);// Bring up gallery to select a photo
    }

    public void selectVideos(View view){
        Intent intent5 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);// Create intent for picking a Video from the gallery
        startActivityForResult(intent5, PICK_VIDEO_CODE_REQUEST_CODE);// Bring up gallery to select a Video
    }

    public void videoAction(View view){
        view.setClickable(false);
        String currentPosition=view.getContentDescription().toString();
        MessageModel messageModel=incomeMessages.get(Integer.parseInt(currentPosition));
        RelativeLayout videoViewContainer=(RelativeLayout)view;
        try {
            final VideoView videoView= (VideoView) videoViewContainer.getChildAt(0);
            videoView.setVisibility(View.VISIBLE);
            String originalUrl = new JSONObject(messageModel.getContent()).getString("origin");
            Log.d("MessageBoxActivity", String.format("originalUrl %s", originalUrl));
            videoView.setVideoPath(originalUrl);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        view.setClickable(true);
    }

    public void voiceSendAction(View view){
        GridLayout gridLayout= (GridLayout) findViewById(R.id.chatOptionalFunctions);
        if (gridLayout.getVisibility()==View.VISIBLE){
            gridLayout.setVisibility(View.GONE);
        }
        view.setVisibility(View.GONE);
        findViewById(R.id.et_sendmessage).setVisibility(View.GONE);
        findViewById(R.id.btn_chatMessage).setVisibility(View.VISIBLE);
        findViewById(R.id.recordBtn).setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void chatMessageAction(View view){
        view.setVisibility(View.GONE);
        findViewById(R.id.recordBtn).setVisibility(View.GONE);
        findViewById(R.id.et_sendmessage).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_voiceSend).setVisibility(View.VISIBLE);
    }

    public void chatPeopleInfo(View view){
        Intent intent = new Intent(MessageBoxActivity.this, GroupChatInfoActivity.class);
        intent.putExtra("user",mUser);
        intent.putExtra("myUserId",myUserId);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        final GridLayout gridLayout= (GridLayout) findViewById(R.id.chatOptionalFunctions);
        if (gridLayout.getVisibility()==View.VISIBLE){
            gridLayout.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            gridLayout.setVisibility(View.GONE);
                        }
                    });
        }else{
            Intent data=new Intent();
            data.putExtra("position",position);
            setResult(RESULT_OK, data);
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position",position);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position=savedInstanceState.getInt("position",-1);
    }


}
