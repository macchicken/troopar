package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.UploadPhotosService;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PostActivityActivity extends AppCompatActivity {

    private final int PICK_PHOTO_CODE_REQUEST_CODE=611;
    private String[] images;
    private int current=0;
    private ViewGroup viewGroup;
    private int imageCount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        images=new String[9];
        viewGroup= (ViewGroup)myToolbar.getParent();
        for (int i=0;i<9;i++){
            final int position=i;
            viewGroup.getChildAt(i+4).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (imageCount==0){return true;}
                    images[position]=null;
                    --imageCount;
                    if (imageCount==0){
                        ImageView imageView= (ImageView) ((ViewGroup)v.getParent()).getChildAt(4);
                        imageView.setImageResource(R.drawable.plus_icon_black);
                    }else{
                        v.setVisibility(View.GONE);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void cancelAction(View view){
        onBackPressed();
    }

    public void sendComments(View view) {
        view.setPressed(true);
        view.setClickable(false);
        String content=((EditText)findViewById(R.id.commentDescription)).getText().toString();
        if (Tools.isNullString(content)&&imageCount<1){
            Toast.makeText(PostActivityActivity.this,"comments your findings",Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageCount>0){
            long offset= TimeZone.getDefault().getRawOffset();
            Intent serviceIntent=new Intent(PostActivityActivity.this, UploadPhotosService.class);
            serviceIntent.putExtra("images",images);
            serviceIntent.putExtra("userId",Constants.USERID);
            serviceIntent.putExtra("createdDate",Constants.simpleDateFormatUS.format(new Date(System.currentTimeMillis()-offset)));
            serviceIntent.putExtra("description",content);
            serviceIntent.putExtra("type","photo");
            serviceIntent.putExtra("imageCount",imageCount);
            startService(serviceIntent);
            cancelAction(null);
        }else{
            SendPostTask sendPostTask=new SendPostTask(content,Constants.USERID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                sendPostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                sendPostTask.execute();
            }
        }
    }

    private class SendPostTask extends AsyncTask<Void,Void,JSONObject> {

        private String mMyUserId;
        private String mContent;
        private ProgressDialog progressDialog;

        public SendPostTask(String content, String myUserId) {
            this.mContent = content;
            this.mMyUserId = myUserId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(PostActivityActivity.this, "sending comment...", "...", false, false);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(300000, TimeUnit.MILLISECONDS).readTimeout(300000,TimeUnit.MILLISECONDS).writeTimeout(300000,TimeUnit.MILLISECONDS).build();
            MultipartBody.Builder formBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(Constants.EQUIPID, Constants.DEVEICEIDVALUE).addFormDataPart(Constants.SIGNATURE,Constants.SIGNATUREVALUE)
                    .addFormDataPart("userId",mMyUserId).addFormDataPart("content",mContent);
            Request request=new Request.Builder().url(BuildConfig.API_READHOST+"/activity/add_activity_post.php").post(formBody.build()).build();
            ResponseBody response = null;
            try {
                response=client.newCall(request).execute().body();
                return new JSONObject(response.string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (response!=null){
                    response.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.dismiss();
            if (jsonObject==null){
                Toast.makeText(PostActivityActivity.this,"server error",Toast.LENGTH_SHORT).show();
                View saveComments=findViewById(R.id.saveComments);
                saveComments.setPressed(false);
                saveComments.setClickable(true);
                return;
            }
            try {
                if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                    Intent data=new Intent();
                    data.putExtra("postResult",jsonObject.toString());
                    setResult(RESULT_OK,data);
                    onBackPressed();
                }else{
                    Toast.makeText(PostActivityActivity.this,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                    View saveComments=findViewById(R.id.saveComments);
                    saveComments.setPressed(false);
                    saveComments.setClickable(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void choseImageAction(View v){
        current=Integer.parseInt(v.getContentDescription().toString());
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// Create intent for picking a photo from the gallery
        startActivityForResult(intent, PICK_PHOTO_CODE_REQUEST_CODE);// Bring up gallery to select a photo
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=RESULT_OK||data==null){return;}
        switch (requestCode){
            case PICK_PHOTO_CODE_REQUEST_CODE:
                FileOutputStream out=null;
                try {
                    Uri photoUri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(photoUri, projection, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        String path = cursor.getString(column_index);
                        cursor.close();
                        BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
                        sampleOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, sampleOptions);
                        int inSampleSize = Tools.calculateInSampleSize(sampleOptions, 300, 300);
                        sampleOptions.inJustDecodeBounds = false;
                        sampleOptions.inSampleSize = inSampleSize;
                        Bitmap bitmapImage = BitmapFactory.decodeFile(path, sampleOptions);
                        String[] pathList=path.split(File.separator);
                        String temp=Tools.checkAppDirectory("image")+ File.separator+pathList[pathList.length-1];
                        out = new FileOutputStream(temp);
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 70, out);// compress and save into local storage
                        out.close();
                        ImageView imageView= (ImageView) viewGroup.getChildAt(current+4);
                        imageView.setImageBitmap(bitmapImage);
                        images[current]=temp;
                        if (current<8){
                            viewGroup.getChildAt(current+5).setVisibility(View.VISIBLE);
                        }
                        if (imageCount<9){
                            ++imageCount;
                        }
                    }
                }catch (Throwable t){
                    t.printStackTrace();
                }finally {
                    if (out!=null){
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:break;
        }
    }


}
