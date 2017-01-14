package ar.textdetection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by MJA on 13.01.2017.
 */

public class TrainDataActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_data);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
