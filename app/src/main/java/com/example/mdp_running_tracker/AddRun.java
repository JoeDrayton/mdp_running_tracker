package com.example.mdp_running_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import java.util.ArrayList;

public class AddRun extends AppCompatActivity {

    ArrayList<Location> journey = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_viewer);

    }

    public String timeToString(long time){
        long millis = time % 1000;
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }

    public void addRun(View v){
        Bundle bundle = getIntent().getExtras();
        journey = bundle.getParcelableArrayList("journey");
        final EditText nameField = findViewById(R.id.name);
        final EditText descriptionField = findViewById(R.id.description);
        final SeekBar ratingField = findViewById(R.id.rating);
        long time = bundle.getLong("time");
        float distance = bundle.getFloat("distance");
        // image bs placeholder
        String name = nameField.getText().toString();
        String description = descriptionField.getText().toString();
        int rating = ratingField.getProgress();
        ContentValues newRun = new ContentValues();
        newRun.put(ContentContract.NAME, name);
        newRun.put(ContentContract.DESCRIPTION, description);
        newRun.put(ContentContract.TIME, timeToString(time));
        newRun.put(ContentContract.DISTANCE, distance);
        newRun.put(ContentContract.RATING, rating);
        if (journey.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "No coordinates, please record again");
            finish();
        } else if (name.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "Please enter a run name");
        } else if (description.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "Please enter a run description");
        } else {
            Uri result = getContentResolver().insert(ContentContract.RUNS_URI, newRun);
            long runID = Long.parseLong(result.getLastPathSegment());

            long coordinateID;
            ContentValues coords = new ContentValues();
            ContentValues runCoordID = new ContentValues();

            for (Location current : journey) {
                coords.put(ContentContract.LATITUDE, current.getLatitude());
                coords.put(ContentContract.LONGITUDE, current.getLongitude());
                Log.d("g53mdp", "Inserting :" + current.getLatitude() + ":" + current.getLongitude());
                result = getContentResolver().insert(ContentContract.COORDINATES_URI, coords);
                coordinateID = Long.parseLong(result.getLastPathSegment());
                runCoordID.put(ContentContract.RUN_ID, runID);
                runCoordID.put(ContentContract.COORDINATE_ID, coordinateID);
                getContentResolver().insert(ContentContract.RUN_COORDINATES, runCoordID);
            }
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public void cancelRun(View v){
        finish();
    }
}
