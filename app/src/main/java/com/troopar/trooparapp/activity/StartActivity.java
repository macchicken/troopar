package com.troopar.trooparapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.service.LocalDBHelper;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.MyAppSharePreference;
import com.troopar.trooparapp.utils.ScreenTools;
import com.troopar.trooparapp.utils.Tools;


public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_my01);
		ScreenTools.getInstance().setMContext(getApplicationContext());
		ImageDownloader.getInstance().setContext(getApplicationContext());
		MyAppSharePreference.getInstance().setSharedPreferences(getApplicationContext());
		try{
			SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
			String deviceId=sharedPreferences.getString(Constants.DEVICEID,null);
			String signature=sharedPreferences.getString(Constants.SIGNATURE,null);
			if (deviceId==null&&signature==null){
				try{
					TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
					deviceId=tm.getDeviceId();
					if (!Tools.isNullString(deviceId)){
						signature = Tools.sha1OfStr(deviceId + "ANZStudio");
						SharedPreferences.Editor editor=sharedPreferences.edit();
						editor.putString(Constants.DEVICEID,deviceId);
						editor.putString(Constants.SIGNATURE,signature);
						editor.commit();
					}
				}catch (SecurityException |NullPointerException e){
					e.printStackTrace();
				}
			}
			String myUserId=sharedPreferences.getString("userId",null);
			if (!Tools.isNullString(myUserId)){// each user with own local database
				LocalDBHelper.getUserInstance(getApplicationContext(),myUserId).initDatabase();
				Constants.USERID=myUserId;// init the global value
				Constants.USERNAME=sharedPreferences.getString("userName","");
				Constants.USEREMAIL=sharedPreferences.getString("userEmail","");
				Constants.USERGENDER=sharedPreferences.getString("userGender","");
				Constants.USERFIRSTNAME=sharedPreferences.getString("userFirstName","");
				Constants.USERLASTNAME=sharedPreferences.getString("userLastName","");
				Constants.USERPHONE=sharedPreferences.getString("userPhone","");
				Constants.AVATARORIGIN=sharedPreferences.getString("avatarOrigin","");
				Constants.AVATARSTANDARD=sharedPreferences.getString("avatarStandard","");
				Constants.USERPASSWORD=sharedPreferences.getString("userPassword","");
				Constants.PHOTOFILENAME=sharedPreferences.getString(myUserId+Constants.USERNAME,"");
				Constants.USERDOB=sharedPreferences.getString(myUserId+"userDOB","");
				Constants.BACKGROUNDPHOTOFILENAME=sharedPreferences.getString(myUserId+Constants.USERNAME+"background","");
			}else{
				LocalDBHelper.getInstance(getApplicationContext()).initDatabase();
			}
			Constants.DEVEICEIDVALUE=deviceId;
			Constants.SIGNATUREVALUE=signature;
			Constants.DENSITYSCALE=getResources().getDisplayMetrics().density;
		}catch (Throwable t){
			t.printStackTrace();
		}
		Constants.RESULT_OK=RESULT_OK;
		Constants.RESULT_CANCELED=RESULT_CANCELED;
		WindowManager w = getWindowManager();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			Point size = new Point();
			w.getDefaultDisplay().getSize(size);
			Constants.Measuredwidth = size.x;
			Constants.Measuredheight = size.y;
		}else{
			Display d = w.getDefaultDisplay();
			Constants.Measuredwidth = d.getWidth();
			Constants.Measuredheight = d.getHeight();
		}
		ApplicationContextStore applicationContextStore= ApplicationContextStore.getInstance();
		Constants.WidthPixels=getResources().getDisplayMetrics().widthPixels;
		Constants.HeightPixels=getResources().getDisplayMetrics().heightPixels;
		if (applicationContextStore.getBACKICON()==null){
			applicationContextStore.setBACKICON(new BitmapDrawable(getResources(),generateScaleBitmap(40,R.drawable.back_icon)));
		}
		if (applicationContextStore.getMAPLOCATIONICONBITMAP()==null){
			int pixels = (int) (30 * Constants.DENSITYSCALE + 0.5f);
			applicationContextStore.setMAPLOCATIONICONBITMAP(resizeMapIcons("message_location_icon",pixels,pixels));
		}
		if (applicationContextStore.getCHATPEOPLEINFOICON()==null){
			applicationContextStore.setCHATPEOPLEINFOICON(generateScaleBitmap(40,R.drawable.people_icon));
		}
		if (applicationContextStore.getMESSAGECHOOSEPHOTOICON()==null){
			applicationContextStore.setMESSAGECHOOSEPHOTOICON(generateScaleBitmap(30,R.drawable.messagechoosephoto_icon));
		}
		if (applicationContextStore.getMESSAGECAMERAICON()==null){
			applicationContextStore.setMESSAGECAMERAICON(generateScaleBitmap(30,R.drawable.messagecamera_icon));
		}
		AsyncTask<Void,Void,Void> task=new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void aVoid) {
				Intent intent = new Intent(StartActivity.this, MainTabActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				finish();
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("StartActivity","KEYCODE_BACK on key down");
			onBackPressed();
		}
		return true;
	}

	private Bitmap resizeMapIcons(String iconName,int width, int height){
		Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
		return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
	}

	private Bitmap generateScaleBitmap(int scale,int imageResId){
		Bitmap b = BitmapFactory.decodeResource(getResources(), imageResId);
		int pixels = (int) (scale * Constants.DENSITYSCALE + 0.5f);
		return Bitmap.createScaledBitmap(b, pixels, pixels, true);
	}


}
