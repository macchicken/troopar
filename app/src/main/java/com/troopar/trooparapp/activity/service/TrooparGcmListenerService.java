package com.troopar.trooparapp.activity.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.activity.StartActivity;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;


/**
 * Created by Barry on 28/06/2016.
 */
public class TrooparGcmListenerService extends GcmListenerService {

    private static final int MESSAGENOTIFICATION=630;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String title = data.getString("title");
        Log.d("TrooparGcmListenerSer",String.format("From: %s, Message: %s, Title: %s",from,message,title));
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String myUserId=sharedPreferences.getString("userId",null);
        sendNotification(title,message,myUserId,Constants.NOTINAPP);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title,String message,String userId,boolean notInApp) {
        if (Tools.isNullString(userId)){// in case of not login or data being deleted
            return;
        }
        if (LocalDBHelper.getInstance()!=null){
            LocalDBHelper.getInstance().updateTotalUnreadMessageCount(userId);
        }else{
            LocalDBHelper.getUserInstance(getApplicationContext(),userId).updateTotalUnreadMessageCount(userId);
        }
        if (notInApp) {
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, MESSAGENOTIFICATION, intent, PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.troopar_logo_r9_circle_nofitication)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.troopar_logo_r9_circle))
                    .setContentTitle(Tools.isNullString(title)?"Troopar Message":title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder.build());
        }else{
            Intent intent=new Intent("TrooparGcmListenerService.local.message.data");
            intent.putExtra("status", Constants.RESULT_OK);
            intent.putExtra("increment",true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }


}
