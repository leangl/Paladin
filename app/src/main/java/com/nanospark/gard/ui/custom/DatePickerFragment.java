package com.nanospark.gard.ui.custom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import mobi.tattu.utils.Tattu;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static String ARG_ID = "arg_id";
    private static String ARG_CAL = "arg_cal";
    private int mId;
    private Calendar mCalendar;

    public static DatePickerFragment newInstance(int id, Calendar cal) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putSerializable(ARG_CAL, cal != null ? cal : Calendar.getInstance());
        DatePickerFragment fragment = new DatePickerFragment();
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
        // Use the current date as the default date in the picker
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Tattu.post(new DatePickerSelected(view, year, month, day, this.mId));
    }

    public static class DatePickerSelected {
        public final DatePicker view;
        public final int year;
        public final int month;
        public final int day;
        public final int id;

        public DatePickerSelected(DatePicker view, int year, int month, int day, int id) {
            this.view = view;
            this.year = year;
            this.month = month;
            this.day = day;
            this.id = id;

        }

    }

}