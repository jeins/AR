package net.steamspace.cv.featuredetection;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by MJA on 09.01.2017.
 */

public class RestClient {
    private static final String  TAG = "ORBDetector::REST_CLIENT";
    private String url;
    public String result = "";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public RestClient()
    {
        url = "http://ar.mjuan.me/api/";
    }

    public void getVersion()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url + "version")
                .build();

//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    throw new IOException("Unexpected code " + response);
//                } else {
//                    String res = response.body().string();
//                    Log.i(TAG, "Result: " + res);
//                    result = res;
//                }
//            }
//        });
        try{

            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getLocationData(double latitude, double longitude)
    {
        OkHttpClient client = new OkHttpClient();
        HashMap<String, String> map = new HashMap<String, String>();

        try{
            JSONObject latLonObj = new JSONObject();
            latLonObj.put("latitude", latitude);
            latLonObj.put("longitude", longitude);

            JSONObject locationObj = new JSONObject();
            locationObj.put("location", latLonObj);

            Log.i(TAG, "getLocationDataReqBody: " + locationObj.toString());

            RequestBody body = RequestBody.create(JSON, locationObj.toString());

            Request request = new Request.Builder()
                    .url(url + "image/list")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            JSONObject responseObj = new JSONObject(response.body().string());
            Iterator<?> keys = responseObj.keys();

            while(keys.hasNext()){
                String key = (String)keys.next();
                map.put(key, responseObj.getString(key));

                Log.i(TAG, "HASILL: " + key + " " + responseObj.getString(key));
            }

            return map;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public String getResult()
    {
        return result;
    }
}
