package com.devrobin.locationservice.RetrofiteServices;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("main")
    private Main main;

    @SerializedName("name")
    private String cityName;

    @SerializedName("cod")
    private int cod;

    @SerializedName("message")
    private String message;

    //Getters
    public List<Weather> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public String getCityName() {
        return cityName;
    }

    public int getCod() {
        return cod;
    }

    public String getMessage() {
        return message;
    }

    //Nested class for Weather array
    public static class Weather{

        @SerializedName("id")
        private int id;

        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        //Getters and Setters
        public int getId() {
            return id;
        }

        public String getMain() {
            return main;
        }


        public String getDescription() {
            return description;
        }


    }

    //Nested class for Main weather data
    public static class Main{

        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private int humidity;

        @SerializedName("pressure")
        private int pressure;

        //Getters and Setters


        public double getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }
    }
}
