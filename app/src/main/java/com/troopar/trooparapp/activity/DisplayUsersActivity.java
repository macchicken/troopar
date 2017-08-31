package com.troopar.trooparapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.UserService;
import com.troopar.trooparapp.adapter.DisplayUsersViewAdapter;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;

import java.util.ArrayList;

public class DisplayUsersActivity extends AppCompatActivity {

    private ArrayList<User> users;
    private User myUser;
    private String profileType;
    private String mEquipId;
    private String mSignature;
    private int count;
    private int total;
    private int lastVisibleItem,mTotalItemCount;
    private boolean loadingMore;
    private View loadMoreView;
    private DisplayUsersViewAdapter usersViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_users);
        initView();
    }

    private void initView(){
        Intent mData=getIntent();
        ListView userListView= (ListView) findViewById(R.id.userList);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        users=(ArrayList<User>) mData.getSerializableExtra("userArrayList");
        profileType=mData.getStringExtra("profileType");
        total=mData.getIntExtra("total",-1);
        usersViewAdapter=new DisplayUsersViewAdapter(DisplayUsersActivity.this,users);
        userListView.setAdapter(usersViewAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DisplayUsersActivity.this,UserDetailActivity.class);
                User user=users.get(position);
                intent.putExtra("user", user);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        if (profileType!=null&&total>10){// 10 is limit for each results defined in each server query
            myUser= (User) mData.getSerializableExtra("myUser");
            mEquipId=Constants.DEVEICEIDVALUE;
            mSignature=Constants.SIGNATUREVALUE;
            count+=users.size();
            loadMoreView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.refreshable_listview_footer, null, false);
            ((ListView) findViewById(R.id.userList)).addFooterView(loadMoreView);
            userListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if(lastVisibleItem == mTotalItemCount && scrollState == SCROLL_STATE_IDLE && !loadingMore){
                        loadingMore=true;
                        ((ListView) findViewById(R.id.userList)).addFooterView(loadMoreView);
                        GetUserProfileWithType task=new GetUserProfileWithType();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            task.execute();
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastVisibleItem = firstVisibleItem + visibleItemCount;
                    mTotalItemCount=totalItemCount;
                }
            });
        }
        TextView textView= (TextView) findViewById(R.id.toolbar_logo);
        textView.setText("follows".equals(profileType)?"Following":"Followers");
    }

    private class GetUserProfileWithType extends AsyncTask<Void,Void,ArrayList> {

        @Override
        protected ArrayList doInBackground(Void... params) {
            return UserService.getInstance().getUserProfileWithType(myUser,profileType,count,10,mEquipId,mSignature);
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            try {
                ListView userList=((ListView) findViewById(R.id.userList));
                if (userList!=null){
                    userList.removeFooterView(loadMoreView);
                    if (result==null){
                        loadingMore=false;
                        return;
                    }
                    int tTotal=result.size();
                    users.addAll(result);
                    count+=tTotal;
                    usersViewAdapter.notifyDataSetChanged();
                    loadingMore = count >= total;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        users=null;
        usersViewAdapter.clearUsers();
        usersViewAdapter=null;
        loadMoreView=null;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mOwnData",users);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        users= (ArrayList<User>) savedInstanceState.getSerializable("mOwnData");
    }


}
