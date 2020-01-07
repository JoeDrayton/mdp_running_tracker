package com.example.mdp_running_tracker;

import android.content.Context;
import android.database.ContentObservable;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class ViewRunFragment extends Fragment {
    public ViewRunFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ViewRunFragment newInstance(String param1, String param2) {
        ViewRunFragment fragment = new ViewRunFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inf = inflater.inflate(R.layout.fragment_view_run, container, false);

        String[] runData = getArguments().getStringArray("runData");
        TextView runName = inf.findViewById(R.id.name);
        runName.setText("Run name: " + runData[0]);
        TextView runDescription = inf.findViewById(R.id.description);
        runDescription.setText("Description: " + runData[1]);
        TextView runTime = inf.findViewById(R.id.time);
        runTime.setText("Active time: " + runData[2]);
        TextView runDistance = inf.findViewById(R.id.distance);
        runDistance.setText("Run distance: " + runData[3] + "m");
        TextView runRating = inf.findViewById(R.id.rating);
        runRating.setText("Run rating: " + runData[4]);
        // Inflate the layout for this fragment
        return inf;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
