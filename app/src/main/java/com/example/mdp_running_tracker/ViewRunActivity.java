package com.example.mdp_running_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ViewRunActivity extends AppCompatActivity {
    private Button data, maps;

    /**
     * onCreate override, holds button onClickListeners to select which fragment to view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_run);
        data = findViewById(R.id.data);
        maps = findViewById(R.id.maps);
        final int runID = getIntent().getExtras().getInt("run id");
        // Loads viewRunFragment first for user ease
        data.setBackgroundResource(R.drawable.roundedbuttonselected);
        Bundle runInfo = new Bundle();
        runInfo.putStringArray("runData", getRunInfo(runID));
        Fragment viewRunFragment = new ViewRunFragment();
        viewRunFragment.setArguments(runInfo);
        addFragment(viewRunFragment, false, "one");

        data.setOnClickListener(new View.OnClickListener(){
            /**
             * Loads the vewRunFragment
             * @param v
             */
            @Override
            public void onClick(View v){
                maps.setBackgroundResource(R.drawable.roundedbutton);
                data.setBackgroundResource(R.drawable.roundedbuttonselected);
                Bundle runInfo = new Bundle();
                runInfo.putStringArray("runData", getRunInfo(runID));
                Fragment viewRunFragment = new ViewRunFragment();
                viewRunFragment.setArguments(runInfo);
                addFragment(viewRunFragment, false, "one");
            }
        });

        /**
         * Loads the MapFragment
         */
        maps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                maps.setBackgroundResource(R.drawable.roundedbuttonselected);
                data.setBackgroundResource(R.drawable.roundedbutton);
                Bundle coords = new Bundle();
                ArrayList<LatLng> journey = getCoordinates(runID);
                coords.putParcelableArrayList("journey", journey);
                Fragment mapFragment = new MapFragment();
                mapFragment.setArguments(coords);
                addFragment(mapFragment, false, "one");
            }
        });
    }

    /**
     * Breaks down the Location ArrayList into a LatLng ArrayList
     * @param runID
     * @return
     */
    public ArrayList<LatLng> getCoordinates(int runID){
        ArrayList<LatLng> journey = new ArrayList<>();
        LatLng current;
        String[] id = {String.valueOf(runID)};
        String[] columns = {ContentContract.LATITUDE, ContentContract.LONGITUDE};
        String whereClause = ContentContract.RUN_ID + "= ?";

        // Query from ContentProvider for the run coordinates table
        Cursor c = getContentResolver().query(ContentContract.RUN_COORDINATES, columns, whereClause, id, null);
        Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        if(c.moveToFirst()){
            do {
                // Builds the LatLng list
                double latitude = c.getDouble(3);
                double longitude = c.getDouble(4);
                current = new LatLng(latitude, longitude);
                journey.add(current);
            } while(c.moveToNext());
        }
        return journey;
    }

    /**
     * Builds a String array for all the TextView fields for the information fragment
     * @param runID
     * @return
     */
    public String[] getRunInfo(final int runID) {
        // queries the database and populates the fields with relevant info
        String[] columns = new String[] {
                ContentContract.NAME,
                ContentContract.DESCRIPTION,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.AVGSPEED,
                ContentContract.RATING,
                ContentContract.IMAGE,
                ContentContract.WEATHER
        };
        final String[] args = new String[] {
                String.valueOf(runID)
        };
        final String whereClause = ContentContract._ID + "= ?";
        // ContentProvider query for the selected run's information
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, whereClause, args, null);
        //Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        if(c.moveToFirst()){
            String runName = c.getString(0);
            String runDescription = c.getString(1);
            String time = c.getString(2);
            String runTime = time;
            String runDistance = c.getString(3);
            String runAverageSpeed = c.getString(4);
            String runRating = c.getString(5);
            String runImagePath = c.getString(6);
            String runWeather = c.getString(7);

            return new String[] {runName, runDescription, runTime, runDistance,runAverageSpeed, runRating, runImagePath, runWeather};
        }
        return new String[]{""};
    }

    /**
     * Uses the fragment manager to build and show the fragment
     * @param fragment
     * @param addToBackStack
     * @param tag
     */
    public void addFragment(Fragment fragment, boolean addToBackStack, String tag){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.container_frame_back, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    /**
     * Finishes activty and returns to MainActivity
     * @param v
     */
    public void onBack(View v){
        finish();
    }

    /**
     * Uses the runID to delete the coordinates and run/coordinate relationships in the run_coordinates tables
     * @param runID
     */
    public void deleteRunCoordinates(int runID){
        String[] columns = {
                ContentContract.RUN_ID,
                ContentContract.COORDINATE_ID
        };
        String whereClause = ContentContract.RUN_ID + "= ?";
        Cursor c = getContentResolver().query(ContentContract.RUN_COORDINATES, columns, whereClause, new String[]{String.valueOf(runID)}, null);
        Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));

        // If query worked, delete the coordinate
        if(c.moveToFirst()){
            do {
                int coordinateId = c.getInt(2);
                // Delete the relationship between the run and the coordinates
                getContentResolver().delete(ContentContract.RUN_COORDINATES, ContentContract.RUN_ID + "=" + runID, null);
                // Delete coordinate
                getContentResolver().delete(ContentContract.COORDINATES_URI, ContentContract._ID + "=" + coordinateId, null);
            } while(c.moveToNext());
        }
    }

    /**
     * Button delete function, calls deleteRunCoordinates and deletes the run itself.
     *
     * Finishes with result OK
     * @param v
     */
    public void onDelete(View v){
        // Calls delete ingredients and deletes the recipe
        int runId = getIntent().getExtras().getInt("run id");
        deleteRunCoordinates(runId);
        getContentResolver().delete(ContentContract.RUNS_URI, ContentContract._ID + "="+runId, null);
        setResult(Activity.RESULT_OK, new Intent());
        finish();
    }
}
