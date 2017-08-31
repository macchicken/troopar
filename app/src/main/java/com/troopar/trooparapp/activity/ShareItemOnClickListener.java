package com.troopar.trooparapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.troopar.trooparapp.R;
import com.troopar.trooparapp.model.EventModel;
import com.troopar.trooparapp.myview.EventSharePopupWindow;
import com.troopar.trooparapp.utils.ApplicationContextStore;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;

/**
 * Created by Barry on 4/07/2016.
 */
public class ShareItemOnClickListener implements View.OnClickListener{

    private EventSharePopupWindow mEventSharePopupWindow;
    private Context mContext;
    private ShareDialog mShareDialog;
    private EventModel mEvent;

    public ShareItemOnClickListener(Context context, ShareDialog shareDialog) {
        mContext = context;
        mShareDialog = shareDialog;
    }

    @Override
    public void onClick(View v) {
        mEventSharePopupWindow.dismiss();
        switch (v.getId()) {
            case R.id.btn_troopar_share:
                Intent shareIntent4=new Intent(mContext,ShareEventActivity.class);
                shareIntent4.putExtra("eventModel", mEvent);
                mContext.startActivity(shareIntent4);
                break;
            case R.id.btn_weibo_share:
                Intent shareIntent3 = new Intent(Intent.ACTION_SEND);
                shareIntent3.setType("text/plain");
                String eventStartDate3=mEvent.getStartDate();
                String eventEndDate3=mEvent.getEndDate();
                String startDateOfWeek3= Tools.getDateOfWeekFromDate(eventStartDate3);
                String time3=String.format("%s,%s (%s)", startDateOfWeek3, Tools.formatTime(eventStartDate3), Tools.calculateTimeRange(eventStartDate3, eventEndDate3));
                shareIntent3.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n%s\n(share@Troopar)",mEvent.getName(),mEvent.getLocation(),time3, Constants.EVENTURL+mEvent.getId()));
                shareIntent3.setPackage("com.sina.weibo");
                mContext.startActivity(shareIntent3);
                break;
            case R.id.btn_tecentqq_share:
                Intent shareIntent6 = new Intent(Intent.ACTION_SEND);
                shareIntent6.setType("text/plain");
                String eventStartDate6=mEvent.getStartDate();
                String eventEndDate6=mEvent.getEndDate();
                String startDateOfWeek6=Tools.getDateOfWeekFromDate(eventStartDate6);
                String time6=String.format("%s,%s (%s)", startDateOfWeek6, Tools.formatTime(eventStartDate6), Tools.calculateTimeRange(eventStartDate6, eventEndDate6));
                shareIntent6.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n%s\n(share@Troopar)",mEvent.getName(),mEvent.getLocation(),time6,Constants.EVENTURL+mEvent.getId()));
                shareIntent6.setPackage("com.tencent.mobileqq");
                mContext.startActivity(shareIntent6);
                break;
            case R.id.btn_moreoptions_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                String eventStartDate=mEvent.getStartDate();
                String eventEndDate=mEvent.getEndDate();
                String startDateOfWeek=Tools.getDateOfWeekFromDate(eventStartDate);
                String time=String.format("%s,%s (%s)", startDateOfWeek, Tools.formatTime(eventStartDate), Tools.calculateTimeRange(eventStartDate, eventEndDate));
                shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n%s\n(share@Troopar)",mEvent.getName(),mEvent.getLocation(),time,Constants.EVENTURL+mEvent.getId()));
                shareIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(shareIntent, "share choosers"));
                break;
            case R.id.btn_instagram_share:
                AsyncTask<Void,Void,File> task=new AsyncTask<Void, Void, File>() {
                    @Override
                    protected File doInBackground(Void... params) {
                        FutureTarget<File> futureTarget= Glide.with(ApplicationContextStore.getInstance().getContext()).load(mEvent.getMediumImageUrl()).downloadOnly(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);// share medium image
                        try {
                            return futureTarget.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(File file) {
                        super.onPostExecute(file);
                        if (file!=null){
                            String imageTmpDir;
                            if ((imageTmpDir=Tools.checkAppDirectory("image"))!=null){
                                String[] urls=mEvent.getImage().split("/");// use original image name
                                String[] imgFilePart=urls[urls.length-1].split("\\.");
                                FileInputStream inStream = null;
                                FileOutputStream outStream = null;
                                boolean normalWritten=true;
                                File output = null;
                                try {
                                    inStream = new FileInputStream(file.getAbsolutePath());
                                    output=new File(String.format("%s/%s.%s",imageTmpDir,imgFilePart[0],imgFilePart[1]));
                                    outStream = new FileOutputStream(output);
                                    FileChannel inChannel = inStream.getChannel();
                                    FileChannel outChannel = outStream.getChannel();
                                    inChannel.transferTo(0, inChannel.size(), outChannel);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    normalWritten=false;
                                }finally {
                                    if (outStream!=null){
                                        try {
                                            outStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (inStream!=null){
                                        try {
                                            inStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if (normalWritten){
                                    Intent shareIntent5 = new Intent(Intent.ACTION_SEND);
                                    shareIntent5.setType("image/*");
                                    String eventStartDate5=mEvent.getStartDate();
                                    String eventEndDate5=mEvent.getEndDate();
                                    String startDateOfWeek5=Tools.getDateOfWeekFromDate(eventStartDate5);
                                    String time5=String.format("%s,%s (%s)", startDateOfWeek5, Tools.formatTime(eventStartDate5), Tools.calculateTimeRange(eventStartDate5, eventEndDate5));
                                    shareIntent5.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n%s\n(share@Troopar)",mEvent.getName(),mEvent.getLocation(),time5,Constants.EVENTURL+mEvent.getId()));
                                    shareIntent5.putExtra(Intent.EXTRA_SUBJECT, mEvent.getName());
                                    Log.d("NearbyActivity",String.format("instagram file path %s",output.getAbsolutePath()));
                                    shareIntent5.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + output.getAbsolutePath()));
                                    shareIntent5.setPackage("com.instagram.android");
                                    mContext.startActivity(shareIntent5);
                                }
                            }
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
                break;
            case R.id.btn_twitter_share:
                Intent shareIntent7 = new Intent(Intent.ACTION_SEND);
                shareIntent7.setType("text/plain");
                String eventStartDate7=mEvent.getStartDate();
                String eventEndDate7=mEvent.getEndDate();
                String startDateOfWeek7=Tools.getDateOfWeekFromDate(eventStartDate7);
                String time7=String.format("%s,%s (%s)", startDateOfWeek7, Tools.formatTime(eventStartDate7), Tools.calculateTimeRange(eventStartDate7, eventEndDate7));
                shareIntent7.putExtra(Intent.EXTRA_TEXT, String.format("%s\n%s\n%s\n%s\n(share@Troopar)",mEvent.getName(),mEvent.getLocation(),time7,Constants.EVENTURL+mEvent.getId()));
                shareIntent7.setPackage("com.twitter.android");
                mContext.startActivity(shareIntent7);
                break;
            case R.id.btn_facebook_share:
                if (ShareDialog.canShow(ShareLinkContent.class)){
                    ShareLinkContent content = new ShareLinkContent.Builder().setContentTitle(mEvent.getName()).setContentDescription(mEvent.getDescription()).setContentUrl(Uri.parse(Constants.EVENTURL+mEvent.getId())).build();
                    mShareDialog.show(content);
                }
                break;
            default:
                break;
        }
    }

    public void setEvent(EventModel event) {
        mEvent = event;
    }

    public void setEventSharePopupWindow(EventSharePopupWindow eventSharePopupWindow) {
        mEventSharePopupWindow = eventSharePopupWindow;
    }

    public void setShareDialog(ShareDialog shareDialog) {
        mShareDialog = shareDialog;
    }

    public void setContext(Context context) {
        mContext = context;
    }


}
