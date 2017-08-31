package com.troopar.trooparapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.meg7.widget.CircleImageView;
import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.MyAppSharePreference;
import com.troopar.trooparapp.utils.Tools;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UsersRegActivity extends AppCompatActivity {

    private String url= BuildConfig.API_READHOST+"/user/registration.php";
    private TextView userNameInput;
    private TextView userPasswordInput;
    private TextView userRetypePasswordInput;
    private TextView emailAddressInput;
    private CircleImageView userImage;
    private String deviceId;
    private String photoFileName;
    private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=609;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("UsersRegActivity", "UsersRegActivity on create");
        setContentView(R.layout.activity_users_reg);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_newuser_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userNameInput = (TextView) findViewById(R.id.userNameInput);
        userPasswordInput = (TextView) findViewById(R.id.userPasswordInput);
        userRetypePasswordInput = (TextView) findViewById(R.id.userRetypePasswordInput);
        emailAddressInput = (TextView) findViewById(R.id.emailAddress);
        userImage= (CircleImageView) findViewById(R.id.userImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_newuser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void submitNewUserInfo(View v){
        String userPassword=userPasswordInput.getText().toString();
        String userRetypePassword=userRetypePasswordInput.getText().toString();
        String username=userNameInput.getText().toString();
        String email=emailAddressInput.getText().toString();
        if (Tools.isNullString(userPassword)){
            Toast.makeText(UsersRegActivity.this, " enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Tools.isNullString(userRetypePassword)){
            Toast.makeText(UsersRegActivity.this, " re enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Tools.isNullString(username)){
            Toast.makeText(UsersRegActivity.this, " enter username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Tools.isNullString(email)){
            Toast.makeText(UsersRegActivity.this, " enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!userPassword.equals(userRetypePassword)){
            Toast.makeText(UsersRegActivity.this, " both password not the same", Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (tm!=null){
            try{
                deviceId=tm.getDeviceId();
            }catch(SecurityException se){
                Toast.makeText(UsersRegActivity.this, "enable access phone to register with your device", Toast.LENGTH_SHORT).show();
            }
        }
        if (deviceId==null){return;}
        AsyncTask<Void, Void, JSONObject> resultTask=submitNewUserInfo(username,userPassword,email);
        if (resultTask!=null){
            try {
                JSONObject result=resultTask.get();// wait until successfully registration
                if (result!=null){
                    String status = (String) result.get("status");
                    String message = (String) result.get("message");
                    if (Constants.TAG_SUCCESS.equals(status)){
                        JSONObject user=result.getJSONObject("user");
                        Intent data=new Intent();
                        data.putExtra("userid",String.valueOf(user.getInt("id")));
                        data.putExtra("username", user.getString("username"));
                        if (!Tools.isNullString(photoFileName)){
                            Constants.PHOTOFILENAME=photoFileName;
                            MyAppSharePreference.getInstance().saveStringValue(String.valueOf(user.getInt("id")) + user.getString("username"), photoFileName);
                        }
                        setResult(RESULT_OK,data);
                        onBackPressed();
                    }else{
                        if (message!=null){
                            Toast.makeText(UsersRegActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(UsersRegActivity.this, "user already exists", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException | JSONException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private AsyncTask<Void, Void, JSONObject> submitNewUserInfo(final String username,final String userPassword,final String email){
        Log.d("UsersRegActivity","submitNewUserInfo");
        AsyncTask<Void, Void, JSONObject> task = new AsyncTask<Void, Void, JSONObject>(){
            @Override
            protected JSONObject doInBackground(Void... params) {
                Response response=null;
                try {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
                    MultipartBody.Builder builder= new MultipartBody.Builder().setType(MultipartBody.FORM);
                    builder.addFormDataPart("username",username).addFormDataPart("email",email).addFormDataPart("password",Tools.sha1OfStr(userPassword)).addFormDataPart(Constants.EQUIPID,deviceId).addFormDataPart(Constants.SIGNATURE,Tools.sha1OfStr(deviceId + "ANZStudio"));
                    if (!Tools.isNullString(photoFileName)){
                        String fileFullPath=Tools.checkAppImageDirectory()+File.separator+photoFileName;
                        builder.addFormDataPart("avatar",fileFullPath,RequestBody.create(MediaType.parse("text/plain"), new File(fileFullPath)));
                    }
                    Request request=new Request.Builder().url(url).post(builder.build()).build();
                    response=client.newCall(request).execute();
                    if (response.isSuccessful()){
                        return new JSONObject(response.body().string());
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (response!=null){
                        response.body().close();
                    }
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }

    private AsyncTask<Void, Void, JSONObject> runAsyncTask(AsyncTask<Void, Void, JSONObject> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
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

    public void captureUserImage(View v){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFileName = UUID.randomUUID().toString() + ".jpg";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri());
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private Uri getFileUri(){
        return Uri.fromFile(new File(Tools.checkAppImageDirectory()+File.separator+photoFileName));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode==RESULT_OK){
                    loadUserImage();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadUserImage(){
        Log.d("UsersRegActivity","load user image "+photoFileName);
        FileOutputStream out=null;
        try{
            String imagePath=getFileUri().getPath();
            BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
            sampleOptions.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(imagePath,sampleOptions);
            int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
            sampleOptions.inJustDecodeBounds=false;
            sampleOptions.inSampleSize=inSampleSize;
            Bitmap takenImage=BitmapFactory.decodeFile(imagePath,sampleOptions);
            photoFileName=String.format("%s%s%s_compress.jpg",Tools.checkAppImageDirectory(),File.separator,photoFileName.split("\\.")[0]);
            out = new FileOutputStream(photoFileName);
            takenImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.close();
            takenImage.recycle();
            Field mWeakBitmapField = userImage.getClass().getSuperclass().getDeclaredField("mWeakBitmap");
            mWeakBitmapField.setAccessible(true);
            WeakReference<Bitmap> mWeakBitmap= (WeakReference<Bitmap>) mWeakBitmapField.get(userImage);
            if (mWeakBitmap!=null){
                if (mWeakBitmap.get()!=null){
                    mWeakBitmap.get().recycle();
                }
            }
            userImage.setImageBitmap(BitmapFactory.decodeFile(photoFileName));
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            if (out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("UsersRegActivity","UsersRegActivity on post resume");
    }


}
