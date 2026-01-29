package com.devrobin.locationservice.RetrofiteServices;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationAPIServices {

    //URL = http://api.openweathermap.org/ data/2.5/weather ?id=524901&api_key=d8996a21eae65193e982a50fc5187dc7
    @GET("data/2.5/weather")
    Call<WeatherResponse> getLocations(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String api_key,
            @Query("units") String units
    );

}
