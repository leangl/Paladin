package com.nanospark.gard.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.text.TextUtils;
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
import com.nanospark.gard.model.user.ControlSchedule;
import com.nanospark.gard.model.user.Limit;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.ui.custom.DatePickerFragment;
import com.nanospark.gard.ui.custom.TimerPickerFragment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cristian on 17/10/15.
 */
public class CreateUserFragment extends BaseFragment {

    public static final String ARG_ID_USER = "argID_user";

    private ControlSchedule mControlSchedule;
    private User mUser;
    private EditText mNameEditText;
    private EditText mPhoneEditText;
    private EditText mPasswordEditText;
    private Spinner mDoorEventSpinner;
    private EditText mRepeatEventWeeksEditText;
    private TextView mDateEventEditText;
    private TextView mTimeStartTextView;
    private TextView mTimeEndTextView;
    private TextView mDateStartTextView;
    private CheckBox mRequirePassCheckBox;
    private CheckBox mNotifyCheckBox;
    private CheckBox mTimelimitsCheckBox;
    private TextView mLimitCount;

    @Inject
    private UserManager mUserManager;

    public static CreateUserFragment newInstance(String idUser) {
        Bundle args = new Bundle();
        args.putString(ARG_ID_USER, idUser);
        CreateUserFragment fragment = new CreateUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(ARG_ID_USER);
        if (id == null) {
            mUser = new User();
        } else {
            this.mUser = mUserManager.getUser(id);
            this.mControlSchedule = this.mUser.getSchedule();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_user, container, false);
        this.mNameEditText = (EditText) view.findViewById(R.id.edittext_name);
        this.mPhoneEditText = (EditText) view.findViewById(R.id.edittext_phone);
        this.mPasswordEditText = (EditText) view.findViewById(R.id.edittext_password);
        this.mTimelimitsCheckBox = (CheckBox) view.findViewById(R.id.checkbox_time_limits);
        this.mRequirePassCheckBox = (CheckBox) view.findViewById(R.id.checkbox_password);
        this.mNotifyCheckBox = (CheckBox) view.findViewById(R.id.checkbox_notify);
        this.mDoorEventSpinner = (Spinner) view.findViewById(R.id.spinner_door_event);
        View scheduleContainer = view.findViewById(R.id.schedule_container);
        scheduleContainer.setVisibility(View.GONE);
        LinearLayout daysContainer = (LinearLayout) scheduleContainer.findViewById(R.id.days_container);

        this.mDoorEventSpinner.setEnabled(true);
        this.mPasswordEditText.setEnabled(false);

        ArrayAdapter<User.Notify> adapter = new ArrayAdapter<>(getBaseActivity(),
                android.R.layout.simple_dropdown_item_1line,
                new User.Notify[]{User.Notify.OPEN, User.Notify.CLOSE, User.Notify.ALL});

        this.mDoorEventSpinner.setAdapter(adapter);
        this.mDoorEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUser.setNotify((User.Notify) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mTimelimitsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibilty;
            if (isChecked) {
                if (mControlSchedule == null) {
                    mControlSchedule = new ControlSchedule();
                    mUser.setSchedule(this.mControlSchedule);
                    initDays(daysContainer);
                }
                visibilty = View.VISIBLE;
            } else {
                mUser.setSchedule(null);
                visibilty = View.GONE;
            }
            scheduleContainer.setVisibility(visibilty);
        });
        this.mRequirePassCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> this.mPasswordEditText.setEnabled(isChecked));
        this.mNotifyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mDoorEventSpinner.setEnabled(isChecked);
            if (!isChecked) {
                mUser.setNotify(User.Notify.NONE);
            }
        });
        initScheduleView(scheduleContainer, daysContainer);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changeTitleActionBar(getString(R.string.create_user));

    }

    private void initScheduleView(View scheduleContainer, LinearLayout daysContainer) {
        this.mTimeStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheduleContainer.findViewById(R.id.textview_end_time);
        this.mDateStartTextView = (TextView) scheduleContainer.findViewById(R.id.textview_start_day);
        this.mRepeatEventWeeksEditText = (EditText) scheduleContainer.findViewById(R.id.edittext_repeat_weeks);
        this.mLimitCount = (TextView) scheduleContainer.findViewById(R.id.limit_current);
        this.mDateEventEditText = (TextView) scheduleContainer.findViewById(R.id.edittext_date_event);
        CompoundButton repeatEveryOtherWeek = (CompoundButton) scheduleContainer.findViewById(R.id.checkbox_repeat_every_week);
        CompoundButton repeatEveryWeek = (CompoundButton) scheduleContainer.findViewById(R.id.checkbox_repeat);
        Spinner limitSpinner = (Spinner) scheduleContainer.findViewById(R.id.spinner_date_event);

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
                    mDateEventEditText.setOnClickListener(v -> showDatePicker(view.getId(), mControlSchedule.getEndDate()));
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

    private void loadData(CompoundButton repeatEveryDayCheckBox, CompoundButton repeatCheckBox, Spinner limitSpinner) {
        if (mUser.getName() != null && !mUser.getName().isEmpty()) {
            ControlSchedule controlSchedule = mUser.getSchedule();
            if (controlSchedule != null) {

                mTimelimitsCheckBox.setChecked(true);

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
            this.mNameEditText.setText(this.mUser.getName());
            this.mPhoneEditText.setText(this.mUser.getPhone());
            this.mPasswordEditText.setText(this.mUser.getPassword());
            this.mRequirePassCheckBox.setChecked(mUser.getPassword() != null);
            if (mUser.getNotify().equals(User.Notify.NONE)) {
                this.mNotifyCheckBox.setChecked(false);
            } else {
                this.mNotifyCheckBox.setChecked(true);
                this.mDoorEventSpinner.setSelection(getIndex(mDoorEventSpinner, mUser.getNotify()));

            }
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

    private void showDatePicker(int id, Calendar defaultCal) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(id, defaultCal);
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
        if (mUser.getSchedule() != null && mUser.getSchedule().getDays().contains(day)) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(R.color.red)));
            floatingActionButton.setImageResource(drawableSelected);
            floatingActionButton.setTag(R.string.key_state, true);
        }
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    @Subscribe
    public void on(TimerPickerFragment.TimerPickerSelected timerPickerSelected) {
        Calendar calendar = Utils.createCalendarTime(timerPickerSelected.hourOfDay, timerPickerSelected.minute);
        if (timerPickerSelected.id == R.id.textview_start_time) {
            mControlSchedule.setStartHour(timerPickerSelected.hourOfDay);
            mControlSchedule.setStartMinute(timerPickerSelected.minute);
            mTimeStartTextView.setTag(calendar);
            mTimeStartTextView.setText(Utils.getHour(calendar));
        } else {
            mControlSchedule.setEndHour(timerPickerSelected.hourOfDay);
            mControlSchedule.setEndMinute(timerPickerSelected.minute);
            mTimeEndTextView.setText(Utils.getHour(calendar));
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

        if (validateStartTime((Calendar) mDateStartTextView.getTag(), calendar)) {
            if (datePickerSelected.id == R.id.textview_start_day) {
                mControlSchedule.setStartDay(datePickerSelected.day);
                mControlSchedule.setStartMonth(datePickerSelected.month);
                mControlSchedule.setStartYear(datePickerSelected.year);
                mDateStartTextView.setTag(calendar);
                mDateStartTextView.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
                resetLimitDay(mControlSchedule);
            } else {
                mControlSchedule.setLimitDay(datePickerSelected.day);
                mControlSchedule.setLimitMonth(datePickerSelected.month);
                mControlSchedule.setLimitYear(datePickerSelected.year);
                mDateEventEditText.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
            }
        } else {
            if (this.mDateEventEditText.getVisibility() == View.VISIBLE) {
                toast(R.string.error_date_msg);
            }
        }
    }

    public void resetLimitDay(ControlSchedule controlSchedule) {
        controlSchedule.setLimitDay(null);
        controlSchedule.setLimitMonth(null);
        controlSchedule.setLimitYear(null);
    }

    public String getDay(int day, int month, int year) {
        return (month + 1) + "/" + day + "/" + year;
    }

    public boolean validateStartTime(Calendar startTime, Calendar endTime) {
        return startTime == null || startTime.before(endTime);
    }

    public boolean save() {
        String name = this.mNameEditText.getText().toString();
        String phone = this.mPhoneEditText.getText().toString();
        String pass = this.mPasswordEditText.getText().toString();
        String repeatEvent = this.mRepeatEventWeeksEditText.getText().toString();
        String userName = this.mUser.getName();

        if (userName == null || !userName.equals(this.mNameEditText.getText().toString())) {
            if (mUserManager.exists(name)) {
                toast(getString(R.string.error_name_msg));
                return false;
            } else {
                if (User.isUsernameValid(name)) {
                    this.mUser.setName(name);
                } else {
                    toast(getString(R.string.error_name_invalid));
                    return false;
                }
            }
        }

        if (validateField(phone, getString(R.string.phone_label))) {
            if (phone.toString().trim().length() < 10) {
                toast("Phone number is less than 10 digits, please include your area code.");
                return false;
            }
            this.mUser.setPhone(phone);
        } else {
            return false;
        }
        if (this.mRequirePassCheckBox.isChecked()) {
            if (validateField(pass, getString(R.string.passcode_label))) {
                this.mUser.setPassword(pass);
            } else {
                return false;
            }
        } else {
            this.mUser.setPassword(null);
        }

        if (this.mTimelimitsCheckBox.isChecked()) {
            if (!validateSchedule()) {
                return false;
            }
        }
        if (this.mRepeatEventWeeksEditText.isEnabled()) {
            if (validateField(repeatEvent, "Week")) {
                this.mUser.getSchedule().setRepeatWeeks(true);
                this.mUser.getSchedule().setRepeatWeeksNumber(Integer.parseInt(repeatEvent));
            } else {
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

        this.mUserManager.add(this.mUser);

        return true;
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

    private boolean validateField(String text, String nameField) {
        if (TextUtils.isEmpty(text)) {
            toast(getString(R.string.field_empty_msg, nameField));
            return false;
        }
        return true;
    }
}
