package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.MessageService;
import com.troopar.trooparapp.model.User;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditGroupProfileActivity extends AppCompatActivity {

    private EditText editText;
    private User group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_profile);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Intent data=getIntent();
        group= (User) data.getSerializableExtra("group");
        String originalName=group.getUserName();
        editText= (EditText) findViewById(R.id.groupName);
        editText.setText(originalName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveAction(View view){
        String groupName=editText.getText().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new UpdateGroupInfoTask(groupName,group).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            new UpdateGroupInfoTask(groupName,group).execute();
        }
    }

    private class UpdateGroupInfoTask extends AsyncTask<Void,Void,String>{

        private String mGroupName;
        private User mGroup;

        public UpdateGroupInfoTask(String groupName, User group) {
            mGroupName = groupName;
            mGroup=group;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("userId",Constants.USERID).addFormDataPart(Constants.EQUIPID,Constants.DEVEICEIDVALUE)
                    .addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE).addFormDataPart("name",mGroupName).addFormDataPart("groupId",mGroup.getFirstName())
                    .addFormDataPart("ownerId",mGroup.getLastName()).build();
            Request request=new Request.Builder().url(BuildConfig.API_READHOST + "/group/edit_group.php").post(formBody).build();
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s!=null){
                try {
                    JSONObject result=new JSONObject(s);
                    if (Constants.TAG_SUCCESS.equals(result.getString("status"))){
                        MessageService.getInstance().send(Constants.USERID,String.format("group/%s/%s",mGroup.getFirstName(),Constants.USERID),mGroupName,"change_group_name", Tools.buildGroupChatMetaData(mGroup,null));
                        onBackPressed();
                    }else{
                        Toast.makeText(EditGroupProfileActivity.this,result.getString("message"),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
