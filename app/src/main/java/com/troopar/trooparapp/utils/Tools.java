package com.troopar.trooparapp.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;


/**
 * Created by Barry on 11/01/2016.
 * common functions used in the application
 */
public class Tools {

    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

    public static String getDateOfWeekFromDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date time = null;
        try {
            time = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df.applyPattern( "EEE" );
        return df.format(time);
    }

    /**
     * format the date time with 20 Dec 2015
     * @param date
     * @return String
     */
    public static String formatTime(String date){
        String[] temp=date.trim().split("-");
        String month;
        switch (Integer.parseInt(temp[1])){
            case 1:month="Jan";break;
            case 2:month="Feb";break;
            case 3:month="Mar";break;
            case 4:month="Apr";break;
            case 5:month="May";break;
            case 6:month="June";break;
            case 7:month="July";break;
            case 8:month="Aug";break;
            case 9:month="Sep";break;
            case 10:month="Oct";break;
            case 11:month="Nov";break;
            case 12:month="Dev";break;
            default:month="Jan";
        }
        String dateOfMonth=temp[2].split(" ")[0];
        if (dateOfMonth.startsWith("0")){
            dateOfMonth=dateOfMonth.substring(1);
        }
        return dateOfMonth+' '+month+' '+temp[0];
    }

    public static String formatTimeToMinutes(String dataTime){
        try{
            String[] temp=dataTime.trim().split(" ")[1].split(":");
            int hour=Integer.parseInt(temp[0]);
            if (hour<=12){
                return String.format("%s:%sam",String.valueOf(hour),temp[1]);
            }else{
                return String.format("%s:%spm",String.valueOf(hour-12),temp[1]);
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        return "";
    }

    public static String parseDateOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:return "Mon";
            case 2:return "Tue";
            case 3:return "Wed";
            case 4:return "Thu";
            case 5:return "Fri";
            case 6:return "Sat";
            case 0:return "Sun";
        }
        return "Sun";
    }

    public static String parseDateOfMonth(String month) {
        switch (month) {
            case "01":return "Jan";
            case "02":return "Feb";
            case "03":return "Mar";
            case "04":return "Apr";
            case "05":return "May";
            case "06":return "June";
            case "07":return "July";
            case "08":return "Aug";
            case "09":return "Sept";
            case "10":return "Oct";
            case "11":return "Nov";
            case "12":return "Dec";
            default:return "Jan";
        }
    }

