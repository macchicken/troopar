package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.ImageDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;
import com.troopar.trooparapp.utils.Tools;

public class WriteReviewActivity extends AppCompatActivity {

    private String eventId;
    private String url= BuildConfig.API_READHOST+"/review/add_review.php";
    private JSONParser jParser = new JSONParser();
    private String userId="";
    private EditText reviewInputField;
    private String reviewRating;
    private EventModel eventModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirte_review);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        reviewInputField= (EditText) findViewById(R.id.reviewInputDialog);
        userId = Constants.USERID;
        Intent intent = getIntent();
        if (intent != null) {
            eventId = intent.getStringExtra("eventId");
            eventModel = (EventModel) intent.getSerializableExtra("eventModel");
            if (eventModel == null) {
                return;
            }
        }
        TextView eventTitle= (TextView) findViewById(R.id.eventTitle);
        TextView eventCategory = (TextView) findViewById(R.id.eventCategory);
        ImageView eventCategoryText = (ImageView) findViewById(R.id.eventCategoryText);
        TextView eventSnippet = (TextView) findViewById(R.id.eventSnippet);
        TextView eventSnippet2 = (TextView) findViewById(R.id.eventSnippet2);
        try{
            ImageView imageView = (ImageView) findViewById(R.id.eventImageView);
            if (!Tools.isNullString(eventModel.getThumbnailImageUrl())) {
                ImageDownloader.getInstance().getRequestManager().load(eventModel.getThumbnailImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            }else{
                imageView.setImageResource(R.drawable.troopar_logo_red_square_t);
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        eventCategoryText.setImageResource(getCategoryImage(eventModel.getCategory()));
        eventTitle.setText(eventModel.getName());
        String eventStartDate = eventModel.getStartDate();
        String eventEndDate = eventModel.getEndDate();
        String startDateOfWeek = Tools.getDateOfWeekFromDate(eventStartDate);
        eventSnippet.setText(String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate)));
        eventSnippet2.setText(Html.fromHtml("<u>" + eventModel.getLocation() + "</u>"));
        eventCategory.setText(Html.fromHtml("<u>" + eventModel.getCategory() + "</u>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sendreview:
                if (reviewInputField!=null){
                    postReviewContent(reviewInputField.getText().toString());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postReviewContent(final String reviewContent){
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>(){
            private ProgressDialog progressDialog;
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(WriteReviewActivity.this, "Submitting...", "please wait...", false, false);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    HashMap<String,String> parameters = new HashMap<>();
                    parameters.put("eventId",eventId);
                    parameters.put("content",reviewContent);
                    parameters.put("rate",reviewRating);
                    parameters.put("userId",userId);
                    parameters.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
                    parameters.put(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
                    JSONObject obj = jParser.makeRequestForHttp(url,"POST",parameters);
                    if (obj!=null) {
                        try {
                            return Constants.TAG_SUCCESS.equals(obj.getString("status"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean postSuccess) {
                progressDialog.dismiss();
                if (postSuccess){
                    Intent data = new Intent();
                    data.putExtra("eventId", eventId);
                    setResult(RESULT_OK, data);
                    onBackPressed();
                }else{
                    Toast.makeText(WriteReviewActivity.this,"submit review error",Toast.LENGTH_SHORT).show();
                }
            }
        };
        runAsyncTask(task);
    }

    private void runAsyncTask(AsyncTask<Void, Void, Boolean> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private int getCategoryImage(String category){
        switch (category){
            default:return R.drawable.event_category_icon;
        }
    }


}
