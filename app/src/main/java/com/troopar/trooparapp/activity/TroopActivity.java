package com.troopar.trooparapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.adapter.UsersViewAdapter;
import com.troopar.trooparapp.model.MessageModel;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import java.io.Serializable;
import java.util.ArrayList;

public class TroopActivity extends AppCompatActivity {

    private final int CHATTING_REQUEST_CODE=621;
    private final int ADMIN_MESSAGE_REQUEST_CODE=640;
    private ListView userListView;
    private BroadcastReceiver broadcastReceiver;
    private UsersViewAdapter usersViewAdapter;
    private ArrayList<User> users;
    private LocalDBHelper localDBHelper;
    private int mSetUsers;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_chat);
        localDBHelper=LocalDBHelper.getInstance();
        initView();
    }

    private void initView(){
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userListView= (ListView) findViewById(R.id.userList);
        userId= Constants.USERID;
        if (!Tools.isNullString(userId)){
            Intent intent3=new Intent("TrooparGcmListenerService.local.message.data");
            intent3.putExtra("status", RESULT_OK);
            intent3.putExtra("increment",false);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent3);
            localDBHelper.resetTotalUnreadMessageCount(userId);
            users=localDBHelper.getRecentContacts(userId);
            usersViewAdapter=new UsersViewAdapter(TroopActivity.this,users);
            usersViewAdapter.setLocalDBHelper(localDBHelper);
            userListView.setAdapter(usersViewAdapter);
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(TroopActivity.this,MessageBoxActivity.class);
                    User user=users.get(position);
                    intent.putExtra("user", user);
                    intent.putExtra("position", position);
                    startActivityForResult(intent, CHATTING_REQUEST_CODE);
                }
            });
            usersViewAdapter.notifyDataSetChanged();
        }else{
			View adminMessageContainer=findViewById(R.id.adminMessageContainer);
			if (adminMessageContainer.getVisibility()==View.VISIBLE){
				adminMessageContainer.setVisibility(View.GONE);
			}
		}
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent!=null&&intent.getIntExtra("status",0)==RESULT_OK){
                    int setUsers=intent.getIntExtra("setUsers",-1);
                    Log.d("TroopActivity",String.format("broadcastReceiver setUsers: %d",setUsers));
                    switch (setUsers){
                        case 1:
                            mSetUsers=setUsers;
                            localDBHelper=LocalDBHelper.getInstance();
                            break;
                        case 3:
                            mSetUsers=setUsers;
                            if (users==null){break;}
                            users.clear();
                            users=null;
                            userId=null;
                            usersViewAdapter.setLocalDBHelper(null);
                            usersViewAdapter.notifyDataSetChanged();
                            usersViewAdapter=null;
                            userListView.setAdapter(null);
                            userListView.setOnItemClickListener(null);
                            userListView.invalidateViews();
                            userListView.refreshDrawableState();
                            localDBHelper=LocalDBHelper.getInstance();
                            View adminMessageContainer=findViewById(R.id.adminMessageContainer);
                            if (adminMessageContainer.getVisibility()==View.VISIBLE){
                                adminMessageContainer.setVisibility(View.GONE);
                            }
                            break;
                        case 7:
                            MessageModel messageModel7= (MessageModel) intent.getSerializableExtra("messageModel");
                            if ("comment".equals(messageModel7.getType())){
                                TextView view= (TextView) findViewById(R.id.commentUnreadMessageCount);
                                if (view.getVisibility()==View.GONE){
                                    view.setVisibility(View.VISIBLE);
                                }
                                int uCount=Integer.parseInt(view.getText().toString());
                                view.setText(String.valueOf(++uCount));
                            }else if ("like".equals(messageModel7.getType())){
                                TextView view= (TextView) findViewById(R.id.likeUnreadMessageCount);
                                if (view.getVisibility()==View.GONE){
                                    view.setVisibility(View.VISIBLE);
                                }
                                int uCount=Integer.parseInt(view.getText().toString());
                                view.setText(String.valueOf(++uCount));
                            }
                            break;
                        default:
                            if (users==null||userId==null){break;}
                            MessageModel messageModel= (MessageModel) intent.getSerializableExtra("messageModel");
                            Log.d("TroopActivity",String.format("message id %s, content %s",messageModel.getId(),messageModel.getContent()));
                            String chatType=messageModel.getType();
                            String from=messageModel.getFrom();
                            if ("create_group".equals(chatType)){
                                User group= (User) intent.getSerializableExtra("group");
                                users.add(group);
                                usersViewAdapter.setPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()),users.size()-1);
                                usersViewAdapter.notifyDataSetChanged();
                                return;
                            }else if ("group_invite".equals(chatType)){
                                User group= (User) intent.getSerializableExtra("group");
                                Integer position=usersViewAdapter.getPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()));
                                if (position!=null){
                                    users.set(position,group);//indicate joining an normal group chat, group users in the gender attribute
                                }else{
                                    users.add(group);//indicate joining an normal group chat, group users in the gender attribute
                                    usersViewAdapter.setPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()),users.size()-1);
                                }
                                usersViewAdapter.notifyDataSetChanged();
                                return;
                            }else if ("group_quit".equals(chatType)){
                                User group= (User) intent.getSerializableExtra("group");
                                if (intent.getBooleanExtra("deleted",false)){
                                    users.remove(group);
                                }else{
                                    Integer position=usersViewAdapter.getPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()));
                                    if (position!=null){
                                        users.set(position,group);//indicate joining an normal group chat, group users in the gender attribute
                                    }else{
                                        users.add(group);//indicate joining an normal group chat, group users in the gender attribute
                                        usersViewAdapter.setPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()),users.size()-1);
                                    }
                                }
                                usersViewAdapter.notifyDataSetChanged();
                                return;
                            }else if ("change_group_name".equals(chatType)){
                                User group= (User) intent.getSerializableExtra("group");
                                Integer position=usersViewAdapter.getPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()));
                                if (position!=null){
                                    users.set(position,group);//indicate joining an normal group chat, group users in the gender attribute
                                }else{
                                    users.add(group);//indicate joining an normal group chat, group users in the gender attribute
                                    usersViewAdapter.setPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",group.getFirstName()),users.size()-1);
                                }
                                usersViewAdapter.notifyDataSetChanged();
                                return;
                            }
                            Integer position;
                            if (messageModel.isGrouped()){
                                String[] temp=from.split("/");
                                position=usersViewAdapter.getPositionWithId(String.format(from.startsWith("group")?"group%s":"event%s",temp[1]));
                            }else{
                                if (userId.equals(messageModel.getFrom())){// a message from my user login in other devices
                                    position=usersViewAdapter.getPositionWithId(messageModel.getTo());
                                }else{
                                    position=usersViewAdapter.getPositionWithId(messageModel.getFrom());
                                }
                            }
                            if (position!=null){
                                TextView textView= (TextView) userListView.getChildAt(position).findViewById(R.id.unreadMessageCount);
                                if (textView.getVisibility()==View.GONE){
                                    textView.setVisibility(View.VISIBLE);
                                }
                                textView.setText(String.valueOf(Integer.parseInt(textView.getText().toString())+1));
                            }
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver,new IntentFilter("MessageService.local.message.data"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHATTING_REQUEST_CODE:
                int position=data.getIntExtra("position",-1);
                if (position!=-1){
                    users.get(position).setUnreadMessageCount(0);
                    View item=userListView.getChildAt(position);
                    if (item!=null){
                        TextView textView= (TextView) item.findViewById(R.id.unreadMessageCount);
                        textView.setText("0");
                        textView.setVisibility(View.GONE);
                    }
                }
                localDBHelper.resetTotalUnreadMessageCount(userId);
                Intent intent3=new Intent("TrooparGcmListenerService.local.message.data");
                intent3.putExtra("status", RESULT_OK);
                intent3.putExtra("increment",false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent3);
                break;
            case ADMIN_MESSAGE_REQUEST_CODE:
                String messageTitle=data.getStringExtra("messageTitle");
                if ("Comments".equals(messageTitle)){
                    TextView view= (TextView) findViewById(R.id.commentUnreadMessageCount);
                    if (view.getVisibility()==View.VISIBLE){
                        view.setVisibility(View.GONE);
                        view.setText("0");
                    }
                }else if ("Likes".equals(messageTitle)){
                    TextView view= (TextView) findViewById(R.id.likeUnreadMessageCount);
                    if (view.getVisibility()==View.VISIBLE){
                        view.setVisibility(View.GONE);
                        view.setText("0");
                    }
                }
                break;
            default:break;
        }
    }

    public void viewAdminCommentsAction(View view){
        if (Tools.isNullString(Constants.USERID)){
            return;
        }
        Intent intent=new Intent(TroopActivity.this,AdminMessagesActivity.class);
        intent.putExtra("messageType","comment");
        intent.putExtra("messageTitle","Comments");
        intent.putExtra("myUserId",Constants.USERID);
        startActivityForResult(intent,ADMIN_MESSAGE_REQUEST_CODE);
    }

    public void viewAdminLikeAction(View view){
        if (Tools.isNullString(Constants.USERID)){
            return;
        }
        Intent intent=new Intent(TroopActivity.this,AdminMessagesActivity.class);
        intent.putExtra("messageType","like");
        intent.putExtra("messageTitle","Likes");
        intent.putExtra("myUserId",Constants.USERID);
        startActivityForResult(intent,ADMIN_MESSAGE_REQUEST_CODE);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mSetUsers==1){
            mSetUsers=-1;
            userId=Constants.USERID;
			Intent intent3=new Intent("TrooparGcmListenerService.local.message.data");
            intent3.putExtra("status", RESULT_OK);
            intent3.putExtra("increment",false);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent3);
            localDBHelper.resetTotalUnreadMessageCount(userId);
            users=localDBHelper.getRecentContacts(userId);
            usersViewAdapter=new UsersViewAdapter(TroopActivity.this,users);
            userListView.setAdapter(usersViewAdapter);
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(TroopActivity.this,MessageBoxActivity.class);
                    User user=users.get(position);
                    intent.putExtra("user", user);
                    intent.putExtra("position", position);
                    startActivityForResult(intent, CHATTING_REQUEST_CODE);
                }
            });
			usersViewAdapter.setLocalDBHelper(localDBHelper);
            usersViewAdapter.notifyDataSetChanged();
            View view=findViewById(R.id.adminMessageContainer);
            if (view.getVisibility()==View.GONE){
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        Log.d("TroopActivity","TroopActivity on destroy");
        super.onDestroy();
        if (broadcastReceiver!=null){
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        }
		users=null;
		if (usersViewAdapter!=null){
			usersViewAdapter.clearUsers();
			usersViewAdapter.setLocalDBHelper(null);
			usersViewAdapter=null;
		}
        userListView.setAdapter(null);
        userListView.setOnItemClickListener(null);
        userListView=null;
        broadcastReceiver=null;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_troop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_createGroup:
                if (userId==null||Tools.isNullString(Constants.USERID)) {
                    return true;
                }
                Intent intent = new Intent(TroopActivity.this,CreateGroupActivity.class);
                intent.putExtra("myUserId",userId);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (users!=null){
            outState.putSerializable("mOwnData",users);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable object=savedInstanceState.getSerializable("mOwnData");
        if (object!=null){
            users= (ArrayList<User>) object;
        }
    }


}
