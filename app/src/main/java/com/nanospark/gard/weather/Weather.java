package com.nanospark.gard.weather;

import com.nanospark.gard.model.Day;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Leandro on 17/11/2015.
 */
public class Weather implements Serializable {

    private Date date;
    private String name;
    private Double temp;
    private Double tempMax;
    private Double tempMin;

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Double getTemp() {
        return temp;
    }
    public void setTemp(Double temp) {
        this.temp = temp;
    }
    public Double getTempMax() {
        return tempMax;
    }
    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }
    public Double getTempMin() {
        return tempMin;
    }
    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public Day getDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return Day.fromCalendar(cal.get(Calendar.DAY_OF_WEEK));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weather weather = (Weather) o;

        return date.equals(weather.date);

    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
