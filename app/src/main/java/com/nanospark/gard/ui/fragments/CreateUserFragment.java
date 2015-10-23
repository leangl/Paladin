package com.nanospark.gard.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.events.DatePickerSelected;
import com.nanospark.gard.events.TimerPickerSelected;
import com.nanospark.gard.model.user.ControlSchedule;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.ui.activity.CreateUserActivity;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.ui.custom.DatePickerFragment;
import com.nanospark.gard.ui.custom.TimerPickerFragment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;

import mobi.tattu.utils.ToastManager;

/**
 * Created by cristian on 17/10/15.
 */
public class CreateUserFragment extends BaseFragment implements CreateUserActivity.CreateUserListener {

    public static final String ARG_ID_USER = "argID_user";
    private ControlSchedule mControlSchedule;
    private User mUser;
    private AppCompatEditText mNameEditText;
    private AppCompatEditText mPhoneEditText;
    private AppCompatEditText mPasswordEditText;
    private AppCompatSpinner mDoorEventSpinner;
    private AppCompatEditText mRepeatEventWeeksEditText;
    private AppCompatEditText mDateEventEditText;
    private TextView mTimeStartTextView;
    private TextView mTimeEndTextView;
    private TextView mDateStartTextView;

    private CheckBox mRequirePassCheckBox;
    private CheckBox mNotifyCheckBox;
    private CheckBox mTimelimitsCheckBox;

    @Inject
    private UserManager mUserManager;
    private boolean mSave;
    private boolean mEdit;


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
        ((CreateUserActivity) getBaseActivity()).setListener(this);
        String id = getArguments().getString(ARG_ID_USER);
        if (id == null) {
            mUser = new User();
        } else {
            this.mUser = mUserManager.findByName(id);
            this.mControlSchedule = this.mUser.getSchedule();
            this.mEdit = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_user, container, false);
        this.mNameEditText = (AppCompatEditText) view.findViewById(R.id.edittext_name);
        this.mPhoneEditText = (AppCompatEditText) view.findViewById(R.id.edittext_phone);
        this.mPasswordEditText = (AppCompatEditText) view.findViewById(R.id.edittext_password);
        this.mTimelimitsCheckBox = (CheckBox) view.findViewById(R.id.checkbox_time_limits);
        this.mRequirePassCheckBox = (CheckBox) view.findViewById(R.id.checkbox_password);
        this.mNotifyCheckBox = (CheckBox) view.findViewById(R.id.checkbox_notify);
        this.mDoorEventSpinner = (AppCompatSpinner) view.findViewById(R.id.spinner_door_event);
        View scheludeContainer = view.findViewById(R.id.schedule_container);
        scheludeContainer.setVisibility(View.GONE);
        LinearLayout daysContainer = (LinearLayout) scheludeContainer.findViewById(R.id.days_container);

        this.mDoorEventSpinner.setEnabled(true);
        this.mPasswordEditText.setEnabled(false);

        ArrayList<User.Notify> notifyList = new ArrayList<>(3);
        notifyList.add(User.Notify.OPEN);
        notifyList.add(User.Notify.CLOSE);
        notifyList.add(User.Notify.ALL);

