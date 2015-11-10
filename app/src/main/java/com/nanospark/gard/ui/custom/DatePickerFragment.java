package com.nanospark.gard.ui.custom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import mobi.tattu.utils.Tattu;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static String ARG_ID = "arg_id";
    private int mId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mId = getArguments().getInt(ARG_ID);
    }

    public static DatePickerFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

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