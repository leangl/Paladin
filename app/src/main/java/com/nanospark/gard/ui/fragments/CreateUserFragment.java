package com.nanospark.gard.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.events.DatePickerSelected;
import com.nanospark.gard.events.TimerPickerSelected;
import com.nanospark.gard.model.user.ControlSchedule;
import com.nanospark.gard.ui.activity.CreateUserActivity;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.ui.custom.DatePickerFragment;
import com.nanospark.gard.ui.custom.TimerPickerFragment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;

import mobi.tattu.utils.Tattu;

/**
 * Created by cristian on 17/10/15.
 */
public class CreateUserFragment extends BaseFragment implements CreateUserActivity.CreateUserListener {

    private ControlSchedule mControlSchedule;
    private AppCompatEditText mNameEditText;
    private AppCompatEditText mPhoneEditText;
    private AppCompatEditText mPasswordEditText;
    private AppCompatSpinner mDoorEventSpinner;
    private TextView mTimeStartTextView;
    private TextView mTimeEndTextView;
    private TextView mDateStartTextView;
    private TextView mDateEndTextView;


    public static CreateUserFragment newInstance() {

        Bundle args = new Bundle();
        CreateUserFragment fragment = new CreateUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mControlSchedule = new ControlSchedule();
        mControlSchedule.setDays(new ArrayList<>());
        ((CreateUserActivity) getBaseActivity()).setListener(this);
        Tattu.register(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_user, container, false);

        CheckBox timeLimitsCheckBox = (CheckBox) view.findViewById(R.id.checkbox_time_limits);
        CheckBox requirePassword = (CheckBox) view.findViewById(R.id.checkbox_password);

        View scheludeContainer = view.findViewById(R.id.schedule_container);
        LinearLayout daysContainer = (LinearLayout) scheludeContainer.findViewById(R.id.days_container);

        initScheduleView(scheludeContainer, daysContainer);


        this.mPasswordEditText = (AppCompatEditText) view.findViewById(R.id.edittext_password);
        this.mPasswordEditText.setEnabled(false);

        timeLimitsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scheludeContainer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        });
        requirePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mPasswordEditText.setEnabled(isChecked);

        });

        return view;
    }

    private void initScheduleView(View scheludeContainer, LinearLayout daysContainer) {
        this.mTimeStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheludeContainer.findViewById(R.id.textview_end_time);
        this.mDoorEventSpinner = (AppCompatSpinner) scheludeContainer.findViewById(R.id.spinner_door_event);
        this.mDateStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_day);
        this.mDateEndTextView = (TextView) scheludeContainer.findViewById(R.id.textview_end_day);
        ((CheckBox) scheludeContainer.findViewById(R.id.checkbox_repeat_every_day)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatEveryOtherDay(isChecked);
        });
        ((CheckBox)scheludeContainer.findViewById(R.id.checkbox_repeat)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatWeeks(isChecked);
        });

        populateDays(daysContainer);

        this.mTimeStartTextView.setOnClickListener(v -> {
            showTimerPicker(R.id.textview_start_time);
        });
        this.mTimeEndTextView.setOnClickListener(v -> {
            showTimerPicker(R.id.textview_end_time);
        });
        this.mDateStartTextView.setOnClickListener(v -> {
            showDatePicker(R.id.textview_start_day);
        });
        this.mDateEndTextView.setOnClickListener(v -> {
            showDatePicker(R.id.textview_end_day);
        });
    }

    private void showDatePicker(int id) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(id);
        datePickerFragment.show(getBaseActivity().getSupportFragmentManager(), datePickerFragment.toString());
    }

    private void showTimerPicker(int id) {
        TimerPickerFragment timerPickerFragment = TimerPickerFragment.newInstance(id);
        timerPickerFragment.show(getBaseActivity().getSupportFragmentManager(), timerPickerFragment.toString());
    }

    private void populateDays(LinearLayout daysContainer) {
        createDays(daysContainer, Calendar.SUNDAY, R.drawable.ic_sun, R.drawable.ic_sun_selected);
        createDays(daysContainer, Calendar.MONDAY, R.drawable.ic_mon, R.drawable.ic_mon_selected);
        createDays(daysContainer, Calendar.TUESDAY, R.drawable.ic_tue, R.drawable.ic_tue_selected);
        createDays(daysContainer, Calendar.WEDNESDAY, R.drawable.ic_wed, R.drawable.ic_wed_selected);
        createDays(daysContainer, Calendar.THURSDAY, R.drawable.ic_thr, R.drawable.ic_thr_selected);
        createDays(daysContainer, Calendar.FRIDAY, R.drawable.ic_fri, R.drawable.ic_fri_selected);
        createDays(daysContainer, Calendar.SATURDAY, R.drawable.ic_sat, R.drawable.ic_sat_selected);
    }

    private void createDays(LinearLayout container, int day, int drawable, int drawableSelected) {
        FloatingActionButton floatingActionButton = new FloatingActionButton(getBaseActivity());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        floatingActionButton.setRippleColor(getColorFromResource(R.color.white));
        floatingActionButton.setTag(R.string.key_state, Boolean.FALSE);
        floatingActionButton.setTag(R.string.key_day, day);
        floatingActionButton.setLayoutParams(layoutParams);
        floatingActionButton.setImageResource(drawable);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(R.color.white)));
        floatingActionButton.setOnClickListener(v -> {
            FloatingActionButton fb = (FloatingActionButton) v;
            boolean state = (boolean) fb.getTag(R.string.key_state);
            int color;
            int drawableAux;
            if (state) {
                color = R.color.white;
                drawableAux = drawable;
                state = false;
                mControlSchedule.getDays().remove((Integer) fb.getTag(R.string.key_day));

            } else {
                color = R.color.red;
                drawableAux = drawableSelected;
                state = true;
                mControlSchedule.getDays().add((Integer) fb.getTag(R.string.key_day));
            }
            fb.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(color)));
            fb.setImageResource(drawableAux);
            fb.setScaleType(ImageView.ScaleType.CENTER);
            fb.setTag(R.string.key_state, state);

        });
        container.addView(floatingActionButton);
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }


    @Override
    public void save() {

    }

    @Subscribe
    public void on(TimerPickerSelected timerPickerSelected) {
        if (timerPickerSelected.id == R.id.textview_start_time) {
            mControlSchedule.setStartHour(timerPickerSelected.hourOfDay);
            mControlSchedule.setStartMinute(timerPickerSelected.minute);
            mTimeStartTextView.setText(timerPickerSelected.hourOfDay + ":" + timerPickerSelected.minute);
        } else {
            mControlSchedule.setEndHour(timerPickerSelected.hourOfDay);
            mControlSchedule.setEndMinute(timerPickerSelected.minute);
            mTimeEndTextView.setText(timerPickerSelected.hourOfDay + ":" + timerPickerSelected.minute);
        }
    }

    @Subscribe
    public void on(DatePickerSelected datePickerSelected) {
        if (datePickerSelected.id == R.id.textview_start_day) {
            mControlSchedule.setStartDay(datePickerSelected.day);
            mControlSchedule.setStartMonth(datePickerSelected.month);
            mControlSchedule.setStartYear(datePickerSelected.year);
            mDateStartTextView.setText(getDay(datePickerSelected.day,datePickerSelected.month,datePickerSelected.year));
        } else {
            mControlSchedule.setLimitDay(datePickerSelected.day);
            mControlSchedule.setLimitMonth(datePickerSelected.month);
            mControlSchedule.setLimitYear(datePickerSelected.year);
            mDateEndTextView.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
        }
    }

    private String getDay(int day, int month, int year) {
        return month + "/" + day + "/" + year;
    }
}
