package com.example.mdp_running_tracker;

import android.content.Context;
import android.database.ContentObservable;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ViewRunFragment extends Fragment {
    public ViewRunFragment() {
        // Required empty public constructor
    }

    /**
     * Calls super method
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the view for the fragment
     *
     * Loads in all the run data and sets it all to its relevant fields
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inf = inflater.inflate(R.layout.fragment_view_run, container, false);

        // Loads and sets various fields
        String[] runData = getArguments().getStringArray("runData");
        TextView runName = inf.findViewById(R.id.name);
        runName.setText("Run name: " + runData[0]);
        TextView runDescription = inf.findViewById(R.id.description);
        runDescription.setText("Description: " + runData[1]);
        TextView runTime = inf.findViewById(R.id.time);
        runTime.setText("Active time: " + runData[2]);
        TextView runDistance = inf.findViewById(R.id.distance);
        runDistance.setText("Run distance: " + runData[3] + "m");
        TextView runAverageSpeed = inf.findViewById(R.id.averagespeed);
        runAverageSpeed.setText("Average speed: " + runData[4] + "m/s");
        TextView runRating = inf.findViewById(R.id.rating);
        runRating.setText("Run rating: " + runData[5]);
        String imagePath = runData[6];
        TextView runWeather = inf.findViewById(R.id.weather);
        runWeather.setText("Weather: " + runData[7]);
        ImageView imageView = inf.findViewById(R.id.runImageView);
        if(imagePath == null){
            imageView.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            myBitmap = rotateImage(90, myBitmap);
            imageView.setImageBitmap(myBitmap);
        }
        // Inflate the layout for this fragment
        return inf;
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
     * Calls super method
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Calls super method
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

}
