package com.example.mdp_running_tracker;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean recording = false;
    static final int ACTIVITY_TWO_REQUEST_CODE = 1;
    private MyService.MyBinder myService = null;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(INITIAL_PERMS, 1337);
        this.startService(new Intent(this, MyService.class));
        this.bindService(new Intent(this, MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        displayRuns();
        //journey.clear();
    }


    public void displayRuns(){
        String[] columns = {
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.RATING
        };
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, null, null, null);

        String colsToDisplay[] = new String[]{
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.RATING
        };
        int[] colResIds = new int[]{
                R.id._id,
                R.id.name,
                R.id.time,
                R.id.distance,
                R.id.rating
        };
        // Sets the list view adapter to the cursor and relevant columns
        final ListView lv = findViewById(R.id.runsListView);
        lv.setAdapter(new SimpleCursorAdapter(this, R.layout.runs, c, colsToDisplay, colResIds, 0));
        //Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("g53mdp", "position " + position + " id " + id);
                Intent intent = new Intent(MainActivity.this, ViewRunActivity.class);
                Bundle recipe = new Bundle();
                recipe.putInt("run id", (int) id);
                intent.putExtras(recipe);
                startActivityForResult(intent, ACTIVITY_TWO_REQUEST_CODE);
            }
        });
    }
    public void onRecordActivity(View v){
        Button recorder = findViewById(R.id.recordButton);
        TextView time = findViewById(R.id.time);
        TextView distance = findViewById(R.id.distance);
        if(recording){
            recorder.setText("Record New Activity");
            time.setVisibility(View.INVISIBLE);
            distance.setVisibility(View.INVISIBLE);
            Intent newRun = new Intent(MainActivity.this, AddRun.class);
            Bundle bundle = new Bundle();
            ArrayList<Location> journey = myService.stop();
            bundle.putParcelableArrayList("journey", journey);
            long totalTime = journey.get(journey.size()-1).getTime() - journey.get(0).getTime();
            float floatDistance = myService.getDistance();
            bundle.putLong("time", totalTime);
            bundle.putFloat("distance", floatDistance);
            newRun.putExtras(bundle);
            recording = false;
            startActivityForResult(newRun, ACTIVITY_TWO_REQUEST_CODE);
        } else {
            recorder.setText("Finish");
            time.setVisibility(View.VISIBLE);
            distance.setVisibility(View.VISIBLE);
            myService.record();
            recording = true;
        }
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (MyService.MyBinder) service;
            myService.registerCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unRegisterCallback(callback);
            myService = null;
        }
    };

    ICallback callback = new ICallback() {
        @Override
        public void counterEvent(final int counter) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView time = findViewById(R.id.time);
                    time.setText("Active time is " + counter);
                    float floatDistance = myService.getDistance();
                    TextView distance = findViewById(R.id.distance);
                    distance.setText(("Total distance travelled is " + floatDistance));
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("g53mdp", "MainActivity onDestroy");
        if(serviceConnection!=null){
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        this.stopService(new Intent(this, MyService.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if returning from activity two and the result is ok, reload the query
        if (requestCode == ACTIVITY_TWO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                displayRuns();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
    }
}
