package ar.textdetection;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by MJA on 12.01.2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "TextDetection::MapActivity";

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location location;

    private HashMap<String, HashMap<String, String>> imgData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            //Toast.makeText(this, "Alles Ok!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_map);
            initMap();
        } else {

        }
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.map_action_traindata) {
            Intent i = new Intent(getApplicationContext(), TrainDataActivity.class);
            i.putExtra("latitude", location.getLatitude());
            i.putExtra("longitude", location.getLongitude());

            startActivity(i);
        } else if (item.getItemId() == R.id.map_action_scanning) {
            startActivity(new Intent(getApplicationContext(), DetectorActivity.class));
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(final Location location) {
        if(location == null){
            Toast.makeText(this, "Cant get current location!", Toast.LENGTH_LONG).show();
        } else{

            if(distance(location) > 200){
                Log.i(TAG, "locationIsChange!!!");
                new AsyncTask<Void, Void, String>(){

                    @Override
                    protected String doInBackground(Void... params) {
                        imgData = RestClient.getNearestData(location.getLatitude(), location.getLongitude());

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String params) {
                        ArrayList<LatLng> markersPosiiton = new ArrayList<LatLng>();

                        Set<String> keys = imgData.keySet();
                        for(String key : keys){
                            HashMap<String, String> data = imgData.get(key);

                            drawMarker(new LatLng(
                                    Double.parseDouble(data.get("latitude")),
                                    Double.parseDouble(data.get("longitude")))
                            );
                        }
                    }
                }.execute();
            }

            this.location = location;
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            googleMap.animateCamera(update);
        }
    }

    private double distance(Location newLocation) {
        if(location == null){
            return 1000;
        }

        final int R = 6371;

        Log.i(TAG, "Coorlocation: " + location.getLatitude() + '-' + location.getLongitude());
        Log.i(TAG, "CoorNewlocation: " + newLocation.getLatitude() + '-' + newLocation.getLongitude());

        Double latDistance = Math.toRadians(newLocation.getLatitude() - location.getLatitude());
        Double lonDistance = Math.toRadians(newLocation.getLongitude() - location.getLongitude());
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(location.getLatitude())) * Math.cos(Math.toRadians(newLocation.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    private void drawMarker(LatLng position){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);

        googleMap.addMarker(markerOptions);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng markerPosition = marker.getPosition();

        Log.i(TAG, "markerClick!");

        Set<String> keys = imgData.keySet();
        for(String key : keys){
            HashMap<String, String> data = imgData.get(key);
            Double dLatitude = Double.parseDouble(data.get("latitude"));
            Double dLongitude = Double.parseDouble(data.get("longitude"));

            Log.i(TAG, "markerPosition: " + markerPosition.latitude + '_' + markerPosition.longitude);
            Log.i(TAG, "dataPosition: " + dLatitude + '_' + dLongitude);

            if(markerPosition.latitude == dLatitude && markerPosition.longitude == dLongitude)
            {
                Intent i = new Intent(getApplicationContext(),DetectorActivity.class);
                i.putExtra("latitude", dLatitude);
                i.putExtra("longitude", dLongitude);
                i.putExtra("imageFile", data.get("imageFile"));

                startActivity(i);
            }
        }

        return false;
    }
}
