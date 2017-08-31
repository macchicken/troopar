package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.troopar.trooparapp.model.Review;
import com.troopar.trooparapp.myview.PaginationReviewsListView;
import com.troopar.trooparapp.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.adapter.ReviewsAdapter;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;


public class MoreReviewsActivity extends AppCompatActivity {

    private JSONParser jParser = new JSONParser();
    private String url= BuildConfig.API_READHOST+"/event_reviews.php";
    private String eventId;
    private int offset;
    private ArrayList<Review> reviews;
    private ReviewsAdapter reviewsAdapter;
    private PaginationReviewsListView eventReviewsView;
    private ProgressDialog progressDialog;
    private JSONArray reviewResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MoreReviewsActivity","MoreReviewsActivity on create");
        prepareDataFromIntent();
        new LoadViewTask().execute();
    }

    private AsyncTask<Void, Void, JSONArray> getReviewsFromService(final String deviceId,final String signature){
        Log.d("MoreReviewsActivity","getReviewsFromService");
        AsyncTask<Void, Void, JSONArray> task = new AsyncTask<Void, Void, JSONArray>(){
            @Override
            protected JSONArray doInBackground(Void... params) {
                try {
                    HashMap<String,String> parameters=new HashMap<>();
                    parameters.put("eventId",eventId);
                    parameters.put("offSet",String.valueOf(offset));
                    parameters.put(Constants.EQUIPID,deviceId);
                    parameters.put(Constants.SIGNATURE,signature);
                    JSONObject obj=jParser.makeRequestForHttp(url,"POST",parameters);
                    if (obj!=null){
                        try {
                            String status = (String) obj.get("status");
                            if (Constants.TAG_SUCCESS.equals(status)){
                                return obj.getJSONArray("result");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }

    private AsyncTask<Void, Void, JSONArray> runAsyncTask(AsyncTask<Void, Void, JSONArray> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadViewTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(MoreReviewsActivity.this, "Loading...", "Loading event reviews, please wait...", false, false);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                HashMap<String,String> parameters=new HashMap<>();
                parameters.put("eventId", eventId);
                parameters.put("offSet",  String.valueOf(offset));
                parameters.put(Constants.EQUIPID, Constants.DEVEICEIDVALUE);
                parameters.put(Constants.SIGNATURE, Constants.SIGNATUREVALUE);
                JSONObject obj=jParser.makeRequestForHttp(url, "POST", parameters);
                if (obj!=null){
                    try {
                        String status = (String) obj.get("status");
                        if (Constants.TAG_SUCCESS.equals(status)){
                            reviewResults=obj.getJSONArray("result");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            progressDialog.dismiss();//close the progress dialog
            initView();
        }
    }

    private void prepareDataFromIntent(){
        Intent tintent=getIntent();
        if (tintent!=null){
            eventId=tintent.getStringExtra("eventId");
        }
    }


    private void initView(){
        setContentView(R.layout.activity_more_reviews);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        eventReviewsView= (PaginationReviewsListView) findViewById(R.id.reviewListview);
        if (eventReviewsView==null){
            return;
        }
        reviews=new ArrayList<>();
        reviewsAdapter=new ReviewsAdapter(this,reviews);
        eventReviewsView.setAdapter(reviewsAdapter);
        eventReviewsView.setOnLoadListener(new PaginationReviewsListView.OnLoadListener() {
            @Override
            public void onLoad() {
                AsyncTask<Void, Void, JSONArray> resultTask = getReviewsFromService(Constants.DEVEICEIDVALUE, Constants.SIGNATUREVALUE);
                if (resultTask != null) {
                    try {
                        JSONArray result = resultTask.get();
                        if (result != null) {
                            int reviewTotal = result.length();
                            if (reviewTotal > 0) {
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject c = result.getJSONObject(i);
                                    JSONObject user=c.getJSONObject("user");
                                    String displayName= Tools.isNullString(user.getString("firstName"))&&Tools.isNullString(user.getString("lastName"))?user.getString("username"):String.format("%s %s", user.getString("firstName"), user.getString("lastName"));
                                    reviews.add(new Review(displayName, c.getString("createdDate"), c.getString("content"), c.getString("rate"),user.getString("avatarStandard")));
                                }
                                reviewsAdapter.notifyDataSetChanged();
                                offset += reviewTotal;
                            } else {
                                Toast.makeText(MoreReviewsActivity.this, "no more reviews", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                eventReviewsView.loadComplete();
            }
        });
        try {
            if (reviewResults!=null){
                int reviewTotal=reviewResults.length();
                if (reviewTotal>0){
                    for (int i = 0; i < reviewTotal; i++){
                        JSONObject c = reviewResults.getJSONObject(i);
                        JSONObject user=c.getJSONObject("user");
                        reviews.add(new Review(user.getString("username"),c.getString("createdDate"),c.getString("content"),c.getString("rate"),user.getString("avatarStandard")));
                    }
                    reviewsAdapter.notifyDataSetChanged();
                    offset+=reviewTotal;
                }else{
                    Toast.makeText(MoreReviewsActivity.this, "no more reviews", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("getReviewsFromService","getReviewsFromService on post resume");
    }


}
