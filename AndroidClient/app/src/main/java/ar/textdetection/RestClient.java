package ar.textdetection;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by MJA on 09.01.2017.
 */

public class RestClient {
    private static final String  TAG = "TextDetection::REST_CLIENT";
    private static final String url = "http://ar.mjuan.me/api/";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static String getVersion()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url + "version")
                .build();
        try{

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, String> getLocationData(double latitude, double longitude)
    {
        OkHttpClient client = new OkHttpClient();
        HashMap<String, String> map = new HashMap<String, String>();
//        ImageTask imageTask = new ImageTask();

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

//            imageTask.execute(map.get("name"), map.get("extension"));

            return map;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, HashMap<String, String>> getNearestData(double latitude, double longitude)
    {
        OkHttpClient client = new OkHttpClient();
        HashMap<String, HashMap<String, String>> results = new HashMap<>();

        try{
            Request request = new Request.Builder()
                    .url(url + "image/nearest-from/" + latitude + '/' + longitude)
                    .build();

            Log.i(TAG, "requestGetNearestData: " + url + "image/nearest-from/" + latitude + '/' + longitude);

            Response response = client.newCall(request).execute();

            JSONArray resArrObj = new JSONObject(response.body().string()).getJSONArray("data");

            for(int i=0; i<resArrObj.length(); i++){
                HashMap<String, String> content = new HashMap<>();
                JSONObject resObj = resArrObj.getJSONObject(i);

                String resLatitude = Double.toString(resObj.getDouble("latitude"));
                String resLongitude = Double.toString(resObj.getDouble("longitude"));

                content.put("imageFile", resObj.getString("name") + '.' + resObj.getString("extension"));
                content.put("message", resObj.getString("message"));
                content.put("latitude", resLatitude);
                content.put("longitude", resLongitude);

                results.put(resLatitude + ',' + resLongitude, content);
            }

            Log.i(TAG, "resultGetNearestData: " + results.toString());

            return results;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
