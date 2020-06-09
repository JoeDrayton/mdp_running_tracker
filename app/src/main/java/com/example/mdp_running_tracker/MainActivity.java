package com.example.mdp_running_tracker;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private boolean recording = false;
    static final int ACTIVITY_TWO_REQUEST_CODE = 1;
    private MyService.MyBinder myService = null;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * onCreate  function to request location permissions and initialise the location tracking service
     * Starts and binds service as well as registering broadcast receiver
     * Also displays rpreexisting runs in a listview
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(INITIAL_PERMS, 1337);
        // Start and bind service
        this.startService(new Intent(this, MyService.class));
        this.bindService(new Intent(this, MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        // Register broadcast receiver
        registerReceiver(gpsTracker, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        // Populate ListView with runs
        displayRuns();
    }

    /**
     * Queries the content provider for all existing runs and populates a ListView
     * Also sets an onClickListener for the ListView
     */
    public void displayRuns(){
        // Columns to retrieve
        String[] columns = {
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.AVGSPEED,
                ContentContract.RATING
        };
        // Query for all runs
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, null, null, null);

        // Columns to display
        String colsToDisplay[] = new String[]{
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.AVGSPEED,
                ContentContract.RATING
        };

        // Column IDs for the TextViews
        int[] colResIds = new int[]{
                R.id._id,
                R.id.name,
                R.id.time,
                R.id.distance,
                R.id.speed,
                R.id.rating
        };
        // Sets the list view adapter to the cursor and relevant columns
        final ListView lv = findViewById(R.id.runsListView);

        lv.setAdapter(new SimpleCursorAdapter(this, R.layout.runs, c, colsToDisplay, colResIds, 0));
        //Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));

        // Listener for the runs to go to specific run view activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("g53mdp", "position " + position + " id " + id);
                Intent intent = new Intent(MainActivity.this, ViewRunActivity.class);
                Bundle run = new Bundle();
                run.putInt("run id", (int) id);
                intent.putExtras(run);
                startActivityForResult(intent, ACTIVITY_TWO_REQUEST_CODE);
            }
        });
    }

    /**
     * Handles starting and stopping of recording activity by user
     *
     * When finishes recording sends location points and metadata to the AddRunActivity
     * @param v
     */
    public void onRecordActivity(View v){
        Button recorder = findViewById(R.id.recordButton);
        TextView time = findViewById(R.id.time);
        TextView distance = findViewById(R.id.distance);
        // If the service is already recording and the button is pressed enter the finish state
        if(recording){
            // Gathers all the variables for the new activity addition
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(Calendar.getInstance().getTime());
            // Sets the button text to record new activity for the next record
            recorder.setText("Record New Activity");
            time.setVisibility(View.INVISIBLE);
            distance.setVisibility(View.INVISIBLE);
            // New Intent and Bundle for transferring data
            Intent newRun = new Intent(MainActivity.this, AddRunActivity.class);
            Bundle bundle = new Bundle();
            ArrayList<Location> journey = myService.stop();
            // Check the journey array list is not empty, if it is inform the user no location data
            // has been recorded and set recording to false to try again
            if(journey.isEmpty()){
                Tools.exceptionToast(getApplicationContext(),"No gps recorded, please try again");
                recording = false;
            } else {
                // Fill the bundle will all the relevant data
                bundle.putParcelableArrayList("journey", journey);
                long totalTime = journey.get(journey.size() - 1).getTime() - journey.get(0).getTime();
                float floatDistance = myService.getDistance();
                bundle.putString("date", date);
                bundle.putLong("time", totalTime);
                bundle.putFloat("distance", floatDistance);
                newRun.putExtras(bundle);
                recording = false;
                // Start the add run activity
                startActivityForResult(newRun, ACTIVITY_TWO_REQUEST_CODE);
            }
        } else {
            // Set button text to finish and make timer and distance marker visible
            recorder.setText("Finish");
            time.setVisibility(View.VISIBLE);
            distance.setVisibility(View.VISIBLE);
            // Tell service to begin recording and set recording to true
            myService.record();
            recording = true;
        }
    }

    /**
     * Button onClick to take user to the stats page
     * @param v
     */
    public void viewStats(View v){
        if(!recording) {
            Intent viewStats = new Intent(MainActivity.this, ViewStatsActivity.class);
            startActivity(viewStats);
        } else {
            Tools.exceptionToast(getApplicationContext(),"Please finish recording before viewing stats");
        }
    }

    /**
     * Creates and handles service connection between MainActivity and service
     */
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        /**
         * Registers callback to the service for the timer when service connected
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (MyService.MyBinder) service;
            myService.registerCallback(callback);
        }

        /**
         * Unregisters callback when service is disconnected
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unRegisterCallback(callback);
            myService = null;
        }
    };
    private String ConvertSecondToString(int nSecondTime) {
        return LocalTime.MIN.plusSeconds(nSecondTime).toString();
    }

    ICallback callback = new ICallback() {
        @Override
        public void counterEvent(final int counter) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView time = findViewById(R.id.time);
                    time.setText("Active time is " + ConvertSecondToString(counter));
                    float floatDistance = myService.getDistance();
                    TextView distance = findViewById(R.id.distance);
                    DecimalFormat df = new DecimalFormat("0.00");

                    distance.setText("Total distance travelled is " + Double.valueOf(df.format(floatDistance)) + "m");
                }
            });
        }
    };

    /**
     * When the MainActivity is destroyed unbind the service and stop it
     */
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

    /**
     * Checks activity result for AddRunActivity, if result is ok then repopulate the runs ListView
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * Broadcast receiver for changes to the LocationManager, displays a message if changed
     */
    private BroadcastReceiver gpsTracker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                Tools.exceptionToast(getApplicationContext(), "Location changed, make sure it is enabled");
            }
        }
    };
}
