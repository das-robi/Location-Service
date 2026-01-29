package com.devrobin.locationservice;

import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.devrobin.locationservice.MVVM.LocationViewModel;
import com.devrobin.locationservice.MVVM.LocationData;
import com.devrobin.locationservice.RetrofiteServices.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Widgets
    private Button btnStart, btnStop, btnClear;
    private TextView tvStatus, tvLatest, tvWeather;
    private RecyclerView recyclerView;

    private LocationAdapter locationAdapter;
    private LocationViewModel locationViewModel;

    private boolean LOCATION_CODE = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);
        tvLatest = findViewById(R.id.tvLatest);
        recyclerView = findViewById(R.id.recyclerView);
        tvWeather = findViewById(R.id.tvWeather);

        //ViewModel
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        CheckPermission();
        observeLocations();


        //Set OnClink Listener
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartLocationService();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StopLocationService();

            }});


        locationAdapter = new LocationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(locationAdapter);
    }

    private void observeLocations() {


        locationViewModel.getAllLocations().observe(this, new Observer<List<LocationData>>() {
            @Override
            public void onChanged(List<LocationData> locationData) {

                if (locationData != null){
                    locationAdapter.setLocations(locationData);
                }

            }
        });


        //Get recent Location
        locationViewModel.getLastLocation().observe(this, new Observer<LocationData>() {
            @Override
            public void onChanged(LocationData locationData) {

                if (locationData != null){

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault());

                    StringBuilder data = new StringBuilder();

                    //Check City
                    if (locationData.getPlaceName() != null && !locationData.getPlaceName().isEmpty()){
                        data.append(locationData.getPlaceName()).append("\n\n");
                    }

                    //Check Address
                    if (locationData.getAddress() != null && !locationData.getAddress().isEmpty()){
                        data.append(locationData.getAddress()).append("\n\n");
                    }


                    data.append(String.format(Locale.getDefault(),
                            "Accuracy: %.0f meters\n\n",
                            locationData.getAccuracy()));


                    if (locationData.getWeatherDesc() != null && locationData.getTemperature() != null) {

                        data.append(String.format("%s  %s Humidity: %s\n\n",
                                locationData.getWeatherDesc(),
                                locationData.getTemperature(),
                                locationData.getHumidity() != null ? locationData.getHumidity() : "N/A"));

                        tvWeather.setText(data.toString());
                    }
                    else {
                        data.append("Loading Weather...");
                    }


                    //Date & time
                    data.append(dateFormat.format(new Date(locationData.getTimestamp())));

                    tvLatest.setText(data.toString());
                    Log.d("Tag", "Latest Location " + locationData.getPlaceName());
                }
                else {
                    tvLatest.setText("No recorded Location \n\n Tracking Latest Location and update");
                }

            }
        });

    }


    //Check Permissions
    private void CheckPermission(){

        if (!PermissionManager.ForegroundLocationPermission(this)){
            showPermissionAlertDialogue(
                    "Location Permission",
                    "This app need location permission to track Location",
                    ()-> PermissionManager.RequestForeGroundLocation(this)
            );
        }
        else if (!PermissionManager.NotificationPermission(this)){
            showPermissionAlertDialogue(
                    "Notification Permission",
                    "This app need Notification Permission to show Notification Status",
                    () -> PermissionManager.RequestNotification(this)
            );
        }
        else if (!PermissionManager.BackgroundLocationPermission(this)){
            showPermissionAlertDialogue(
                    "Background Location Permission Required",
                    "To to track location when your app is close, select All the Time",
                    ()-> PermissionManager.RequestBackgroundLocation(this)
            );
        }
        else {
            AllPermissionsGranted();
        }

    }

    private void showPermissionAlertDialogue(String title, String message, Runnable runnable){

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant", (new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runnable.run();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == PermissionManager.REQUEST_LOCATION_PERMISSION_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                CheckPermission();
            }else {
                handlePermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        }
        else if (requestCode == PermissionManager.REQUEST_BACKGROUND_LOCATION_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Background permission Granted", Toast.LENGTH_SHORT).show();
                AllPermissionsGranted();
            }
            else {
                Toast.makeText(this, "Background Permission Denied", Toast.LENGTH_SHORT).show();
                AllPermissionsGranted();
            }

        }
        else if (requestCode == PermissionManager.REQUEST_NOTIFICATION_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
                CheckPermission();
            }
            else {
                Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
                CheckPermission();
            }

        }
    }

    private void handlePermissionDenied(String locPermission) {

        if (PermissionManager.PermissionDenied(this, locPermission)){

            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please enable")
                    .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            openSettings();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    private void AllPermissionsGranted() {

        btnStart.setEnabled(true);
        tvStatus.setText("Ready to track....");

    }

    public void StartLocationService(){

        if (!GPSEnabled()){
            showGPSDialog();
            return;
        }

        if (!PermissionManager.ForegroundLocationPermission(this)){
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            CheckPermission();
            return;
        }

        Intent intent = new Intent(this, LocationForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent);
        }
        else {
            startService(intent);
        }

        LOCATION_CODE = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tvStatus.setText("Tracking Active");
        Toast.makeText(this, "Location Tracking Start", Toast.LENGTH_SHORT).show();

    }

    public void StopLocationService(){

        Intent intent = new Intent(this, LocationForegroundService.class);
        stopService(intent);

        LOCATION_CODE = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        tvStatus.setText("Tracking Stop...");
        Toast.makeText(this, "Location Tracking Stop", Toast.LENGTH_SHORT).show();

    }

    private boolean GPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    public void showGPSDialog(){

        new AlertDialog.Builder(this)
                .setTitle("Required GPS On")
                .setMessage("Turn on your GPS")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Tag", "Service running in Background");
    }
}