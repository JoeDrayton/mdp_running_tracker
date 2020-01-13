package com.example.mdp_running_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddRunActivity extends AppCompatActivity {
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    ArrayList<Location> journey = new ArrayList<>();
    String imageFilePath;

    /**
     * Standard onCreate activity, uses super and sets content view to layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_run);
    }

    /**
     * Button method to sent an image intent
     * @param v
     */
    public void openCameraIntent(View v) {
        // Create intent for MediaStore image capture
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null){
            // Create file for image storage
            File photo = null;
            try {
                // calls createImage into file
                photo = createImage();
            } catch (IOException e) {
                // Error occurred while creating the File
                Log.d("g53mdp", e.toString());
            }
            if (photo != null) {
                // Creates uri for new image file
                Uri photoURI = FileProvider.getUriForFile(this,getApplicationContext().getPackageName() + ".provider", photo);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                // Starts activity
                startActivityForResult(pictureIntent,
                        REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    /**
     * After camera intent has activated and user has taken an image, come back with
     * result ok and display image on the AddRunActivity page
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // Checks we are coming back from image capture request
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            // Decode and rotate image so that it is the correct way round
            Bitmap myBitmap = BitmapFactory.decodeFile(imageFilePath);
            myBitmap = rotateImage(90, myBitmap);
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(myBitmap);
            Log.d("g53mdp", "image stored at " + imageFilePath);
        }
    }

    /**
     * Creates image file for storage
     * @return
     * @throws IOException
     */
    private File createImage() throws IOException {
        // Creates timestamp for the file path
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        // Stuffs file in pictures directory
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Sets global image path variable to image files path
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Uses the radio group to let the user select the weather for their activity
     * @return
     */
    public String getWeather(){
        // Finds radio group
        final RadioGroup weatherSelect = findViewById(R.id.weatherSelect);
        // Checks selected radiobutton
        int selected = weatherSelect.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton radioButton = findViewById(selected);
        // Returns string value
        return radioButton.getText().toString();
    }

    /**
     * Formats the time into Hours Minutes and seconds
     * @param time
     * @return
     */
    public static String formatTimeString(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("H:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

    /**
     * Calculates average speed from distance and time
     * @param time
     * @param distance
     * @return
     */
    public double getAverageSpeed(long time, float distance){
        long second = (time / 1000) % 60;
        return distance / second;
    }

    /**
     * Takes all the data passed from main activity and adds it into the content provider
     * @param v
     */
    public void addRun(View v){
        // Gets all relevant variables and calculates un-calculated data
        DecimalFormat df = new DecimalFormat("0.00");
        Bundle bundle = getIntent().getExtras();
        journey = bundle.getParcelableArrayList("journey");
        final EditText nameField = findViewById(R.id.name);
        final EditText descriptionField = findViewById(R.id.description);
        final SeekBar ratingField = findViewById(R.id.rating);
        long time = bundle.getLong("time");
        String date = bundle.getString("date");
        float distance = bundle.getFloat("distance");
        distance = Float.valueOf(df.format(distance));
        String weather = getWeather();
        double avgSpeed = getAverageSpeed(time, distance);
        avgSpeed = Double.valueOf(df.format(avgSpeed));
        String name = nameField.getText().toString();
        String description = descriptionField.getText().toString();
        int rating = ratingField.getProgress();
        String timeString = formatTimeString(time);
        // Puts all that data into a ContentValues object
        ContentValues newRun = new ContentValues();
        newRun.put(ContentContract.NAME, name);
        newRun.put(ContentContract.DESCRIPTION, description);
        newRun.put(ContentContract.TIME, timeString);
        newRun.put(ContentContract.DATE, date);
        newRun.put(ContentContract.DISTANCE, distance);
        newRun.put(ContentContract.WEATHER, weather);
        newRun.put(ContentContract.AVGSPEED, avgSpeed);
        newRun.put(ContentContract.IMAGE, imageFilePath);
        newRun.put(ContentContract.RATING, rating);

        // Validation for key variables to ensure the insert will perform as expected
        if (journey.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "No coordinates, please record again");
            finish();
        } else if (name.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "Please enter a run name");
        } else if (description.isEmpty()) {
            Tools.exceptionToast(getApplicationContext(), "Please enter a run description");
        } else {
            // Inserts data into run tables and collects run ID for coordinate values
            Uri result = getContentResolver().insert(ContentContract.RUNS_URI, newRun);
            long runID = Long.parseLong(result.getLastPathSegment());

            long coordinateID;
            ContentValues coords = new ContentValues();
            ContentValues runCoordID = new ContentValues();

            // For each location coordinate in the full journey adds to the coordinate table and adds to the
            // run_coordinates table
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
            // Finishes activity with result okay
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Basic function to use a matrix rotate to spin the image 90 degrees to negate
     * camera always posting images sideways
     * @param angle
     * @param bitmapSrc
     * @return
     */
    public Bitmap rotateImage(int angle, Bitmap bitmapSrc) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmapSrc, 0, 0,
                bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true);
    }

    /**
     * If cancel button pressed finish and go back to MainActivity
     * @param v
     */
    public void cancelRun(View v){
        finish();
    }
}
