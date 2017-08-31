package com.troopar.trooparapp.activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocationService;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;


public class MyEventMapoActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private LatLng myCurrLatLng;
    private Geocoder geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyEventMapoActivity", "MyEventMapoActivity on create");
        setContentView(R.layout.find_event_map);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setNavigationIcon(ApplicationContextStore.getInstance().getBACKICON());
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.findEvMap);
        mapFragment.getMapAsync(this);
        Location locationCt= LocationService.getInstance().getLastKnownLocation();
        if (locationCt!=null){
            Log.d("MyEventMapoActivity","get location with locationCt");
            myCurrLatLng = new LatLng(locationCt.getLatitude(), locationCt.getLongitude());
            if (getIntent()!=null&&"changeAddress".equals(getIntent().getStringExtra("intentRequestCode"))){
                geocoder = new Geocoder(this, Locale.getDefault());
            }
        }else{
            Toast.makeText(MyEventMapoActivity.this, "location service not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Return false so that we don't consume the event and the default behavior still occurs
                // (the camera animates to the user's current position).
                return false;
            }
        });
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    ApplicationContextStore applicationContextStore=ApplicationContextStore.getInstance();
                    if (applicationContextStore.getMAPLOCATIONICONBITMAP()==null){
                        int pixels = (int) (30 * Constants.DENSITYSCALE + 0.5f);
                        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("message_location_icon", "drawable", getPackageName()));
                        applicationContextStore.setMAPLOCATIONICONBITMAP(Bitmap.createScaledBitmap(imageBitmap, pixels, pixels, false));
                    }
                    if (applicationContextStore.getMAPLOCATIONICON()==null){
                        applicationContextStore.setMAPLOCATIONICON(BitmapDescriptorFactory.fromBitmap(applicationContextStore.getMAPLOCATIONICONBITMAP()));
                    }
                    mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.toString()).icon(applicationContextStore.getMAPLOCATIONICON()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent data = new Intent();
                    LatLngBounds temp=mMap.getProjection().getVisibleRegion().latLngBounds;
                    if (geocoder!=null){
                        try {
                            List<Address> addresses = geocoder.getFromLocation(temp.getCenter().latitude, temp.getCenter().longitude, 1);//TODO invoke gaode to reverse location to address description while google is not available
                            if (addresses.size()>0){
                                String address = addresses.get(0).getAddressLine(0)==null?"":addresses.get(0).getAddressLine(0);
                                String city = addresses.get(0).getAddressLine(1)==null?"":addresses.get(0).getAddressLine(1);
                                String country = addresses.get(0).getAddressLine(2)==null?"":addresses.get(0).getAddressLine(2);
                                data.putExtra("myAddress",address+" "+city+" "+country);
                                data.putExtra("myLatitude",addresses.get(0).getLatitude());
                                data.putExtra("myLongitude",addresses.get(0).getLongitude());
                                data.putExtra("mySuburb",addresses.get(0).getSubAdminArea()==null?addresses.get(0).getLocality():addresses.get(0).getLocality());
                                data.putExtra("myCity",addresses.get(0).getLocality());
                                data.putExtra("myLocation", address);
                                data.putExtra("myCountry", country);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    data.putExtra("marker",temp);
                    setResult(RESULT_OK, data);
                    onBackPressed();
                    return false;
                }
            });
            if (myCurrLatLng!=null){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myCurrLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    Log.d("MyEventMapoActivity",String.format("google map onCameraChange [%s]",cameraPosition.toString()));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK, null);
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("MyEventMapoActivity","MyEventMapoActivity on post resume");
    }


}
