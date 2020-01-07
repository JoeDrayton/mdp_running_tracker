package com.example.mdp_running_tracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.util.Log;

import java.util.ArrayList;

public class MyService extends Service {
    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<>();
    private ArrayList<Location> journey = new ArrayList<>();
    LocationManager locationManager;
    MyLocationListener locationListener;
    float distance;
    protected Counter counter;

    protected class Counter extends Thread implements Runnable {
        public boolean running = true;
        public int count = 0;

        public Counter() {
            this.start();
        }

        public void run() {
            while (this.running) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    return;
                }
                count++;
                doCallbacks(count);
                Log.d("g53mdp", "Service counter " + count);
            }
            Log.d("g53mdp", "Service counter thread terminated");
        }
    }

    public void doCallbacks(int count) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.counterEvent(count);
        }
        remoteCallbackList.finishBroadcast();
    }

    public void stop(){
        this.stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("g53mdp", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("g53mdp", "service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("g53mdp", "service onUnbind");
        stop();
        counter.running = false;
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("g53mdp", "Service onBind");
        return new MyBinder();
    }

    public class MyBinder extends Binder implements IInterface {
        @Override
        public IBinder asBinder() {
            return this;
        }
        void record() {
            Log.d("g53mdp", "Service started recording");
            counter = new Counter();
            distance = 0;
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MyLocationListener();
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5, //min time interval between updates);
                        1, //min distance between updates, in metres
                        locationListener);
            } catch (SecurityException e) {
                Log.d("g53mdp", e.toString());
            }
        }
        ArrayList<Location> stop() {
            Log.d("g53mdp", "Service finished recording");
            counter.running = false;
            counter.count = 0;
            locationManager.removeUpdates(locationListener);
            return journey;
        }
        float getDistance(){
            return distance;
        }

        void registerCallback(ICallback callback){
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        void unRegisterCallback(ICallback callback){
            remoteCallbackList.unregister(MyBinder.this);
        }

        ICallback callback;
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
            journey.add(location);

            if(journey.size()>1){
                distance += journey.get(journey.size()-2).distanceTo(journey.get(journey.size()-1));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // information about the signal
            Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            // the user enabled for example the GPS
            Log.d("g53mdp", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // the user disabled for example the GPS
            Log.d("g53mdp", "onProviderDisabled: " + provider);
        }
    }
}
