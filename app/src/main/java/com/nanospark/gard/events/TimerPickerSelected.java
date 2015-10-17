package com.nanospark.gard.events;

import android.widget.TimePicker;

/**
 * Created by cristian on 17/10/15.
 */
public class TimerPickerSelected {

    public TimePicker view;
    public int hourOfDay;
    public int minute;
    public int id;


    public TimerPickerSelected(int id, int hourOfDay, TimePicker view, int minute) {
        this.id = id;
        this.hourOfDay = hourOfDay;
        this.view = view;
        this.minute = minute;
    }
}
