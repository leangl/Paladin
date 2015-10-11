package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.custom.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulesFragment extends BaseFragment {


    public static SchedulesFragment newInstance() {
        
        Bundle args = new Bundle();
        
        SchedulesFragment fragment = new SchedulesFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedules, container, false);
    }


    @Override
    public boolean showHomeIcon() {
        return false;
    }
}
