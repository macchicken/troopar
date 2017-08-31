package com.troopar.trooparapp.utils;


import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Barry on 12/01/2016.
 */
public class Constants {

    public static final String lsep= System.getProperty("line.separator");
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_SUCCESS_STR = "{\"status\":\"success\"";

    public static final SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat timeformat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat simpleDateFormatUS=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final int oneDay=24*60*60*1000;
    public static final long ONE_MINUTE = 60 * 1000;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_MONTH = 30 * ONE_DAY;
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    public static final int nearbyRefreshId=0;
    public static final int refreshTimeDelay=3000;

    public static final String SIGNATURE="signature";
    public static final String DEVICEID="deviceId";
    public static final String EQUIPID="equipId";

    public static int Measuredwidth=768;
    public static int WidthPixels=768;
    public static int Measuredheight=1024;
    public static int HeightPixels=1024;

    public static final String REVIEWACT="review";
    public static final String PHOTOACT="photo";
    public static final String UPLOADPHOTOACT="upload_photo";
    public static final String POSTACT="post";
    public static final String CREATEEVENT="create_event";
    public static final String SHAREEVENT="share_event";
    public static final String JOINEVENT="join_event";
    public static final String EVENT="event";
    public static final String NOMOREDATA="nomore";

    public static final String eventPhotosBroadcastId="com.troopar.trooparapp.activity.eventPhotos";

    public static final String EVENTURL="http://www.troopar.com/event/?eventid=";

    public static int RESULT_OK=-1;
    public static int RESULT_CANCELED=0;
    public static boolean NOTINAPP=false;
    public static String DEVEICEIDVALUE;
    public static String SIGNATUREVALUE;
    public static float DENSITYSCALE=1;

    public static String USERID="";
    public static String USERNAME="";
    public static String USEREMAIL="";
    public static String USERGENDER="";
    public static String USERFIRSTNAME="";
    public static String USERLASTNAME="";
    public static String USERPHONE="";
    public static String AVATARORIGIN="";
    public static String AVATARSTANDARD="";
    public static String USERPASSWORD="";
    public static String PHOTOFILENAME="";
    public static String USERDOB="";
    public static String BACKGROUNDPHOTOFILENAME="";

    public static int RADIUS=5000;

    public static int RESETUSER=1;
    public static int CLEARUSER=3;
    public static int ADMINMESSAGE=7;

}