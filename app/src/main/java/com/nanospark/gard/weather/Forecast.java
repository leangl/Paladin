package com.nanospark.gard.weather;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Leandro on 17/11/2015.
 */
public class Forecast implements Serializable {

    private Date date = new Date();
    private List<Weather> list = new ArrayList<>();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Weather> getList() {
        return list;
    }

    public void setList(List<Weather> list) {
        this.list = list;
    }

    public void add(Weather weather) {
        list.add(weather);
    }

    public Weather getCurrent() {
        return list.get(0);
    }

    public List<Weather> getForecast(int daysAhead) {
        List<Weather> forecast = new ArrayList<>(daysAhead);

        /*Date previousDate = normalize(getCurrent().getDate());
        double tempMax;
        double tempMin;
        for (Weather weather : list.subList(1, list.size())) {
            if (normalize(weather.getDate()).after(previousDate)) {
                if (Math.abs(12 - getHour(weather.getDate())) <= 2) { // get the weather closest to 12AM
                    forecast.add(weather);
                    previousDate = weather.getDate();
                }
            }
            if (forecast.size() == daysAhead) {
                break;
            }
        }*/
        Date previousDate = null;
        double tempMax = Double.MIN_VALUE;
        double tempMin = Double.MAX_VALUE;
        Weather midDay = null;
        boolean first = true;
        for (Weather weather : list) {
            if (first) {
                previousDate = normalize(weather.getDate()); // init previous
                first = false;
            }
            if (normalize(weather.getDate()).after(previousDate)) { // if day changed
                Weather w = new Weather();
                w.setTempMax(tempMax);
                w.setTempMin(tempMin);
                w.setTemp(tempMax + tempMin / 2);
                w.setDate(previousDate);
                w.setName(midDay.getName());
                forecast.add(w);

                previousDate = normalize(weather.getDate());
                tempMax = Double.MIN_VALUE;
                tempMin = Double.MAX_VALUE;
                midDay = null;
            } else { // if same day
                if (midDay == null || Math.abs(12 - getHour(weather.getDate())) <= 2) { // get the weather conditions closest to 12AM
                    midDay = weather;
                }
                // recheck max/min temperatures
                if (weather.getTempMax() > tempMax) tempMax = weather.getTempMax();
                if (weather.getTempMin() < tempMin) tempMin = weather.getTempMin();
            }
            if (forecast.size() == daysAhead) {
                break;
            }
        }
        return forecast;
    }

    private Date normalize(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private int getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public long getAge() {
        return TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - getDate().getTime());
    }


}