        ArrayAdapter<User.Notify> adapter = new ArrayAdapter<User.Notify>(getBaseActivity(), android.R.layout.simple_dropdown_item_1line, notifyList);

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
                    mControlSchedule.setDays(new ArrayList<>());
                    mUser.setSchedules(this.mControlSchedule);
                }
                visibilty = View.VISIBLE;
            } else {
                mUser.setSchedules(null);
                visibilty = View.GONE;
            }
            scheludeContainer.setVisibility(visibilty);

        });
        this.mRequirePassCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mPasswordEditText.setEnabled(isChecked);

        });
        this.mNotifyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mDoorEventSpinner.setEnabled(isChecked);
            if (!isChecked) {
                mUser.setNotify(User.Notify.NONE);
            }
        });
        initScheduleView(scheludeContainer, daysContainer);
        return view;
    }

    private void initScheduleView(View scheludeContainer, LinearLayout daysContainer) {
        this.mTimeStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheludeContainer.findViewById(R.id.textview_end_time);
        this.mDateStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_day);
        this.mRepeatEventWeeksEditText = (AppCompatEditText) scheludeContainer.findViewById(R.id.edittext_repeat_weeks);
        this.mDateEventEditText = (AppCompatEditText) scheludeContainer.findViewById(R.id.edittext_date_event);
        CheckBox repeatEveryDayCheckBox = (CheckBox) scheludeContainer.findViewById(R.id.checkbox_repeat_every_day);
        CheckBox repeatCheckBox = (CheckBox) scheludeContainer.findViewById(R.id.checkbox_repeat);
        AppCompatSpinner limitSpinner = (AppCompatSpinner) scheludeContainer.findViewById(R.id.spinner_date_event);

        this.mRepeatEventWeeksEditText.setEnabled(false);

        repeatEveryDayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            mControlSchedule.setRepeatEveryOtherDay(isChecked);
        });

        repeatCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatWeeks(isChecked);
            mRepeatEventWeeksEditText.setEnabled(isChecked);

        });
        ArrayAdapter<ControlSchedule.Limit> adapter = new ArrayAdapter<ControlSchedule.Limit>(getBaseActivity(), android.R.layout.simple_dropdown_item_1line, ControlSchedule.Limit.values());


        limitSpinner.setAdapter(adapter);
        limitSpinner.setPrompt("Event");
        limitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ControlSchedule.Limit limit = (ControlSchedule.Limit) parent.getAdapter().getItem(position);

                if (limit.equals(ControlSchedule.Limit.DATE)) {
                    if(parent.getTag() == null){
                        showDatePicker(view.getId());
                    }
                    mDateEventEditText.setVisibility(View.VISIBLE);
                    mDateEventEditText.setFocusable(true);
                    mDateEventEditText.setFocusableInTouchMode(true);
                    mDateEventEditText.setOnClickListener(v -> {
                        showDatePicker(v.getId());
                    });
                } else if (limit.equals(ControlSchedule.Limit.EVENTS)) {

                    mDateEventEditText.setText(null);
                    mDateEventEditText.setVisibility(View.VISIBLE);
                    mDateEventEditText.setOnClickListener(null);
                    mDateEventEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    mDateEventEditText.setText(null);
                    mDateEventEditText.setVisibility(View.GONE);
                    mDateEventEditText.setOnClickListener(null);
                }
                if(mControlSchedule != null){
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
        loadData(repeatEveryDayCheckBox, repeatCheckBox, limitSpinner);

    }

    private void loadData(CheckBox repeatEveryDayCheckBox, CheckBox repeatCheckBox, AppCompatSpinner limitSpinner) {
        if (mUser.getName() != null && !mUser.getName().isEmpty()) {
            ControlSchedule controlSchedule = mUser.getSchedule();
            if (controlSchedule != null) {

                Calendar calendarStart = createCalendarTime(controlSchedule.getStartHour(), controlSchedule.getStartMinute());
                mTimeStartTextView.setText(Utils.getHour(calendarStart));
                Calendar calendarEnd = createCalendarTime(controlSchedule.getEndHour(), controlSchedule.getEndMinute());
                mTimeEndTextView.setText(Utils.getHour(calendarEnd));
                mDateStartTextView.setText(getDay(controlSchedule.getStartDay(), controlSchedule.getStartMonth(), controlSchedule.getStartYear()));

                repeatEveryDayCheckBox.setChecked(controlSchedule.isRepeatEveryOtherDay());
                repeatCheckBox.setChecked(controlSchedule.isRepeatWeeks());
                if(controlSchedule.getRepeatWeeksNumber() != null){
                    mRepeatEventWeeksEditText.setText(controlSchedule.getRepeatWeeksNumber() + "");
                }
                if (controlSchedule.getLimit() != null) {
                    limitSpinner.setTag(true);
                    limitSpinner.setSelection(getIndex(limitSpinner, controlSchedule.getLimit()));
                }
                if(controlSchedule.getLimitDay() != null){
                    this.mDateEventEditText.setText(getDay(controlSchedule.getLimitDay(), controlSchedule.getLimitMonth(), controlSchedule.getLimitYear()));
                    this.mDateStartTextView.setVisibility(View.VISIBLE);
                }
                mTimelimitsCheckBox.setChecked(true);
            }
            this.mNameEditText.setText(this.mUser.getName());
            this.mPhoneEditText.setText(this.mUser.getPhone());
            this.mPasswordEditText.setText(this.mUser.getPassword());
            this.mRequirePassCheckBox.setChecked(mUser.getPassword() != null);
            if (mUser.getNotify().equals(User.Notify.NONE)) {
                this.mNotifyCheckBox.setChecked(false);
            }else{
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

    private void showDatePicker(int id) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(id);
        datePickerFragment.show(getBaseActivity().getSupportFragmentManager(), datePickerFragment.toString());
    }

    private void showTimerPicker(int id) {
        TimerPickerFragment timerPickerFragment = TimerPickerFragment.newInstance(id);
        timerPickerFragment.show(getBaseActivity().getSupportFragmentManager(), timerPickerFragment.toString());
    }

    private void initDays(LinearLayout daysContainer) {
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sun), Calendar.SUNDAY, R.drawable.ic_sun, R.drawable.ic_sun_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_mon), Calendar.MONDAY, R.drawable.ic_mon, R.drawable.ic_mon_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_tue), Calendar.TUESDAY, R.drawable.ic_tue, R.drawable.ic_tue_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_wed), Calendar.WEDNESDAY, R.drawable.ic_wed, R.drawable.ic_wed_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_thr), Calendar.THURSDAY, R.drawable.ic_thr, R.drawable.ic_thr_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_fri), Calendar.FRIDAY, R.drawable.ic_fri, R.drawable.ic_fri_selected);
        handlerDay((FloatingActionButton) daysContainer.findViewById(R.id.fb_day_sat), Calendar.SATURDAY, R.drawable.ic_sat, R.drawable.ic_sat_selected);
    }

    private void handlerDay(FloatingActionButton floatingActionButton, int day, int drawable, int drawableSelected) {
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
        if ((mUser.getName() != null && !mUser.getName().isEmpty()) && (mUser.getSchedule() != null && mUser.getSchedule().getDays().indexOf(day) != -1)) {

            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getColorFromResource(R.color.red)));
            floatingActionButton.setImageResource(drawableSelected);
            floatingActionButton.setTag(R.string.key_state,true);
        }
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }


    @Subscribe
    public void on(TimerPickerSelected timerPickerSelected) {
        Calendar calendar = createCalendarTime(timerPickerSelected.hourOfDay, timerPickerSelected.minute);

        if (validateStartTime((Calendar) this.mTimeStartTextView.getTag(), calendar)) {
            if (timerPickerSelected.id == R.id.textview_start_time) {
                mControlSchedule.setStartHour(timerPickerSelected.hourOfDay);
                mControlSchedule.setStartMinute(timerPickerSelected.minute);
                mTimeStartTextView.setTag(calendar);
                mTimeStartTextView.setText(Utils.getHour(calendar));
                resetTimeEnd(mControlSchedule, this.mTimeEndTextView);
            } else {
                mControlSchedule.setEndHour(timerPickerSelected.hourOfDay);
                mControlSchedule.setEndMinute(timerPickerSelected.minute);
                mTimeEndTextView.setText(Utils.getHour(calendar));
            }
        } else {
            ToastManager.get().showToast(R.string.error_time_msg);
        }
    }

    @NonNull
    private Calendar createCalendarTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Subscribe
    public void on(DatePickerSelected datePickerSelected) {
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
                ToastManager.get().showToast(R.string.error_date_msg);
            }
        }
    }

    public void resetLimitDay(ControlSchedule controlSchedule) {
        controlSchedule.setLimitDay(null);
        controlSchedule.setLimitMonth(null);
        controlSchedule.setLimitYear(null);

    }

    public void resetTimeEnd(ControlSchedule controlSchedule, TextView textView) {
        controlSchedule.setEndHour(null);
        controlSchedule.setEndMinute(null);
        textView.setHint(R.string.hint_time_limits);
    }

    public String getDay(int day, int month, int year) {
        return month + "/" + day + "/" + year;
    }

    public boolean validateStartTime(Calendar startTime, Calendar endTime) {
        return startTime == null ? true : startTime.before(endTime);
    }

    @Override
    public void save() {
        String name = this.mNameEditText.getText().toString();
        String phone = this.mPhoneEditText.getText().toString();
        String pass = this.mPasswordEditText.getText().toString();
        String repeatEvent = this.mRepeatEventWeeksEditText.getText().toString();
        String userName = this.mUser.getName();


        if(userName == null || !userName.equals(this.mNameEditText.getText().toString())){
            if (mUserManager.exists(name)) {
                ToastManager.get().showToast(getString(R.string.error_name_msg));
                return;
            } else {
                if (User.isUsernameValid(name)) {
                    this.mSave = true;
                    this.mUser.setName(name);
                } else {
                    this.mSave = false;
                    ToastManager.get().showToast(getString(R.string.error_name_invalid));
                    return;
                }
            }
        }

        if (validateField(phone, getString(R.string.phone_label))) {
            this.mUser.setPhone(phone);
        } else {
            return;
        }
        if (this.mRequirePassCheckBox.isChecked()) {
            if (validateField(pass, getString(R.string.passcode_label))) {
                this.mUser.setPassword(pass);
            } else {
                return;
            }
        }

        if (this.mTimelimitsCheckBox.isChecked()) {
            mSave = validateSchelude();
        }
        if (this.mRepeatEventWeeksEditText.isEnabled()) {
            if (validateField(repeatEvent, getString(R.string.repeat_only_every_day_label))) {
                this.mUser.getSchedule().setRepeatWeeks(true);
                this.mUser.getSchedule().setRepeatWeeksNumber(Integer.parseInt(repeatEvent));
            } else {
                return;
            }
        }

        if (mSave) {
            this.mUserManager.add(this.mUser);
            getBaseActivity().onBackPressed();

        }


    }

    private boolean validateSchelude() {
        String timeStart = mTimeStartTextView.getText().toString();
        String timeEnd = this.mTimeEndTextView.getText().toString();
        String dayStart = this.mDateStartTextView.getText().toString();
        String dayEnd = this.mDateEventEditText.getText().toString();
        if (!validateField(timeStart, getString(R.string.start_label))) {
            return false;
        } else if (!validateField(timeEnd, getString(R.string.end_label))) {
            return false;
        } else if (!validateField(dayStart, getString(R.string.start_label) + " Day")) {
            return false;
        } else if (this.mDateEventEditText.getVisibility() == View.VISIBLE && !validateField(dayEnd, getString(R.string.end_label) + " Day")) {
            return false;
        } else {
            return true;
        }


    }

    private boolean validateField(String text, String nameField) {
        if (TextUtils.isEmpty(text)) {
            ToastManager.get().showToast(getString(R.string.field_empty_msg, nameField));
            this.mSave = false;
            return this.mSave;
        }
        this.mSave = true;
        return this.mSave;
    }


}
