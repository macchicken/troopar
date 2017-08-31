package com.troopar.trooparapp.utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Barry on 7/01/2016.
 */

public class JSONParser {


    // constructor
    public JSONParser() {}

    public JSONObject makeRequestForHttp(String url, String method,HashMap<String,String> parameters) {
        // Making HTTP request
        String json;
        HttpURLConnection connection = null;
        Log.d("JSONParser",url);
        StringBuilder chaine = new StringBuilder("");
        try {
            URL urlCon = new URL(url);
            connection = (HttpURLConnection) urlCon.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod(method);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            Uri.Builder builder = new Uri.Builder();
            for (String key:parameters.keySet()){
                builder.appendQueryParameter(key,parameters.get(key));
            }
            String query = builder.build().getEncodedQuery();
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }
            json = chaine.toString();
            inputStream.close();
            rd.close();
            // try parse the string to a JSON object
            return new JSONObject(json);
            // return JSON String
        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e){
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }finally {
            assert connection != null;
            connection.disconnect();
        }
        return null;
    }


}
