package com.troopar.trooparapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.troopar.trooparapp.activity.service.EventService;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.troopar.trooparapp.R;

public class SearchCriteriaActivity extends AppCompatActivity {

    private static final String[] sortKeys={"Date","Distance","Popular"};
    private TextView sortByKey;
    private EditText searchText;
    private EditText searchWithLocationText;
    private EventService eventService;
    private Geocoder geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SearchCriteriaActivity","SearchCriteriaActivity on create");
        setContentView(R.layout.activity_search_criteria);
        String[] preDefinedKeys={"Business","Dining","Entertainment","Events","Family","Game","Life Style","Party","Pets","Religion","Shopping","Sports","Study","Others"};
        Spinner sortByKeySelector= (Spinner) findViewById(R.id.sortByKeySelector);
        searchText= (EditText) findViewById(R.id.search_text);
        searchWithLocationText= (EditText) findViewById(R.id.searchWithLocation_text);
        sortByKey= (TextView) findViewById(R.id.sortByKey);
        ListView categories= (ListView) findViewById(R.id.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchCriteriaActivity.this,android.R.layout.simple_spinner_item,sortKeys);
        ArrayAdapter<String> preDefinedKeysAdapter = new ArrayAdapter<>(SearchCriteriaActivity.this,R.layout.item_category,preDefinedKeys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortByKeySelector.setAdapter(adapter);
        sortByKeySelector.setOnItemSelectedListener(new SpinnerSelectedListener());
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchEvent(v.getText().toString(),false);
                    return true;
                }
                return false;
            }
        });
        categories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchText.setText(((TextView) view).getText().toString());
            }
        });
        categories.setAdapter(preDefinedKeysAdapter);
        eventService=EventService.getInstance();
        Location location= LocationService.getInstance().getLastKnownLocation();
        if (location==null){
            Toast.makeText(SearchCriteriaActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
        }else{
            geocoder=new Geocoder(this, Locale.getDefault());
            searchWithLocationText.setHint("Current Location");
        }
        searchWithLocationText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchEvent(v.getText().toString(),true);
                    return true;
                }
                return false;
            }
        });
    }

    private class SearEventTask extends AsyncTask<Void,Void,ArrayList<EventModel>>{

        private boolean mSearchLocation;
        private String mSearchKeyWords;
        private Location mLocation;
        private String mSortKw;
        private ProgressDialog progressDialog;
        private Address mAddress;

        public SearEventTask(Location location, String searchKeyWords, boolean searchLocation, String sortKw,Address address) {
            mLocation = location;
            mSearchKeyWords = searchKeyWords;
            mSearchLocation = searchLocation;
            mSortKw = sortKw;
            mAddress=address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SearchCriteriaActivity.this, "Searching...", "please wait...", false, false);
        }

        @Override
        protected ArrayList<EventModel> doInBackground(Void... params) {
            try {
                if (mSearchLocation){
                    return eventService.searchEvents(mLocation,null,mSortKw,0,false);
                }
                return eventService.searchEvents(mLocation,mSearchKeyWords,mSortKw,0,false);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<EventModel> eventModels) {
            super.onPostExecute(eventModels);
            progressDialog.cancel();
            if (eventModels!=null){
                Intent data=new Intent();
                data.putExtra("searchResult", eventModels);
                data.putExtra("location", mLocation);
                data.putExtra("address", mAddress);
                if (mSearchLocation){
                    data.putExtra("searchKeyWords", "");
                }else{
                    data.putExtra("searchKeyWords", mSearchKeyWords);
                }
                data.putExtra("sortKw", mSortKw);
                setResult(RESULT_OK, data);
                onBackPressed();
            }else{
                Toast.makeText(SearchCriteriaActivity.this, "no more activities", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchEvent(final String searchKeyWords,final boolean searchLocation){
        if (!searchLocation&&Tools.isNullString(searchKeyWords)){return;}
        final Location location= LocationService.getInstance().getLastKnownLocation();
        if (location==null){
            Toast.makeText(SearchCriteriaActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
            return;
        }else{
            if (geocoder==null){
                geocoder=new Geocoder(this, Locale.getDefault());
            }
        }
        final String sortKw=sortByKey.getText().toString();
        Address address = null;
        if (searchLocation&&!Tools.isNullString(searchKeyWords)){
            try {
                List<Address> addresses=geocoder.getFromLocationName(searchKeyWords,1);
                if (addresses==null||addresses.size()==0){
                    Toast.makeText(SearchCriteriaActivity.this, String.format("location not found %s",searchKeyWords), Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    address=addresses.get(0);
                    location.setLatitude(address.getLatitude());
                    location.setLongitude(address.getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SearEventTask task=new SearEventTask(location,searchKeyWords,searchLocation,sortKw,address);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public void backPressAction(View view){
        onBackPressed();
    }

    private class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            sortByKey.setText(sortKeys[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("SearchCriteriaActivity","SearchCriteriaActivity on destroy");
        searchText.setOnEditorActionListener(null);
        searchWithLocationText.setOnEditorActionListener(null);
        sortByKey=null;
        searchText=null;
        searchWithLocationText=null;
    }


}
