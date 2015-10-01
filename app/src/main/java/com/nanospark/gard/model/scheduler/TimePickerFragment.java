package com.nanospark.gard.model.scheduler;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by cristian on 09/08/15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TimePickerFragmentListener mListener;
    private TimePickerDialog mTimePickerDialog;
    private boolean callListener = true;

    public void setTimePickerFragmentListener(TimePickerFragmentListener listener) {
        this.mListener = listener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        mTimePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

        mTimePickerDialog.setOnCancelListener(dialog -> {
            dialog.dismiss();
        });

        return mTimePickerDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (callListener == true) {
            this.mListener.onTimeSet(view, hourOfDay, minute);
            callListener = false;
        }
    }


    public interface TimePickerFragmentListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }
}
