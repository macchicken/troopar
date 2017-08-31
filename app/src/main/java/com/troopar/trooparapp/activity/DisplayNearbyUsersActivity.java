package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.NearybyUsersAdapter;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;

import java.util.ArrayList;

public class DisplayNearbyUsersActivity extends AppCompatActivity {

    private ArrayList<User> userArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_nearby_users);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        RecyclerView userList = (RecyclerView) findViewById(R.id.userList);
        userList.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        userList.setLayoutManager(staggeredGridLayoutManager);
        userArrayList= (ArrayList<User>) getIntent().getSerializableExtra("userArrayList");
        NearybyUsersAdapter nearbyUsersAdapter=new NearybyUsersAdapter(userArrayList,R.layout.griduser_item_view,100,true);
        userList.setAdapter(nearbyUsersAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void userDetailAction(View view){
        String position=view.getContentDescription().toString();
        Intent intent = new Intent(DisplayNearbyUsersActivity.this,UserDetailActivity.class);
        User user=userArrayList.get(Integer.parseInt(position));
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userArrayList=null;
    }


}
