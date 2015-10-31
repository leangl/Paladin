package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nanospark.gard.R;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.ui.custom.BaseFragment;

import mobi.tattu.utils.annotations.SaveState;

/**
 * Created by Leandro on 30/10/2015.
 */
public class CreateScheduleFragment extends BaseFragment {

    @SaveState
    private Schedule mSchedule;

    public static CreateScheduleFragment newInstance(Schedule schedule) {
        CreateScheduleFragment instance = new CreateScheduleFragment();
        instance.mSchedule = schedule;
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
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

}
