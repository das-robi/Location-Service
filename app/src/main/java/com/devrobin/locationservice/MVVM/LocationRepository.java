package com.devrobin.locationservice.MVVM;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.devrobin.locationservice.RetrofiteServices.RetrofitClient;
import com.devrobin.locationservice.RetrofiteServices.WeatherResponse;
import com.devrobin.locationservice.RetrofiteServices.LocationAPIServices;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationRepository {

    private static final String TAG = "LOCATION_REPOSITORY";

    //API KEY
    private static final String WEATHER_API_KEY = "d8996a21eae65193e982a50fc5187dc7";


    private LocationDAO locationDAO;
    private LocationAPIServices locationAPIServices;
    private ExecutorService executorService;

    public LocationRepository(Context context){
        LocationDatabase locationDatabase = LocationDatabase.getInstance(context);
        locationDAO = locationDatabase.locationDAO();
        locationAPIServices = RetrofitClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
    }

    //Insert location data using background thread
    public void insertLocation(LocationData locationData){
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    long locId = locationDAO.insertLocation(locationData);
                    Log.d(TAG, "Location Inserted with id " + locId);

                    fetchWeather((int) locId, locationData.getLatitude(), locationData.getLongitude());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    //Fetch Weather data from openWeather
    private void fetchWeather(int locId, double latitude, double longitude) {

        if (WEATHER_API_KEY.equals("YOUR_API_KEY_HERE")){
            Log.w(TAG, "API is not configured skip fetch data");
            return;
        }

        Log.d(TAG, "Fetching weather for location: " + latitude + ", " + longitude);

//        Invalid Coordinates
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180){
            Log.d(TAG, "Coordinates: " + latitude + longitude);
            return;
        }

        //Make API call
        Call<WeatherResponse> call = locationAPIServices.getWeathers(latitude, longitude, WEATHER_API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                if (response.isSuccessful() && response.body() != null){

                    WeatherResponse weatherResponse = response.body();

                    if (weatherResponse.getCod() == 200){
                        UpdateDataWithLocationEntity(locId, weatherResponse);
                        Log.d(TAG, "Weather data fetched successfully");
                    }
                    else {
                        Log.e(TAG, "API is error" + weatherResponse.getMessage());
                    }

                }
                else {
                    Log.d(TAG, "API call failed" + response.code());
                }

            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Weather API call failed" + t.getMessage(), t);
            }
        });
    }

    //Update fetch Data with LocationEntity
    public void UpdateDataWithLocationEntity(int locId, WeatherResponse weatherResponse) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationData locations = locationDAO.getLocationById(locId);

                    if (locations != null){

                        //Get Weather data
                        if (weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()){
                            String description = weatherResponse.getWeather().get(0).getDescription();
                            locations.setWeatherDesc(description);
                            Log.d(TAG, "Weather description: " + description);
                        }

                        //Get temp and humidity
                        if (weatherResponse.getMain() != null){

                            double temp = weatherResponse.getMain().getTemp();
                            int humidity = weatherResponse.getMain().getHumidity();

                            locations.setTemperature(String.format("%.1fC", temp));
                            locations.setHumidity(humidity + "%");
                        }

                        //Update Database
                        locationDAO.updateLocation(locations);

                    }
                    else {
                        Log.e(TAG, "Location not find by id" + locId);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error updating weather: " + e.getMessage());
                }
            }
        });


    }

    //Get All Locations
    public LiveData<List<LocationData>> getAllLocations(){
        return locationDAO.getAllLocations();
    }

    //Get most recent location
    public LiveData<LocationData> getLastLocation(){
        return locationDAO.getLastLocations();
    }

    //Delete Locations
    public void DeleteLocations(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                locationDAO.DeleteLocation();
            }
        });
    }

    public void ShutDown(){

        if (executorService != null && executorService.isShutdown()){
            executorService.shutdown();
        }

    }
}
