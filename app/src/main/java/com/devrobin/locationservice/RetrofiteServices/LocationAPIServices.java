package com.devrobin.locationservice.RetrofiteServices;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationAPIServices {

    //URL = http://api.openweathermap.org/ data/2.5/weather ?id=524901&apikey=d8996a21eae65193e982a50fc5187dc7
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeathers(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apikey,
            @Query("units") String units
    );

}
