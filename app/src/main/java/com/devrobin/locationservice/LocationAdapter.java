package com.devrobin.locationservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.locationservice.MVVM.LocationData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationData> locationList = new ArrayList<>();

    public void setLocations(List<LocationData> locationList){
        this.locationList = locationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_items, parent,false);

        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {

        LocationData locationData = locationList.get(position);


        holder.placeName.setText(locationData.getPlaceName());

        //Make String
        String coordinates = String.format(Locale.getDefault(),
                "Lat: %.4f, Lon: %.4f : Accuracy: %.0fm",
                locationData.getLatitude(),
                locationData.getLongitude(),
                locationData.getAccuracy());

        holder.coordinators.setText(coordinates);

        //Date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault());
        String dateTime = dateFormat.format(new Date(locationData.getTimestamp()));
        holder.timeDate.setText(dateTime);

        //Weather info
        String weatherInfo;
        if (locationData.getWeatherDesc() != null && locationData.getTemperature() != null){
            weatherInfo = String.format("%s %s Humidity: %s",
                    locationData.getWeatherDesc(),
                    locationData.getTemperature(),
                    locationData.getHumidity());
        }
        else {
            weatherInfo = "Loading...";
        }

        holder.weather.setText(weatherInfo);
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }


    public class LocationViewHolder extends RecyclerView.ViewHolder{

        TextView placeName, timeDate, coordinators, weather;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            placeName = itemView.findViewById(R.id.tvPlaceName);
            coordinators = itemView.findViewById(R.id.tvCoordinates);
            timeDate = itemView.findViewById(R.id.tvTime);
            weather = itemView.findViewById(R.id.tvWeather);

        }
    }

}
