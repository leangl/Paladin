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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.model.Day;
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
import mobi.tattu.utils.ToastManager;
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
    private TextView mLimitCount;

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

        mOpen.setOnClickListener(v -> showTimerPicker(v.getId(), mSchedule.getOpenTime()));
        mClose.setOnClickListener(v -> showTimerPicker(v.getId(), mSchedule.getCloseTime()));

        initScheduleView();
    }

    private void initScheduleView() {
        View scheduleContainer = getView().findViewById(R.id.schedule_container);
        LinearLayout daysContainer = (LinearLayout) scheduleContainer.findViewById(R.id.days_container);

        View mHourContainer = scheduleContainer.findViewById(R.id.hour_container);
        this.mTimeStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheduleContainer.findViewById(R.id.textview_end_time);
        this.mDateStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_day);
        this.mRepeatEventWeeksEditText = (EditText) scheduleContainer.findViewById(R.id.edittext_repeat_weeks);
        this.mLimitCount = (TextView) scheduleContainer.findViewById(R.id.limit_current);
        this.mDateEventEditText = (TextView) scheduleContainer.findViewById(R.id.edittext_date_event);
        CompoundButton repeatEveryOtherWeek = (CompoundButton) scheduleContainer.findViewById(R.id.checkbox_repeat_every_week);
        CompoundButton repeatEveryWeek = (CompoundButton) scheduleContainer.findViewById(R.id.checkbox_repeat);
        Spinner limitSpinner = (Spinner) scheduleContainer.findViewById(R.id.spinner_date_event);

        mHourContainer.setVisibility(View.GONE);

        this.mRepeatEventWeeksEditText.setEnabled(false);

        repeatEveryOtherWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatEveryOtherWeek(isChecked);
            if (isChecked) repeatEveryWeek.setChecked(false);
        });

        repeatEveryWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatWeeks(isChecked);
            mRepeatEventWeeksEditText.setEnabled(isChecked);
            if (isChecked) repeatEveryOtherWeek.setChecked(false);
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
                        showDatePicker(view.getId(), mControlSchedule.getEndDate());
                    }
                    mDateEventEditText.setVisibility(View.VISIBLE);
                    mDateEventEditText.setFocusable(false);
                    mDateEventEditText.setFocusableInTouchMode(false);
                    mDateEventEditText.setOnClickListener(v -> showDatePicker(v.getId(), mControlSchedule.getEndDate()));
                    if (mControlSchedule != null && mControlSchedule.isEndDateSet()) {
                        mDateEventEditText.setText(getDay(mControlSchedule.getLimitDay(), mControlSchedule.getLimitMonth(), mControlSchedule.getLimitYear()));
                    } else {
                        mDateEventEditText.setText(null);
                    }
                } else if (limit.equals(Limit.EVENTS)) {
                    mLimitCount.setText(mControlSchedule.getTriggeredEvents().toString());
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
        this.mTimeStartTextView.setOnClickListener(v -> showTimerPicker(R.id.textview_start_time, mControlSchedule.getStartTime()));
        this.mTimeEndTextView.setOnClickListener(v -> showTimerPicker(R.id.textview_end_time, mControlSchedule.getEndTime()));
        this.mDateStartTextView.setOnClickListener(v -> showDatePicker(R.id.textview_start_day, mControlSchedule.getStartDate()));
        Calendar today = Calendar.getInstance();
        mDateStartTextView.setText(getDay(today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH), today.get(Calendar.YEAR)));

        loadData(repeatEveryOtherWeek, repeatEveryWeek, limitSpinner);
    }

    private void showDatePicker(int id, Calendar defaultDate) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(id, defaultDate);
        datePickerFragment.show(getBaseActivity().getSupportFragmentManager(), datePickerFragment.toString());
    }

    private void showTimerPicker(int id, Calendar defaultCal) {
        TimerPickerFragment timerPickerFragment = TimerPickerFragment.newInstance(id, defaultCal);
        timerPickerFragment.show(getBaseActivity().getSupportFragmentManager(), timerPickerFragment.toString());
    }

    private void initDays(LinearLayout daysContainer) {
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sun), Day.SUNDAY, R.drawable.ic_sun, R.drawable.ic_sun_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_mon), Day.MONDAY, R.drawable.ic_mon, R.drawable.ic_mon_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_tue), Day.TUESDAY, R.drawable.ic_tue, R.drawable.ic_tue_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_wed), Day.WEDNESDAY, R.drawable.ic_wed, R.drawable.ic_wed_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_thr), Day.THURSDAY, R.drawable.ic_thr, R.drawable.ic_thr_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_fri), Day.FRIDAY, R.drawable.ic_fri, R.drawable.ic_fri_selected);
        handleDaySelected((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sat), Day.SATURDAY, R.drawable.ic_sat, R.drawable.ic_sat_selected);
    }

    private void handleDaySelected(FloatingActionButton floatingActionButton, Day day, int drawable, int drawableSelected) {
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
                mControlSchedule.getDays().add((Day) fb.getTag(R.string.key_day));
            }
            fb.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(color)));
            fb.setImageResource(drawableAux);
            fb.setScaleType(ImageView.ScaleType.CENTER);
            fb.setTag(R.string.key_state, state);

        });
        if (mControlSchedule.getDays().contains(day)) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(R.color.red)));
            floatingActionButton.setImageResource(drawableSelected);
            floatingActionButton.setTag(R.string.key_state, true);
        }
    }

    public String getDay(int day, int month, int year) {
        return (month + 1) + "/" + day + "/" + year;
    }

    private void loadData(CompoundButton repeatEveryOtherWeek, CompoundButton repeatEveryWeek, Spinner limitSpinner) {
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

        repeatEveryOtherWeek.setChecked(controlSchedule.isRepeatEveryOtherWeek());
        repeatEveryWeek.setChecked(controlSchedule.isRepeatWeeks());
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

    public boolean validateDate(Calendar startTime, Calendar endTime) {
        if (startTime == null || endTime == null || startTime.before(endTime)) {
            return true;
        } else {
            toast(R.string.error_date_msg);
            return false;
        }
    }

    @Subscribe
    public void on(DatePickerFragment.DatePickerSelected datePickerSelected) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, datePickerSelected.day);
        calendar.set(Calendar.MONTH, datePickerSelected.month);
        calendar.set(Calendar.YEAR, datePickerSelected.year);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (datePickerSelected.id == R.id.textview_start_day) {
            if (validateDate(calendar, mControlSchedule.getEndDate())) {
                mControlSchedule.setStartDay(datePickerSelected.day);
                mControlSchedule.setStartMonth(datePickerSelected.month);
                mControlSchedule.setStartYear(datePickerSelected.year);
                mDateStartTextView.setTag(calendar);
                mDateStartTextView.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
                resetLimitDay(mControlSchedule);
            }
        } else if (datePickerSelected.id == R.id.edittext_date_event) {
            if (validateDate(mControlSchedule.getStartDate(), calendar)) {
                mControlSchedule.setLimitDay(datePickerSelected.day);
                mControlSchedule.setLimitMonth(datePickerSelected.month);
                mControlSchedule.setLimitYear(datePickerSelected.year);
                mDateEventEditText.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
            }
        }
    }

    public void resetLimitDay(ControlSchedule controlSchedule) {
        controlSchedule.setLimitDay(null);
        controlSchedule.setLimitMonth(null);
        controlSchedule.setLimitYear(null);
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

        if (this.mRepeatEventWeeksEditText.isEnabled()) {
            if (validateField(mRepeatEventWeeksEditText.getText(), "Week")) {
                this.mSchedule.getControlSchedule().setRepeatWeeks(true);
                this.mSchedule.getControlSchedule().setRepeatWeeksNumber(Integer.parseInt(mRepeatEventWeeksEditText.getText().toString()));
            } else {
                toast("Please enter the Repeat Weeks number");
                return false;
            }
        }

        if (mControlSchedule != null && Limit.EVENTS.equals(mControlSchedule.getLimit())) {
            try {
                mControlSchedule.setLimitEvents(Integer.parseInt(mDateEventEditText.getText().toString()));
            } catch (Exception e) {
                toast("Number of Events is not a valid number");
                return false;
            }
        }

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
        return validateSchedule();
    }

    private boolean validateSchedule() {
        String dayStart = this.mDateStartTextView.getText().toString();
        String limitText = this.mDateEventEditText.getText().toString();
        if (!validateField(dayStart, "Start Date")) {
            return false;
        }
        if (Limit.DATE.equals(mControlSchedule.getLimit()) && !validateField(limitText, "End Date")) {
            return false;
        }
        if (Limit.EVENTS.equals(mControlSchedule.getLimit()) && !validateField(limitText, "Number of Events")) {
            return false;
        }
        return true;
    }

    private boolean validateField(CharSequence text, String nameField) {
        if (StringUtils.isBlank(text)) {
            ToastManager.show(getString(R.string.field_empty_msg, nameField));
            return false;
        }
        return true;
    }
}
