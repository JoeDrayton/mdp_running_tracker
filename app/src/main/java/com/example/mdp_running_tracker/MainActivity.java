package com.example.mdp_running_tracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean recording = false;
    private ArrayList<LatLng> journey = new ArrayList<>();
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //journey.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRecordActivity(View v){
        if(recording){
            for(int i=0;i<journey.size();i++){
                LatLng current = journey.get(i);
                Log.d("g53mdp",current.latitude + ":" + current.longitude);
            }
            recording = false;
        } else {
            requestPermissions(INITIAL_PERMS, 1337);
            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            MyLocationListener locationListener = new MyLocationListener();

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5, //min time interval between updates);
                        1, //min distance between updates, in metres
                        locationListener);
            } catch (SecurityException e) {
                Log.d("g53mdp", e.toString());
            }
            recording = true;
        }
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            journey.add(current);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // information about the signal
            Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            // the user enabled for example the GPS
            Log.d("g53mdp", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // the user disabled for example the GPS
            Log.d("g53mdp", "onProviderDisabled: " + provider);
        }
    }
}
