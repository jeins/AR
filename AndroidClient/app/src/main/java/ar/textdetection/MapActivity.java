package ar.textdetection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by MJA on 12.01.2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TextDetection::MapActivity";

    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(googleServicesAvailable()){
            Toast.makeText(this, "Alles Ok!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_map);
            initMap();
        } else{

        }
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable()
    {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if(isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else{
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();;
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.map_action_train){
            startActivity(new Intent(getApplicationContext(), TrainActivity.class));
        } else if(item.getItemId() == R.id.map_action_scanning){
            startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
        }

        return true;
    }
}
