package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.adapter.AdminMessagesAdapter;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.utils.ApplicationContextStore;

import java.util.ArrayList;

public class AdminMessagesActivity extends AppCompatActivity {

    private String messageType;
    private String messageTitle;
    private RecyclerView listView;
    private ArrayList<MessageModel> messageModels;
    private AdminMessagesAdapter adminMessagesAdapter;
    private long counter;
    private long limit=10;
    private boolean theEnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LocalDBHelper localDBHelper=LocalDBHelper.getInstance();
        setContentView(R.layout.activity_admin_messages);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarLogo= (TextView) findViewById(R.id.toolbar_logo);
        Intent data=getIntent();
        messageType=data.getStringExtra("messageType");
        messageTitle=data.getStringExtra("messageTitle");
        final String myUserId=data.getStringExtra("myUserId");
        toolbarLogo.setText(messageTitle);
        listView = (RecyclerView) findViewById(R.id.listView);
        messageModels=new ArrayList<>();
        listView.setLayoutManager(new LinearLayoutManager(AdminMessagesActivity.this));
        adminMessagesAdapter=new AdminMessagesAdapter(messageModels,listView);
        adminMessagesAdapter.setOnLoadMoreListener(new AdminMessagesAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (counter<limit){
                    limit=counter;
                    counter=0;
                    theEnd=true;
                }else{
                    counter-=limit;
                }
                messageModels.addAll(localDBHelper.getAdminMessagesWithUser(myUserId, messageType, limit, counter));
                adminMessagesAdapter.notifyDataSetChanged();
                adminMessagesAdapter.setLoaded();
                if (theEnd){
                    adminMessagesAdapter.setNoMore(true);
                }
            }
        });
        listView.setAdapter(adminMessagesAdapter);
        counter=localDBHelper.getAdminMessagesCountWithUser(myUserId,messageType);
        if (counter<limit){
            limit=counter;
            counter=0;
            theEnd=true;
            adminMessagesAdapter.setNoMore(true);
        }else{
            counter-=limit;
        }
        adminMessagesAdapter.setLoading();
        messageModels.addAll(localDBHelper.getAdminMessagesWithUser(myUserId, messageType, limit, counter));
        adminMessagesAdapter.notifyDataSetChanged();
        adminMessagesAdapter.setLoaded();
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
    public void onBackPressed() {
        Intent data=new Intent();
        data.putExtra("messageType",messageType);
        data.putExtra("messageTitle",messageTitle);
        setResult(RESULT_OK,data);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adminMessagesAdapter.setOnLoadMoreListener(null);
        RecyclerView.RecycledViewPool recycledViewPool=listView.getRecycledViewPool();
        recycledViewPool.clear();
        messageModels=null;
        adminMessagesAdapter=null;
        listView.removeAllViews();
    }


}
