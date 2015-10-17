package com.nanospark.gard.events;

import android.widget.DatePicker;

/**
 * Created by cristian on 17/10/15.
 */
public class DatePickerSelected {
    public DatePicker view;
    public int year;
    public int month;
    public int day;
    public int id;

    public DatePickerSelected(DatePicker view, int year, int month, int day,int id) {
        this.view = view;
        this.year = year;
        this.month = month;
        this.day = day;
        this.id = id;

    }



}
