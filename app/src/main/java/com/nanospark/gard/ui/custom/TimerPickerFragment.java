package com.nanospark.gard.ui.custom;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import mobi.tattu.utils.Tattu;

/**
 * Created by cristian on 17/10/15.
 */
public class TimerPickerFragment extends android.support.v4.app.DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static String ARG_ID = "arg_id";
    private static String ARG_CAL = "arg_cal";
    private int mId;
    private Calendar mCalendar;

    public static TimerPickerFragment newInstance(int id, Calendar cal) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putSerializable(ARG_CAL, cal != null ? cal : Calendar.getInstance());
        TimerPickerFragment fragment = new TimerPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getInt(ARG_ID);
        mCalendar = (Calendar) getArguments().getSerializable(ARG_CAL);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Tattu.post(new TimerPickerSelected(mId, hourOfDay, view, minute));
    }

    public static class TimerPickerSelected {

        public final TimePicker view;
        public final int hourOfDay;
        public final int minute;
        public final int id;


        public TimerPickerSelected(int id, int hourOfDay, TimePicker view, int minute) {
            this.id = id;
            this.hourOfDay = hourOfDay;
            this.view = view;
            this.minute = minute;
        }
    }

}
