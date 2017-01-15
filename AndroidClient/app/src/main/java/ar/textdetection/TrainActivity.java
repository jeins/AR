package ar.textdetection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by MJA on 13.01.2017.
 */

public class TrainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "TextDetection::TrainActivity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int TRAIN = 8;
    public static int _viewMode = VIEW_MODE_RGBA;

    private CameraBridgeViewBase mOpenCvCameraView;

    private String contentRequest;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    System.loadLibrary("nonfree");
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_train);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        contentRequest = getIntent().getStringExtra("content");
        Log.i(TAG, "contentReq: " + contentRequest);
    }

    private void trainImage(CvCameraViewFrame inputFrame) {
        Mat trainedImage = inputFrame.gray();

        File tmpImage = Utilities.saveImg(trainedImage);
        Log.i(TAG, "uriReq: "+ "http://ar.mjuan.me/api/image/" + contentRequest);
        Ion.with(getApplicationContext())
                .load("http://ar.mjuan.me/api/image/" + contentRequest)
                .setMultipartFile("file", "image/jpeg", tmpImage)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.i(TAG, "result: " + result.toString());

                        final Context context = getApplicationContext();
                        Handler handler =  new Handler(context.getMainLooper());
                        handler.post( new Runnable(){
                            public void run(){
                                final Toast toast = Toast.makeText(context, "Your Text is Safe!",Toast.LENGTH_LONG);
                                toast.show();
                                new CountDownTimer(1000, 1000){
                                    public void onTick(long millisUntilFinished) {toast.show();}
                                    public void onFinish() {
                                        toast.cancel();

                                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                                    }
                                }.start();
                            }
                        });
                    }
                });
    }

    @Override
    public void onCameraViewStarted(int i, int i1) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame cvCameraViewFrame) {
        Mat rgba = cvCameraViewFrame.rgba();

        switch (TrainActivity._viewMode) {
            case TrainActivity.VIEW_MODE_RGBA:
                break;
            case TrainActivity.TRAIN:
                _viewMode = VIEW_MODE_RGBA;
                trainImage(cvCameraViewFrame);
                break;
        }
        return rgba;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.train_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.train_action_train)
            _viewMode = TRAIN;
        else
            _viewMode = VIEW_MODE_RGBA;

        return true;
    }
}
