package com.example.mdp_running_tracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class MapFragment extends Fragment {
    public MapFragment(){
       // Empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for the fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        final ArrayList<LatLng> journey = getArguments().getParcelableArrayList("journey");
        // Use SupportMapFragment
        SupportMapFragment map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.clear();
                googleMap.addPolyline(new PolylineOptions()
                            .addAll(journey)
                        .width(2f)
                        .color(Color.RED)
                );

                googleMap.addMarker(new MarkerOptions().position(journey.get(0)).title("Start point"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(journey.get(0), 18f));
            }
        });
        return rootView;
    }
}