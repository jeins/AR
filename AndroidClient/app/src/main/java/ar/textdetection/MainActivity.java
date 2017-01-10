package ar.textdetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import ar.textdetection.R;

public class MainActivity extends Activity implements CvCameraViewListener2{
    private static final String  TAG                 = "TextDetection::Activity";
    String _toastMsg = "";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int TRAIN = 8;
    public static final int SHOW_MATCHES = 9;
    public static final int SHOW_BOX = 10;
    public static final int SHOW_KEYPOINTS = 11;
    public static final int MIN_MATCHES_KEYPOINTS = 120;

    private MenuItem             mItemPreviewRGBA;
    private CameraBridgeViewBase mOpenCvCameraView;

    public static int _viewMode = VIEW_MODE_RGBA;

    TextView textView;

    DescriptorExtractor descriptorExtractor;
    DescriptorMatcher _matcher;
    FeatureDetector _detector;

    Mat _descriptors;
    MatOfKeyPoint _keypoints;

    Mat _descriptors2;
    MatOfKeyPoint _keypoints2;

    // GUI Controls
    Mat _img1;
    String _numMatches;
    int _minDistance;
    int _maxMin = 50;

    Menu _menu;
    int _featureDetectorID = FeatureDetector.ORB;
    int _descriptorExtractorID = DescriptorExtractor.ORB;

    int totalMatchesKeyPoints = 0;

    RestClient restClient;
    HashMap<String, String> locationData;

    String imageName;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
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

    public MainActivity() {
//        Log.i(TAG, "Instantiated new " + this.getClass());
        restClient = new RestClient();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        textView = (TextView) findViewById(R.id.textView);

        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {
                locationData = restClient.getLocationData(52.456925, 13.526658); //TODO: SHOULD BE DYNAMIC!
                imageName = locationData.get("name") + '.' + locationData.get("extension");
                return null;
            }

            @Override
            protected void onPostExecute(String params) {
                new ImageTask().execute(locationData.get("name"), locationData.get("extension"));
            }
        }.execute();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemPreviewRGBA)
            _viewMode = VIEW_MODE_RGBA;
        else if (item.getItemId() == R.id.action_train)
            _viewMode = TRAIN;
        else
            setModel();

        return true;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();

        switch (MainActivity._viewMode) {
            case MainActivity.VIEW_MODE_RGBA:
                break;

            case MainActivity.TRAIN:
                _viewMode = SHOW_MATCHES;
                return trainFeatureDetector();
            case MainActivity.SHOW_MATCHES:
            case MainActivity.SHOW_BOX:
            case MainActivity.SHOW_KEYPOINTS:
                try {
                    Mat gray2 = inputFrame.gray();
                    List<DMatch> good_matches = findMatches(inputFrame);
                    if (good_matches == null) return gray2;
                }
                catch (Exception e) {
                    _numMatches = "";
                    _minDistance = -1;
                    return rgba;
                }
        }

        return rgba;
    }

    private List<DMatch> findMatches(CvCameraViewFrame inputFrame) {
        Log.i(TAG, "Start match");
        Mat gray2 = inputFrame.gray();
        _descriptors2 = new Mat();
        _keypoints2 = new MatOfKeyPoint();
        if (_detector == null) {
            return null;
        }

        _detector.detect(gray2, _keypoints2);
        descriptorExtractor.compute(gray2, _keypoints2, _descriptors2);

        List<DMatch> matches12_list;

        MatOfDMatch matches12 = new MatOfDMatch();
        _matcher.match(_descriptors, _descriptors2, matches12);
        matches12_list = matches12.toList();

        List<DMatch> filtered_list;
        filtered_list = matches12_list;

        double max_dist = 0;
        double min_dist = 100;

        //-- Quick calculation of max and min distances between keypoints
        for (int i = 0; i < filtered_list.size(); i++) {
            double dist = filtered_list.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if (dist > max_dist)
                max_dist = dist;
        }

        //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
        List<DMatch> good_matches_list = new ArrayList<>();
        for (int i = 0; i < filtered_list.size(); i++) {
            if (filtered_list.get(i).distance < _maxMin) {
                good_matches_list.add(filtered_list.get(i));
            }
        }

        Log.i(TAG, "Found this many matches: " + good_matches_list.size());
        totalMatchesKeyPoints = good_matches_list.size();
        _numMatches = String.format("%d/%d/%d", matches12_list.size(), filtered_list.size(), good_matches_list.size());
        _minDistance = (int) min_dist;
        updateTextViews();

        return good_matches_list;
    }

    private Mat trainFeatureDetector() {
        Mat gray1 = Utilities.getImage(imageName);//inputFrame.gray();
        _descriptors = new Mat();
        _keypoints = new MatOfKeyPoint();
        _detector = FeatureDetector.create(_featureDetectorID);

        descriptorExtractor = DescriptorExtractor.create(_descriptorExtractorID);

        _matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        _detector.detect(gray1, _keypoints, _descriptors);
        descriptorExtractor.compute(gray1, _keypoints, _descriptors);

        _img1 = gray1.clone();

        Log.i(TAG, "currImageName: " + imageName);
        Log.i(TAG, "currImageKeypoints: " + _keypoints.toList().size());
        return gray1;
    }

    private void updateTextViews() {
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
            Log.i(TAG, "hoi: " + _numMatches);
            Log.i(TAG, "Total Matches keypoints: " + totalMatchesKeyPoints);
            if(totalMatchesKeyPoints > MIN_MATCHES_KEYPOINTS){
                textView.setText(locationData.get("message"));
            }else {
                textView.setText(null);
            }
            }
        });
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        _menu = menu;
        MenuItem item = menu.findItem(0);

//        item.setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }
    private void setModel() {
        _featureDetectorID = FeatureDetector.ORB;
        _descriptorExtractorID = DescriptorExtractor.ORB;
        _detector = null;   // force user to retrain.
        _viewMode = VIEW_MODE_RGBA;
    }
}
