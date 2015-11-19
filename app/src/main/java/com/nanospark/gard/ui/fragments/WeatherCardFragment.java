package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.weather.Forecast;
import com.nanospark.gard.weather.Weather;
import com.nanospark.gard.weather.WeatherManager;

import java.text.DecimalFormat;

import mobi.tattu.utils.fragments.BaseFragment;
import roboguice.inject.InjectView;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Leandro on 17/11/2015.
 */
public class WeatherCardFragment extends BaseFragment {

    private static final String THUNDERSTORM = "Thunderstorm";
    private static final String DRIZZLE = "Drizzle";
    private static final String RAIN = "Rain";
    private static final String SNOW = "Snow";
    private static final String ATMOSPHERE = "Atmosphere";
    private static final String CLEAR = "Clear";
    private static final String CLOUDS = "Clouds";
    private static final String EXTREME = "Extreme";
    private static final String ADDITIONAL = "Additional";

    @InjectView(R.id.container)
    private View mContainer;
    @InjectView(R.id.card_content)
    private View mCardContent;
    @InjectView(R.id.forecast_container)
    private ViewGroup mForecastContainer;
    @InjectView(R.id.progress)
    private View mProgress;
    @InjectView(R.id.error)
    private TextView mError;
    @InjectView(R.id.title)
    private TextView mTitle;
    @InjectView(R.id.last_update)
    private TextView mLastUpdate;
    @InjectView(R.id.weather_icon)
    private ImageView mWeatherIcon;

    @Inject
    private WeatherManager mManager;

    public static WeatherCardFragment newInstance() {
        return new WeatherCardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (!mManager.isEnabled()) {
            mContainer.setVisibility(View.GONE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mCardContent.setVisibility(View.INVISIBLE);
            mError.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            mManager.getForecast().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(forecast -> {
                        mCardContent.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);

                        renderWeather(forecast);
                    }, error -> {
                        mCardContent.setVisibility(View.INVISIBLE);
                        mProgress.setVisibility(View.GONE);
                        mError.setVisibility(View.VISIBLE);
                    });
        }
    }

    private void renderWeather(Forecast forecast) {
        Weather current = forecast.getCurrent();
        mTitle.setText(printTemp(current.getTemp()) + " " + mManager.getCity().getName());
        mLastUpdate.setText(forecast.getAge() + " hours ago");
        mWeatherIcon.setImageResource(getWeatherIcon(current));

        mForecastContainer.removeAllViews();
        if (forecast.getList().size() > 1) {
            for (Weather weather : forecast.getForecast(4)) {
                View v = inflate(R.layout.weather_day, mForecastContainer, false);
                TextView day = findViewById(v, R.id.day);
                day.setText(weather.getDay().abbr(3));
                ImageView icon = findViewById(v, R.id.weather_icon);
                icon.setImageResource(getWeatherIcon(weather));
                TextView tempMax = findViewById(v, R.id.temp_max);
                tempMax.setText(printTemp(weather.getTempMax()));
                TextView tempMin = findViewById(v, R.id.temp_min);
                tempMin.setText(printTemp(weather.getTempMin()));
                mForecastContainer.addView(v);
            }
        }
    }

    private String printTemp(Double temp) {
        if (temp == null) return "";
        return new DecimalFormat("#.#").format(Math.abs(temp)) + "°";
    }

    private int getWeatherIcon(Weather weather) {
        switch (weather.getName()) {
            case THUNDERSTORM:
                return R.drawable.weather_thunderstorms;
            case DRIZZLE:
                return R.drawable.weather_light_showers;
            case RAIN:
                return R.drawable.weather_rain;
            case SNOW:
                return R.drawable.weather_snow;
            case ATMOSPHERE:
                return R.drawable.weather_fog;
            case CLEAR:
                return R.drawable.weather_sunny;
            case CLOUDS:
                return R.drawable.weather_sunny_interval;
            case EXTREME: // TODO no icons that match this conditions
            case ADDITIONAL:
            default:
                return R.drawable.weather_overcast;
        }
    }


}
