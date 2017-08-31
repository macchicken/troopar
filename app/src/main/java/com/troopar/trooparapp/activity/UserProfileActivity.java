package com.troopar.trooparapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.activity.task.ChangeUserProfileTask;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.MyAppSharePreference;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.troopar.trooparapp.R;

public class UserProfileActivity extends AppCompatActivity {

    private EditText userName;
    private EditText userFirstName;
    private EditText userLastName;
    private EditText userEmail;
    private EditText userAddress;
    private EditText userCountry;
    private EditText userZipCode;
    private EditText userGender;
    private EditText userInterests;
    private EditText userPassword;
    private String password;
    private String userId;
    private static final int PICK_PHOTO_CODE_REQUEST_CODE=611;
    private String photoFileName;
    private ImageView userImage;
    private boolean userImageChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userName= (EditText) findViewById(R.id.userName);
        userFirstName= (EditText) findViewById(R.id.userFirstName);
        userLastName= (EditText) findViewById(R.id.userLastName);
        userAddress= (EditText) findViewById(R.id.userAddress);
        userCountry= (EditText) findViewById(R.id.userCountry);
        userZipCode= (EditText) findViewById(R.id.userZipCode);
        userGender= (EditText) findViewById(R.id.userGender);
        userInterests= (EditText) findViewById(R.id.userInterests);
        userEmail= (EditText) findViewById(R.id.userEmail);
        userPassword= (EditText) findViewById(R.id.userPassword);
        userName.setText(Constants.USERNAME);
        userFirstName.setText(Constants.USERFIRSTNAME);
        userLastName.setText(Constants.USERLASTNAME);
        userGender.setText(Constants.USERGENDER);
        userEmail.setText(Constants.USEREMAIL);
        password=Constants.USERPASSWORD;
        userId=Constants.USERID;
        userImage=((ImageView)findViewById(R.id.userImage));
        ((EditText)findViewById(R.id.userDOB)).setText(Constants.USERDOB);
        Intent intent=getIntent();
        if (intent!=null){
            userImageChanged=intent.getBooleanExtra("userImageChanged",false);
            if (userImageChanged){
                photoFileName=intent.getStringExtra("userImageFile");
            }else{
                photoFileName=Constants.AVATARORIGIN;
            }
            if (Tools.isNullString(photoFileName)){
                userImage.setImageResource(R.drawable.user_image);
            }else{
                if (photoFileName.startsWith("http")){
                    Glide.with(UserProfileActivity.this).load(photoFileName).override(70,70).diskCacheStrategy(DiskCacheStrategy.ALL).into(userImage);
                }else{
                    userImage.setImageBitmap(loadImage(photoFileName));
                }
            }
        }
        userPassword.setText(password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_userprofile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK, null);
                onBackPressed();
                return true;
            case R.id.action_saveprofile:
                Log.d("UserProfileActivity", "UserProfileActivity save user profile");
                final String newPassword=userPassword.getText().toString();
                if (Tools.isNullString(newPassword)){
                    Toast.makeText(UserProfileActivity.this,"password must not empty",Toast.LENGTH_LONG).show();
                    return true;
                }
                final ProgressDialog[] progressDialog = new ProgressDialog[1];
                runAsyncTask(new ChangeUserProfileTask(userEmail.getText().toString(), userFirstName.getText().toString(), userGender.getText().toString(), userLastName.getText().toString(),
                        password,newPassword,userName.getText().toString(), new ChangeUserProfileTask.UIAction() {
                    @Override
                    public void onPreExecute() {
                        progressDialog[0] = ProgressDialog.show(UserProfileActivity.this, "saving...", "...", false, false);
                    }
                    @Override
                    public void onPostExecute(JSONObject jsonObject) {
                        progressDialog[0].dismiss();
                        try {
                            if (jsonObject==null){
                                Toast.makeText(UserProfileActivity.this, "server error", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))) {
                                HashMap<String, String> temp = new HashMap<>();
                                temp.put("userName", userName.getText().toString());
                                temp.put("userEmail", userEmail.getText().toString());
                                temp.put("userGender", userGender.getText().toString());
                                temp.put("userFirstName", userFirstName.getText().toString());
                                temp.put("userLastName", userLastName.getText().toString());
                                temp.put(userId + "userDOB", ((EditText) findViewById(R.id.userDOB)).getText().toString());
                                Constants.USERNAME=userName.getText().toString();
                                Constants.USEREMAIL=userEmail.getText().toString();
                                Constants.USERGENDER=userGender.getText().toString();
                                Constants.USERFIRSTNAME=userFirstName.getText().toString();
                                Constants.USERLASTNAME=userLastName.getText().toString();
                                Constants.USERDOB=((EditText) findViewById(R.id.userDOB)).getText().toString();
                                if (!password.equals(newPassword)){
                                    Constants.USERPASSWORD=newPassword;
                                    temp.put("userPassword",newPassword);
                                }
                                if (userImageChanged){
                                    JSONObject user=jsonObject.getJSONObject("user");
                                    Constants.AVATARORIGIN=user.getString("avatarOrigin");
                                    Constants.AVATARSTANDARD=user.getString("avatarStandard");
                                    temp.put("avatarOrigin", Constants.AVATARORIGIN);
                                    temp.put("avatarStandard", Constants.AVATARSTANDARD);
                                    Constants.PHOTOFILENAME=Constants.AVATARORIGIN;
                                    Constants.USERPHONE=user.getString("phone");
                                    temp.put(userId + Constants.USERNAME, Constants.PHOTOFILENAME);
                                }
                                MyAppSharePreference.getInstance().saveMultipleStringValues(temp);
                                Toast.makeText(UserProfileActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                onBackPressed();
                            } else {
                                Toast.makeText(UserProfileActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                },userImageChanged?photoFileName:null));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeTime(View view){
        Dialog timeDialog = new Dialog(UserProfileActivity.this);
        timeDialog.setContentView(R.layout.userdob_time_picker);
        final DatePicker datePicker1=((DatePicker)timeDialog.findViewById(R.id.datePicker1));
        timeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                writeTime(String.format("%s-%s-%s", datePicker1.getYear(), datePicker1.getMonth() + 1, datePicker1.getDayOfMonth()));
            }
        });
        timeDialog.show();
    }

   private void writeTime(String dataTime){
       ((EditText)findViewById(R.id.userDOB)).setText(dataTime);
   }

    private void runAsyncTask(AsyncTask<Void, Void, JSONObject> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void uploadUserImage(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// Create intent for picking a photo from the gallery
        startActivityForResult(intent, PICK_PHOTO_CODE_REQUEST_CODE);// Bring up gallery to select a photo
    }

    private Bitmap loadImage(String photoFileName){
        Log.d("UserProfileActivity", "load image " + photoFileName);
        BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
        sampleOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(photoFileName,sampleOptions);
        int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
        sampleOptions.inJustDecodeBounds=false;
        sampleOptions.inSampleSize=inSampleSize;
        return BitmapFactory.decodeFile(photoFileName, sampleOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICK_PHOTO_CODE_REQUEST_CODE:
                Log.d("UserProfileActivity", "back from PICK_PHOTO_CODE_REQUEST_CODE");
                if (resultCode==RESULT_OK&&data!=null){
                    FileOutputStream out=null;
                    try {
                        Uri photoUri = data.getData();
                        String[] projection = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(photoUri, projection, null, null, null);
                        if (cursor!=null){
                            cursor.moveToFirst();
                            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            String path=cursor.getString(column_index);
                            cursor.close();
                            BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
                            sampleOptions.inJustDecodeBounds=true;
                            BitmapFactory.decodeFile(path, sampleOptions);
                            int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
                            sampleOptions.inJustDecodeBounds=false;
                            sampleOptions.inSampleSize=inSampleSize;
                            Bitmap bitmapImage=BitmapFactory.decodeFile(path, sampleOptions);
                            userImage.setImageBitmap(bitmapImage);
                            String[] pathList=path.split(File.separator);
                            String temp=Tools.checkAppImageDirectory()+File.separator+pathList[pathList.length-1];
                            out = new FileOutputStream(temp);
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 70, out);// compress and save into local storage
                            photoFileName=temp;
                            userImageChanged=true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
                break;
        }
    }

    public void enableEditText(View view){
        EditText editText= (EditText) ((ViewGroup)view.getParent()).getChildAt(0);
        if (editText.isFocusable()){
            editText.setFocusableInTouchMode(false);
            editText.setFocusable(false);
        }else{
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        }
    }

    public void enableEdit(View view){
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }


}
