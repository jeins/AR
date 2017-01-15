package ar.textdetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MJA on 13.01.2017.
 */

public class TrainDataActivity extends AppCompatActivity{
    private static final String TAG = "TextDetection::TrainDataActivity";
    private double latitude;
    private double longitude;

    private EditText txtMessage;
    private EditText txtLatitude;
    private EditText txtLongitude;
    private Button btnTrainImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_data);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        txtLatitude = (EditText) findViewById(R.id.txt_latitude);
        txtLongitude = (EditText) findViewById(R.id.txt_longitude);
        txtMessage = (EditText) findViewById(R.id.txt_message);
        btnTrainImage = (Button) findViewById(R.id.btn_train_image);

        txtLatitude.setText(Double.toString(latitude));
        txtLongitude.setText(Double.toString(longitude));

        btnTrainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encryptedContent = generateRequestContent();
                Intent i = new Intent(getApplicationContext(), TrainActivity.class);
                i.putExtra("content", encryptedContent);

                Log.i(TAG, "contentEncrypt: " + encryptedContent);

                startActivity(i);
            }
        });
    }

    private String generateRequestContent() {
        try {
            JSONObject latLon = new JSONObject();
            latLon.put("latitude", latitude);
            latLon.put("longitude", longitude);

            JSONObject contentObj = new JSONObject();
            contentObj.put("location", latLon);
            contentObj.put("message", txtMessage.getText().toString());

            return Base64.encodeToString(contentObj.toString().getBytes(), Base64.NO_WRAP);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
