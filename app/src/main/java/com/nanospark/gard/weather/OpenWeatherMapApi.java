package com.nanospark.gard.weather;

import com.google.gson.JsonElement;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Leandro on 17/11/2015.
 */
public interface OpenWeatherMapApi {

    @GET("/data/2.5/forecast")
    Observable<JsonElement> getForecast(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);

    @GET("/data/2.5/find")
    Observable<JsonElement> getCityByZipcode(@Query("q") String zipCode, @Query("appid") String apiKey);
}
