package ar.textdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by MJA on 01.12.2016.
 */

public class Utilities {
    private static final String  TAG                 = "TextDetection::Utilities";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static String writeToFile(String fileNameRoot, String data) {
        try {
            File mediaStorageDir = getStorageDirectory();
            File outputFile = File.createTempFile(fileNameRoot, ".yml", mediaStorageDir);
            FileOutputStream stream = new FileOutputStream(outputFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            stream.close();
            String fileName = outputFile.getAbsolutePath();
//            Log.i(TAG, fileName);
            return fileName;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static File saveImg(Mat outputImage) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.e(TAG, "Error creating media file, check storage permissions: ");
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Bitmap m_bmp = Bitmap.createBitmap(outputImage.width(), outputImage.height(),
                    Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputImage, m_bmp);
            m_bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "Saved image as: " + pictureFile.getName());
            return pictureFile;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return null;
    }

    public static Mat getImage(String imageName){
        File mediaStorageDir = getStorageDirectory();
        Mat imgMat = null;

        try{
            Bitmap bmp = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator + imageName);
            imgMat = new Mat();
            Utils.bitmapToMat(bmp, imgMat);
        } catch (Exception e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return imgMat;
    }

    public static ArrayList<String> getListOfImages()
    {
        File mediaStorageDir = new File(getStorageDirectory().getPath() + File.separator);
        ArrayList<String> imageList = new ArrayList<String>();

        for (File f : mediaStorageDir.listFiles()){
            if(f.isFile()){
                Log.i(TAG, "find image: " + f.getName());
                imageList.add(f.getName());
            }
        }

        return imageList;
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = getStorageDirectory();
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    public static File getStorageDirectory() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TextDetectionApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("TextDetectionApp", "failed to create directory");
                return null;
            }
        }
        return mediaStorageDir;
    }
}
