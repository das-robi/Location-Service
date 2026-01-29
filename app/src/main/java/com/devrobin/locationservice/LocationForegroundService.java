package com.devrobin.locationservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.devrobin.locationservice.MVVM.LocationRepository;
import com.devrobin.locationservice.MVVM.LocationData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "Location Service";
    private static final int NOTIFICATION_ID = 100;

    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationCallback locationCallback;
    private static LocationRepository locationRepository;
    private Geocoder geocoder;

    private static final long UPDATE_TIME = 5 * 60 * 1000;
    private static final long FASTEST_TIME = 2 * 60 * 1000;



    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRepository = new LocationRepository(getApplicationContext());
        geocoder = new Geocoder(this, Locale.getDefault());

        CreateNotificationChannel();
        createLocationCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTIFICATION_ID, CreateNotification("Starting Location Tracking..."));

        startLocationUpdates();

        return START_STICKY;
    }

    public void createLocationCallback(){

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                if (locationResult == null){
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null){
                    handleLocationUpdate(location);
                }

            }
        };

    }



    private void startLocationUpdates() {

        //CheckPermission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Tag", "Location Permission not Granted");
            stopSelf();
            return;
        }

        //Create LocationRequest
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                UPDATE_TIME
        )
                .setMinUpdateIntervalMillis(FASTEST_TIME)
                .setWaitForAccurateLocation(true)
                .setMaxUpdateDelayMillis(0)
                .build();


        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        UpdateNotification("Tracking your location..");
    }

    private void handleLocationUpdate(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String provider = location.getProvider();
        float accuracy = location.getAccuracy();
        long timestamp = System.currentTimeMillis();


        //place name using Geocoding
        String placeName = "Unknown Place";
        String cityName = "";
        String address = "";
        String countryName = "";

        //Call GeoCoding API for Get places Name;
        try {

            if (Geocoder.isPresent()){

                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()){

                    Address addr = addresses.get(0);

                    StringBuilder fullAddress = new StringBuilder();

                    if (addr.getThoroughfare() != null){
                        fullAddress.append(addr.getThoroughfare());
                    }

                    //sub locality
                    if (addr.getSubLocality() != null){
                        if (fullAddress.length() > 0){
                            fullAddress.append(", ");
                        }
                        fullAddress.append(addr.getSubLocality());
                    }

                    //Locality
                    if (addr.getLocality() != null){

                        if (fullAddress.length() > 0){
                            fullAddress.append(", ");
                        }
                        fullAddress.append(addr.getLocality());
                        cityName = addr.getLocality();
                    }

                    //Main Area
                    if (addr.getAdminArea() != null){

                        if (fullAddress.length() > 0){
                            fullAddress.append(", ");
                        }
                        fullAddress.append(addr.getAdminArea());
                    }


                    //Country
                    if (addr.getCountryName() != null){
                        if (fullAddress.length() > 0){
                            fullAddress.append(" ");
                        }

                        fullAddress.append(addr.getCountryName());
                        countryName = addr.getCountryName();
                    }

                    address = fullAddress.toString();

                    //Short place name
                    if (cityName != null && !cityName.isEmpty() && countryName != null && !countryName.isEmpty()){
                        placeName = cityName + "," + countryName;
                    }
                    else if (addr.getLocality() != null){
                        placeName = addr.getLocality();
                    }
                    else if (fullAddress.length() > 0){
                        placeName = fullAddress.toString();
                    }

                }
            }

        } catch (IOException e) {
            Log.e("Tag", "Geocoder Error" + e.getMessage());
            placeName = "Unknown Location";
        }
        catch (IllegalArgumentException e){
            Log.e("Tag", "Invalid Coordinates" + e.getMessage());
            placeName = "Invalid Location";
        }


        //Show Toast with place name
        String toastMessage = String.format("%s\nLat: %.4f, Lon: %.4f", placeName, latitude, longitude);
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();


        //Create Location entity
        LocationData locationData = new LocationData(latitude, longitude, timestamp, accuracy, provider);
        locationData.setPlaceName(placeName);
        locationData.setAddress(address);
        locationData.setCityName(cityName);
        locationData.setCountryName(countryName);

        //call Repository for insert data
        locationRepository.insertLocation(locationData);

        //UpdateNotification
        UpdateNotification(placeName);
    }

    public void CreateNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Services",
                    NotificationManager.IMPORTANCE_LOW
            );

            notificationChannel.setDescription("Tracks your location in Background");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private Notification CreateNotification(String contentTxt){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracker")
                .setContentText(contentTxt)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build();
    }

    private void UpdateNotification(String contentText){

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null){
            manager.notify(NOTIFICATION_ID, CreateNotification(contentText));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Tag", "Task Destroy");

        if (fusedLocationProviderClient != null && locationCallback != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("Tag", "Task Remove");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
