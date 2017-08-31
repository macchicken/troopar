package com.troopar.trooparapp.activity.task;

import android.os.AsyncTask;

import com.troopar.trooparapp.BuildConfig;
import com.troopar.trooparapp.utils.Constants;
import com.troopar.trooparapp.utils.JSONParser;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Barry on 23/03/2016.
 */
public class ShareEventTask extends AsyncTask<Void, Integer, JSONObject> {

    private String userId;
    private String description;
    private String eventId;
    private String equipId;
    private String signature;
    private ExecuteCallBack executeCallBack;
    private JSONParser jParser = new JSONParser();


    public ShareEventTask(String eventId,String description) {
        super();
        this.eventId=eventId;
        this.description=description;
        userId=Constants.USERID;
        equipId=Constants.DEVEICEIDVALUE;
        signature=Constants.SIGNATUREVALUE;
    }

    @Override
    protected void onPreExecute() {
        executeCallBack.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        executeCallBack.onProgressUpdate(values[0]);
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String,String> parameters=new HashMap<>();
        parameters.put("userId",userId);
        parameters.put("eventId",eventId);
        parameters.put("description",description);
        parameters.put(Constants.EQUIPID,equipId);
        parameters.put(Constants.SIGNATURE, signature);
        return jParser.makeRequestForHttp(BuildConfig.API_READHOST + "/event/share_event.php", "POST", parameters);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        executeCallBack.onPostExecute(jsonObject);
    }

    public interface ExecuteCallBack{
        void onPreExecute();
        void onProgressUpdate(Integer value);
        void onPostExecute(JSONObject jsonObject);
    }

    public void setExecuteCallBack(ExecuteCallBack executeCallBack) {
        this.executeCallBack = executeCallBack;
    }


}
