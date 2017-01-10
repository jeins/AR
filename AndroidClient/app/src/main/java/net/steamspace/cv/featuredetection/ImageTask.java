package net.steamspace.cv.featuredetection;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by MJA on 10.01.2017.
 */

public class ImageTask extends AsyncTask<String, Integer, HashMap<String, String>>{
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected HashMap<String, String> doInBackground(String... params) {
        String apiUri = params[0] + "/image/list";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("latitude", params[1]);
            jsonBody.put("longitude", params[2]);
            RequestBody body = RequestBody.create(JSON, jsonBody.toString());
            Request request = new Request.Builder()
                    .url(apiUri)
                    .post(body)
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
