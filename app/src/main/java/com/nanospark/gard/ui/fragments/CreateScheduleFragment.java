package com.nanospark.gard.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.model.scheduler.ScheduleManager;
import com.nanospark.gard.model.user.ControlSchedule;
import com.nanospark.gard.model.user.Limit;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.ui.custom.DatePickerFragment;
import com.nanospark.gard.ui.custom.TimerPickerFragment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.annotations.SaveState;
import roboguice.inject.InjectView;

/**
 * Created by Leandro on 30/10/2015.
 */
public class CreateScheduleFragment extends BaseFragment {

    @InjectView(R.id.name)
    private EditText mName;
    @InjectView(R.id.door1)
    private CheckBox mDoor1;
    @InjectView(R.id.door2)
    private CheckBox mDoor2;
    @InjectView(R.id.open)
    private EditText mOpen;
    @InjectView(R.id.close)
    private EditText mClose;
    @Inject
    private ScheduleManager mManager;

    @SaveState
    private Schedule mSchedule;

    private EditText mRepeatEventWeeksEditText;
    private TextView mDateEventEditText;
    private TextView mTimeStartTextView;
    private TextView mTimeEndTextView;
    private TextView mDateStartTextView;
    private ControlSchedule mControlSchedule;

    public static CreateScheduleFragment newInstance(Schedule schedule) {
        CreateScheduleFragment instance = new CreateScheduleFragment();
        if (schedule != null) {
            instance.mSchedule = schedule;
        } else {
            instance.mSchedule = new Schedule();
        }
        instance.mControlSchedule = instance.mSchedule.getControlSchedule();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mName.setText(mSchedule.getName());

        mDoor1.setText(Door.getInstance(1).getName());
        mDoor2.setText(Door.getInstance(2).getName());

        if (mSchedule.getDoors().contains(1)) mDoor1.setChecked(true);
        if (mSchedule.getDoors().contains(2)) mDoor2.setChecked(true);

        if (mSchedule.isOpenTimeSet())
            mOpen.setText(Utils.getHour(mSchedule.getOpenHour(), mSchedule.getOpenMinute()));
        if (mSchedule.isCloseTimeSet())
            mClose.setText(Utils.getHour(mSchedule.getCloseHour(), mSchedule.getCloseMinute()));

        mOpen.setOnClickListener(v -> {
            showTimerPicker(v.getId());
        });
        mClose.setOnClickListener(v -> {
            showTimerPicker(v.getId());
        });

        initScheduleView();
    }

    private void initScheduleView() {
        View scheduleContainer = getView().findViewById(R.id.schedule_container);
        LinearLayout daysContainer = (LinearLayout) scheduleContainer.findViewById(R.id.days_container);

        this.mTimeStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheduleContainer.findViewById(R.id.textview_end_time);
        this.mDateStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_day);
        this.mRepeatEventWeeksEditText = (EditText) scheduleContainer.findViewById(R.id.edittext_repeat_weeks);
        this.mDateEventEditText = (TextView) scheduleContainer.findViewById(R.id.edittext_date_event);
        CheckBox repeatEveryDayCheckBox = (CheckBox) scheduleContainer.findViewById(R.id.checkbox_repeat_every_day);
        CheckBox repeatCheckBox = (CheckBox) scheduleContainer.findViewById(R.id.checkbox_repeat);
        Spinner limitSpinner = (Spinner) scheduleContainer.findViewById(R.id.spinner_date_event);

        this.mRepeatEventWeeksEditText.setEnabled(false);

        repeatEveryDayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatEveryOtherWeek(isChecked);
        });

        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatWeeks(isChecked);
            mRepeatEventWeeksEditText.setEnabled(isChecked);

        });
        ArrayAdapter<Limit> adapter = new ArrayAdapter<>(getBaseActivity(),
                android.R.layout.simple_dropdown_item_1line,
                Limit.values());

        limitSpinner.setAdapter(adapter);
        limitSpinner.setPrompt("Event");
        limitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Limit limit = (Limit) parent.getAdapter().getItem(position);

                if (limit.equals(Limit.DATE)) {
                    if (parent.getTag() == null) {
                        showDatePicker(view.getId());
                    }
                    mDateEventEditText.setVisibility(View.VISIBLE);
                    mDateEventEditText.setFocusable(false);
                    mDateEventEditText.setFocusableInTouchMode(false);
                    mDateEventEditText.setOnClickListener(v -> {
                        showDatePicker(v.getId());
                    });
                    if (mControlSchedule != null && mControlSchedule.isEndDateSet()) {
                        mDateEventEditText.setText(getDay(mControlSchedule.getLimitDay(), mControlSchedule.getLimitMonth(), mControlSchedule.getLimitYear()));
                    } else {
                        mDateEventEditText.setText(null);
                    }
                } else if (limit.equals(Limit.EVENTS)) {
                    mDateEventEditText.setVisibility(View.VISIBLE);
                    mDateEventEditText.setFocusable(true);
                    mDateEventEditText.setFocusableInTouchMode(true);
                    mDateEventEditText.setOnClickListener(null);
                    mDateEventEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    if (mControlSchedule != null && mControlSchedule.getLimitEvents() != null) {
                        mDateEventEditText.setText(mControlSchedule.getLimitEvents() + "");
                    } else {
                        mDateEventEditText.setText(null);
                    }
                } else {
                    mDateEventEditText.setText(null);
                    mDateEventEditText.setVisibility(View.GONE);
                    mDateEventEditText.setOnClickListener(null);
                }
                if (mControlSchedule != null) {
                    mControlSchedule.setLimit(limit);
                }
                parent.setTag(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setTag(null);
            }
        });
        initDays(daysContainer);
        this.mTimeStartTextView.setOnClickListener(v -> {
            showTimerPicker(R.id.textview_start_time);
        });
        this.mTimeEndTextView.setOnClickListener(v -> {
            showTimerPicker(R.id.textview_end_time);
        });
        this.mDateStartTextView.setOnClickListener(v -> {
            showDatePicker(R.id.textview_start_day);
        });
        Calendar today = Calendar.getInstance();
        mDateStartTextView.setText(getDay(today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH), today.get(Calendar.YEAR)));

        loadData(repeatEveryDayCheckBox, repeatCheckBox, limitSpinner);
    }

    private void showDatePicker(int id) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(id);
        datePickerFragment.show(getBaseActivity().getSupportFragmentManager(), datePickerFragment.toString());
    }

    private void showTimerPicker(int id) {
        TimerPickerFragment timerPickerFragment = TimerPickerFragment.newInstance(id);
        timerPickerFragment.show(getBaseActivity().getSupportFragmentManager(), timerPickerFragment.toString());
    }

    private void initDays(LinearLayout daysContainer) {
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sun), Calendar.SUNDAY, R.drawable.ic_sun, R.drawable.ic_sun_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_mon), Calendar.MONDAY, R.drawable.ic_mon, R.drawable.ic_mon_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_tue), Calendar.TUESDAY, R.drawable.ic_tue, R.drawable.ic_tue_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_wed), Calendar.WEDNESDAY, R.drawable.ic_wed, R.drawable.ic_wed_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_thr), Calendar.THURSDAY, R.drawable.ic_thr, R.drawable.ic_thr_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_fri), Calendar.FRIDAY, R.drawable.ic_fri, R.drawable.ic_fri_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sat), Calendar.SATURDAY, R.drawable.ic_sat, R.drawable.ic_sat_selected);
    }

    private void handleDaySelected(FloatingActionButton floatingActionButton, int day, int drawable, int drawableSelected) {
        floatingActionButton.setTag(R.string.key_state, Boolean.FALSE);
        floatingActionButton.setTag(R.string.key_day, day);
        floatingActionButton.setOnClickListener(v -> {
            FloatingActionButton fb = (FloatingActionButton) v;
            boolean state = (boolean) fb.getTag(R.string.key_state);
            int color;
            int drawableAux;
            if (state) {
                color = R.color.white;
                drawableAux = drawable;
                state = false;
                mControlSchedule.getDays().remove(fb.getTag(R.string.key_day));
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
        if (mControlSchedule.getDays().indexOf(day) != -1) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(R.color.red)));
            floatingActionButton.setImageResource(drawableSelected);
            floatingActionButton.setTag(R.string.key_state, true);
        }
    }

    public String getDay(int day, int month, int year) {
        return month + "/" + day + "/" + year;
    }

    private void loadData(CheckBox repeatEveryDayCheckBox, CheckBox repeatCheckBox, Spinner limitSpinner) {
        ControlSchedule controlSchedule = mControlSchedule;
            if (controlSchedule.isStartTimeSet()) {
                mTimeStartTextView.setText(Utils.getHour(controlSchedule.getStartHour(), controlSchedule.getStartMinute()));
            }
            if (controlSchedule.isEndTimeSet()) {
                mTimeEndTextView.setText(Utils.getHour(controlSchedule.getEndHour(), controlSchedule.getEndMinute()));
            }

            if (controlSchedule.isStartDateSet()) {
                mDateStartTextView.setText(getDay(controlSchedule.getStartDay(), controlSchedule.getStartMonth(), controlSchedule.getStartYear()));
            }

            repeatEveryDayCheckBox.setChecked(controlSchedule.isRepeatEveryOtherWeek());
            repeatCheckBox.setChecked(controlSchedule.isRepeatWeeks());
            if (controlSchedule.getRepeatWeeksNumber() != null) {
                mRepeatEventWeeksEditText.setText(controlSchedule.getRepeatWeeksNumber() + "");
            }
            if (controlSchedule.getLimit() != null) {
                limitSpinner.setTag(true);
                limitSpinner.setSelection(getIndex(limitSpinner, controlSchedule.getLimit()));
            }
    }

    private int getIndex(Spinner spinner, Object object) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(object)) {
                index = i;
            }
        }
        return index;
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    @Subscribe
    public void on(TimerPickerFragment.TimerPickerSelected event) {
        if (event.id == mOpen.getId()) {
            mSchedule.setOpenHour(event.hourOfDay);
            mSchedule.setOpenMinute(event.minute);
            mOpen.setText(Utils.getHour(event.hourOfDay, event.minute));
        } else {
            mSchedule.setCloseHour(event.hourOfDay);
            mSchedule.setCloseMinute(event.minute);
            mClose.setText(Utils.getHour(event.hourOfDay, event.minute));
        }
    }

    public boolean save() {
        if (!validate()) {
            return false;
        }
        mSchedule.setName(mName.getText().toString());
        List<Integer> doors = new ArrayList<>();
        if (mDoor1.isChecked()) doors.add(1);
        if (mDoor2.isChecked()) doors.add(2);
        mSchedule.setDoors(doors);

        mManager.add(mSchedule);

        return true;
    }

    public boolean validate() {
        if (StringUtils.isBlank(mName.getText())) {
            toast("Enter schedule name");
            return false;
        }
        if (!mDoor1.isChecked() && !mDoor2.isChecked()) {
            toast("Select at least one door");
            return false;
        }
        if (StringUtils.isBlank(mOpen.getText()) && StringUtils.isBlank(mClose.getText())) {
            toast("Enter open or close time");
            return false;
        }
        return true;
    }
}
