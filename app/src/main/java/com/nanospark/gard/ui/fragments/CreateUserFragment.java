package com.nanospark.gard.ui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.UUID;

import mobi.tattu.utils.ToastManager;

/**
 * Created by cristian on 17/10/15.
 */
public class CreateUserFragment extends BaseFragment implements CreateUserActivity.CreateUserListener {

    private ControlSchedule mControlSchedule;
    private User mUser;
    private AppCompatEditText mNameEditText;
    private AppCompatEditText mPhoneEditText;
    private AppCompatEditText mPasswordEditText;
    private AppCompatSpinner mDoorEventSpinner;
    private TextView mTimeStartTextView;
    private TextView mTimeEndTextView;
    private TextView mDateStartTextView;
    private TextView mDateEndTextView;
    private CheckBox mRequirePassCheckBox;
    private CheckBox mNotifyCheckBox;
    private CheckBox mTimelimitsCheckBox;
    @Inject
    private UserManager mUserManager;
    private boolean mSave;


    public static CreateUserFragment newInstance() {

        Bundle args = new Bundle();
        CreateUserFragment fragment = new CreateUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = new User();
        mUser.setId(UUID.randomUUID().toString());
        mControlSchedule = new ControlSchedule();
        mControlSchedule.setDays(new ArrayList<>());
        ((CreateUserActivity) getBaseActivity()).setListener(this);
        mUser.setSchedules(this.mControlSchedule);


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
        LinearLayout daysContainer = (LinearLayout) scheludeContainer.findViewById(R.id.days_container);

        this.mDoorEventSpinner.setEnabled(true);
        this.mPasswordEditText.setEnabled(false);

        initScheduleView(scheludeContainer, daysContainer);
        ArrayAdapter<User.Notify> adapter = new ArrayAdapter<User.Notify>(getBaseActivity(), android.R.layout.simple_dropdown_item_1line, User.Notify.values());

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
            scheludeContainer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        });
        this.mRequirePassCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mPasswordEditText.setEnabled(isChecked);

        });
        this.mNotifyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.mDoorEventSpinner.setEnabled(isChecked);
            if(!isChecked){
                mUser.setNotify(null);
            }
        });

        return view;
    }

    private void initScheduleView(View scheludeContainer, LinearLayout daysContainer) {
        this.mTimeStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_time);
        this.mTimeEndTextView = (TextView) scheludeContainer.findViewById(R.id.textview_end_time);
        this.mDateStartTextView = (TextView) scheludeContainer.findViewById(R.id.textview_start_day);
        this.mDateEndTextView = (TextView) scheludeContainer.findViewById(R.id.textview_end_day);
        AppCompatSpinner eventSpinner = (AppCompatSpinner) scheludeContainer.findViewById(R.id.spinner_events);

        ((CheckBox) scheludeContainer.findViewById(R.id.checkbox_repeat_every_day)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatEveryOtherDay(isChecked);
        });
        ((CheckBox) scheludeContainer.findViewById(R.id.checkbox_repeat)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            mControlSchedule.setRepeatWeeks(isChecked);
            eventSpinner.setEnabled(true);

        });
        ArrayAdapter<ControlSchedule.Limit> adapter = new ArrayAdapter<ControlSchedule.Limit>(getBaseActivity(), android.R.layout.simple_dropdown_item_1line, ControlSchedule.Limit.values());

        eventSpinner.setEnabled(false);
        eventSpinner.setAdapter(adapter);
        eventSpinner.setPrompt("Event");
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mControlSchedule.setLimit((ControlSchedule.Limit) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }


    @Subscribe
    public void on(TimerPickerSelected timerPickerSelected) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timerPickerSelected.hourOfDay);
        calendar.set(Calendar.MINUTE, timerPickerSelected.minute);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

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

    @Subscribe
    public void on(DatePickerSelected datePickerSelected) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, datePickerSelected.day);
        calendar.set(Calendar.MONTH, datePickerSelected.month);
        calendar.set(Calendar.YEAR, datePickerSelected.year);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        if (validateStartTime((Calendar) mDateStartTextView.getTag(), calendar)) {
            if (datePickerSelected.id == R.id.textview_start_day) {
                mControlSchedule.setStartDay(datePickerSelected.day);
                mControlSchedule.setStartMonth(datePickerSelected.month);
                mControlSchedule.setStartYear(datePickerSelected.year);
                mDateStartTextView.setTag(calendar);
                mDateStartTextView.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
                resetLimitDay(mControlSchedule, mDateEndTextView);
            } else {
                mControlSchedule.setLimitDay(datePickerSelected.day);
                mControlSchedule.setLimitMonth(datePickerSelected.month);
                mControlSchedule.setLimitYear(datePickerSelected.year);
                mDateEndTextView.setText(getDay(datePickerSelected.day, datePickerSelected.month, datePickerSelected.year));
            }
        } else {
            ToastManager.get().showToast(R.string.error_date_msg);
        }
    }

    public void resetLimitDay(ControlSchedule controlSchedule, TextView textView) {
        controlSchedule.setLimitDay(null);
        controlSchedule.setLimitMonth(null);
        controlSchedule.setLimitYear(null);
        textView.setText(null);
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

        if (validateField(name, getString(R.string.name_label))) {
            this.mUser.setName(name);
        }else{
            return;
        }
        if (validateField(phone, getString(R.string.phone_label))) {
            this.mUser.setPhone(phone);
        }else{
            return;
        }
        if(this.mRequirePassCheckBox.isChecked()){
            if (validateField(pass, getString(R.string.passcode_label))) {
                this.mUser.setPassword(pass);
            }else{
                return;
            }
        }
        if (this.mTimelimitsCheckBox.isChecked()) {
            mSave = validateSchelude();
        }else{
            return;
        }
        if(mSave){
            this.mUserManager.add(this.mUser);
            getBaseActivity().onBackPressed();

        }
    }

    private boolean validateSchelude() {
        String timeStart = mTimeStartTextView.getText().toString();
        String timeEnd = this.mTimeEndTextView.getText().toString();
        String dayStart = this.mDateStartTextView.getText().toString();
        String dayEnd = this.mDateEndTextView.getText().toString();
        if (!validateField(timeStart, getString(R.string.start_label))) {
            return false;
        } else if (!validateField(timeEnd, getString(R.string.end_label))) {
            return false;
        } else if (!validateField(dayStart, getString(R.string.start_label) + " Day")) {
            return false;
        } else if (!validateField(dayEnd, getString(R.string.end_label) + " Day")) {
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
