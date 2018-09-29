package kled.pagesaver;

/**
 * Created by Weazel on 2/26/17.
 */

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


/*
Class allows the MapsView to track the user's location
 */
public class MyTrackingService extends Service implements LocationListener {

    private final IBinder binder = new TrackingServiceBinder();
    private LocationManager myLocationManager;

    private NotificationManager myNotificationManager;

    private double lat;
    private double lng;


    private boolean isStarted;

    public class TrackingServiceBinder extends Binder {

        MyTrackingService getService() {
            return MyTrackingService.this;
        }

    }


    @Override
    public void onCreate() {
        isStarted = false;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TrackingService","OnStartCommand Called");
        start(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        start(intent);
        return binder;
    }


    private void start(Intent intent) {
        Log.d("TrackingService", "Start Called");
        if (isStarted) {
            return;
        }
        isStarted = true;

        newNotification();

        startLocationUpdate();
    }



    @Override
    public void onDestroy() {
        isStarted = false;


        cancelLocationUpdates();
        myNotificationManager.cancelAll();

        super.onDestroy();
    }


    //Notify tracking is occurring
    private void newNotification() {
        Intent mapDisplayIntent = new Intent(getApplicationContext(), MainActivity.class);
        mapDisplayIntent.setAction(Intent.ACTION_MAIN);
        mapDisplayIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mapDisplayIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mapDisplayPendingIntent = PendingIntent.getActivity(getBaseContext(),
                0, mapDisplayIntent, 0);


        Notification trackNotification = new Notification.Builder(this)
                .setContentTitle("PageSaver")
                .setContentText("Getting Current Location")
                .setSmallIcon(R.drawable.ic_menu_send)
                .setContentIntent(mapDisplayPendingIntent)
                .build();

        // Set the flags to signify ongoing event
        trackNotification.flags |= Notification.FLAG_ONGOING_EVENT;

        // Start notification
        myNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myNotificationManager.notify(5, trackNotification);
    }
    @TargetApi(23)
    private void startLocationUpdate() {
        Log.d("TrackingService","Start Location Update");

        // Setup criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            Log.d("TrackingService", "request location updates");
            myLocationManager.requestLocationUpdates(myLocationManager
                    .getBestProvider(criteria, true), 0, 15, MyTrackingService.this);
            doUpdate(myLocationManager.getLastKnownLocation(myLocationManager
                    .getBestProvider(criteria, true)));
        }
        else{
            Log.d("TrackingService", "Permission not granted");

        }
    }
    private void doUpdate(Location loc) {
        Intent intent = new Intent(PSMapActivity.LocationUpdateReceiver.class.getName());
        if(loc != null) {
            intent.putExtra("update", true);
            intent.putExtra("lat", loc.getLatitude());
            intent.putExtra("long", loc.getLongitude());
            Log.d("TrackingService", "Lat: " + String.valueOf(loc.getLatitude())
                    + " Long: " + String.valueOf(loc.getLongitude()));
            this.sendBroadcast(intent);
        }

    }
    private void cancelLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            myLocationManager.removeUpdates(this);
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("TrackingService", "onLocationChanged called");
        if (location!=null) {
            doUpdate(location);
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }




}