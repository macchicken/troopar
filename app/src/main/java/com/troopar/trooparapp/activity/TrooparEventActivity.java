package com.troopar.trooparapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.activity.task.CreateEventTask;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Tools;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class TrooparEventActivity extends AppCompatActivity {

    private TextView eventStartTime;
    private TextView eventEndTime;
    private TextView eventTitle;
    private TextView eventDescription;
    private ImageView categoryText;
    private String eventStartTimeStr;
    private String eventEndTimeStr;
    private String myCity;
    private String myLocation;
    private String mySuburb;
    private String eventImagePath;
    private String eventCategory="Events";
    private double myLatitude,myLongitude;
    private final int TROOPAR_EVENT_ADDRESS_REQUEST_CODE=613;
    private final int PICK_EVENT_PHOTO_CODE_REQUEST_CODE=614;
    private Dialog setFeeDialog;
    private Dialog setPeopleLimitDialog;
    private String fee;
    private String people;
    private String city;
    private String country;
    private String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troopar_event);
        Toolbar myToolbar=(Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        eventStartTime= (TextView) findViewById(R.id.eventStartTime);
        eventEndTime= (TextView) findViewById(R.id.eventEndTime);
        eventTitle= (TextView) findViewById(R.id.eventTitle);
        eventDescription= (TextView) findViewById(R.id.eventDescription);
        categoryText = (ImageView) findViewById(R.id.categoryText);
        categoryText.setImageResource(getCategoryImage(eventCategory));
        setFeeDialog=new Dialog(TrooparEventActivity.this);
        setFeeDialog.setContentView(R.layout.single_input_dialog);
        ((TextView)setFeeDialog.findViewById(R.id.inputTitle)).setText(R.string.entryFee);
        setFeeDialog.findViewById(R.id.dialogButtonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFeeDialog.hide();
            }
        });
        setFeeDialog.findViewById(R.id.dialogButtonConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fee=((EditText)setFeeDialog.findViewById(R.id.inputField)).getText().toString();
                ((Button)findViewById(R.id.feeBtn)).setText("0".equals(fee)||Tools.isNullString(fee)?"Free":fee);
                setFeeDialog.hide();
            }
        });
        setPeopleLimitDialog=new Dialog(TrooparEventActivity.this);
        setPeopleLimitDialog.setContentView(R.layout.single_input_dialog);
        ((TextView)setPeopleLimitDialog.findViewById(R.id.inputTitle)).setText(R.string.maximumPeople);
        setPeopleLimitDialog.findViewById(R.id.dialogButtonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPeopleLimitDialog.hide();
            }
        });
        setPeopleLimitDialog.findViewById(R.id.dialogButtonConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                people=((EditText)setPeopleLimitDialog.findViewById(R.id.inputField)).getText().toString();
                ((Button)findViewById(R.id.pplBtn)).setText("0".equals(people)||Tools.isNullString(people)?"- ppl.":people+" ppl.");
                setPeopleLimitDialog.hide();
            }
        });
        AsyncTask<Void,Void,Boolean> task=new AsyncTask<Void,Void,Boolean>(){

            private String dataTime,clockTime;

            @Override
            protected Boolean doInBackground(Void... params) {
                if (setCurrentAddress()){
                    Calendar now=Calendar.getInstance();
                    Date date = new Date(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    String tempStr=String.format("%s-%s-%s", now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DAY_OF_MONTH));
                    dataTime = Tools.parseDateOfWeek(date.getDay()) + " " + Tools.formatTime(tempStr);
                    clockTime = String.format("%s:%s", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                    eventStartTimeStr=tempStr+" "+clockTime;
                    eventEndTimeStr=tempStr+" "+clockTime;
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean addressResolved) {
                super.onPostExecute(addressResolved);
                if (addressResolved){
                    eventStartTime.setText(Html.fromHtml("<u>" + dataTime + " " + clockTime + "</u>"));
                    eventEndTime.setText(Html.fromHtml("<u>" + dataTime + " " + clockTime + "</u>"));
                    ((TextView)findViewById(R.id.eventCategory)).setText(Html.fromHtml("<u>Events</u>"));
                    ((TextView)findViewById(R.id.eventPlace)).setText(Html.fromHtml("<u>"+address+" "+city+" "+country+"</u>"));
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trooparevent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK, null);
                onBackPressed();
                return true;
            case R.id.action_sendevent:
                Log.d("TrooparEventActivity", "create new event");
                String eventTitleText=eventTitle.getText().toString();
                final String eventDesText=eventDescription.getText().toString();
                if (Tools.isNullString(eventTitleText)){
                    Toast.makeText(TrooparEventActivity.this,"must have a title for an activity", Toast.LENGTH_LONG).show();
                    return true;
                }
                if (Tools.isNullString(eventDesText)){
                    Toast.makeText(TrooparEventActivity.this,"must have some description for an activity", Toast.LENGTH_LONG).show();
                    return true;
                }
                if (Tools.calculateTimeDiff(eventEndTimeStr,eventStartTimeStr)<=0){
                    Toast.makeText(TrooparEventActivity.this,"finish time must be larger than start time", Toast.LENGTH_LONG).show();
                    return true;
                }
                if (myLatitude==0||myLatitude==-1||myLongitude==0||myLongitude==-1){
                    Toast.makeText(TrooparEventActivity.this,"location service not enabled", Toast.LENGTH_LONG).show();
                    return true;
                }
                try {
                    String availability=((Button)findViewById(R.id.visibilityBtn)).getText().toString();
                    String approveToJoin=((Button)findViewById(R.id.acceptBtn)).getText().toString();
                    CreateEventTask task=new CreateEventTask(eventTitleText,eventDesText,myLocation,eventStartTimeStr,
                            eventEndTimeStr,myCity,mySuburb,myLatitude,myLongitude,availability,approveToJoin);
                    task.setRequires(fee);
                    task.setMaxNumPeople(people);
                    task.setEventImagePath(eventImagePath);
                    final ProgressDialog[] progressDialog = new ProgressDialog[1];
                    task.setExecuteCallBack(new CreateEventTask.ExecuteCallBack() {
                        @Override
                        public void onPreExecute() {
                            progressDialog[0] =ProgressDialog.show(TrooparEventActivity.this, "Trooping...", "Creating a new Event :)", false, false);
                        }
                        @Override
                        public void onProgressUpdate(Integer value) {
                            progressDialog[0].setProgress(value);
                        }
                        @Override
                        public void onPostExecute(JSONObject jsonObject) {
                            progressDialog[0].dismiss();
                            try{
                                if (jsonObject==null){
                                    Toast.makeText(TrooparEventActivity.this,"server error", Toast.LENGTH_LONG).show();
                                }else if (Constants.TAG_SUCCESS.equals(jsonObject.getString("status"))){
                                    Toast.makeText(TrooparEventActivity.this,"you troop a event", Toast.LENGTH_LONG).show();
                                    onBackPressed();
                                }else{
                                    Toast.makeText(TrooparEventActivity.this,"server error", Toast.LENGTH_SHORT).show();
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        task.execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeTime(View view){
        Dialog timeDialog = new Dialog(TrooparEventActivity.this);
        timeDialog.setContentView(R.layout.event_time_picker);
        boolean startTime=true;
        if ("endTime".equals(view.getContentDescription())){
            timeDialog.setTitle("finish Time");
            startTime=false;
        }else{
            timeDialog.setTitle("start time");
        }
        final DatePicker datePicker1=((DatePicker)timeDialog.findViewById(R.id.datePicker1));
        final TimePicker timePicker1=((TimePicker)timeDialog.findViewById(R.id.timePicker1));
        final boolean finalStartTime = startTime;
        timeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Date date = new Date(datePicker1.getYear() - 1900, datePicker1.getMonth(), datePicker1.getDayOfMonth());
                String tempStr = String.format("%s-%s-%s", datePicker1.getYear(), datePicker1.getMonth() + 1, datePicker1.getDayOfMonth());
                String dataTime = Tools.parseDateOfWeek(date.getDay()) + " " + Tools.formatTime(tempStr);
                String clockTime = String.format("%s:%s", timePicker1.getCurrentHour(), timePicker1.getCurrentMinute());
                if (finalStartTime) {
                    eventStartTimeStr = tempStr + " " + clockTime;
                    eventStartTime.setText(Html.fromHtml("<u>"+dataTime+" "+clockTime+"</u>"));
                } else {
                    eventEndTimeStr = tempStr + " " + clockTime;
                    eventEndTime.setText(Html.fromHtml("<u>" + dataTime + " " + clockTime + "</u>"));
                }
            }
        });
        timeDialog.show();
    }

    public void changeAddress(View view){
        Intent intent=new Intent(TrooparEventActivity.this,MyEventMapoActivity.class);
        intent.putExtra("intentRequestCode", "changeAddress");
        startActivityForResult(intent, TROOPAR_EVENT_ADDRESS_REQUEST_CODE);
    }

    public void uploadEventPhoto(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);// Create intent for picking a photo from the gallery
        startActivityForResult(intent, PICK_EVENT_PHOTO_CODE_REQUEST_CODE);// Bring up gallery to select a photo
    }

    public void changeProven(View view) {
        String provenStr = ((Button)view).getText().toString();
        ((Button)view).setText("Auto join".equals(provenStr)?"Approve":"Auto join");
    }

    public void changeOpenness(View view) {
        String openStr=((Button)view).getText().toString();
        ((Button) view).setText("Public".equals(openStr)?"Friend":"Public");
    }

    public void setFee(View view) {
        setFeeDialog.show();
    }

    public void setPeopleLimit(View view) {
        setPeopleLimitDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TROOPAR_EVENT_ADDRESS_REQUEST_CODE:
                Log.d("TrooparEventActivity" ,"back from MyEventMapoActivity");
                if(resultCode==RESULT_OK&&data!=null){
                    ((TextView)findViewById(R.id.eventPlace)).setText(Html.fromHtml("<u>"+data.getStringExtra("myAddress")+"</u>"));
                    mySuburb=data.getStringExtra("mySuburb");
                    myCity=data.getStringExtra("myCity");
                    myLocation=data.getStringExtra("myLocation")+" "+myCity;
                    myLatitude=data.getDoubleExtra("myLatitude", -1);
                    myLongitude=data.getDoubleExtra("myLongitude", -1);
                }
                break;
            case PICK_EVENT_PHOTO_CODE_REQUEST_CODE:
                if (resultCode==RESULT_OK&&data!=null){
                try {
                    Uri photoUri = data.getData();
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(photoUri, projection, null, null, null);
                    if (cursor!=null){
                        cursor.moveToFirst();
                        int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        eventImagePath=cursor.getString(column_index);
                        cursor.close();
                        BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
                        sampleOptions.inJustDecodeBounds=true;
                        BitmapFactory.decodeFile(eventImagePath, sampleOptions);
                        int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
                        sampleOptions.inJustDecodeBounds=false;
                        sampleOptions.inSampleSize=inSampleSize;
                        Bitmap takenImage=BitmapFactory.decodeFile(eventImagePath, sampleOptions);
                        RoundedImageView eventImageView=((RoundedImageView)findViewById(R.id.eventImageView));
                        Drawable drawable=eventImageView.getDrawable();
                        if (drawable instanceof RoundedDrawable){
                            ((RoundedDrawable) drawable).getSourceBitmap().recycle();
                        }
                        eventImageView.setImageBitmap(takenImage);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            break;
            default:break;
        }
    }

    private boolean setCurrentAddress() {
        Location bestLocation= LocationService.getInstance().getLastKnownLocation();
        if (bestLocation==null){
            Toast.makeText(TrooparEventActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
            return false;
        }
        Geocoder geocoder= new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(bestLocation.getLatitude(), bestLocation.getLongitude(), 1);
            if (addresses.size()>0){
                address = addresses.get(0).getAddressLine(0)==null?"":addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getAddressLine(1)==null?"":addresses.get(0).getAddressLine(1);
                country = addresses.get(0).getAddressLine(2)==null?"":addresses.get(0).getAddressLine(2);
                myLatitude=addresses.get(0).getLatitude();
                myLongitude=addresses.get(0).getLongitude();
                mySuburb=addresses.get(0).getSubAdminArea()==null?addresses.get(0).getLocality():addresses.get(0).getLocality();
                myCity=addresses.get(0).getLocality();
                myLocation=address+" "+myCity;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private int getCategoryImage(String category){
        switch (category){
            default:return R.drawable.event_category_icon;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fee=null;
        people=null;
        setFeeDialog.cancel();
        setPeopleLimitDialog.cancel();
        setFeeDialog=null;
        setPeopleLimitDialog=null;
        eventStartTime=null;
        eventEndTime=null;
        eventTitle=null;
        eventDescription=null;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
