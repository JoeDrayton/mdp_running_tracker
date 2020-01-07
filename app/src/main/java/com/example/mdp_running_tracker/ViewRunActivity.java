package com.example.mdp_running_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_run);
        data = findViewById(R.id.data);
        maps = findViewById(R.id.maps);
        final int runID = getIntent().getExtras().getInt("run id");

        data.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Bundle runInfo = new Bundle();
                runInfo.putStringArray("runData", getRunInfo(runID));
                Fragment viewRunFragment = new ViewRunFragment();
                viewRunFragment.setArguments(runInfo);
                addFragment(viewRunFragment, false, "one");
            }
        });

        maps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Bundle coords = new Bundle();
                ArrayList<LatLng> journey = getCoordinates(runID);
                coords.putParcelableArrayList("journey", journey);
                Fragment mapFragment = new MapFragment();
                mapFragment.setArguments(coords);
                addFragment(mapFragment, false, "one");
            }
        });
    }

    public ArrayList<LatLng> getCoordinates(int runID){
        ArrayList<LatLng> journey = new ArrayList<>();
        LatLng current;
        String[] id = {String.valueOf(runID)};
        String[] columns = {ContentContract.LATITUDE, ContentContract.LONGITUDE};
        String whereClause = ContentContract.RUN_ID + "= ?";

        Cursor c = getContentResolver().query(ContentContract.RUN_COORDINATES, columns, whereClause, id, null);
        Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        if(c.moveToFirst()){
            do {
                double latitude = c.getDouble(3);
                double longitude = c.getDouble(4);
                current = new LatLng(latitude, longitude);
                journey.add(current);
            } while(c.moveToNext());
        }
        return journey;
    }

    public String[] getRunInfo(final int runID) {
        // queries the database and populates the fields with relevant info
        String[] columns = new String[] {
                ContentContract.NAME,
                ContentContract.DESCRIPTION,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.RATING
        };
        final String[] args = new String[] {
                String.valueOf(runID)
        };
        final String whereClause = ContentContract._ID + "= ?";
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, whereClause, args, null);
        //Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        if(c.moveToFirst()){
            String runName = c.getString(0);
            String runDescription = c.getString(1);
            String runTime = c.getString(2);
            String runDistance = c.getString(3);
            String runRating = c.getString(4);

            return new String[] {runName, runDescription, runTime, runDistance, runRating};
        }
        return new String[]{""};
    }
    public void addFragment(Fragment fragment, boolean addToBackStack, String tag){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.container_frame_back, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    public void onBack(View v){
        finish();
    }

    public void deleteRunCoordinates(int runID){
        // deletes the relationship between recipe and ingredient then verifies the ingredient has no other relationships
        String[] columns = {
                ContentContract.RUN_ID,
                ContentContract.COORDINATE_ID
        };
        String whereClause = ContentContract.RUN_ID + "= ?";
        String whereClause2 = ContentContract.COORDINATE_ID + "= ?";
        Cursor i;
        Cursor c = getContentResolver().query(ContentContract.RUN_COORDINATES, columns, whereClause, new String[]{String.valueOf(runID)}, null);
        Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        getContentResolver().delete(ContentContract.RUN_COORDINATES, ContentContract.RUN_ID + "=" + runID, null);
        if(c.moveToFirst()){
            do {
                int coordinateId = c.getInt(2);
                getContentResolver().delete(ContentContract.COORDINATES_URI, ContentContract._ID + "=" + coordinateId, null);
            } while(c.moveToNext());
        }
    }

    public void onDelete(View v){
        // Calls delete ingredients and deletes the recipe
        int runId = getIntent().getExtras().getInt("run id");
        deleteRunCoordinates(runId);
        getContentResolver().delete(ContentContract.RUNS_URI, ContentContract._ID + "="+runId, null);
        setResult(Activity.RESULT_OK, new Intent());
        finish();
    }
}
