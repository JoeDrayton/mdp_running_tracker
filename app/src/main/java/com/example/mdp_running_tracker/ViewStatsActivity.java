package com.example.mdp_running_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
        sortByDate();
        loadTopFive();
        RadioGroup radioGroup = findViewById(R.id.runCategory);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // Listener for top five radio group
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                loadTopFive();
            }
        });
    }

    /**
     * Gets string for which top five is going to be displayed
     * @return
     */
    public String selectTopFive(){
        final RadioGroup runCategory = findViewById(R.id.runCategory);
        int selected = runCategory.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selected);
        switch (radioButton.getText().toString()){
            case "Distance":
                return "distance DESC";
            case "Speed":
                return "avgspeed DESC";
            case "Time":
                return "time DESC";
            default:
                return "";
        }
    }

    /**
     * Queries the runs and adds up statistics based on how many days since today it has been
     */
    public void sortByDate() {
        // queries the database and populates the fields with relevant info
        String[] columns = new String[] {
                ContentContract.DISTANCE,
                ContentContract.DATE,
                ContentContract.AVGSPEED
        };

        // ContentProvider query sorted by date
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, null, null, "date DESC");
        Log.d("g53mdp", DatabaseUtils.dumpCursorToString(c));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysPrior = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -28);
        Date monthPrior = calendar.getTime();
        double dayDistance = 0 , weekDistance = 0, monthDistance = 0, averageSpeed = 0;
        int i = 0;
        if(c.moveToFirst()){

            do {
                try {
                    if (isDateToday(c.getString(1), Calendar.getInstance().getTime())) {
                        dayDistance += c.getDouble(0);
                        weekDistance += c.getDouble(0);
                        monthDistance += c.getDouble(0);
                        averageSpeed += c.getDouble(2);
                        i++;
                    } else if (isDateExpired(c.getString(1), sevenDaysPrior)) {
                        weekDistance += c.getDouble(0);
                        monthDistance += c.getDouble(0);
                        averageSpeed += c.getDouble(2);
                        i++;
                    } else if (isDateExpired(c.getString(1), monthPrior)) {
                        weekDistance += c.getDouble(0);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }
        TextView runsToday = findViewById(R.id.runsToday);
        TextView runsWeek = findViewById(R.id.runsWeek);
        TextView runsMonth = findViewById(R.id.runsMonth);
        TextView avgSpeed = findViewById(R.id.avgSpeed);
        averageSpeed = averageSpeed/i;
        runsToday.setText("Distance today: " + String.format("%.2f", dayDistance) + "m");
        runsWeek.setText("Distance this week: " + String.format("%.2f", weekDistance) + "m");
        runsMonth.setText("Distance this month: " + String.format("%.2f", monthDistance) + "m");
        avgSpeed.setText("Average speed this week: " +String.format("%.2f", averageSpeed) + "m/s");
    }

    /**
     * Checks whether the activity happened today
     * @param input
     * @param today
     * @return
     * @throws ParseException
     */
    public boolean isDateToday(String input, Date today) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse(input);
        today = dateFormat.parse(dateFormat.format(today));
        return date.equals(today);
    }

    /**
     * Checks whether the run is past an expiry threshold passed as an argument
     * @param input
     * @param expiration
     * @return
     * @throws ParseException
     */
    public boolean isDateExpired(String input, Date expiration) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse(input);
        expiration = dateFormat.parse(dateFormat.format(expiration));
        return date.after(expiration);
    }

    /**
     * Go back
     * @param v
     */
    public void goBack(View v){
        finish();
    }

    /**
     * Loads the top five runs sorted by either Speed, time or distance
     */
    public void loadTopFive(){
        String[] columns = {
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.AVGSPEED,
                ContentContract.RATING
        };
        Cursor c = getContentResolver().query(ContentContract.RUNS_URI, columns, null, null, selectTopFive() + " LIMIT 5");

        String colsToDisplay[] = new String[]{
                ContentContract._ID,
                ContentContract.NAME,
                ContentContract.TIME,
                ContentContract.DISTANCE,
                ContentContract.AVGSPEED,
                ContentContract.RATING
        };
        int[] colResIds = new int[]{
                R.id._id,
                R.id.name,
                R.id.time,
                R.id.distance,
                R.id.speed,
                R.id.rating
        };
        // Sets the list view adapter to the cursor and relevant columns
        final ListView lv = findViewById(R.id.topFiveRuns);
        lv.setAdapter(new SimpleCursorAdapter(this, R.layout.runs, c, colsToDisplay, colResIds, 0));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("g53mdp", "position " + position + " id " + id);
                Intent intent = new Intent(ViewStatsActivity.this, ViewRunActivity.class);
                Bundle run = new Bundle();
                run.putInt("run id", (int) id);
                intent.putExtras(run);
                startActivity(intent);
            }
        });
    }
}
