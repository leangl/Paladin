package com.nanospark.gard.weather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.persistance.datastore.DataStore;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.RoboGuice;
import roboguice.util.Ln;
import rx.Observable;

/**
 * Created by Leandro on 17/11/2015.
 */
@Singleton
public class WeatherManager {

    private static final String API_KEY = "3096911bf9d52bd438829370a9abedcb";
    private static final String UNIT_METRIC = "metric";

    private OpenWeatherMapApi mApi;

    private Config mConfig;

    private Forecast mForecast;

    public WeatherManager() {
        Gson gson = new GsonBuilder().create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.openweathermap.org")
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(Ln::i)
                .build();

        mApi = restAdapter.create(OpenWeatherMapApi.class);

        mConfig = DataStore.getInstance().getObject(Config.class.getSimpleName(), Config.class).get();
        if (mConfig == null) {
            mConfig = new Config();
            mConfig.city = new City("US", 0l, "New York City", "10001"); // defaults to NY
        }
    }

    public static final WeatherManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(WeatherManager.class);
    }

    public Observable<Forecast> getForecast() {
        if (mForecast != null && TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - mForecast.getDate().getTime()) < 3) {
            return Observable.just(mForecast);
        }
        return mApi.getForecast(mConfig.city.getName() + ",US", API_KEY, UNIT_METRIC)
                .map(result -> {
                    JsonObject object = result.getAsJsonObject();
                    JsonArray list = object.get("list").getAsJsonArray();
                    if (list.size() == 0) {
                        throw new IllegalArgumentException();
                    }
                    Forecast forecast = new Forecast();
                    for (JsonElement element : list) {
                        JsonObject jsonWeather = element.getAsJsonObject();
                        Weather weather = new Weather();
                        weather.setDate(new Date(jsonWeather.get("dt").getAsLong() * 1000)); // timestamp is in seconds
                        weather.setName(jsonWeather.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("main").getAsString());
                        weather.setTemp(jsonWeather.get("main").getAsJsonObject().get("temp").getAsDouble());
                        weather.setTempMin(jsonWeather.get("main").getAsJsonObject().get("temp_min").getAsDouble());
                        weather.setTempMax(jsonWeather.get("main").getAsJsonObject().get("temp_max").getAsDouble());
                        forecast.add(weather);
                    }

                    mForecast = forecast;
                    return forecast;
                }).onErrorReturn(error -> {
                    if (mForecast != null) {
                        return mForecast;
                    }
                    throw new RuntimeException("Error obtaining forecast");
                });
    }

    public Observable<City> setCity(String zipCode) {
        return mApi.getCityByZipcode(zipCode + ",US", API_KEY)
                .map(result -> {
                    JsonObject object = result.getAsJsonObject();
                    JsonArray list = object.get("list").getAsJsonArray();
                    if (list.size() == 0) {
                        throw new IllegalArgumentException();
                    }
                    JsonObject jsonCity = list.get(0).getAsJsonObject();
                    mConfig.city = new City();
                    mConfig.city.setId(jsonCity.get("id").getAsLong());
                    mConfig.city.setName(jsonCity.get("name").getAsString());
                    mConfig.city.setCountry(jsonCity.get("sys").getAsJsonObject().get("country").getAsString());
                    mConfig.city.setZipCode(zipCode);

                    persit();

                    mForecast = null;

                    return mConfig.city;
                });
    }

    public void persit() {
        DataStore.getInstance().putObject(Config.class.getSimpleName(), mConfig);
    }

    public void setEnabled(boolean enabled) {
        mConfig.enabled = enabled;
        persit();
    }

    public boolean isEnabled() {
        return mConfig.enabled;
    }

    public static class Config {
        public City city;
        public boolean enabled = true;
    }

    public City getCity() {
        return mConfig.city;
    }

}