    public static long calculateTimeDiffByDay(String start,String end){
        try {
            Date dStart = Constants.timeformat.parse(start);
            Date dEnd = Constants.timeformat.parse(end);
            long timeDiff=dStart.getTime()-dEnd.getTime();
            return timeDiff<=0?0:timeDiff/Constants.oneDay;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long calculateTimeDiff(String start,String end){
        try {
            Date dStart = Constants.timeformat2.parse(start);
            Date dEnd = Constants.timeformat2.parse(end);
            return dStart.getTime()-dEnd.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String sha1OfStr(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aResult : result) {
            sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static boolean shouldAskPermission(){
        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean checkNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String calculateTimeRange(String startTime,String endTime){
        try {
            long startMi=Constants.timeformat.parse(startTime).getTime();
            long endMi=Constants.timeformat.parse(endTime).getTime();
            long timePassed=endMi-startMi;
            if (timePassed<= Constants.ONE_MINUTE){
                return "1 minute";
            }else if(timePassed <= Constants.ONE_HOUR){
                long timeIntoFormat = timePassed / Constants.ONE_MINUTE;
                return timeIntoFormat + " minutes";
            }else if (timePassed <= Constants.ONE_DAY) {
                long timeIntoFormat = timePassed / Constants.ONE_HOUR;
                if (timeIntoFormat>1){
                    return timeIntoFormat + " hrs";
                }else{
                    return timeIntoFormat + " hr";
                }
            }else if (timePassed <= Constants.ONE_MONTH) {
                long timeIntoFormat = timePassed / Constants.ONE_DAY;
               return timeIntoFormat + " day";
            }else if (timePassed <= Constants.ONE_YEAR) {
                long timeIntoFormat = timePassed / Constants.ONE_MONTH;
                return timeIntoFormat + " month";
            }else {
                long timeIntoFormat = timePassed / Constants.ONE_YEAR;
                return timeIntoFormat + " year";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String calculateTimeElapsed(String sourceTime) {
        long timeZoneDiff = Calendar.getInstance().getTimeZone().getRawOffset();
        long currentTime = System.currentTimeMillis()-timeZoneDiff;
        String updateAtValue = null;
        try {
            long timeIntoFormat;
            long timePassed = currentTime - Constants.timeformat.parse(sourceTime).getTime();
            if (timePassed < Constants.ONE_MINUTE) {
                updateAtValue = "just now";
            } else if (timePassed < Constants.ONE_HOUR) {
                timeIntoFormat = timePassed / Constants.ONE_MINUTE;
                String value = timeIntoFormat + " minutes";
                updateAtValue = String.format("%1$s ago", value);
            } else if (timePassed < Constants.ONE_DAY) {
                timeIntoFormat = timePassed / Constants.ONE_HOUR;
                String value = timeIntoFormat + " hour";
                updateAtValue = String.format("%1$s ago", value);
            } else if (timePassed < Constants.ONE_MONTH) {
                timeIntoFormat = timePassed / Constants.ONE_DAY;
                String value = timeIntoFormat + " day";
                updateAtValue = String.format("%1$s ago", value);
            } else if (timePassed < Constants.ONE_YEAR) {
                timeIntoFormat = timePassed / Constants.ONE_MONTH;
                String value = timeIntoFormat + " month";
                updateAtValue = String.format("%1$s ago", value);
            } else {
                timeIntoFormat = timePassed / Constants.ONE_YEAR;
                String value = timeIntoFormat + " year";
                updateAtValue = String.format("%1$s ago", value);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return updateAtValue;
    }

    public static boolean isNullString(String str){
        return str==null||"".equals(str.trim())||"null".equals(str.trim());
    }

    public static String checkAppDirectory(String subDirectory){
        String appDirName= Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+ BuildConfig.APP_NAME;
        File appDirectory=new File(appDirName);
        if (!appDirectory.exists()&&!appDirectory.mkdir()){
            Log.d("Tools","troopar fail to create app directory");
        }else{
            String audioDirName=appDirName+File.separator+subDirectory;
            File appAudioDir=new File(audioDirName);
            if (!appAudioDir.exists()&&!appAudioDir.mkdir()){
                Log.d("Tools","troopar fail to create app audio directory");
            }else{
                return audioDirName;
            }
        }
        return null;
    }

    public static String checkAppImageDirectory(){
        File mediaStorageDir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),BuildConfig.APP_NAME);
        if (!mediaStorageDir.exists()&&!mediaStorageDir.mkdir()){
            Log.d("MoreActivity","troopar fail to create directory");
        }
        return mediaStorageDir.getPath();
    }

    public static void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            SDK16.postOnAnimation(view, runnable);
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
        }
    }

    public static String replaceCharInStringList(String str,String value){
        return str.replace("["+value+",","[").replace(","+value+"]","]").replace(","+value+",",",");
    }

    public static JSONObject buildGroupChatMetaData(User user, JSONObject data) throws JSONException {
        if (user.getId()==-3||user.getId()==-5){
            JSONObject remarks=new JSONObject();
            remarks.put("uid",user.getFirstName());
            remarks.put("flag",user.getId()==-3?"event":"group");
            remarks.put("creator",user.getLastName());
            remarks.put("groupName",user.getUserName());
            remarks.put("smallImageUrl",user.getAvatarStandard());
            String[] groupUsers=user.getGender().substring(1,user.getGender().length()-1).split(",");//Json array format in string
            int[] userIds=new int[groupUsers.length];
            int i=0;
            for (String userId:groupUsers){
                userIds[i]=Integer.parseInt(userId);
                i++;
            }
            remarks.put("groupUsers",userIds);
            remarks.put("joiners",userIds);
            if (data!=null){
                Iterator<String> keysIt=data.keys();
                if (keysIt!=null){
                    while (keysIt.hasNext()){
                        String key=keysIt.next();
                        remarks.put(key,data.get(key));
                    }
                }
            }
            return remarks;
        }else{
            return data;
        }
    }


}
