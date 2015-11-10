package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.model.scheduler.ScheduleManager;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.ui.custom.TimerPickerFragment;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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

    public static CreateScheduleFragment newInstance(Schedule schedule) {
        CreateScheduleFragment instance = new CreateScheduleFragment();
        if (schedule != null) {
            instance.mSchedule = schedule;
        } else {
            instance.mSchedule = new Schedule();
        }
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
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    private void showTimerPicker(int id) {
        TimerPickerFragment timerPickerFragment = TimerPickerFragment.newInstance(id);
        timerPickerFragment.show(getBaseActivity().getSupportFragmentManager(), timerPickerFragment.toString());
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
