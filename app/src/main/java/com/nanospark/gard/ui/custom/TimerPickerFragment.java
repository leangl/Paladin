package com.nanospark.gard.ui.custom;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.nanospark.gard.events.TimerPickerSelected;

import java.util.Calendar;

import mobi.tattu.utils.Tattu;

/**
 * Created by cristian on 17/10/15.
 */
public class TimerPickerFragment extends android.support.v4.app.DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private static String ARG_ID;
    private int mId;

    public static TimerPickerFragment newInstance(int id) {

        Bundle args = new Bundle();
        args.putInt(ARG_ID,id);
        TimerPickerFragment fragment = new TimerPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mId = getArguments().getInt(ARG_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Tattu.post(new TimerPickerSelected(mId,hourOfDay,view,minute));
    }


}
