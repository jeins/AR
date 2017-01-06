package net.steamspace.cv.featuredetection;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.steamspace.cv.featuredetection.R;

public class MainActivity extends Activity implements CvCameraViewListener2{
    private static final String  TAG                 = "OCVSample::Activity";
    String _toastMsg = "";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int TRAIN = 8;
    public static final int SHOW_MATCHES = 9;
    public static final int SHOW_BOX = 10;
    public static final int SHOW_KEYPOINTS = 11;
    public static final int MIN_MATCHES_KEYPOINTS = 120;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem mItemShowMatches;
    private MenuItem             mItemShowBox;
    private MenuItem             mItemShowKeypoints;
    private CameraBridgeViewBase mOpenCvCameraView;

    public static int _viewMode = VIEW_MODE_RGBA;

    SeekBar _seekBarRansac;
    SeekBar _seekBarMinMax;
    TextView _minDistanceTextView;
    TextView _numMatchesTextView;
    TextView _ransacThresholdTextView;
    TextView _maxMinTextView;
    TextView textView;

    DescriptorExtractor descriptorExtractor;
    DescriptorMatcher _matcher;
    FeatureDetector _detector;

    Mat _descriptors;
    MatOfKeyPoint _keypoints;

    Mat _descriptors2;
    MatOfKeyPoint _keypoints2;

    int _lastViewMode = VIEW_MODE_RGBA;
    boolean _takePicture = false;

    // GUI Controls
    Mat _img1;
    String _numMatches;
    int _minDistance;
    int _ransacThreshold = 3;
    int _maxMin = 50;

    Menu _menu;
    MenuItem _modelMenu;
    int _featureDetectorID = FeatureDetector.ORB;
    int _descriptorExtractorID = DescriptorExtractor.ORB;

    int totalMatchesKeyPoints = 0;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    System.loadLibrary("nonfree");
//                    Log.i(TAG, "OpenCV loaded successfully");
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
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
//        _seekBarRansac = (SeekBar) findViewById(R.id.ransacSeekBar);
//        _seekBarMinMax = (SeekBar) findViewById(R.id.maxMinSeekBar);
//        _ransacThresholdTextView = (TextView) findViewById(R.id.ransacThreshold);
//        _maxMinTextView = (TextView) findViewById(R.id.maxMinValue);
        _numMatchesTextView = (TextView) findViewById(R.id.numMatches);
//        _minDistanceTextView = (TextView) findViewById(R.id.minValue);
        textView = (TextView) findViewById(R.id.textView);
//        _seekBarRansac.setOnSeekBarChangeListener(this);
//        _seekBarMinMax.setOnSeekBarChangeListener(this);
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
//        Log.i(TAG, "called onCreateOptionsMenu");
//        mItemPreviewRGBA  = menu.add("Reset");
//        mItemShowKeypoints = menu.add("Show Key Points");
//        mItemShowMatches = menu.add("Show Matches");
//        mItemShowBox = menu.add("Show Box");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            _viewMode = VIEW_MODE_RGBA;
//        else if (item == mItemShowMatches)
//            _viewMode = SHOW_MATCHES;
//        else if (item == mItemShowBox)
//            _viewMode = SHOW_BOX;
//        else if (item == mItemShowKeypoints)
//            _viewMode = SHOW_KEYPOINTS;
        else if (item.getItemId() == R.id.action_train)
            _viewMode = TRAIN;
//        else if (item.getItemId() == R.id.action_screen_shot)
//            _takePicture = true;
        else
            setModel();
//        else if (item.getItemId() == R.id.ORB || item.getItemId() == R.id.BRISK || item.getItemId() == R.id.ORBFREAK
////                || item.getItemId() == R.id.SIFT || item.getItemId() == R.id.SURF || item.getItemId() == R.id.SURFBRIEF
//                || item.getItemId() == R.id.STAR) {
//            int id = item.getItemId();
//            setModel(id);
//            item.setChecked(true);
//        }
//        else {
//            item.setChecked(!item.isChecked());
//            if (item.getItemId() == R.id.Ratio && item.isChecked()) {
//                _menu.findItem(R.id.KNN).setChecked(true);  // KNN must be checked if running ratio test.
//                showToast("Ratio Test requires KNN too, enabling KNN.");
//            }
//            if (item.getItemId() == R.id.KNN && _menu.findItem(R.id.Ratio).isChecked()) {
//                _menu.findItem(R.id.Ratio).setChecked(false);  // KNN must be checked if running ratio test.
//                showToast("Ratio Test requires KNN too, disabling Ratio test.");
//            }
//        }
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
//                    Log.e(TAG, e.getMessage());
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
            showToast("Detector is null. You must re-train.");
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
        String imageName = "IMG_20170103_110826.jpg";
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
//        showToast("Trained " +  _modelMenu.getTitle());
        return gray1;
    }

    private void updateTextViews() {
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "hoi: " + _numMatches);
                Log.i(TAG, "Total Matches keypoints: " + totalMatchesKeyPoints);
                if(totalMatchesKeyPoints > MIN_MATCHES_KEYPOINTS){
                    _numMatchesTextView.setText("SAMA Gambarnya");
                    textView.setText("Hello World");
                }else {
                    _numMatchesTextView.setText("GA SAMA!!!");
                    textView.setText(null);
                }
//                _minDistanceTextView.setText(String.valueOf(_minDistance));
//                _ransacThresholdTextView.setText(String.valueOf(_ransacThreshold));
//                _maxMinTextView.setText(String.valueOf(_maxMin));
            }
        });
    }

    private void showToast(String msg) {
        _toastMsg = msg;
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MainActivity.this, _toastMsg, Toast.LENGTH_SHORT).show();

            }
        });
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
//        _modeMenuItem = menu.findItem(R.id.action_settings);
//        Log.i(TAG, "ORB ID: " + R.id.ORB);
        _menu = menu;
//        _modelMenu = menu.findItem(R.id.model_selection);
        MenuItem item = menu.findItem(0);
//        item.setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }
    private void setModel() {
        _featureDetectorID = FeatureDetector.ORB;
        _descriptorExtractorID = DescriptorExtractor.ORB;
//        _modelMenu.setTitle("Model: ORB");
        _detector = null;   // force user to retrain.
        _viewMode = VIEW_MODE_RGBA;
        showToast("Model updated, please press 'Train'.");
    }
}
