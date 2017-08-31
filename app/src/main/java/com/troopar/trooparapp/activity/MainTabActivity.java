package com.troopar.trooparapp.activity;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.MyAppSharePreference;
import com.troopar.trooparapp.utils.ScreenTools;
import com.troopar.trooparapp.utils.Tools;

public class MainTabActivity extends TabActivity {

    private TabHost tabHost;
    private View prevSelectedTv;
    private String prevId;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainTabActivity","MainTabActivity on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        tabHost = getTabHost();
        setTabs();
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d("MainTabActivity", "onchange tab id " + tabId);
                if (prevSelectedTv instanceof RelativeLayout) {
                    prevSelectedTv.getBackground().setColorFilter(Color.parseColor("#88ffffff"), PorterDuff.Mode.MULTIPLY);
                } else {
                    ((ImageView) prevSelectedTv).setColorFilter(Color.parseColor("#88ffffff"), PorterDuff.Mode.MULTIPLY);
                }
                if ("tabMyTroopar".equals(tabId)) {
                    RelativeLayout view = (RelativeLayout) tabHost.getCurrentTabView().findViewById(R.id.tabIcon);
                    view.getBackground().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
                    prevSelectedTv = view;
                    prevId = tabId;
                } else {
                    ImageView view = (ImageView) tabHost.getCurrentTabView().findViewById(R.id.tabIcon);
                    view.setColorFilter(Color.parseColor("#ffffff"));
                    prevSelectedTv = view;
                    prevId = tabId;
                }
            }
        });
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent!=null&&intent.getIntExtra("status",RESULT_CANCELED)==RESULT_OK){
                    TextView textView= (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(R.id.unreadMessageCount);
					if (intent.getBooleanExtra("initUnreadCount",false)){
						int totalUnread=LocalDBHelper.getInstance().readUnreadTotal(Constants.USERID);
						if (totalUnread>0){
							if (textView.getVisibility()==View.GONE){
								textView.setVisibility(View.VISIBLE);
							}
							textView.setText(String.valueOf(totalUnread));
						}
					}else{
						if (intent.getBooleanExtra("increment",false)){
							if (textView.getVisibility()==View.GONE){
								textView.setVisibility(View.VISIBLE);
							}
							textView.setText(String.valueOf(Integer.parseInt(textView.getText().toString())+1));
						}else{
							if (textView.getVisibility()==View.VISIBLE){
								textView.setVisibility(View.GONE);
							}
							textView.setText("0");
						}
					}
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver,new IntentFilter("TrooparGcmListenerService.local.message.data"));
        if (!Tools.isNullString(Constants.USERID)){
            MessageService.getInstance().connect(Constants.USERID);
            int totalUnread=LocalDBHelper.getInstance().readUnreadTotal(Constants.USERID);
            if (totalUnread>0){
                TextView textView= (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(R.id.unreadMessageCount);
                if (textView.getVisibility()==View.GONE){
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setText(String.valueOf(totalUnread));
            }
        }
        Constants.NOTINAPP=false;
    }

    private void setTabs(){
        prevId = "tabHome";
        addTab("Home", R.layout.tab_indicator, R.drawable.home_icon, NearbyActivity.class);//index 0
        addTab("Search", R.layout.tab_indicator, R.drawable.search_icon, MyFindMapEventActivity.class);//index 1
        addButtonToTab("MyTroopar",R.drawable.chat_icon, TroopActivity.class);
        addTab("Activity", R.layout.tab_indicator, R.drawable.discover_icon, MyActivity.class);//index 3
        addTab("More", R.layout.tab_indicator, R.drawable.people_icon, MoreActivity.class);//index 4
        prevSelectedTv=tabHost.getCurrentTabView().findViewById(R.id.tabIcon);
    }

    private void addTab(String labelId, int layoutResource, int drawableId, Class<?> c)
    {
        TabWidget t=tabHost.getTabWidget();
        t.setDividerDrawable(null);
        Intent intent = new Intent(MainTabActivity.this, c);
        TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);
        View tabIndicator = LayoutInflater.from(MainTabActivity.this).inflate(layoutResource, t, false);
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.tabIcon);
        icon.setImageResource(drawableId);
        if (!labelId.equals("Home")) {
            icon.setColorFilter(Color.parseColor("#88ffffff"), PorterDuff.Mode.MULTIPLY);
        }
        spec.setIndicator(tabIndicator);
        spec.setContent(intent);
        tabHost.addTab(spec);
    }

    private void addButtonToTab(String labelId,int drawableId,Class<?> c){
        TabWidget t=tabHost.getTabWidget();
        t.setDividerDrawable(null);
        TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);
        View tabIndicator = LayoutInflater.from(MainTabActivity.this).inflate(R.layout.tab_indicator_withcounter, t, false);
        RelativeLayout icon = (RelativeLayout) tabIndicator.findViewById(R.id.tabIcon);
        icon.setBackgroundResource(drawableId);
        icon.getBackground().setColorFilter(Color.parseColor("#88ffffff"), PorterDuff.Mode.MULTIPLY);
        spec.setIndicator(tabIndicator);
        spec.setContent(new Intent(MainTabActivity.this, c));
        tabHost.addTab(spec);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("MainTabActivity","MainTabActivity on post resume");
        Constants.NOTINAPP=false;
    }

    @Override
    protected void onStop() {
        Log.d("MainTabActivity","MainTabActivity on stop");
        super.onStop();
        Constants.NOTINAPP=true;
    }

    @Override
    protected void onPause() {
        Log.d("MainTabActivity","MainTabActivity on pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainTabActivity", "MainTabActivity on destroy");
        if (!"".equals(Constants.USERID)){
            MessageService.getInstance().disconnect(Constants.USERID);
        }
        prevSelectedTv=null;
        tabHost.clearAllTabs();
        tabHost=null;
        if (broadcastReceiver!=null){
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
            broadcastReceiver=null;
        }
        ScreenTools.getInstance().setMContext(null);
        MyAppSharePreference.getInstance().releaseSharePreference();
        ImageDownloader.getInstance().releaseContext();
        Constants.NOTINAPP=true;
        Runtime.getRuntime().gc();
    }

    public int getPrevIndex() {
        switch (prevId) {
            case "tabHome":
                return 0;
            case "tabSearch":
                return 1;
            case "tabActivity":
                return 3;
            case "tabMore":
                return 4;
            default:
                return 2;
        }
    }


}
