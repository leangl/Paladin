package com.nanospark.gard.ui.fragments;

import com.nanospark.gard.model.user.ControlSchedule;

import mobi.tattu.utils.annotations.SaveState;
import mobi.tattu.utils.fragments.BaseFragment;

/**
 * Created by Leandro on 7/12/2015.
 */
public class RepeatFragment extends BaseFragment {

    @SaveState
    private ControlSchedule mSchedule;

    public static RepeatFragment newInstance(ControlSchedule schedule) {
        RepeatFragment instance = new RepeatFragment();
        instance.mSchedule = schedule;
        return instance;
    }

}
