package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.HashMap;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;

/**
 * Created by Barry on 8/02/2016.
 */
public class JoinEventTask extends AsyncTask<Void, Integer, JSONObject> {

    private JSONParser jParser = new JSONParser();
    private String mUserId;
    private String mEventId;
    private PostExecuteCallBack postExecuteCallBack;


    public JoinEventTask(String userId,String eventId){
        super();
        mUserId=userId;
        mEventId=eventId;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        HashMap<String,String> parameters=new HashMap<>();
        parameters.put("userId",mUserId);
        parameters.put("eventId",mEventId);
        parameters.put(Constants.EQUIPID,Constants.DEVEICEIDVALUE);
        parameters.put(Constants.SIGNATURE,Constants.SIGNATUREVALUE);
        JSONObject obj = jParser.makeRequestForHttp(BuildConfig.API_READHOST + "/event/join_event.php", "POST", parameters);
        if (obj != null) {
            try {
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        postExecuteCallBack.postExecute(jsonObject);
    }

    public interface PostExecuteCallBack{
        void postExecute(JSONObject jsonObject);
    }

    public void setPostExecuteCallBack(PostExecuteCallBack postExecuteCallBack) {
        this.postExecuteCallBack = postExecuteCallBack;
    }


}
