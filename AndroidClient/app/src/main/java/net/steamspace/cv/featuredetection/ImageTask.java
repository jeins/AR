package net.steamspace.cv.featuredetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by MJA on 10.01.2017.
 */

public class ImageTask extends AsyncTask<String, Void, byte[]>{
    OkHttpClient client = new OkHttpClient();
    String imageFile = "";
    private static final String  TAG                 = "ORBDetector::ImageTask";

    @Override
    protected byte[] doInBackground(String... params) {
        imageFile = "-53563.jpg";
        String apiUri = "http://ar.mjuan.me/api/image/-53563/jpg";
        Log.d(TAG, "ImageTaskParam: " + params[0]);
        try{
            Request request = new Request.Builder()
                    .url(apiUri)
                    .build();

            Response response = client.newCall(request).execute();

            return response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);

        File imagePath = new File(Utilities.getStorageDirectory().getPath() + File.separator + imageFile);

        try {
            if(bytes != null && bytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                FileOutputStream fos = new FileOutputStream(imagePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Log.d(TAG, "Saved image as: " + imagePath.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
